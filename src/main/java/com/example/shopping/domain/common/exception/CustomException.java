package com.example.shopping.domain.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    private final String customMessage;

    // ExceptionCode 그대로 사용
    public CustomException(ExceptionCode code) {
        super(code.getMessage());
        this.exceptionCode = code;
        this.customMessage = code.getMessage();
    }

    // Message 변경 필요할 때 사용
    public CustomException(ExceptionCode code, String customMessage) {
        super(customMessage);
        this.exceptionCode = code;
        this.customMessage = customMessage;
    }
}
