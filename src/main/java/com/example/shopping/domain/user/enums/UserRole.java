package com.example.shopping.domain.user.enums;

import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;

import java.util.Arrays;

public enum UserRole {
    USER, ADMIN;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_USER_ROLE));
    }
}
