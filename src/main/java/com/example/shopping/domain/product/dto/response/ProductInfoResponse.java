package com.example.shopping.domain.product.dto.response;

import com.example.shopping.domain.product.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInfoResponse {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Integer stock;
    private Long viewCount;

    public ProductInfoResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.viewCount = product.getViewCount();
    }
}
