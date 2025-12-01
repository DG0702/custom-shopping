package com.example.shopping.domain.order.repository.impl;

import com.example.shopping.domain.order.dto.orderResponse.OrderItemResponse;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.repository.OrderRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.order.dto.OrderCancel;
import com.example.shopping.domain.order.dto.orderResponse.OrderResponse;
import com.example.shopping.domain.order.entity.Order;

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
    private final OrderRepositoryQueryDSL orderRepositoryQueryDSL;
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public Order findById(Long id) {
        return orderjpaRepository.findById(id).
            orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    public Order save(Order order) {
        return orderjpaRepository.save(order);
    }

    @Override
    public Page<OrderResponse> getOrders(User user, Pageable pageable) {
        return orderRepositoryQueryDSL.getOrders(user, pageable);
    }

    @Override
    public OrderItemResponse getOrderItems(Long orderId) {
        return orderRepositoryQueryDSL.getOrderItems(orderId);
    }

    @Override
    public List<OrderCancel> orderCancel(Long orderId) {
        return orderRepositoryQueryDSL.orderCancel(orderId);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemJpaRepository.findByOrderId(orderId);
    }
}
