package com.example.shopping.domain.order.controller;

import com.example.shopping.domain.order.dto.CommonResponseDto;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.service.OrderService;
import com.example.shopping.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/cart")
    public ResponseEntity<CommonResponseDto> productOrder(
            @RequestBody OrderRequestDto dto,
            @AuthenticationPrincipal User user
    ){
        orderService.saveOrder(user, dto);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
