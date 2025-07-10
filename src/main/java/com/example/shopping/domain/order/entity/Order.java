package com.example.shopping.domain.order.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


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

    // @Builder가 필드의 초기화를 무시하여 무시하도록 하기 위해 설정
    @Builder.Default
    @OneToMany (mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Order 저장 시 OrderItem 저장하기 위한 메서드 
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }


}