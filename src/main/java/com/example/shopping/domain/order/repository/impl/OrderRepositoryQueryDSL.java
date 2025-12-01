package com.example.shopping.domain.order.repository.impl;

import com.example.shopping.domain.order.dto.orderResponse.OrderItemResponse;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.order.dto.OrderCancel;
import com.example.shopping.domain.order.dto.OrderItemList;
import com.example.shopping.domain.order.dto.orderResponse.OrderResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.shopping.domain.order.entity.QOrder.order;
import static com.example.shopping.domain.order.entity.QOrderItem.orderItem;
import static com.example.shopping.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
// QueryDSL 사용할 Repository
public class OrderRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    // 주문 목록 조회
    public Page<OrderResponse> getOrders(User user, Pageable pageable) {
        // 1. 전제 개수 조회
        Long total = queryFactory.select(order.count())
            .from(order)
            .where(
                order.user.id.eq(user.getId())
            )
            .fetchOne();

        long totalCount = total != null ? total : 0L;

        if (totalCount == 0L) {
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2. 데이터 조회
        List<OrderResponse> purchaseList =
            queryFactory.select(Projections.constructor(
                    OrderResponse.class,
                    order.id,
                    order.status,
                    order.totalPrice
                ))
                .from(order)
                .where(
                    order.user.id.eq(user.getId())
                )
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3. PageImpl 생성 및 반환
        return new PageImpl<>(purchaseList, pageable, totalCount);
    }

    // 주문 상품들 조회
    public OrderItemResponse getOrderItems(Long orderId) {

        List<OrderItem> orderItems = queryFactory
            .selectFrom(orderItem)
            .leftJoin(orderItem.order, order).fetchJoin()
            .leftJoin(orderItem.product, product).fetchJoin()
            .where(order.id.eq(orderId))
            .fetch();

        // orderId, totalPrice 구하기
        Order foundOrder = orderItems.get(0).getOrder();

        List<OrderItemList> items = orderItems.stream()
            .map(item -> new OrderItemList(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getPrice(),
                item.getQuantity()))
            .toList();

        return new OrderItemResponse(
            foundOrder.getId(),
            foundOrder.getTotalPrice(),
            items);

    }

    // 주문 취소
    public List<OrderCancel> orderCancel(Long orderId) {
        return queryFactory.select(Projections.constructor(
                OrderCancel.class,
                orderItem.product.id,
                orderItem.quantity
            ))
            .from(orderItem)
            .where(orderItem.order.id.eq(orderId))
            .fetch();
    }
}
