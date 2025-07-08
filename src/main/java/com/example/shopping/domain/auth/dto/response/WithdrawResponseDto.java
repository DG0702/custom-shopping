package com.example.shopping.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class WithdrawResponseDto {
    private final String message;

    public WithdrawResponseDto(String message) {
        this.message = message;
    }
}
