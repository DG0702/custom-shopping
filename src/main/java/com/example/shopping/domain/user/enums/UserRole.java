package com.example.shopping.domain.user.enums;

import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;

import java.util.Arrays;

public enum UserRole {
    USER, ADMIN;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
            .filter(r -> r.name().equalsIgnoreCase(role))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ROLE));
    }
}
