package com.example.shopping.domain.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class ResponseDto<T> {
    boolean success;
    private final String message;
    private final T data;
    private final String timestamp;

    // message, data(ResponseDto) 이용해서 공통응답 생성
    public ResponseDto(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().toString();
    }
}
