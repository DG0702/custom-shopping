package com.example.shopping.domain.order.controller;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.ResponseDto;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.service.OrderService;
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
    public ResponseEntity<ResponseDto<OrderResponseDto>> productOrder(
            @RequestBody OrderRequestDto dto,
            @AuthenticationPrincipal AuthUser user
    ){

        OrderResponseDto orderResponseDto = orderService.saveOrder(user, dto);

        ResponseDto<OrderResponseDto> success = new ResponseDto<>("주문이 완료되었습니다.", orderResponseDto);


        return ResponseEntity.status(HttpStatus.OK).body(success);
    }


}
