package com.example.shopping.domain.cart.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CartResponseDto {
    private final Long productId;
    private final String name;
    private final Integer price;
    private final Integer quantity;

    @QueryProjection
    public CartResponseDto(Long productId, String name, Integer price, Integer quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
