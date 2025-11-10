package com.example.shopping.domain.user.service;

import com.example.shopping.domain.user.repository.UserRepository;
import com.example.shopping.global.jwt.JwtUtil;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.user.dto.ChangeUserRoleRequestDto;
import com.example.shopping.domain.user.dto.ProfileResponseDto;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ProfileResponseDto profile(Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new ProfileResponseDto(user);
    }

    @Transactional
    public ProfileResponseDto changeRole(Long userId, String bearerAccessToken, ChangeUserRoleRequestDto request) {

        // 관리자 권한 확인
        String accessToken = jwtUtil.substringToken(bearerAccessToken);
        Claims claims = jwtUtil.extractClaims(accessToken);

        Long adminId = Long.parseLong(claims.getSubject());
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (admin.getUserRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 대상 유저 조회
        UserRole newUserRole = UserRole.of(request.getUserRole());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserRole().equals(newUserRole)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE, "요청한 사용자 권한이 현재 권한과 동일합니다.");
        }

        user.changeRole(newUserRole);

        return new ProfileResponseDto(user);
    }

}
