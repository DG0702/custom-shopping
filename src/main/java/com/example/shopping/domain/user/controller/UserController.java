package com.example.shopping.domain.user.controller;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.ResponseDto;
import com.example.shopping.domain.user.dto.ProfileResponseDto;
import com.example.shopping.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<ProfileResponseDto>> profile(@AuthenticationPrincipal AuthUser user) {

        ProfileResponseDto responseData = userService.profile(user);
        ResponseDto<ProfileResponseDto> response = new ResponseDto<>("프로필 조회", responseData);

        return ResponseEntity.ok(response);
    }
}