package com.example.shopping.domain.order.dto;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;

@NoArgsConstructor
@Getter
public class OrderRequestDto {
    @NotEmpty
    private List<CartCreateRequestDto> items;
}
