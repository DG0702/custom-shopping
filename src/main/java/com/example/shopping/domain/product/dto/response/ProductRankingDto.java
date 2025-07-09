package com.example.shopping.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRankingDto {
    private Long id;
    private String name;
    private Long viewCount;
}
