package com.example.shopping.domain.product.repository;

import com.example.shopping.domain.product.dto.ViewCountUpdateDto;

import java.util.List;

public interface JdbcProductRepository {
    void batchUpdateDailyViewCount(List<ViewCountUpdateDto> updateList);
}
