package com.example.shopping.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long orderId;
    private Integer totalPrice;
    private List<OrderItemListDto> items;
    private final long totalElements;
    private final int totalPages;
    private final int size;
    private final int number;
}
