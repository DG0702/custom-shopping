package com.example.shopping.domain.order.repository;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.order.dto.OrderCancelDto;
import com.example.shopping.domain.order.dto.OrderItemListDto;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor

// OrderRepository 구현체
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderjpaRepository;
    private final QueryDslRepository queryDslRepository;


    @Override
    public Order findById(Long id) {
        return orderjpaRepository.findById(id).
                orElseThrow(() -> new CustomException(ExceptionCode.ORDER_NOT_FOUND));
    }

    @Override
    public Order save(Order order) {
        return orderjpaRepository.save(order);
    }

    @Override
    public Page<OrderResponseDto> getOrders(AuthUser authUser, Pageable pageable) {
        return queryDslRepository.getOrders(authUser, pageable);
    }

    @Override
    public Page<OrderItemListDto> getOrderItems(Order order, Pageable pageable) {
        return queryDslRepository.getOrderItems(order, pageable);
    }

    @Override
    public List<OrderCancelDto> orderCancel(Long orderId) {
        return queryDslRepository.orderCancel(orderId);
    }

    @Override
    public OrderStatus findByOrderStatus(Long orderId) {
        return orderjpaRepository.findByOrderStatus(orderId);
    }

    @Override
    public void changeStatus(Long orderId, String status) {
        orderjpaRepository.changeStatus(orderId,status);
    }


}
