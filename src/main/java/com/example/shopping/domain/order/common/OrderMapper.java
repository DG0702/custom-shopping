package com.example.shopping.domain.order.common;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.user.entity.User;

public class OrderMapper {

    // order Entity
    public static Order order(User user, Integer totalPrice){

        return Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .status(OrderStatus.ORDERED)
                .build();
    }

    // OrderItem Entity
    public static OrderItem orderItem(Order order, Product product, CartCreateRequestDto itemDto){

        return OrderItem.builder()
                .order(order)
                .product(product)
                .price(product.getPrice())
                .quantity(itemDto.getQuantity())
                .build();
    }

    // Entity -> Dto
    public static OrderResponseDto payment(Order save){
            return OrderResponseDto.builder()
                    .orderId(save.getId())
                    .status(save.getStatus())
                    .totalPrice(save.getTotalPrice())
                    .build();
    }
}
