package com.example.shopping.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderItemListDto {
    private Long productId;
    private String productName;
    private Integer price;
    private Integer quantity;
}
