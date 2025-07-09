package com.example.shopping.domain.cart.controller;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.cart.dto.CartResponseDto;
import com.example.shopping.domain.cart.service.CartService;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.PageResponseDto;
import com.example.shopping.domain.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/api/cart")
    public ResponseEntity<ResponseDto<Void>> create(
        @RequestBody @Validated CartCreateRequestDto request,
        @AuthenticationPrincipal AuthUser user
    ){
        cartService.create(request, user.getId());
        ResponseDto<Void> responseDto = new ResponseDto<>("장바구니에 상품이 추가되었습니다.", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/api/cart")
    public ResponseEntity<ResponseDto<PageResponseDto<CartResponseDto>>> getCarts(
        @AuthenticationPrincipal AuthUser user,
        Pageable pageable
    ){
        PageResponseDto<CartResponseDto> page = cartService.getPage(user.getId(), pageable);
        ResponseDto<PageResponseDto<CartResponseDto>> response = new ResponseDto<>("장바구니 목록을 조회하였습니다.", page);
        return ResponseEntity.ok(response);
    }
}
