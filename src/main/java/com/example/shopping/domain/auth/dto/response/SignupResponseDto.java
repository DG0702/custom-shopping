package com.example.shopping.domain.auth.dto.response;

import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import lombok.Getter;

@Getter
public class SignupResponseDto {
    private final Long id;
    private final String email;
    private final String name;
    private final UserRole userRole;

    public SignupResponseDto(Long id, String email, String name, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.userRole = userRole;
    }

    public SignupResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.userRole = user.getUserRole();
    }
}
