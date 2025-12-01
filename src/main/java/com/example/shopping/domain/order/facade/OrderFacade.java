package com.example.shopping.domain.order.facade;

import java.util.List;

import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.domain.order.dto.orderResponse.OrderResponse;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.repository.OrderRepository;
import com.example.shopping.domain.order.service.LockService;
import com.example.shopping.domain.order.service.OrderService;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.service.UserQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final UserQueryService userQueryService;
    private final CartRepository cartRepository;
    private final LockService lockService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    // 주문 생성
    public OrderResponse createOrder(Long userId) {
        // 데이터 조회
        User user = userQueryService.findById(userId);
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        // 상품 ID 추출 → 오름차순 정렬 (데드락 방지)
        List<Long> productIds = cartItems.stream()
            .map(item -> item.getProduct().getId())
            .sorted()
            .toList();

        List<RLock> locks = lockService.lockProducts(productIds);

        try{
            // 트랜잭션 적용
            return orderService.saveOrder(user, cartItems);
        }
        finally {
            // 락 해제 (트랜잭션 커밋 후)
            lockService.unlockAll(locks);
        }
    }

    // 주문 취소
    public void cancelOrder(Long userId, Long orderId) {
        User user = userQueryService.findById(userId);
        List<OrderItem> orderItems = orderRepository.findByOrderId(orderId);

        List<Long> productIds = orderItems.stream()
            .map(orderItem -> orderItem.getProduct().getId())
            .sorted()
            .toList();

        List<RLock> locks = lockService.lockProducts(productIds);

        try{
            orderService.cancelOrder(user, orderId);
        }
        finally {
            lockService.unlockAll(locks);
        }
    }
}
