package com.example.shopping.domain.common.exception;

import com.example.shopping.domain.common.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
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
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ExceptionCode status = ExceptionCode.UNAUTHORIZED_REQUEST;

        String message = status.getMessage();
        if(request.getAttribute("exceptionMessage") != null){
            message = request.getAttribute("exceptionMessage").toString();
        }

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(message, status);
        String responseBody = objectMapper.writeValueAsString(errorResponseDto);

        String uri = request.getAttribute("URI").toString();

        log.error("UNAUTHORIZED: Not Authenticated Request - Uri : {}", uri);

        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }
}