package com.example.shopping.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final Long id;
    private final String email;
    private final String token;
    private final String refreshToken;

    public LoginResponseDto(Long id, String email, String token, String refreshToken) {
        this.id = id;
        this.email = email;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
