package com.example.shopping.domain.cart.repository.impl;

import java.util.List;

import com.example.shopping.domain.cart.entity.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartJpaRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
