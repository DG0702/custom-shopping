package com.example.shopping.domain.order.integration;

import com.example.shopping.domain.order.facade.OrderFacade;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
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
public class RedissonLockTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private Product product3;
    private List<Product> products;
    private int originalStock;
    private int threadCount = 10;
    private int orderQuantity = 20;

    @BeforeEach
    void setup() {
        product1 = testDataFactory.createProduct("상품1", "상품1 설명", 1000, 200);
        product2 = testDataFactory.createProduct("상품2", "상품2 설명", 2000, 200);
        product3 = testDataFactory.createProduct("상품3", "상품3 설명", 500, 200);

        products = List.of(product1, product2, product3);
        originalStock = product1.getStock();
    }

    @Test
    void Redisson_분산락_주문생성() throws InterruptedException {

        List<User> users = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            User user = testDataFactory.createUser(
                "test" + i + "@example.com",
                "password" + i,
                "user" + i,
                "address" + i
            );
            users.add(user);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {

                try {
                    // 동시 시작
                    barrier.await();

                    // 장바구니 목록
                    testDataFactory.cartItems(users.get(index), products, orderQuantity);

                    // 락이 존재한 주문 생성
                    orderFacade.createOrder(users.get(index).getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("주문 실패 : " + e.getMessage());
                }
                latch.countDown();
            });
        }

        latch.await();

        Product productAfterOrder = productRepository.findById(product1.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        int actualStock = productAfterOrder.getStock();
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
    void Redisson_분산락_주문취소() throws InterruptedException {

        List<User> users = new ArrayList<>();
        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            User user = testDataFactory.createUser(
                "test" + i + "@example.com",
                "password" + i,
                "user" + i,
                "address" + i
            );
            users.add(user);

            testDataFactory.cartItems(users.get(index), products, orderQuantity);
            Long orderId = orderFacade.createOrder(users.get(index).getId()).getOrderId();

            orderIds.add(orderId);
        }

        // 주문 후 재고 확인
        Product productAfterOrder = productRepository.findById(product1.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        int stockAfterOrder = productAfterOrder.getStock();

        System.out.println("주문 후 재고: " + stockAfterOrder);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {

                try {
                    barrier.await();

                    // 락이 존재한 주문 취소
                    orderFacade.cancelOrder(users.get(index).getId(), orderIds.get(index));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("주문 실패 : " + e.getMessage());
                }
                latch.countDown();
            });
        }

        latch.await();

        Product productAfterCancel = productRepository.findById(product1.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        int actualStock = productAfterCancel.getStock();
        int expectStock = originalStock;

        long endTime = System.currentTimeMillis();

        System.out.println("=== Redisson 락 주문 취소 테스트 결과 ===");
        System.out.println("총 소요 시간(ms) : " + (endTime - startTime));
        System.out.println("성공 수 : " + successCount.get());
        System.out.println("실패 수 : " + failCount.get());

        System.out.println("기존 재고량 : " + originalStock);
        System.out.println("주문 후 재고량 : " + stockAfterOrder);
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
