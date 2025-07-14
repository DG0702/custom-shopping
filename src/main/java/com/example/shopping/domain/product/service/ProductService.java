package com.example.shopping.domain.product.service;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.PageResponseDto;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.product.dto.request.AddEventProductRequestDto;
import com.example.shopping.domain.product.dto.request.ProductPatchRequestDto;
import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.EventProductDto;
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
    private final RedisProductService redisProductService;

    //상품Create
    @Transactional
    public void createProduct(ProductRequestDto request) {

        Product product = new Product(request.getName(), request.getDescription(), request.getPrice(), request.getStock());
        productRepository.save(product);
    }

    //상품단건Read
    // DB는 실시간 조회수 반영 안함, 자정에 일일 조회수 합산
    // 일일 조회수는 실시간 처리
    @Transactional
    public ReadProductDto readProductById(AuthUser user, Long productId) {

        Product product = findByIdOrElseThrow(productId);

        // 영속성 조회수 증가(테스트용)
        //product.increaseViewCount();

        // redis 일일  조회수 증가
        redisProductService.incrementView(user.getId(), productId);

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
    public void updateProduct(Long productId, ProductPatchRequestDto request) {
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
    public List<ProductRankingDto> getProductRanking(Long size) {
        // Repository 에서 가져온 랭킹
        List<Product> products = productRepository.findProductRanking(size);

        return products.stream()
                .map(product -> new ProductRankingDto(
                        product.getId(),
                        product.getName(),
                        product.getViewCount()
                ))
                .toList();
    }

    //일일 상품 랭킹 조회
    @Transactional(readOnly = true)
    public List<ProductRankingDto> getRedisProductRanking(Long size) {
        return redisProductService.getProductsRanking(size);
    }

    //상품목록 페이징 해서 조회
    @Transactional(readOnly = true)
    public PageResponseDto<ReadProductDto> getAllProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Product> productpage = productRepository.findAllProductPaged(pageable);

        Page<ReadProductDto> readProductDtoPage = productpage.map(product -> new ReadProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getViewCount()));

        return new PageResponseDto<>(readProductDtoPage);
    }

    //상품 예외처리 분리
    public Product findByIdOrElseThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다"));
    }

    //일일 조회수 DB에 적용
    @Transactional
    public void syncTest() {
        redisProductService.syncDailyViewCount();
    }

    // 이벤트 상품 추가
    @Transactional
    public void addEventProduct(Long productId, AddEventProductRequestDto request) {
        Product product = findByIdOrElseThrow(productId);

        if (product.getStock() < request.getEventStock()) {
            throw new CustomException(
                    ExceptionCode.INVALID_PRODUCT_ATTRIBUTE,
                    "이벤트 상품 수량을 재고보다 더 많이 설정할 수 없습니다"
            );
        }
        if (product.getPrice() < request.getEventPrice()) {
            throw new CustomException(
                    ExceptionCode.INVALID_PRODUCT_ATTRIBUTE,
                    "이벤트 상품 가격이 기존 상품 가격보다 비쌀 수 없습니다"
            );
        }

        redisProductService.addEventProduct(
                product,
                request.getEventPrice(),
                request.getEventStock(),
                request.getStart(),
                request.getEnd()
        );
    }

    // 이벤트 상품 조회
    public List<EventProductDto> getEventProducts() {
        return redisProductService.getEventProducts();
    }
}
