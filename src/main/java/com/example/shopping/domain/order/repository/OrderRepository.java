package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.order.dto.orderResponse.OrderItemResponse;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.order.dto.OrderCancel;
import com.example.shopping.domain.order.dto.orderResponse.OrderResponse;
import com.example.shopping.domain.order.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * OrderJpaRepository + OrderItemJpaRepository + QueryDslRepository 를 함께 사용 할 수 있는 공통 인터페이스
 */
public interface OrderRepository {

    Order findById(Long id);

    Order save(Order order);

    Page<OrderResponse> getOrders(User user, Pageable pageable);

    OrderItemResponse getOrderItems(Long orderId);

    List<OrderCancel> orderCancel(Long orderId);
}
