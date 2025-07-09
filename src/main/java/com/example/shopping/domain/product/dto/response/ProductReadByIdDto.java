package com.example.shopping.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductReadByIdDto {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private Long stock;
    private Long viewCount;

}
