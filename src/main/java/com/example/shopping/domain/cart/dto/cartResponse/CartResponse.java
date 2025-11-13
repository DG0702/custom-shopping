package com.example.shopping.domain.cart.dto.cartResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartResponse {
    private final Long productId;
    private final String name;
    private final Integer price;
    private final Integer quantity;

}
