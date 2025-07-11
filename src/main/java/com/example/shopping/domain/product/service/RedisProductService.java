package com.example.shopping.domain.product.service;

import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.product.dto.ProductRankingCacheDto;
import com.example.shopping.domain.product.dto.ViewCountUpdateDto;
import com.example.shopping.domain.product.dto.response.ProductRankingDto;
import com.example.shopping.domain.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, ProductRankingCacheDto> productRankingCacheRedisTemplate;

    public void incrementView(Long productId) {
        String key = "product:viewCount:" + LocalDate.now();
        productRankingCacheRedisTemplate.opsForZSet()
                .incrementScore(
                        key,
                        productRepository.findById(productId).map(ProductRankingCacheDto::new)
                                .orElseThrow(() ->
                                        new CustomException(ExceptionCode.PRODUCT_NOT_FOUND)),
                        1);
        productRankingCacheRedisTemplate.expire(key, Duration.ofDays(1));
    }

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

    //주석한 내용들은 성능 비교용, JDBC batchUpdate 통해서 N+1 문제 해결
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void syncDailyViewCount() {
        //Long start = System.currentTimeMillis();

        // 실제로 사용할 때 자정 기준 전날
        // String Key = "product:viewCount:" + LocalDate.now().minusDays(1);

        // 테스트용 금일
        String Key = "product:viewCount:" + LocalDate.now();

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

        productRankingCacheRedisTemplate.delete(Key);

        //Long end = System.currentTimeMillis();
        //log.info("걸린 시간: {}", end - start);
    }
}