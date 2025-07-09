package com.example.shopping.domain.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProductRequestDto {
    private String name;
    private String description;
    private Long price;
    private Long stock;
}
