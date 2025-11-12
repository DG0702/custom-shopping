package com.example.shopping.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRanking {
    private Long id;
    private String name;
    private Long viewCount;
}
