package com.example.shopping.domain.order.dto;

import com.example.shopping.domain.cart.dto.OrderItemRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;

@NoArgsConstructor
@Getter
public class OrderRequestDto {
    @NotEmpty
    private List<OrderItemRequest> items;
    @NotNull
    private Long totalPrice;
}
