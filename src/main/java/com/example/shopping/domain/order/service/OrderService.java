package com.example.shopping.domain.order.service;


import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.PageResponseDto;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.order.common.OrderMapper;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.dto.OrderResponseDto;
import com.example.shopping.domain.order.entity.Order;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.enums.OrderStatus;
import com.example.shopping.domain.order.repository.OrderRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserQueryService userQueryService;

    @Transactional
    public OrderResponseDto saveOrder(AuthUser user , OrderRequestDto dto){
        User buyer  = userQueryService.findByIdOrElseThrow(user.getId());

        // 주문한 상품 총 가격
        Integer totalPrice = calculateTotalPrice(dto);

        // 상품 주문(결제) 내용
        Order order = OrderMapper.order(buyer, totalPrice);

        // 주문한 수량한 만큼 재고 제거
        decreaseQuantity(dto);

        // 주문한 상품들 리스트에 저장
        getPurchasedItems(order,dto);
        
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
    
    // 주문 취소
    @Transactional
    public void cancelOrder(AuthUser user, Long orderId){

        Order order = orderRepository.findById(orderId);

        // 구매자가 주문을 취소하는 것이 맞는지 확인
        if(!order.getUser().getId().equals(user.getId())){
            throw new CustomException(ExceptionCode.FORBIDDEN);
        }

        order.updateOrderStatus(OrderStatus.CANCELED);
    }


    /**
     *  TODO 다수의 쿼리 발생으로 수정이 필요할것으로 예상됨
     */
    // 주문 상품 총 가격
    private Integer calculateTotalPrice(OrderRequestDto dto){
        int totalPrice = 0;

        for(CartCreateRequestDto itemDto : dto.getItems()){
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다"));

            Integer price = product.getPrice();
            Integer quantity = itemDto.getQuantity();

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
    private void getPurchasedItems(Order order, OrderRequestDto dto){

        // 주문 항목 저장
        for(CartCreateRequestDto itemDto : dto.getItems()){
            Product product  = productRepository.findById(itemDto.getProductId()).
                    orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

            OrderItem orderItem = OrderMapper.orderItem(order, product, itemDto);

            // orderItem 저장하기 위한 과정 
            order.addOrderItem(orderItem);
        }
    }

    // 주문한 수량한 만큼 재고 제거
    private void decreaseQuantity(OrderRequestDto dto){
        for(CartCreateRequestDto itemDto : dto.getItems()){
            Product product  = productRepository.findById(itemDto.getProductId()).
                    orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

            Integer stock = product.getStock();
            Integer quantity = itemDto.getQuantity();

            if(stock < quantity){
                throw new CustomException(ExceptionCode.PRODUCT_OUT_OF_STOCK);
            }

            Integer stockLeft = stock - quantity;
            product.updateStock(stockLeft);
        }
    }







}
