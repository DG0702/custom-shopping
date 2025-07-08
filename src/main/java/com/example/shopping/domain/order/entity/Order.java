package com.example.shopping.domain.order.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "orders")
public class Order extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany
    private List<OrderItem> orderItems;

    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}