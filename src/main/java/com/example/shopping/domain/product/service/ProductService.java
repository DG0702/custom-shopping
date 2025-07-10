package com.example.shopping.domain.product.service;

import com.example.shopping.domain.product.dto.request.ProductPatchRequestDto;
import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.ProductListResponseDto;
import com.example.shopping.domain.product.dto.response.ProductRankingDto;
import com.example.shopping.domain.product.dto.response.ReadProductDto;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    //상품Create
    @Transactional
    public void createProduct(ProductRequestDto request) {

        Product product = new Product(request.getName(), request.getDescription(), request.getPrice(), request.getStock());
        productRepository.save(product);
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
    public void updateProduct (Long productId, ProductPatchRequestDto request) {
        Product product = findByIdOrElseThrow(productId);
        product.updateProduct(request.getName(), request.getDescription(), request.getPrice(), request.getStock());
    }

    //상품Delete
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findByIdOrElseThrow(productId);
        productRepository.delete(product);
    }
    //상품 랭킹 조회
    @Transactional(readOnly = true)
    public List<ProductRankingDto> getProductRanking (Long size) {
        List<Product> products = productRepository.findProductRanking(size);
        return products.stream()
                .map(product -> new ProductRankingDto(
                        product.getId(),
                        product.getName(),
                        product.getViewCount()
                ))
                .toList();
    }
    //상품목록 페이징 해서 조회
    @Transactional(readOnly = true)
    public ProductListResponseDto getAllProductsPaged (int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Product> productpage = productRepository.findAllProductPaged(pageable);

        return new ProductListResponseDto(productpage);
    }

    //상품 예외처리 분리
    public Product findByIdOrElseThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다"));
    }
}
