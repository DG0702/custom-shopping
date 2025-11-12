package com.example.shopping.global.jwt;

import com.example.shopping.global.common.dto.ApiResponse;
import com.example.shopping.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        log.warn("Authentication failed : {}", authException.getMessage());

        ErrorCode status = ErrorCode.UNAUTHORIZED_REQUEST;
        String message = status.getMessage();

        if (request.getAttribute("exceptionMessage") != null) {
            message = request.getAttribute("exceptionMessage").toString();
        }

        ApiResponse<Object> apiResponse = ApiResponse.error(message, status);

        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}