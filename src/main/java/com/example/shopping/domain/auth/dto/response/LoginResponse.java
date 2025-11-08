package com.example.shopping.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private final Long id;

    private String email;

    private final String accessToken;

    private final String refreshToken;
}
