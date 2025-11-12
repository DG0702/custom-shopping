package com.example.shopping.domain.product.repository.impl;

import com.example.shopping.domain.product.entity.Product;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    //상품 목록 조회
    Page<Product> findAll(Pageable pageable);

    //상품 랭킹 조회
    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC LIMIT 10")
    List<Product> findProductRanking();

    // 재고 차감
    @Modifying
    @Query(value = "UPDATE products SET stock = stock - :quantity WHERE id = :productId AND stock >= :quantity", nativeQuery = true)
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 재고 복구
    @Modifying
    @Query(value = "UPDATE products SET stock = stock + :quantity WHERE id = :productId", nativeQuery = true)
    void increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIds(List<Long> ids);
}
