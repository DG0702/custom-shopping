package com.example.shopping.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final Long id;
    private final String email;
    private final String token;

    public LoginResponseDto(Long id, String email, String token) {
        this.id = id;
        this.email = email;
        this.token = token;
    }
}
