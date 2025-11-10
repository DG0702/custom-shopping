package com.example.shopping.domain.user.controller;

import com.example.shopping.domain.auth.dto.response.LoginResponse;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.global.common.dto.AuthUser;
import com.example.shopping.global.common.dto.ApiResponse;
import com.example.shopping.domain.user.dto.ChangeUserRoleRequestDto;
import com.example.shopping.domain.user.dto.ProfileResponseDto;
import com.example.shopping.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> profile(@AuthenticationPrincipal Long userId) {

        ProfileResponseDto response = userService.profile(userId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Look up your profile", response));
    }

    // 권한 변경 (Admin 권한 필요 → 다른 사람 권한 변경)
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> changeUserRole(
        @PathVariable Long userId,
        @RequestHeader("Authorization") String bearerAccessToken,
        @Valid @RequestBody ChangeUserRoleRequestDto request
    ) {

        ProfileResponseDto response = userService.changeRole(userId, bearerAccessToken, request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Changing authorization", response));
    }
}