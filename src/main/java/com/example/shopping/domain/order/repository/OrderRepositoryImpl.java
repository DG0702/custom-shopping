package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor

// OrderRepository 구현체
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderjpaRepository;
    private final QueryDslRepository queryDslRepository;


    @Override
    public Order findById(Long id) {
        return orderjpaRepository.findById(id).
                orElseThrow(() -> new CustomException(ExceptionCode.ORDER_NOT_FOUND));
    }

    @Override
    public Order save(Order order) {
        return orderjpaRepository.save(order);
    }

    @Override
    public Page<OrderResponseDto> getOrders(AuthUser authUser, Pageable pageable) {
        return queryDslRepository.getOrders(authUser, pageable);
    }
}
