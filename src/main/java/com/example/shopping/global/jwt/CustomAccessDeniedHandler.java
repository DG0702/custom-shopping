package com.example.shopping.global.jwt;

import com.example.shopping.global.common.dto.ApiResponse;
import com.example.shopping.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException {

        log.warn("Access denied : {}", accessDeniedException.getMessage());

        ErrorCode status = ErrorCode.ACCESS_DENIED;
        String message = status.getMessage();

        if (request.getAttribute("exceptionMessage") != null) {
            message = request.getAttribute("exceptionMessage").toString();
        }

        ApiResponse<Object> apiResponse = ApiResponse.error(message, status);

        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
