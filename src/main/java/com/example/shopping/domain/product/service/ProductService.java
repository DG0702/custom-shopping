package com.example.shopping.domain.product.service;

import com.example.shopping.domain.product.dto.response.ProductResponse;
import com.example.shopping.domain.cart.dto.CartCreateRequest;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.product.dto.request.AddEventProductRequest;
import com.example.shopping.domain.order.dto.OrderCancelDto;
import com.example.shopping.domain.order.dto.OrderRequestDto;
import com.example.shopping.domain.product.dto.request.ProductPatchRequest;
import com.example.shopping.domain.product.dto.request.ProductRequest;
import com.example.shopping.domain.product.dto.response.EventProduct;
import com.example.shopping.domain.product.dto.response.ProductRanking;
import com.example.shopping.domain.product.dto.response.ProductInfoResponse;
import com.example.shopping.domain.product.entity.Product;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisProductService redisProductService;

    // 상품 생성
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        Product product = new Product(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getStock());

        productRepository.save(product);

        return new ProductResponse(product);
    }

    // 상품 단건 조회
    // DB는 실시간 조회수 반영 안함, 자정에 일일 조회수 합산
    // 일일 조회수는 실시간 처리
    @Transactional
    public ProductInfoResponse getProduct(Long userId, Long productId) {

        Product product = findByIdOrElseThrow(productId);

        // redis 일일 조회수 증가
        redisProductService.incrementView(userId, product);

        return new ProductInfoResponse(product);
    }

    //상품 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<ProductInfoResponse> getAllProducts(Pageable pageable) {

        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> new ProductInfoResponse(
            product.getId(), product.getName(),
            product.getDescription(), product.getPrice(),
            product.getStock(), product.getViewCount()));
    }

    //상품 수정
    @Transactional
    public ProductInfoResponse updateProduct(Long productId, ProductPatchRequest request) {
        Product product = findByIdOrElseThrow(productId);
        product.updateProduct(
            request.getName(), request.getDescription(),
            request.getPrice(), request.getStock());

        return new ProductInfoResponse(product);
    }

    //상품 삭제
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findByIdOrElseThrow(productId);
        productRepository.delete(product);
    }

    //상품 랭킹 TOP 10 조회
    @Transactional(readOnly = true)
    public List<ProductRanking> getProductRanking() {

        List<Product> products = productRepository.findProductRanking();

        return products.stream().map(product -> new ProductRanking(
            product.getId(),
            product.getName(),
            product.getViewCount())).toList();
    }

    //일일 상품 랭킹 TOP 10 조회
    @Transactional(readOnly = true)
    public List<ProductRanking> getRedisProductRanking() {
        return redisProductService.getProductsRanking();
    }

    //일일 조회수 DB에 적용
    @Transactional
    public void syncTest() {
        redisProductService.syncDailyViewCount();
    }

    // 이벤트 상품 추가
    @Transactional
    public void addEventProduct(Long productId, AddEventProductRequest request) {
        Product product = findByIdOrElseThrow(productId);

        if (product.getStock() < request.getEventStock()) {
            throw new CustomException(
                ErrorCode.INVALID_PRODUCT_ATTRIBUTE, "이벤트 상품 수량을 재고보다 더 많이 설정할 수 없습니다");
        }
        if (product.getPrice() < request.getEventPrice()) {
            throw new CustomException(
                ErrorCode.INVALID_PRODUCT_ATTRIBUTE, "이벤트 상품 가격이 기존 상품 가격보다 비쌀 수 없습니다");
        }

        redisProductService.addEventProduct(product, request);
    }

    // 이벤트 상품 조회
    public List<EventProduct> getEventProducts() {
        return redisProductService.getEventProducts();
    }

    // 카트 안 상품
    public Map<Long, Product> getProductMap(OrderRequestDto dto) {
        List<Long> productIds = new ArrayList<>();

        // productId 값만 찾기
        for (CartCreateRequest item : dto.getItems()) {
            productIds.add(item.getProductId());
        }

        // productId 값으로 product 찾기 (내부적으로 존재하는 데이터만 반환) → (없는 값에 대해 예외가 따로 발생하지 않음)
        List<Product> products = productRepository.findAllByIds(productIds);

        if (products.size() != productIds.size()) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : products) {
            productMap.put(product.getId(), product);
        }

        return productMap;
    }

    // 주문시 재고 차감
    public void decreaseQuantity(OrderRequestDto dto) {
        for (CartCreateRequest itemDto : dto.getItems()) {

            Long productId = itemDto.getProductId();
            Integer quantity = itemDto.getQuantity();

            int updateRows = productRepository.decreaseStock(productId, quantity);

            if (updateRows == 0) {
                throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        }
    }

    // 주문 취소 시 재고 추가
    public void increaseQuantity(List<OrderCancelDto> items) {
        for (OrderCancelDto item : items) {

            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            productRepository.increaseStock(productId, quantity);
        }
    }

    @Transactional
    public List<Product> decreaseStock(Map<Long, Integer> productMap) {
        List<Product> products = getAllProductsByIds(new ArrayList<>(productMap.keySet()));

        for (Product product : products) {
            Integer quantity = productMap.get(product.getId());
            if (product.getStock() < quantity) {
                throw new CustomException(ErrorCode.STOCK_NOT_FOUND);
            }
            product.decreaseStock(quantity);
            productRepository.save(product);
        }
        return products;
    }

    //상품 예외처리 분리
    public Product findByIdOrElseThrow(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    //상품 목록 가져오기
    private List<Product> getAllProductsByIds(List<Long> productIds) {
        return productRepository.findAllByIds(productIds);
    }
}
