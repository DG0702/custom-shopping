package com.example.shopping.domain.cart.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "cart_items")
public class CartItem extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private Integer quantity;
}