package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor

// OrderRepository 구현체
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderjpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final QueryDslRepository queryDslRepository;


    @Override
    public Order save(Order order) {
        return orderjpaRepository.save(order);
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        return orderItemJpaRepository.saveAll(orderItems);
    }

    @Override
    public Page<OrderResponseDto> getOrders(AuthUser authUser, Pageable pageable) {
        return queryDslRepository.getOrders(authUser, pageable);
    }
}
