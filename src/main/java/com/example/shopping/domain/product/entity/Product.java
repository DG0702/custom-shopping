package com.example.shopping.domain.product.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Integer price;
    private Integer stock;
    private Long viewCount;

    public Product(String name, String description, Integer price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.viewCount = 0L;
    }

    // product 수정
    public void updateProduct (String name, String description, Integer price, Integer stock) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
        if (stock != null) {
            this.stock = stock;
        }
    }

    public void increaseViewCount () {
        this.viewCount++;
    }

    public void updateStock(Integer stock) {
        this.stock = stock;
    }

}
