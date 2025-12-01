package com.example.shopping.domain.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockService {

    private final RedissonClient redissonClient;

    // 상품에 대한 락 획득 (주문 생성, 취소)
    public List<RLock> lockProducts(List<Long> productIds) {

        List<RLock> locks = new ArrayList<>();

        for (Long productId : productIds) {
            RLock lock = tryGetLockWithBackOff("lock:product : " + productId);
            locks.add(lock);
        }

        return locks;
    }

    // 단일 lock 제거 
    public void unlock(RLock lock){
        if(lock.isHeldByCurrentThread()){
            lock.unlock();
        }
    }

    // 여러 lock 제거
    public void unlockAll(List<RLock> locks){
        locks.forEach(this::unlock);
    }
    

    // 지수형 백오프 사용한 락 획득 재시도 전략
    private RLock tryGetLockWithBackOff(String lockKey) {
        int retry = 0;
        long wait = 50;

        while (retry < 5) {
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);

                if (acquired) {
                    System.out.println("락 획득 성공 : " + lockKey + " retry : " + retry);
                    return lock;
                }
            } catch (InterruptedException e) {
                throw new CustomException(ErrorCode.REDIS_LOCK_INTERRUPTED);
            }

            try {
                // 재시도 시 대기시간 (지수형 → 점차 늘어남)
                Thread.sleep(wait);
            } catch (InterruptedException ignored) {
            }

            wait *= 2;
            retry++;
        }

        throw new CustomException(ErrorCode.ALREADY_ORDERING);
    }
}
