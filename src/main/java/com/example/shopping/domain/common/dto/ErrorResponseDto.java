package com.example.shopping.domain.common.dto;

import com.example.shopping.domain.common.exception.ExceptionCode;
import lombok.Getter;

@Getter
public class ErrorResponseDto extends ResponseDto<Void>{
    private final String errorCode;

    public ErrorResponseDto(String message, String errorCode) {
        super(message, null);
        this.success = false;
        this.errorCode = errorCode;
    }

    public ErrorResponseDto(String message, ExceptionCode errorCode) {
        super(message, null);
        this.success = false;
        this.errorCode = errorCode.toString();
    }
}
