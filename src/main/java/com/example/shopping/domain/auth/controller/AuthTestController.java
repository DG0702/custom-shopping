package com.example.shopping.domain.auth.controller;

import com.example.shopping.domain.common.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController 테스트용 Controller 입니다
 * 테스트 후 삭제 예정 !!
 */

@RestController
@RequestMapping("/test")
public class AuthTestController {

    @GetMapping("/me")
    public ResponseEntity<String> getMyEmail(@AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.badRequest().body("AuthUser is null");
        }
        return ResponseEntity.ok("Hello " + user.getEmail());
    }
}