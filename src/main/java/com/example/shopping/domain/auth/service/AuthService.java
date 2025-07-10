package com.example.shopping.domain.auth.service;

import com.example.shopping.config.JwtUtil;
import com.example.shopping.config.PasswordEncoder;
import com.example.shopping.domain.auth.dto.request.ChangeUserRoleRequestDto;
import com.example.shopping.domain.auth.dto.request.LoginRequestDto;
import com.example.shopping.domain.auth.dto.request.SignupRequestDto;
import com.example.shopping.domain.auth.dto.request.WithdrawRequestDto;
import com.example.shopping.domain.auth.dto.response.LoginResponseDto;
import com.example.shopping.domain.auth.dto.response.SignupResponseDto;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원가입
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new CustomException(ExceptionCode.ALREADY_EXISTS_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserRole userRole = UserRole.of(request.getUserRole());

        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getAddress(),
                userRole
        );
        User savedUser = userRepository.save(user);

        return new SignupResponseDto(savedUser);
    }

    // 회원탈퇴
    @Transactional
    public void withdraw(AuthUser authUser, WithdrawRequestDto request) {

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(ExceptionCode.WRONG_PASSWORD);
        }

        userRepository.delete(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(ExceptionCode.WRONG_PASSWORD);
        }

        // access token 생성
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        // refresh token 생성
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getUserRole());

        // refresh token Redis에 저장
        // 저장 형태
        // Key: "refreshToken : 1"
        // Value: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        // TTL: 7일
        String redisKey = "refreshToken : " + user.getId();
        redisTemplate.opsForValue().set(redisKey, refreshToken, Duration.ofDays(7));

        return new LoginResponseDto(user.getId(), user.getEmail(), token, refreshToken);
    }


    // 로그아웃
    public void logout(String bearerAccessToken, Long userId) {

        String accessToken = jwtUtil.substringToken(bearerAccessToken);
        addToBlacklist(accessToken);

//        Long remainingTokenTime = jwtUtil.getExpiration(accessToken);
//
//        // blacklist token Redis에 저장
//        // 저장 형태
//        // Key : "blacklist : eyJhbGci..."
//        // Value : "logout"
//        // TTL : 남은 시간
//        String blackListTokenKey = "blacklist : " + accessToken;
//        redisTemplate.opsForValue().set(blackListTokenKey, "logout", Duration.ofMillis(remainingTokenTime));

        // 저장된 refresh token 삭제
        String refreshTokenKey = "refreshToken : " + userId;
        redisTemplate.delete(refreshTokenKey);
    }

    // access token 만료 시 refresh
    public LoginResponseDto refresh(String bearerRefreshToken){

        String refreshToken = jwtUtil.substringToken(bearerRefreshToken);
        Claims claims = jwtUtil.extractClaims(refreshToken);

        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

        // refreshToken 검증
        String redisKey = "refreshToken : " + userId;
        String storedBearerRefreshToken = redisTemplate.opsForValue().get(redisKey);
        String storedRefreshToken = jwtUtil.substringToken(storedBearerRefreshToken);

        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 새 토큰 발급, 기존 refresh token 덮어쓰기
        String newAccessToken = jwtUtil.createToken(userId, email, userRole);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, email, userRole);

        redisTemplate.opsForValue().set(redisKey, newRefreshToken, Duration.ofDays(7));

        return new LoginResponseDto(userId, email, newAccessToken, newRefreshToken);
    }

    public LoginResponseDto changeRole(String bearerAccessToken, ChangeUserRoleRequestDto request){

        UserRole newUserRole = UserRole.of(request.getUserRole());

        String accessToken = jwtUtil.substringToken(bearerAccessToken);

        Claims claims = jwtUtil.extractClaims(accessToken);
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

        if(userRole.equals(newUserRole)){
            throw new CustomException(ExceptionCode.INVALID_USER_ROLE, "요청한 사용자 권한이 현재 권한과 동일합니다.");
        }

        addToBlacklist(accessToken);

        String newAccessToken = jwtUtil.createToken(userId, email, newUserRole);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, email, newUserRole);

        String redisKey = "refreshToken : " + userId;
        redisTemplate.opsForValue().set(redisKey, newRefreshToken, Duration.ofDays(7));

        return new LoginResponseDto(userId, email, newAccessToken, newRefreshToken);
    }

    public void addToBlacklist(String accessToken){

        Long remainingTokenTime = jwtUtil.getExpiration(accessToken);

        // blacklist token Redis에 저장
        // 저장 형태
        // Key : "blacklist : eyJhbGci..."
        // Value : "logout"
        // TTL : 남은 시간
        String blackListTokenKey = "blacklist : " + accessToken;
        redisTemplate.opsForValue().set(blackListTokenKey, "logout", Duration.ofMillis(remainingTokenTime));
    }
}
