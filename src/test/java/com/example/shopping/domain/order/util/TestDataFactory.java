package com.example.shopping.domain.order.util;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;

    private Product testProduct;

    /**
     * 테스트 시작 전 상품 1개, 재고 100개 저장
     * @PostConstruct : 테스트 시작 전 초기화 (한 번만 실행)
     */
    //@PostConstruct
    public void init(){

        User user = User.builder()
                .email("test@example.com")
                .password("1234")
                .name("testUser")
                .address("testAddress")
                .userRole(UserRole.USER)
                .build();

        testUser = userRepository.save(user);

        Product product = Product.builder()
                    .name("상품")
                    .price(1000)
                    .stock(100)
                    .build();

        testProduct = productRepository.save(product);
    }

    // 유저 반환
    public User getTestUser(){
        return testUser;
    }

    // 상품 반환
    public Product getTestProduct(){
        return testProduct;
    }

    // 주문 생성할 dto 생성 → 요청 값(@RequestBody)
    public OrderRequestDto orderRequestDto (Long productId, Integer quantity){
        // 카트 안에 객체 생성(카트 안에 1개의 상품만 넣음)
        CartCreateRequestDto item = new CartCreateRequestDto(productId,quantity);

        List<CartCreateRequestDto> items = List.of(item);

        // 빈 객체 생성
        OrderRequestDto dto = new OrderRequestDto();

        // OrderRequestDto 객체 생성 (객체, 필드명, 값)
        ReflectionTestUtils.setField(dto,"items", items);

        return dto;
    }
}
