package com.example.shopping.domain.cart.repository.impl;

import static com.example.shopping.domain.cart.entity.QCartItem.*;
import static com.example.shopping.domain.product.entity.QProduct.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.shopping.domain.cart.dto.CartResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    public Page<CartResponse> getCartPage(Long userId, Pageable pageable) {

        Long total = queryFactory
            .select(cartItem.count())
            .from(cartItem)
            .where(cartItem.user.id.eq(userId))
            .fetchOne();

        long safeTotal = (total != null) ? total : 0L;

        if (safeTotal == 0L) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<CartResponse> carts = queryFactory
            .select(
                Projections.constructor(
                    CartResponse.class,
                    product.id, product.name,
                    product.price, cartItem.quantity))
            .from(cartItem)
            .join(cartItem.product, product)
            .where(cartItem.user.id.eq(userId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(carts, pageable, safeTotal);
    }
}
