package com.example.shopping.domain.cart.entity;

import com.example.shopping.global.common.entity.BaseEntity;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private Integer quantity;

    protected CartItem() {}

    public static CartItem createCartItem(User user, Product product, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.user = user;
        cartItem.product = product;
        cartItem.quantity = quantity;
        return cartItem;
    }
}