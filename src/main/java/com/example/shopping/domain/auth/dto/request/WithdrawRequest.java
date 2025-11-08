package com.example.shopping.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WithdrawRequest {
    @NotBlank(message = "비밀번호는 필수입니다")
    private final String password;
}
