package com.example.shopping.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewCountUpdateDto {
    private Long productId;
    private Long viewCount;
}