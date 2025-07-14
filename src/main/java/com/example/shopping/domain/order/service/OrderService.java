package com.example.shopping.domain.order.service;


import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.PageResponseDto;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.order.common.OrderMapper;
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

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserQueryService userQueryService;
    private final RedissonClient redissonClient;


    // 주문 생성(락 없이)
    @Transactional
    public OrderResponseDto saveOrder(AuthUser user , OrderRequestDto dto){
        User buyer  = userQueryService.findByIdOrElseThrow(user.getId());

        // 주문한 상품
        Map<Long, Product> productMap = productService.getProductMap(dto);

        // 주문한 상품 총 가격
        Integer totalPrice = calculateTotalPrice(dto, productMap);

        // 상품 주문(결제) 내용
        Order order = OrderMapper.order(buyer, totalPrice);

        // 주문한 수량한 만큼 재고 제거
        productService.decreaseQuantity(dto);

        // 주문한 상품들 리스트에 저장
        getPurchasedItems(order, dto, productMap);

        // Order, OrderItem 저장
        Order saveOrder = orderRepository.save(order);

        return OrderMapper.payment(saveOrder);
    }



    // 회원 구매한 상품 목록 조회
    @Transactional
    public PageResponseDto<OrderResponseDto> getOrders(Pageable pageable, AuthUser user){

        // 구매 상풍 목록
        Page<OrderResponseDto> purchaseList = orderRepository.getOrders(user, pageable);

        return new PageResponseDto<>(purchaseList);
    }

    // 주문 취소 (락 없이)
    @Transactional
    public void cancelOrder(AuthUser user, Long orderId){
        Order order = orderRepository.findById(orderId);

        // 로그인한 사용자와 주문 소유자 일치 여부 확인
        validateOrderOwner(order,user);

        // 1차 캐시를 피하기 위해 DB에서 직접 조회
        if(orderRepository.findByOrderStatus(orderId) == OrderStatus.CANCELED){
            throw new CustomException(ExceptionCode.ALREADY_ORDER_CANCEL);
        }

        // 주문 취소 상품들과 수량
        List<OrderCancelDto> items = orderRepository.orderCancel(orderId);

        // 재고 복구
        productService.increaseQuantity(items);
        
        // 상태 변화
        orderRepository.changeStatus(orderId,OrderStatus.CANCELED.name());
    }

    // 주문 상품들 조회
    @Transactional
    public OrderItemResponseDto getOrder(AuthUser user, Long orderId, Pageable pageable){
        Order order = orderRepository.findById(orderId);

        // 로그인한 사용자와 주문 소유자 일치 여부 확인
        validateOrderOwner(order,user);

        Page<OrderItemListDto> orderItems = orderRepository.getOrderItems(order,pageable);

        return OrderMapper.orderItemResponseDto(order,orderId, orderItems);
    }



    /**
     *  TODO 다수의 쿼리 발생으로 수정이 필요할것으로 예상됨
     */
    // 주문 상품 총 가격
    private Integer calculateTotalPrice(OrderRequestDto dto, Map<Long, Product> productMap){
        int totalPrice = 0;

        for(CartCreateRequestDto itemDto : dto.getItems()){
            // 상품 확인
            Product product = productMap.get(itemDto.getProductId());

            if(product.getPrice() == null || itemDto.getQuantity() == null){
                throw new CustomException(ExceptionCode.PRICE_OR_QUANTITY_REQUIRED);
            }

            totalPrice += (product.getPrice() * itemDto.getQuantity());
        }
        return  totalPrice;
    }

    /**
     *  TODO 다수의 쿼리 발생으로 수정이 필요할것으로 예상됨
     */
    // 주문한 상품들
    private void getPurchasedItems(Order order, OrderRequestDto dto, Map<Long, Product> productMap){

        // 주문 항목 저장
        for(CartCreateRequestDto itemDto : dto.getItems()){
            Product product = productMap.get(itemDto.getProductId());

            OrderItem orderItem = OrderMapper.orderItem(order, product, itemDto);

            // orderItem 저장하기 위한 과정
            order.addOrderItem(orderItem);
        }
    }


    // 로그인한 사용자와 주문 소유자 일치 여부 확인
    private void validateOrderOwner(Order order, AuthUser user){
        if(!order.getUser().getId().equals(user.getId())){
            throw new CustomException(ExceptionCode.FORBIDDEN);
        }
    }


    @Transactional
    // 주문 생성 (redisson 락 있음)
    public OrderResponseDto lockCreateOrder(AuthUser user , OrderRequestDto dto){
        List<Long> productIds = new ArrayList<>();

        for(CartCreateRequestDto itemDto : dto.getItems()){
            productIds.add(itemDto.getProductId());
        }

        List<RLock> locks = new ArrayList<>();

        try{
            for(Long productId : productIds){
                RLock lock = redissonClient.getLock("lock:product:" + productId);
                boolean acquiredLock = lock.tryLock(3,5, TimeUnit.SECONDS);
                System.out.println(Thread.currentThread().getName() + " 락 획득: " + acquiredLock);
                
                // 이미 획득한 락 모두 해제(데드락 방지)
                if(!acquiredLock){
                    for(RLock rLock : locks){
                        rLock.unlock();
                    }
                    throw new CustomException(ExceptionCode.ALREADY_ORDERING);
                }
                locks.add(lock);
            }
            return saveOrder(user,dto);

        } catch (InterruptedException e) {
            throw new CustomException(ExceptionCode.REDIS_LOCK_INTERRUPTED);
        } finally {
            for(RLock lock : locks){
                lock.unlock();
            }
        }
    }

    // 주문 취소 (redisson 락 있음)
    @Transactional
    public void lockCancelOrder(AuthUser user , Long orderId){

        String lockKey = "lock:order:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);


        try{
            boolean acquiredLock = lock.tryLock(3,5, TimeUnit.SECONDS);
            System.out.println(Thread.currentThread().getName() + " 락 획득: " + acquiredLock);
            if(!acquiredLock){
                throw new CustomException(ExceptionCode.ALREADY_ORDER_CANCEL);
            }

            cancelOrder(user, orderId);

            // 트랜잭션 커밋 후 락 해제
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if(status == TransactionSynchronization.STATUS_COMMITTED){
                        lock.unlock();
                        System.out.println("트랜잭션 커밋 후 락 해제");
                    }else{
                        if(lock.isHeldByCurrentThread()){
                            lock.unlock();
                        }
                    }
                }
            });
        }catch(InterruptedException e){
            throw new CustomException(ExceptionCode.REDIS_LOCK_INTERRUPTED);
        }
    }

//    public OrderRequestDto lettuceCreateOrder(AuthUser user , OrderRequestDto dto){
//
//    }

}