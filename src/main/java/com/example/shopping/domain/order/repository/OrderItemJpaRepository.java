package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

// OrderItemJpa 사용할 Repository (추후 삭제 될 수 있을 거 같아보임 -> cascade 처리로 인해)
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
