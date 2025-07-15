package com.example.shopping.domain.order.integration;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.order.entity.OrderItem;
import com.example.shopping.domain.order.repository.OrderItemRepository;
import com.example.shopping.domain.order.repository.OrderJpaRepository;
import com.example.shopping.domain.order.service.OrderService;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class OrderTest {

    private final int TEST_COUNT = 1000;
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemJpaRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        orderItemJpaRepository.deleteAll();
        orderRepository.deleteAll(); // 테스트 후 DB 비우기

        Map<Long, Integer> productStocks = new LinkedHashMap<>();
        productStocks.put(1L, 1000);
        productStocks.put(2L, 1000);
        productStocks.put(3L, 500);

        // 상품 재고 초기화
        productStocks.forEach((id, stock) -> {
            Product product = productRepository.findById(id)
                    .orElseGet(() -> productRepository.save(new Product("상품 " + id, "상품 " + id, 10000, stock)));
            product.updateStock(stock);
            productRepository.save(product);
        });

        User user = new User("test@test", "password", "testUser", "testAddress", UserRole.USER);
        userRepository.save(user);
    }

    @Test
    void 다양한상품_다양한수량_동시성_테스트() throws Exception {

        // 테스트용 상품 여러 개 준비
        Map<Long, Integer> productStocks = Map.of(
                1L, 1000,
                2L, 1000,
                3L, 500
        );

        final int THREAD_COUNT = 1000;  // 각 상품별 동시 주문 요청 수

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);

        AtomicInteger totalSuccess = new AtomicInteger();
        AtomicInteger totalFail = new AtomicInteger();

        // 각 productId별 주문된 수량
        Map<Long, AtomicInteger> expectedOrderedCount = new ConcurrentHashMap<>();
        productStocks.keySet().forEach(id -> expectedOrderedCount.put(id, new AtomicInteger(0)));

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    List<CartCreateRequestDto> carts = new ArrayList<>();
                    List<Long> productIds = new ArrayList<>(productStocks.keySet());
                    Collections.shuffle(productIds);

                    int itemCount = ThreadLocalRandom.current().nextInt(1, 4); // 1~3개 상품
                    for (int j = 0; j < itemCount; j++) {
                        Long productId = productIds.get(j);
                        int quantity = ThreadLocalRandom.current().nextInt(1, 4); // 1~3개 수량

                        CartCreateRequestDto cart = new CartCreateRequestDto();
                        ReflectionTestUtils.setField(cart, "productId", productId);
                        ReflectionTestUtils.setField(cart, "quantity", quantity);
                        carts.add(cart);
                    }

                    OrderRequestDto dto = new OrderRequestDto();
                    ReflectionTestUtils.setField(dto, "items", carts);

                    AuthUser user = new AuthUser(1L, "user", UserRole.USER);

                    barrier.await(); // 동시 시작

                    try {
                        orderService.saveOrder2(user, dto);
                        totalSuccess.incrementAndGet();

                        for (CartCreateRequestDto cart : carts) {
                            Long productId = (Long) ReflectionTestUtils.getField(cart, "productId");
                            Integer quantity = (Integer) ReflectionTestUtils.getField(cart, "quantity");
                            expectedOrderedCount.get(productId).addAndGet(quantity);
                        }

                    } catch (Exception e) {
                        totalFail.incrementAndGet();
                        System.out.println("주문 실패: " + e.getMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 실제 DB에서 주문 아이템 집계
        List<OrderItem> savedItems = orderItemJpaRepository.findAll();
        Map<Long, Integer> actualOrderedCount = new HashMap<>();
        for (OrderItem item : savedItems) {
            Long productId = item.getProduct().getId();
            int quantity = item.getQuantity();
            actualOrderedCount.merge(productId, quantity, Integer::sum);
        }

        // 검증
        for (Long productId : productStocks.keySet()) {
            int expected = expectedOrderedCount.get(productId).get();
            int actual = actualOrderedCount.getOrDefault(productId, 0);
            int remaining = productRepository.findById(productId).orElseThrow().getStock();
            int original = productStocks.get(productId);

            System.out.println("상품 ID: " + productId);
            System.out.println("  초기 재고: " + original);
            System.out.println("  예상 주문 총 수량: " + expected);
            System.out.println("  실제 주문 총 수량: " + actual);
            System.out.println("  남은 재고: " + remaining);

            // 검증
            assertEquals(expected, actual, "예상 주문 수량과 실제 주문 수량 불일치");
            assertEquals(original, actual + remaining, "재고 정합성 불일치");
            assertTrue(remaining >= 0, "재고가 음수가 되면 안 됩니다.");
        }

        System.out.println("총 성공 주문 수: " + totalSuccess.get());
        System.out.println("총 실패 주문 수: " + totalFail.get());
    }
}