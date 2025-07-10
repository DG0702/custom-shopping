package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

// OrderItemJpa 사용할 Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
