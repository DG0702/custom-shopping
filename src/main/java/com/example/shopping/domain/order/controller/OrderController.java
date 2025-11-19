package com.example.shopping.domain.order.controller;

import com.example.shopping.global.common.dto.ApiResponse;
import com.example.shopping.domain.order.dto.orderResponse.OrderItemResponse;
import com.example.shopping.domain.order.dto.orderResponse.OrderResponse;
import com.example.shopping.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 상품 주문
    @PostMapping()
    public ResponseEntity<ApiResponse<OrderResponse>> productOrder(
        @AuthenticationPrincipal Long userId) {

        OrderResponse response = orderService.lockCreateOrder(userId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("주문 완료", response));
    }

    // 주문 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
        Pageable pageable,
        @AuthenticationPrincipal Long userId) {

        Page<OrderResponse> response = orderService.getOrders(pageable, userId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("주문 목록 조회", response));
    }

    // 주문 취소
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
        @PathVariable Long orderId, @AuthenticationPrincipal Long userId) {

        orderService.lockCancelOrder(userId, orderId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("주문 취소", null));
    }

    // 주문 상품들 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> getOrder(
        @PathVariable Long orderId,
        @AuthenticationPrincipal Long userId) {

        OrderItemResponse response = orderService.getOrder(userId, orderId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("주문 상품들 조회", response));
    }

}
