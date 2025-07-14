package com.example.shopping.domain.order.integration;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.service.OrderService;
import com.example.shopping.domain.order.util.TestDataFactory;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
public class RedissonLockTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ProductRepository productRepository;

    @Test
    void Redisson_분산락_주문생성() throws InterruptedException{
        User testUser = testDataFactory.getTestUser();
        AuthUser user = new AuthUser(testUser.getId(), testUser.getEmail(), testUser.getUserRole());
        Product testProduct = testDataFactory.getTestProduct();

        // 기존 재고량
        int originalStock = testProduct.getStock();
        int threadCount = 10;
        Long productId = testProduct.getId();
        int orderQuantity = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for(int i = 0; i < threadCount; i++){
            executor.submit(()->{
                OrderRequestDto dto = testDataFactory.orderRequestDto(productId, orderQuantity);

                try{
                    // 락이 존재한 주문 생성
                    orderService.lockCreateOrder(user,dto);
                    successCount.incrementAndGet();
                }
                catch(Exception e){
                    failCount.incrementAndGet();
                    System.out.println("주문 실패 : " + e.getMessage());
                }
                latch.countDown();
            });
        }

        latch.await();

        Product product = productRepository.findById(testProduct.getId())
                .orElseThrow(() -> new CustomException(ExceptionCode.PRODUCT_NOT_FOUND));

        int actualStock = product.getStock();
        int expectStock = originalStock - (successCount.get() * orderQuantity);

        long endTime = System.currentTimeMillis();

        System.out.println("=== Redisson 락 주문 생성 테스트 결과 ===");
        System.out.println("총 소요 시간(ms) : " + (endTime - startTime));
        System.out.println("성공 수 : " + successCount.get());
        System.out.println("실패 수 : " + failCount.get());


        System.out.println("기존 재고량 : " + originalStock);
        System.out.println("주문 수량 합계 : " + (successCount.get()) * orderQuantity);
        System.out.println("예상 재고량 : " + expectStock);
        System.out.println("실제 재고량 : " + actualStock);

        if (actualStock != expectStock) {
            System.out.println("동시성 문제 발생 → 재고 수량 불일치");
        } else {
            System.out.println("재시도 없음, 재고 수량 정상");
        }

        executor.shutdown();
    }
    
    @Test
    void Redisson_분산락_주문취소() throws InterruptedException{
        User testUser = testDataFactory.getTestUser();
        AuthUser user = new AuthUser(testUser.getId(), testUser.getEmail(), testUser.getUserRole());
        Product testProduct = testDataFactory.getTestProduct();


        int originalStock = testProduct.getStock();
        int threadCount = 10;

        OrderRequestDto dto = testDataFactory.orderRequestDto(testProduct.getId(), 1);
        Long orderId = orderService.saveOrder(user, dto).getOrderId();
        int orderAfterStock = productRepository.findById(testProduct.getId()).get().getStock();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for(int i = 0; i < threadCount; i++){
            executor.submit(()->{
                try{
                    // 락이 존재한 주문 취소
                    orderService.lockCancelOrder(user,orderId);
                    successCount.incrementAndGet();
                }
                catch(Exception e){
                    failCount.incrementAndGet();
                    System.out.println("주문 실패 : " + e.getMessage());
                }
                latch.countDown();
            });
        }

        latch.await();

        Product product = productRepository.findById(testProduct.getId())
                .orElseThrow(() -> new CustomException(ExceptionCode.PRODUCT_NOT_FOUND));

        int actualStock = product.getStock();
        int expectStock = originalStock;

        long endTime = System.currentTimeMillis();

        System.out.println("=== Redisson 락 주문 취소 테스트 결과 ===");
        System.out.println("총 소요 시간(ms) : " + (endTime - startTime));
        System.out.println("성공 수 : " + successCount.get());
        System.out.println("실패 수 : " + failCount.get());


        System.out.println("기존 재고량 : " + originalStock);
        System.out.println("주문 후 재고량 : " + orderAfterStock);
        System.out.println("예상 재고량 : " + expectStock);
        System.out.println("실제 재고량 : " + actualStock);

        if (actualStock != expectStock) {
            System.out.println("동시성 문제 발생 → 재고 수량 불일치");
        } else {
            System.out.println("재시도 없음, 재고 수량 정상");
        }
        executor.shutdown();
    }
}
