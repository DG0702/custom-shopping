package com.example.shopping.domain.order.service;


import com.example.shopping.domain.cart.dto.OrderItemRequest;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.order.common.OrderMapper;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.repository.OrderItemRepository;
import com.example.shopping.domain.order.repository.OrderRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;


    public OrderResponseDto saveOrder(AuthUser user , OrderRequestDto dto){
        Long id = user.getId();

        User payer = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        // 주문한 상품 총 가격
        long totalPrice = calculateTotalPrice(dto);

        // 상품 주문(결제) 내용
        Order order = OrderMapper.order(payer, totalPrice);

        // Order 테이블 저장
        Order save = orderRepository.save(order);

        // 주문한 상품들
        List<OrderItem> orderItems = getPurchasedItems(order,dto);
        
        // Order_Item 테이블 저장
        orderItemRepository.saveAll(orderItems);

        return OrderMapper.payment(save);
    }


    /**
     *  TODO 다수의 쿼리 발생으로 수정이 필요할것으로 예상됨
     */
    // 주문 상품 총 가격
    private Long calculateTotalPrice(OrderRequestDto dto){
        long totalPrice = 0L;

        for(OrderItemRequest itemDto : dto.getItems()){
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다"));

            Long price = product.getPrice();
            Long quantity = itemDto.getQuantity();

            if(price == null || quantity == null){
                throw new IllegalArgumentException("가격 또는 수량이 잘못되었습니다.");
            }

            totalPrice += (product.getPrice() * itemDto.getQuantity());
        }
        return  totalPrice;
    }

    /**
     *  TODO 다수의 쿼리 발생으로 수정이 필요할것으로 예상됨
     */
    // 주문한 상품들
    private List<OrderItem> getPurchasedItems(Order order, OrderRequestDto dto){
        List<OrderItem> orderItems = new ArrayList<>();

        // 주문 항목 저장
        for(OrderItemRequest itemDto : dto.getItems()){
            Product product  = productRepository.findById(itemDto.getProductId()).
                    orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

            OrderItem orderItem = OrderMapper.orderItem(order, product, itemDto);

            orderItems.add(orderItem);
        }
        return orderItems;
    }







}
