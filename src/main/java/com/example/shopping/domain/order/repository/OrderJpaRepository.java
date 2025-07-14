package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.enums.OrderStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

// Order 사용할 Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o.status FROM Order o WHERE o.id = :orderId")
    OrderStatus findByOrderStatus(@Param("orderId") Long orderId);

    // 상태 변화 (NativeQuery를 이용하여 트랜잭션 내에서 즉시 변화 → 단 트랜잭션 커밋이 되어야 다른 스레드에서 적용이 됨)
    @Modifying()
    @Query(value = "UPDATE orders  SET status = :status WHERE id = :orderId", nativeQuery = true)
    void changeStatus(@Param("orderId") Long orderId, @Param("status") String status);
}
