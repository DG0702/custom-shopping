package com.example.shopping.domain.auth.service;

import com.example.shopping.global.jwt.JwtUtil;
import com.example.shopping.domain.auth.dto.request.LoginRequest;
import com.example.shopping.domain.auth.dto.request.SignupRequest;
import com.example.shopping.domain.auth.dto.request.WithdrawRequest;
import com.example.shopping.domain.auth.dto.response.LoginResponse;
import com.example.shopping.domain.auth.dto.response.SignupResponse;
import com.example.shopping.global.common.dto.AuthUser;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public SignupResponse signup(SignupRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new CustomException(ErrorCode.ALREADY_EXISTS_EMAIL);
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

        return new SignupResponse(savedUser);
    }

    // 회원탈퇴
    @Transactional
    public void withdraw(Long userId, String bearerAccessToken, WithdrawRequest request) {
        String accessToken = jwtUtil.substringToken(bearerAccessToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
        }

        addToBlacklist(accessToken, "WithDraw");

        String refreshTokenKey = "refreshToken : " + user.getId();
        redisTemplate.delete(refreshTokenKey);

        userRepository.delete(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
        }

        return createNewTokensAndSaveRefresh(user.getId(), user.getEmail(), user.getUserRole());

    }

    // 로그아웃
    public void logout(String bearerAccessToken, Long userId) {

        String accessToken = jwtUtil.substringToken(bearerAccessToken);
        addToBlacklist(accessToken ,"Logout");

        // 저장된 refresh token 삭제
        String refreshTokenKey = "refreshToken : " + userId;
        redisTemplate.delete(refreshTokenKey);
    }

    // access token 만료 시 refresh
    public LoginResponse refresh(String bearerRefreshToken){

        String refreshToken = jwtUtil.substringToken(bearerRefreshToken);
        Claims claims = jwtUtil.extractClaims(refreshToken);

        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String email = user.getEmail();
        UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

        // refreshToken 검증
        String redisKey = "refreshToken : " + userId;
        String storedBearerRefreshToken = redisTemplate.opsForValue().get(redisKey);
        String storedRefreshToken = jwtUtil.substringToken(storedBearerRefreshToken);

        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        return createNewTokensAndSaveRefresh(userId, email, userRole);
    }
    
    // 로그아웃, 회원탈퇴 시 리프레시 토큰 → 블랙리스트 등록
    public void addToBlacklist(String accessToken,String type){

        Long TTL = jwtUtil.getExpiration(accessToken);
        String blackListTokenKey = "blacklist : " + accessToken;

        redisTemplate.opsForValue().set(blackListTokenKey, type, Duration.ofMillis(TTL));
    }

    // 로그인 응답 형식
    public LoginResponse createNewTokensAndSaveRefresh(Long userId, String email, UserRole userRole){
        String newAccessToken = jwtUtil.createAccessToken(userId, userRole);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, userRole);

        String redisKey = "refreshToken : " + userId;
        redisTemplate.opsForValue().set(redisKey, newRefreshToken, Duration.ofDays(7));

        return new LoginResponse(userId, email, newAccessToken, newRefreshToken);
    }
}
