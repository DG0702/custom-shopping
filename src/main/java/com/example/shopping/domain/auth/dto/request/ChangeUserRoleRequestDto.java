package com.example.shopping.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangeUserRoleRequestDto {
    @NotBlank(message = "유저 권한은 필수입니다")
    private final String userRole;

    public ChangeUserRoleRequestDto(String userRole){
        this.userRole = userRole;
    }
}
