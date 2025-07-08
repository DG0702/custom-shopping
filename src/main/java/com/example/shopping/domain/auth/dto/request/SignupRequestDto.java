package com.example.shopping.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupRequestDto {
    @Email
    @NotBlank
    private final String email;
    @NotBlank
    private final String password;
    @NotBlank
    private final String name;
    @NotBlank
    private final String address;
    @NotBlank
    private final String userRole;

    SignupRequestDto(String email, String password, String name, String address, String userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.userRole = userRole;
    }
}
