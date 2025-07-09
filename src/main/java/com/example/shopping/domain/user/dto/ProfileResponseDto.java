package com.example.shopping.domain.user.dto;

import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProfileResponseDto {
    private final Long id;
    private final String email;
    private final String name;
    private final String address;
    private final UserRole userRole;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProfileResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.address = user.getAddress();
        this.userRole = user.getUserRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}