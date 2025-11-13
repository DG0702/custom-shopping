package com.example.shopping.domain.auth.api;

import com.example.shopping.domain.auth.dto.request.LoginRequest;
import com.example.shopping.domain.auth.dto.response.LoginResponse;
import com.example.shopping.domain.auth.service.AuthService;
import com.example.shopping.domain.auth.dto.request.SignupRequest;
import com.example.shopping.domain.auth.dto.request.WithdrawRequest;
import com.example.shopping.domain.auth.dto.response.SignupResponse;
import com.example.shopping.global.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {

        SignupResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("sign up", response));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("login", response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        @RequestHeader("Authorization") String bearerAccessToken,
        @AuthenticationPrincipal Long userId) {

        authService.logout(bearerAccessToken, userId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("logout", null));
    }

    // 회원 탈퇴
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
        @Valid @RequestBody WithdrawRequest request,
        @RequestHeader("Authorization") String bearerAccessToken,
        @AuthenticationPrincipal Long userId
    ) {

        authService.withdraw(userId, bearerAccessToken, request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("withdraw", null));
    }

    // 리프레시 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
        @RequestHeader("Authorization") String bearerRefreshToken) {

        LoginResponse response = authService.refresh(bearerRefreshToken);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("issue a refreshToken", response));
    }

}