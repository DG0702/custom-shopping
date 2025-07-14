package com.example.shopping.domain.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddEventProductRequestDto {
    @NotNull
    private Integer eventPrice;
    @NotNull
    private Integer eventStock;
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
}
