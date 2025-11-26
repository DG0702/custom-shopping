package com.example.shopping.domain.order.integration;

import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.order.service.OrderService;
import com.example.shopping.domain.order.util.TestDataFactory;

import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.user.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


@SpringBootTest
@ActiveProfiles("test")
public class NoRockTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ProductRepository productRepository;

    // User user;
    //
    // Product product1;
    // List<Product> products;

    @Test
    void 락이_없는_주문_생성() throws InterruptedException {

        int originalStock = 100;
        int threadCount = 10;
        int orderQuantity = 20;
        int maxRetry = 100;

        User user = testDataFactory.createUser("test1@example.com", "pass1word", "testUser1", "test1Address");

        Product product1 = testDataFactory.createProduct("상품1", "상품1 설명", 1000, 100);
        Product product2 = testDataFactory.createProduct("상품2", "상품2 설명", 2000, 200);
        Product product3 = testDataFactory.createProduct("상품3", "상품3 설명", 500, 50);

        List<Product> products = List.of(product1, product2, product3);

        // 스레드 풀, 동기화 도구 , 스레드 수 = 주문자 수
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        // 성공/실패, 총시도횟수
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger totalAttemptCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // 스레드 풀에 주문 요청 보냄 (for문을 통해 보내기만 함)
        for (int i = 0; i < threadCount; i++) {
            // 요청 받은 걸 병렬적(동시에)으로 실행
            executor.submit(() -> {

                // 주문 요청
                List<CartItem> cartItems = testDataFactory.cartItems(user, products, orderQuantity);

                int attemptCount = 0;
                boolean success = false;

                while (!success && attemptCount < maxRetry) {
                    attemptCount++;
                    try {
                        // 동시 시작
                        barrier.await();
                        // 락이 없는 주문 생성
                        orderService.saveOrder(user, cartItems);
                        success = true;
                        successCount.incrementAndGet();
                        totalAttemptCount.addAndGet(attemptCount);
                    } catch (Exception e) {
                        if (attemptCount == maxRetry) {
                            failCount.incrementAndGet();
                            totalAttemptCount.addAndGet(attemptCount);
                            System.err.println("주문 실패 : " + e.getMessage());
                        }
                    }
                }
                latch.countDown();
            });
        }

        // 메인 스레드 대기
        latch.await();

        // 상품 1로 확인
        Product productAfterTest = productRepository.findById(product1.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        int retryCount = totalAttemptCount.get() - successCount.get();
        int finalStock = productAfterTest.getStock();
        int expectStock = originalStock - (successCount.get() * orderQuantity);

        long endTime = System.currentTimeMillis();

        System.out.println("=== 락 없이 주문 생성 테스트 결과 ===");
        System.out.println("한 스레드당 실행 횟수 : " + maxRetry + "번 실행");
        System.out.println("총 소요 시간(ms) : " + (endTime - startTime));
        System.out.println("성공 수 : " + successCount.get());
        System.out.println("실패 수 : " + failCount.get());
        System.out.println("총 시도 횟수(재시도 포함) : " + totalAttemptCount.get());
        System.out.println("재시도 횟수 : " + retryCount);

        System.out.println("기존 재고량 : " + originalStock);
        System.out.println("주문 수량 합계 (성공 스레드 x 주문 수량) : " + (successCount.get() * orderQuantity));
        System.out.println("예상 재고량  : " + expectStock);
        System.out.println("실제 재고량  : " + finalStock);

        if (finalStock != expectStock) {
            System.out.println("동시성 문제 발생 → 재고 수량 불일치");
        } else if (retryCount > 0) {
            System.out.println("동시성 문제 발생 → 재시도가 있었음");
        } else {
            System.out.println("재시도 없음, 재고 수량 정상");
        }
        // 스레드 풀 종료
        executor.shutdown();
    }

    @Test
    void 락이_없는_주문_취소() throws InterruptedException {

        int originalStock = 100;
        int orderQuantity = 5;
        int threadCount = 10;
        int maxRetry = 100;

        List<User> users = new ArrayList<>();
        List<Long> orderIds = new ArrayList<>();

        Product product1 = testDataFactory.createProduct("상품1", "상품1 설명", 1000, 100);
        Product product2 = testDataFactory.createProduct("상품2", "상품2 설명", 2000, 200);
        Product product3 = testDataFactory.createProduct("상품3", "상품3 설명", 500, 50);

        List<Product> products = List.of(product1, product2, product3);

        for(int i = 0; i < threadCount; i++){
            User user = testDataFactory.createUser(
                "test1" + i +  "@example.com",
                "pass" + i +"word",
                "testUser" + i,
                "test"+ i + "Address");

            users.add(user);

            List<CartItem> cartItems = testDataFactory.cartItems(user, products, orderQuantity);

            Long orderId = orderService.saveOrder(user, cartItems).getOrderId();
            orderIds.add(orderId);
        }


        Product productAfterOrder = productRepository.findById(product1.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 주문 후 재고량
        int orderAfterStock = productAfterOrder.getStock();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);


        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger totalAttemptCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                int attemptCount = 0;
                boolean success = false;

                while (!success && attemptCount < maxRetry) {
                    attemptCount++;
                    try {
                        barrier.await();
                        orderService.cancelOrder(users.get(index), orderIds.get(index));
                        success = true;
                        successCount.incrementAndGet();
                        totalAttemptCount.addAndGet(attemptCount);
                    } catch (Exception e) {
                        if (attemptCount == maxRetry) {
                            failCount.incrementAndGet();
                            totalAttemptCount.addAndGet(attemptCount);
                            System.out.println("주문 취소 실패 : " + e.getMessage());
                        }
                    }
                }
                latch.countDown();
            });
        }
        latch.await();

        Product productAfterCancel = productRepository.findById(product1.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        int expectStock = originalStock;
        int actualStock = productAfterCancel.getStock();
        long endTime = System.currentTimeMillis();

        System.out.println("=== 락 없는 주문 취소 테스트 결과 ===");
        System.out.println("총 스레드 개수 : " + threadCount);
        System.out.println("한 스레드당 최대 실행 횟수 : " + maxRetry + "번 실행");
        System.out.println("총 소요 시간 (ms) : " + (endTime - startTime));
        System.out.println("성공 횟수 : " + successCount.get());
        System.out.println("실패 횟수 : " + failCount.get());
        System.out.println("총 시도 횟수 : " + totalAttemptCount.get());
        System.out.println("실패 시도 횟수 : " + (totalAttemptCount.get() - successCount.get()));

        System.out.println("기존 재고량 : " + originalStock);
        System.out.println("주문 후 재고량 : " + orderAfterStock);
        System.out.println("예상 재고량 (정상 동작 시) : " + expectStock);
        System.out.println("실제 재고량 : " + actualStock);

        if (actualStock != expectStock) {
            System.out.println("동시성 문제 발생 → 재고 Lost Update");
        } else {
            System.out.println("정상적으로 처리");
        }
        executor.shutdown();
    }
}
