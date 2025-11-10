package com.example.shopping.domain.product.dto.response;

import com.example.shopping.domain.product.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {
    private final String name;
    private final String description;
    private final Integer price;
    private final Integer stock;

    public ProductResponse(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
    }
}
