package com.example.shopping.domain.cart.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.shopping.domain.cart.dto.cartResponse.CartResponse;
import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final CartRepositoryQueryDSL cartRepositoryQueryDSL;

    @Override
    public Optional<CartItem> findById(Long cartId) {
        return cartJpaRepository.findById(cartId);
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return cartJpaRepository.findByUserId(userId);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartJpaRepository.save(cartItem);
    }

    @Override
    public Page<CartResponse> getCartPage(Long userId, Pageable pageable) {
        return cartRepositoryQueryDSL.getCartPage(userId, pageable);
    }

    @Override
    public void delete(CartItem cartItem) {
        cartJpaRepository.delete(cartItem);
    }

    @Override
    public void deleteByUserId(Long userId) {
        cartJpaRepository.deleteByUserId(userId);
    }
}
