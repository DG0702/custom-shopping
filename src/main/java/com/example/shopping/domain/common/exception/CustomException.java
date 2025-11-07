package com.example.shopping.domain.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;

    // ErrorCode 그대로 사용
    public CustomException(ErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
        this.customMessage = code.getMessage();
    }

    // Message 변경 필요할 때 사용
    public CustomException(ErrorCode code, String customMessage) {
        super(customMessage);
        this.errorCode = code;
        this.customMessage = customMessage;
    }
}
