package com.example.shopping.domain.cart.controller;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    public String create(
        @RequestBody @Validated CartCreateRequestDto request
    ){
        //TODO 인증 처리
        Long userId = 1L;
        cartService.create(request, userId);
        return "장바구니에 상품이 추가되었습니다.";
    }
}
