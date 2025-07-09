package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

// Order 사용할 Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

}
