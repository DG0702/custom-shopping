package com.example.shopping.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "상품이름은 필수 입력값 입니다.")
    private String name;

    @NotBlank(message = "설명은 필수 입력값 입니다.")
    private String description;

    @NotNull(message = "가격은 필수 입력값 입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고는 필수 입력값 입니다.")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    private Integer stock;
}
