package com.example.shopping.domain.order.dto.orderResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import com.example.shopping.domain.order.dto.OrderItemList;

@Getter
@AllArgsConstructor
public class OrderItemResponse {
    private Long orderId;
    private Integer totalPrice;
    private List<OrderItemList> items;
}
