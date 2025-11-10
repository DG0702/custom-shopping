package com.example.shopping.global.common.exception;

import com.example.shopping.global.common.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(e.getErrorCode().getMessage()));
    }

    // RequestBody → Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> MethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        log.warn("Validation failed : {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("RequestData Validation failed", errors));
    }

    // Param → Validation
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handlerMethodValidationException(
        HandlerMethodValidationException e) {
        log.warn("Parameter validation failed: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();

        for (MessageSourceResolvable error : e.getAllErrors()) {
            String fieldName = resolveFieldName(error);
            String message = Objects.requireNonNullElse(error.getDefaultMessage(), "잘못된 요청입니다.");

            errors.merge(fieldName, message, (existing, newMsg) -> existing + ", " + newMsg);
        }

        if (errors.isEmpty()) {
            errors.put("parameter", "파라미터 검증에 실패했습니다");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("요청 파라미터 검증에 실패하였습니다.", errors));
    }

    // security 인과(권한)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> accessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getHttpStatus())
            .body(ApiResponse.error(ErrorCode.ACCESS_DENIED.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("Exception : {} ", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("알 수 없는 오류 발생 message : {}", e.getMessage()));
    }

    // 필드 이름 추출 메서드
    private String resolveFieldName(MessageSourceResolvable error) {
        return (error instanceof FieldError fieldError) ? fieldError.getField() : "parameter";
    }
}