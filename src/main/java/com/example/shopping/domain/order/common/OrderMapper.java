package com.example.shopping.domain.order.common;

import com.example.shopping.domain.cart.dto.OrderItemRequest;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.user.entity.User;

public class OrderMapper {

    // order Entity
    public static Order order(User user, OrderRequestDto dto){
        return Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .build();
    }

    public static OrderItem orderItem(Order order, Product product, OrderRequestDto dto, OrderItemRequest itemDto){

        return OrderItem.builder()
                .order(order)
                .product(product)
                .price()
                .quantity(itemDto.getQuantity())
                .build();
    }
}
