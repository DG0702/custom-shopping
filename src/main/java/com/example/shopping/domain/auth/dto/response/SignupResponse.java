package com.example.shopping.domain.auth.dto.response;

import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private final Long id;

    private final String email;

    private final String name;

    private final UserRole userRole;

    public SignupResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.userRole = user.getUserRole();
    }
}
