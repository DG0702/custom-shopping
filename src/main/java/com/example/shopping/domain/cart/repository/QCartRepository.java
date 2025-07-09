package com.example.shopping.domain.cart.repository;

import com.example.shopping.domain.cart.dto.CartResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QCartRepository {

    Page<CartResponseDto> getCartPage(Long userId, Pageable pageable);
}
