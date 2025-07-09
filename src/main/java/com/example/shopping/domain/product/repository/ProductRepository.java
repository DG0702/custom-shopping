package com.example.shopping.domain.product.repository;

import com.example.shopping.domain.product.entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC LIMIT :size")
    List<Product> findProductRanking(@Param("size") Long size);
}
