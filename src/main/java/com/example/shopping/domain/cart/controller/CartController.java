package com.example.shopping.domain.cart.controller;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.cart.service.CartService;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
        return ResponseEntity.ok(responseDto);
    }
}
