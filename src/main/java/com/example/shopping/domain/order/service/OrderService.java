package com.example.shopping.domain.order.service;

import com.example.shopping.config.JwtUtil;
import com.example.shopping.domain.cart.dto.OrderItemRequest;
import com.example.shopping.domain.order.common.OrderMapper;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.repository.OrderItemRepository;
import com.example.shopping.domain.order.repository.OrderRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    public void saveOrder(HttpServletRequest request, OrderRequestDto dto){
        String userEmail = jwtUtil.extractEmail(request);

        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new RuntimeException("유저가 존재하지 않습니다.")
        );

        Order order = OrderMapper.order(user,dto);

        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();

        // 주문 항목 저장
        for(OrderItemRequest itemDto : dto.getItems()){
            Product product  = productRepository.findById(itemDto.getProductId()).
                    orElseThrow(RuntimeException::new);

            OrderItem orderItem = OrderMapper.orderItem(order, product, dto, itemDto);

            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
    }







}
