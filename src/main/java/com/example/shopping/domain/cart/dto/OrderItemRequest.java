package com.example.shopping.domain.cart.dto;

import lombok.Getter;

@Getter
// Order_Item 테이블에 주입하기 위해 임의로 생성해 놓은 dto
public class OrderItemRequest {
    private Long productId;
    private Long quantity;
}
