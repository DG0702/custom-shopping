package com.example.shopping.domain.product.repository;

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
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JdbcProductRepository{
    Optional<Product> findById(Long productId);

    //상품 랭킹 조회
    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC LIMIT :size")
    List<Product> findProductRanking(@Param("size") Long size);

    //상품 목록 조회
    @Query("SELECT p FROM Product p ORDER BY p.id ASC")
    Page<Product> findAllProductPaged(Pageable pageable);

    // 단일 쿼리로 N+1 문제 발생, JDBC 배치 업데이트로 변경
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + :viewCount WHERE p.id = :productId")
    void updateDailyViewCount(@Param("productId")Long productId, @Param("viewCount") Long viewCount);

    @Query("SELECT p.viewCount FROM Product p WHERE p.id = :productId")
    Integer findViewCountById(@Param("productId") Long productId);

    // 재고 차감
    @Modifying
    @Query(value = "UPDATE products SET stock = stock - :quantity WHERE id = :productId AND stock >= :quantity", nativeQuery = true)
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 재고 복구
    @Modifying
    @Query(value = "UPDATE products SET stock = stock + :quantity WHERE id = :productId", nativeQuery = true)
    void increaseStock (@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIds(List<Long> ids);
}
