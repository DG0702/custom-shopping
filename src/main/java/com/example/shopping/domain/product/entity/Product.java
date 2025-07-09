package com.example.shopping.domain.product.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    private Long price;
    private Long stock;
    private Long viewCount;

    public Product(String name, String description, Long price, Long stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.viewCount = 0L;
    }

    // product 수정
    public void updateProduct (String name, String description, Long price, Long stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void increaseViewCount () {
        this.viewCount++;
    }

}
