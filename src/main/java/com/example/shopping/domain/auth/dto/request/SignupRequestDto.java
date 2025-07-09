package com.example.shopping.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupRequestDto {
    @Email(message = "이메일 형식이 잘못됐습니다")
    @NotBlank(message = "이메일은 필수입니다")
    private final String email;
    @NotBlank(message = "비밀번호는 필수입니다")
    private final String password;
    @NotBlank(message = "이름은 필수입니다")
    private final String name;
    @NotBlank(message = "주소는 필수입니다")
    private final String address;
    @NotBlank(message = "유저 권한은 필수입니다")
    private final String userRole;

    SignupRequestDto(String email, String password, String name, String address, String userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.userRole = userRole;
    }
}
