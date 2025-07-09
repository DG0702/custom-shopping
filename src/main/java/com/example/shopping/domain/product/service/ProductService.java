package com.example.shopping.domain.product.service;

import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.ReadProductDto;
import com.example.shopping.domain.product.dto.response.ProductResponseDto;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    //상품Create
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto request) {

        Product product = new Product(request.getName(), request.getDescription(), request.getPrice(), request.getStock());
        productRepository.save(product);

        return new ProductResponseDto(true, "상품이 등록되었습니다.");
    }

    //상품단건Read
    @Transactional
    public ReadProductDto readProductById(Long productId) {

        Product product = findByIdOrElseThrow(productId);
        product.increaseViewCount();

        return new ReadProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getViewCount()
        );
    }

    //상품Update
    @Transactional
    public ProductResponseDto updateProduct (Long productId, ProductRequestDto request) {

        Product product = findByIdOrElseThrow(productId);
        product.updateProduct(request.getName(),request.getDescription(), request.getPrice(), request.getStock());

        return new ProductResponseDto(true,"상품의 정보가 변경되었습니다.");
    }

    //상품Delete
    @Transactional
    public ProductResponseDto deleteProduct(Long productId) {
        Product product = findByIdOrElseThrow(productId);
        productRepository.delete(product);
        return new ProductResponseDto(true, "상품이 삭제되었습니다.");
    }

    //상품 예외처리 분리
    public Product findByIdOrElseThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다"));
    }
}
