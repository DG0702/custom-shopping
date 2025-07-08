package com.example.shopping.domain.auth.controller;

import com.example.shopping.domain.auth.dto.request.LoginRequestDto;
import com.example.shopping.domain.auth.dto.response.LoginResponseDto;
import com.example.shopping.domain.auth.service.AuthService;
import com.example.shopping.domain.auth.dto.request.SignupRequestDto;
import com.example.shopping.domain.auth.dto.request.WithdrawRequestDto;
import com.example.shopping.domain.auth.dto.response.SignupResponseDto;
import com.example.shopping.domain.auth.dto.response.WithdrawResponseDto;
import com.example.shopping.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponseDto> withdraw(@Valid @RequestBody WithdrawRequestDto request) {
        return ResponseEntity.ok(authService.withdraw(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        authService.logout(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}