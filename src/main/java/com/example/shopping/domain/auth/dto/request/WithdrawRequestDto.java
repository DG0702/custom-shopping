package com.example.shopping.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class WithdrawRequestDto {
    @NotBlank(message = "비밀번호는 필수입니다")
    private final String password;

    public WithdrawRequestDto(String password) {
        this.password = password;
    }
}
