package com.example.shopping.domain.order.dto;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;

@NoArgsConstructor
@Getter
public class OrderRequestDto {
    @NotEmpty(message = "주문 항목이 비어 있을 수 없습니다.")
    private List<CartCreateRequestDto> items;
}
