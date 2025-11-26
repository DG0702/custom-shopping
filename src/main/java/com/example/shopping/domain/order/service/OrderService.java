package com.example.shopping.domain.order.service;

import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.domain.order.dto.orderResponse.OrderItemResponse;
import com.example.shopping.domain.order.dto.orderResponse.OrderResponse;
import com.example.shopping.domain.order.repository.OrderItemRepository;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.order.dto.*;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.order.repository.OrderRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.service.ProductService;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.service.UserQueryService;

import lombok.RequiredArgsConstructor;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductService productService;
    private final UserQueryService userQueryService;
    private final RedissonClient redissonClient;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    // 주문 생성 시 주문별 상품 락 생성
    public OrderResponse lockCreateOrder(Long userId) {

        User user = userQueryService.findById(userId);
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        // 오름차순으로 상품 ID 추출 (데드락 방지하기 위해)
        List<Long> productIds = cartItems.stream()
            .map(item -> item.getProduct().getId())
            .sorted()
            .toList();

        List<RLock> locks = new ArrayList<>();

        try {
            for (Long productId : productIds) {
                RLock lock = tryGetLockWithBackOff("lock:product:" + productId);
                locks.add(lock);
            }

            return saveOrder(user, cartItems);

        } finally {
            for (RLock lock : locks) {
                lock.unlock();
            }
        }
    }

    // 주문 생성
    @Transactional
    public OrderResponse saveOrder(User user, List<CartItem> cartItems) {

        // 주문한 상품
        Map<Long, Product> productMap = productService.getCartItems(cartItems);

        // 재고 감소
        productService.decreaseStock(cartItems, productMap);

        // 상품 주문 (상태 변경)
        Order order = Order.createOrder(user);

        Order saveOrder = orderRepository.save(order);

        // 주문한 상품들 리스트에 저장
        List<OrderItem> purchasedItems = getPurchasedItems(order, cartItems, productMap);

        // 총 가격
        order.updateTotalPrice(purchasedItems);

        cartRepository.deleteByUserId(user.getId());

        return new OrderResponse(saveOrder);
    }

    //  주문 목록 조회
    @Transactional
    public Page<OrderResponse> getOrders(Pageable pageable, Long userId) {

        User user = userQueryService.findById(userId);

        return orderRepository.getOrders(user, pageable);
    }

    // 주문 취소 (redisson 락 있음)
    @Transactional
    public void lockCancelOrder(Long userId, Long orderId) {

        User user = userQueryService.findById(userId);
        String lockKey = "lock:order:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquiredLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            System.out.println(Thread.currentThread().getName() + " 락 획득: " + acquiredLock);
            if (!acquiredLock) {
                throw new CustomException(ErrorCode.ALREADY_ORDER_CANCEL);
            }

            cancelOrder(user, orderId);

            // 트랜잭션 커밋 후 락 해제
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        lock.unlock();
                        System.out.println("트랜잭션 커밋 후 락 해제");
                    } else {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }
            });
        } catch (InterruptedException e) {
            throw new CustomException(ErrorCode.REDIS_LOCK_INTERRUPTED);
        }
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId);

        // 로그인한 사용자와 주문 소유자 일치 여부 확인
        validateOrderOwner(order, user);

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.ALREADY_ORDER_CANCEL);
        }

        // 주문 취소 상품들과 수량
        List<OrderCancel> items = orderRepository.orderCancel(orderId);

        // 재고 복구
        productService.increaseStock(items);

        // 상태 변화
        order.updateStatus();
    }

    // 주문 상품들 조회
    @Transactional
    public OrderItemResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId);
        User user = userQueryService.findById(userId);

        // 로그인한 사용자와 주문 소유자 일치 여부 확인
        validateOrderOwner(order, user);

        return orderRepository.getOrderItems(orderId);
    }

    // 주문 상품에 대해 락 획득 시도
    private RLock tryGetLockWithBackOff(String lockKey) {
        int retry = 0;
        long wait = 50;

        while (retry < 5) {
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean acquired = lock.tryLock(1, 5, TimeUnit.SECONDS);

                if (acquired) {
                    System.out.println("락 획득 성공 : " + lockKey + "retry : " + retry);
                    return lock;
                }
            } catch (InterruptedException e) {
                throw new CustomException(ErrorCode.REDIS_LOCK_INTERRUPTED);
            }

            try {
                Thread.sleep(wait);
            } catch (InterruptedException ignored) {
            }

            wait *= 2;
            retry++;
        }

        throw new CustomException(ErrorCode.ALREADY_ORDERING);
    }

    // 주문한 상품들
    private List<OrderItem> getPurchasedItems(Order order,
        List<CartItem> cartItems, Map<Long, Product> productMap) {

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProduct().getId());
            OrderItem orderItem = OrderItem.createOrderItem(
                order, product, product.getPrice(), item.getQuantity());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);

        return orderItems;
    }

    // 로그인한 사용자와 주문 소유자 일치 여부 확인
    private void validateOrderOwner(Order order, User user) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}