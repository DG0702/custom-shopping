package com.example.shopping.domain.order.dto;

import com.example.shopping.domain.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDto {
    private Long orderId;
    private OrderStatus status;
    private Integer totalPrice;
}
