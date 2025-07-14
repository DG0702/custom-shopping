package com.example.shopping.domain.order.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCancelDto {
    private final Long productId;
    private final Integer quantity;
}
