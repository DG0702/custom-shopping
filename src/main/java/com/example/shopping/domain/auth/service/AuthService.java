package com.example.shopping.domain.auth.service;

import com.example.shopping.config.JwtUtil;
import com.example.shopping.config.PasswordEncoder;
import com.example.shopping.domain.auth.dto.request.LoginRequestDto;
import com.example.shopping.domain.auth.dto.request.SignupRequestDto;
import com.example.shopping.domain.auth.dto.request.WithdrawRequestDto;
import com.example.shopping.domain.auth.dto.response.LoginResponseDto;
import com.example.shopping.domain.auth.dto.response.SignupResponseDto;
import com.example.shopping.domain.auth.dto.response.WithdrawResponseDto;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 예외처리 수정

    @Transactional
    public SignupResponseDto signup(@Valid SignupRequestDto request) {
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

    @Transactional
    public WithdrawResponseDto withdraw(@Valid WithdrawRequestDto request) {
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Wrong password");
        }

        userRepository.delete(user);

        return new WithdrawResponseDto("회원 탈퇴가 완료되었습니다");
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(@Valid LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Wrong password");
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


    // 리프레시 토큰 이용한 구현 예정
    public void logout(User user) {

    }

    // access token 만료 시 refresh
    public LoginResponseDto refresh(String refreshToken){
        refreshToken = jwtUtil.substringToken(refreshToken);
        Claims claims = jwtUtil.extractClaims(refreshToken);

        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

        // refreshToken 검증
        String redisKey = "refreshToken : " + userId;
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 새 토큰 발급, 기존 refresh token 덮어쓰기
        String newAccessToken = jwtUtil.createToken(userId, email, userRole);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, email, userRole);

        redisTemplate.opsForValue().set(redisKey, newRefreshToken, Duration.ofDays(7));

        return new LoginResponseDto(userId, email, newAccessToken, newRefreshToken);
    }

}
