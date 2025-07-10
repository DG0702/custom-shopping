package com.example.shopping.domain.order.controller;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.PageResponseDto;
import com.example.shopping.domain.common.dto.ResponseDto;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 상품 주문(결졔)
    @PostMapping("/cart")
    public ResponseEntity<ResponseDto<OrderResponseDto>> productOrder(
            @Valid @RequestBody OrderRequestDto dto,
            @AuthenticationPrincipal AuthUser user
    ){

        OrderResponseDto orderResponseDto = orderService.saveOrder(user, dto);

        ResponseDto<OrderResponseDto> success = new ResponseDto<>("주문이 완료되었습니다.", orderResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(success);
    }

    // 주문 목록 조회
    @GetMapping
    public ResponseEntity<ResponseDto<PageResponseDto<OrderResponseDto>>> getOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(5) int size,
            @AuthenticationPrincipal AuthUser user
    ){
        Pageable pageable = PageRequest.of(page,size, Sort.by("create_at").descending());

        PageResponseDto<OrderResponseDto> orders = orderService.getOrders(pageable, user);

        ResponseDto<PageResponseDto<OrderResponseDto>> success = new ResponseDto<>("주문 목록을 조회 하였습니다.", orders);

        return ResponseEntity.status(HttpStatus.OK).body(success);
    }


}
