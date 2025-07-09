package com.example.shopping.domain.order.controller;

import com.example.shopping.domain.order.dto.CommonResponseDto;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/cart")
    public ResponseEntity<CommonResponseDto> productOrder(
            @RequestBody OrderRequestDto dto,
            HttpServletRequest request
    ){
        orderService.saveOrder(request, dto);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
