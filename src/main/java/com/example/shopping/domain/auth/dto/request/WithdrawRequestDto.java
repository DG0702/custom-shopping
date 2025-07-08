package com.example.shopping.domain.auth.dto.request;

import lombok.Getter;

@Getter
public class WithdrawRequestDto {
    private final String password;

    public WithdrawRequestDto(String password) {
        this.password = password;
    }
}
