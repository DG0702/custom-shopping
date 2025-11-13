package com.example.shopping.domain.cart.controller;

import com.example.shopping.domain.cart.dto.CartCreateRequest;
import com.example.shopping.domain.cart.dto.CartResponse;
import com.example.shopping.domain.cart.service.CartService;
import com.example.shopping.global.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    // 장바구니 상품 추가
    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> create(
        @RequestBody @Valid CartCreateRequest request,
        @AuthenticationPrincipal Long userId
    ) {
        cartService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("장바구니에 상품이 추가", null));
    }

    // 장바구니 목록 조회
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<CartResponse>>> getCarts(
        @AuthenticationPrincipal Long userId,
        Pageable pageable
    ) {
        Page<CartResponse> response = cartService.getAllCartItems(userId, pageable);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("장바구니 목록 조회", response));
    }

    // 장바구니 상품 삭제
    @DeleteMapping("{cartId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @PathVariable Long cartId,
        @AuthenticationPrincipal Long userId
    ) {
        cartService.delete(userId, cartId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("장바구니 항목이 삭제", null));
    }
}
