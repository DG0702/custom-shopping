package com.example.shopping.domain.order.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter
@Entity
@NoArgsConstructor
@Table(name = "orders")
@AllArgsConstructor
@Builder
public class Order extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}