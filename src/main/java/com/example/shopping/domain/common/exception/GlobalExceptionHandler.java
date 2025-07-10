package com.example.shopping.domain.common.exception;

import com.example.shopping.domain.common.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException ex) {
        ExceptionCode code = ex.getExceptionCode();
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getCustomMessage(), code);
        return new ResponseEntity<>(errorResponseDto, code.getHttpStatus());
    }
}