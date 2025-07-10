package com.example.shopping.domain.auth.controller;

import com.example.shopping.config.JwtUtil;
import com.example.shopping.domain.user.dto.ChangeUserRoleRequestDto;
import com.example.shopping.domain.auth.dto.request.LoginRequestDto;
import com.example.shopping.domain.auth.dto.response.LoginResponseDto;
import com.example.shopping.domain.auth.service.AuthService;
import com.example.shopping.domain.auth.dto.request.SignupRequestDto;
import com.example.shopping.domain.auth.dto.request.WithdrawRequestDto;
import com.example.shopping.domain.auth.dto.response.SignupResponseDto;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {

        SignupResponseDto responseData = authService.signup(request);
        ResponseDto<SignupResponseDto> response = new ResponseDto<>("회원가입이 완료되었습니다", responseData);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ResponseDto<Void>> withdraw(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody WithdrawRequestDto request) {

        authService.withdraw(user, request);
        ResponseDto<Void> response = new ResponseDto<>("회원탈퇴가 완료되었습니다", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto responseData = authService.login(request);
        ResponseDto<LoginResponseDto> response= new ResponseDto<>("로그인 했습니다", responseData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(
            @RequestHeader("Authorization") String bearerAccessToken,
            @AuthenticationPrincipal AuthUser user) {

        authService.logout(bearerAccessToken, user.getId());
        ResponseDto<Void> response = new ResponseDto<>("로그아웃 했습니다", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<LoginResponseDto>> refresh(@RequestHeader("Authorization") String bearerRefreshToken){

        LoginResponseDto responseData = authService.refresh(bearerRefreshToken);
        ResponseDto<LoginResponseDto> response = new ResponseDto<>("토큰 재발급", responseData);

        return ResponseEntity.ok(response);
    }


}