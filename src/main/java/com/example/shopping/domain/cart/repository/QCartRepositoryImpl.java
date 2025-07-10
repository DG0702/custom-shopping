package com.example.shopping.domain.cart.repository;

import com.example.shopping.domain.cart.dto.CartResponseDto;
import com.example.shopping.domain.cart.dto.QCartResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.shopping.domain.cart.entity.QCartItem.cartItem;
import static com.example.shopping.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class QCartRepositoryImpl implements QCartRepository {

    private final JPAQueryFactory queryFactory;

    public Page<CartResponseDto> getCartPage(Long userId, Pageable pageable) {

        Long total = queryFactory
                .select(cartItem.count())
                .from(cartItem)
                .where(cartItem.user.id.eq(userId))
                .fetchOne();

        long safeTotal = (total != null) ? total : 0L;

        if (safeTotal == 0L){
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<CartResponseDto> carts = queryFactory
            .select(
                new QCartResponseDto(product.id, product.name, product.price, cartItem.quantity)
            )
            .from(cartItem)
            .join(cartItem.product, product)
            .where(cartItem.user.id.eq(userId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();


        return new PageImpl<>(carts, pageable, safeTotal);
    }
}
