package com.example.shopping.domain.product.repository.impl;

import com.example.shopping.domain.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
