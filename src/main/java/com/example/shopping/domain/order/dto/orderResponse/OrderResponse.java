package com.example.shopping.domain.order.dto.orderResponse;

import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private OrderStatus status;
    private Integer totalPrice;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.status = order.getStatus();
        this.totalPrice = order.getTotalPrice();
    }
}
