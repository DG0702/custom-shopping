package com.example.shopping.domain.common.exception;

import com.example.shopping.domain.common.dto.ErrorResponseDto;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // response에 입력한 내용 순서대로 정리
        List<String> sortlist = Arrays.stream(Objects.requireNonNull(ex.getBindingResult()
                                .getTarget()).getClass()
                        .getDeclaredFields())
                .map(Field::getName)
                .toList();

        // 예외처리 목록 순서대로 정리
        List<FieldError> sortErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparingInt(e -> sortlist.indexOf(e.getField())))
                .toList();

        // 첫번째 예외처리 문구
        String errorMessage = sortErrors.get(0).getDefaultMessage();
        return getErrorResponse(status, errorMessage);
    }

    // Param Validation 예외처리
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodValidationException(HandlerMethodValidationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return getErrorResponse(status, "Parameter 값을 확인해주세요");
    }

    // 예상하지 못한 모든 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Exception: ", ex); // 전체 스택 트레이스 로그 출력
        return getErrorResponse(status, ex.getMessage());
    }

    private ResponseEntity<ErrorResponseDto> getErrorResponse(HttpStatus status, String message) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                message,
                status.getReasonPhrase()
        );
        return new ResponseEntity<>(errorResponseDto, status);
    }
}