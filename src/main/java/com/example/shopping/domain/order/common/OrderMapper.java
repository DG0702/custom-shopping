package com.example.shopping.domain.order.common;

import com.example.shopping.domain.cart.dto.OrderItemRequest;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.user.entity.User;

public class OrderMapper {

    // order Entity
    public static Order order(User user, Long totalPrice){

        return Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .status(OrderStatus.ORDERED)
                .build();
    }

    public static OrderItem orderItem(Order order, Product product, OrderItemRequest itemDto){

        return OrderItem.builder()
                .order(order)
                .product(product)
                .price(product.getPrice())
                .quantity(itemDto.getQuantity())
                .build();
    }
}
