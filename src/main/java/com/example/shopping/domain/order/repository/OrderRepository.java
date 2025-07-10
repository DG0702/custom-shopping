package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



/**
 * OrderJpaRepository + OrderItemJpaRepository + QueryDslRepository 를 함께 사용 할 수 있는 공통 인터페이스
 */
public interface OrderRepository {

    Order findById(Long id);

    Order save(Order order);

    Page<OrderResponseDto> getOrders(AuthUser authUser, Pageable pageable);
}
