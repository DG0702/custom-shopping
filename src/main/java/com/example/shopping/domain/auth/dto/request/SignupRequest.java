package com.example.shopping.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupRequest {

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
}
