package com.example.shopping.domain.product.service;

import com.example.shopping.domain.product.dto.ProductRankingCache;
import com.example.shopping.domain.product.dto.ViewCountUpdateDto;
import com.example.shopping.domain.product.dto.request.AddEventProductRequest;
import com.example.shopping.domain.product.dto.response.EventProduct;
import com.example.shopping.domain.product.dto.response.ProductRanking;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashOperations<String, String, Object> hashOperations;
    private final RedisTemplate<String, ProductRankingCache> productRankingCacheRedisTemplate;

    // Redis 일일 조회수 증가(ZSet)
    public void incrementView(Long userId, Product product) {

        // 어뷰징 확인용 키
        String checkKey = "product :" + product.getId() + ", user :" + userId;

        // 어뷰 방지 검사 (true → 조회 증가, false → return)
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(checkKey, "1", Duration.ofMinutes(30));
        if (Boolean.FALSE.equals(isNew)) {
            log.info("No viewCount increment");
            return;
        }

        // ZSet 사용 (key,value,score)
        String key = "product:viewCount:" + LocalDate.now();
        ProductRankingCache productRankingCache = new ProductRankingCache(product);

        productRankingCacheRedisTemplate.opsForZSet()
            .incrementScore(key, productRankingCache, 1);
        productRankingCacheRedisTemplate.expire(key, Duration.ofDays(1));
    }

    // 일일 랭킹 TOP 10
    public List<ProductRanking> getProductsRanking() {
        String key = "product:viewCount:" + LocalDate.now();
        Set<ZSetOperations.TypedTuple<ProductRankingCache>> ranking =
            productRankingCacheRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 10);

        if (ranking == null || ranking.isEmpty()) {
            return new ArrayList<>();
        }

        List<ProductRanking> response = new ArrayList<>();
        for (ZSetOperations.TypedTuple<ProductRankingCache> tuple : ranking) {
            ProductRankingCache productInfo = tuple.getValue();
            Double score = tuple.getScore();

            //value, score 방어적 처리
            if (productInfo == null || score == null) {
                log.warn("Daily Ranking cache : rank={}, viewCount={}", productInfo, score);
                continue;
            }
            response.add(new ProductRanking(productInfo.getId(), productInfo.getName(), score.longValue()));
        }

        return response;
    }

    // 조회 수 업데이트 (ZSet + BatchUpdate) : Redis → DB
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void syncDailyViewCount() {

        // 실제로 사용할 때 자정 기준 전날
        String key = "product:viewCount:" + LocalDate.now().minusDays(1);

        Set<ZSetOperations.TypedTuple<ProductRankingCache>> data =
            productRankingCacheRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);

        if (data == null || data.isEmpty())
            return;

        List<ViewCountUpdateDto> updateList = new ArrayList<>();
        for (ZSetOperations.TypedTuple<ProductRankingCache> tuple : data) {
            Long productId = Objects.requireNonNull(tuple.getValue()).getId();
            Long viewCount = Objects.requireNonNull(tuple.getScore()).longValue();

            updateList.add(new ViewCountUpdateDto(productId, viewCount));
        }
        productRepository.batchUpdateDailyViewCount(updateList);
        productRankingCacheRedisTemplate.delete(key);
    }

    // 이벤트 상품 추가
    public void addEventProduct(Product product, AddEventProductRequest request) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), request.getEnd());

        String key = "event:product:" + product.getId();
        hashOperations.put(key, "name", product.getName());
        hashOperations.put(key, "price", request.getEventPrice());
        hashOperations.put(key, "stock", request.getEventStock());
        hashOperations.put(key, "startDateTime", request.getStart().toString());
        hashOperations.put(key, "endDateTime", request.getEnd().toString());

        redisTemplate.expire(key, Duration.ofDays(days + 1));
    }

    public List<EventProduct> getEventProducts() {
        Set<String> keys = redisTemplate.keys("event:product:*");

        if (keys == null || keys.isEmpty())
            return new ArrayList<>();

        List<EventProduct> eventProducts = new ArrayList<>();

        for (String key : keys) {
            Map<String, Object> data = hashOperations.entries(key);

            EventProduct dto = new EventProduct(
                Long.valueOf(key.replace("event:product:", "")),
                (String)data.get("name"),
                Integer.valueOf(data.get("price").toString()),
                Integer.valueOf(data.get("stock").toString()),
                LocalDateTime.parse(data.get("startDateTime").toString()),
                LocalDateTime.parse(data.get("endDateTime").toString())
            );

            eventProducts.add(dto);
        }

        return eventProducts;
    }
}