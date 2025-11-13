package com.example.shopping.domain.cart.repository;

import java.util.Optional;

import com.example.shopping.domain.cart.dto.CartResponse;
import com.example.shopping.domain.cart.entity.CartItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartRepository {

    Optional<CartItem> findById(Long cartId);

    CartItem save(CartItem cartItem);

    Page<CartResponse> getCartPage(Long userId, Pageable pageable);

    void delete(CartItem cartItem);
}
