package com.example.shopping.domain.product.dto.response;

import com.example.shopping.domain.product.entity.Product;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
@Getter
public class ProductListResponseDto {
    private List<ReadProductDto> products;
    private int currentPage;
    private int totalPage;
    private Long totalElements;
    private int size;

    public ProductListResponseDto (Page<Product> productPage) {
        this.products = productPage.getContent().stream()
                .map(product -> new ReadProductDto(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getViewCount()))
                .toList();
        this.currentPage = productPage.getNumber();
        this.totalPage = productPage.getTotalPages();
        this.totalElements = productPage.getTotalElements();
        this.size = productPage.getSize();

    }
}
