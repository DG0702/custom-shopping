package com.example.shopping.domain.product.entity;

import com.example.shopping.domain.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Integer stock;
    private Integer viewCount;

    @OneToMany
    private List<OrderItem> orderItems;
}
