package com.example.shopping.domain.product.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import com.example.shopping.domain.product.annotation.EndDate;

@Getter
@AllArgsConstructor
@EndDate
public class AddEventProductRequest {

    @NotNull
    private Integer eventPrice;

    @NotNull
    private Integer eventStock;

    @NotNull
    @FutureOrPresent(message = "시작 시간은 현재 시각 이후여야 합니다.")
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;
}
