package com.example.shopping.domain.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.shopping.domain.product.dto.ViewCountUpdateDto;
import com.example.shopping.domain.product.entity.Product;

public interface ProductRepository {

    Product save(Product product);

    void delete(Product product);

    Optional<Product> findById(Long id);

    Page<Product> findAll(Pageable pageable);

    List<Product> findProductRanking();

    List<Product> findAllByIds(List<Long> ids);

    void batchUpdateDailyViewCount(List<ViewCountUpdateDto> updateList);
}
