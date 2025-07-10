package com.example.shopping.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPatchRequestDto {

    private String name;
    private String description;
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    private Integer stock;
}
