package com.example.shopping.domain.product.service;

import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.product.dto.ProductRankingCacheDto;
import com.example.shopping.domain.product.dto.ViewCountUpdateDto;
import com.example.shopping.domain.product.dto.response.EventProductDto;
import com.example.shopping.domain.product.dto.response.ProductRankingDto;
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
    private final RedisTemplate<String, ProductRankingCacheDto> productRankingCacheRedisTemplate;

    // Redis 일일 조회수 증가(ZSet)
    public void incrementView(Long userId, Product product) {
        // 어뷰징 확인용 키
        String checkKey = "viewd:" + product.getId() + ":" + userId;

        // 동일한 키 있으면 리턴, 없으면 확인용 키 추가하고 조회수 증가
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(checkKey, "1", Duration.ofMinutes(30));
        if (Boolean.FALSE.equals(isNew)) {
            log.info("No viewCount increment");
            return;
        }

        // 일일 조회수 증가
        // 날짜로 묶어서 넣고 <key, score> 에서 score 를 증가시키는 방식
        String key = "product:viewCount:" + LocalDate.now();
        productRankingCacheRedisTemplate.opsForZSet()
                .incrementScore(
                        key,
                        new ProductRankingCacheDto(product), 1);

        productRankingCacheRedisTemplate.expire(key, Duration.ofDays(3));
    }

    // 일일 랭킹 조회
    public List<ProductRankingDto> getProductsRanking(Long rank) {
        String key = "product:viewCount:" + LocalDate.now();
        Set<ZSetOperations.TypedTuple<ProductRankingCacheDto>> ranking =
                productRankingCacheRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, rank - 1);

        if(ranking == null || ranking.isEmpty()) {
            return new ArrayList<>();
        }

        List<ProductRankingDto> response = new ArrayList<>();
        for(ZSetOperations.TypedTuple<ProductRankingCacheDto> tuple : ranking) {
            ProductRankingCacheDto productDto = tuple.getValue();
            Double score = tuple.getScore();

            //value, score 방어적 처리
            if(productDto == null || score == null) {
                log.warn("Daily Ranking cache : rank={}, viewCount={}", productDto, score);
                continue;
            }
            response.add(new ProductRankingDto(productDto.getId(), productDto.getName(), score.longValue()));
        }

        return response;
    }

    // 주석한 내용들은 성능 비교용, JDBC batchUpdate 통해서 N+1 문제 해결
    // 자정마다 Redis 일일 조회수 정산해서 DB에 합산
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void syncDailyViewCount() {
        //Long start = System.currentTimeMillis();

        // 실제로 사용할 때 자정 기준 전날
        String Key = "product:viewCount:" + LocalDate.now().minusDays(1);

        // 테스트용 금일
        //String Key = "product:viewCount:" + LocalDate.now();

        Set<ZSetOperations.TypedTuple<ProductRankingCacheDto>> data = productRankingCacheRedisTemplate
                .opsForZSet().reverseRangeWithScores(Key, 0, -1);

        if(data == null || data.isEmpty()) return;

        List<ViewCountUpdateDto> updateList = new ArrayList<>();
        for(ZSetOperations.TypedTuple<ProductRankingCacheDto> tuple : data) {
            Long productId = Objects.requireNonNull(tuple.getValue()).getId();
            Long viewCount = Objects.requireNonNull(tuple.getScore()).longValue();
            //productRepository.updateDailyViewCount(productId, viewCount);

            updateList.add(new ViewCountUpdateDto(productId, viewCount));
        }
        productRepository.batchUpdateDailyViewCount(updateList);

        //Long end = System.currentTimeMillis();
        //log.info("걸린 시간: {}", end - start);
    }

    public void addEventProduct(Product product, Integer eventPrice, Integer eventStock, LocalDateTime start, LocalDateTime end){
        long days = ChronoUnit.DAYS.between(LocalDate.now(), end);

        String key = "event:product:" + product.getId();
        hashOperations.put(key, "name", product.getName());
        hashOperations.put(key, "price", eventPrice);
        hashOperations.put(key, "stock", eventStock);
        hashOperations.put(key, "startDateTime", start.toString());
        hashOperations.put(key, "endDateTime", end.toString());

        redisTemplate.expire(key, Duration.ofDays(days + 1));
    }

    public List<EventProductDto> getEventProducts() {
        Set<String> keys = redisTemplate.keys("event:product:*");

        List<EventProductDto> eventProducts = new ArrayList<>();

        for(String key : keys) {
            Map<String, Object> data = hashOperations.entries(key);

            EventProductDto dto = new EventProductDto(
                    Long.valueOf(key.replace("event:product:", "")),
                    (String) data.get("name"),
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