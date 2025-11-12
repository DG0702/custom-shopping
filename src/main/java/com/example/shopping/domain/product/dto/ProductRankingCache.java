package com.example.shopping.domain.product.dto;

import com.example.shopping.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRankingCache {
    private Long id;
    private String name;

    public ProductRankingCache(Product product) {
        this.id = product.getId();
        this.name = product.getName();
    }
}
