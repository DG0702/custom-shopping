package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * OrderJpaRepository + OrderItemJpaRepository + QueryDslRepository 를 함께 사용 할 수 있는 공통 인터페이스
 */
public interface OrderRepository {

    Order save(Order order);

    List<OrderItem> saveAll(List<OrderItem> orderItems);

    Page<OrderResponseDto> getOrders(AuthUser authUser, Pageable pageable);
}
