package com.example.shopping.domain.product.controller;

import com.example.shopping.domain.product.dto.response.ProductResponse;
import com.example.shopping.global.common.dto.ApiResponse;
import com.example.shopping.domain.product.dto.request.AddEventProductRequest;
import com.example.shopping.domain.product.dto.request.ProductPatchRequest;
import com.example.shopping.domain.product.dto.request.ProductRequest;
import com.example.shopping.domain.product.dto.response.EventProduct;
import com.example.shopping.domain.product.dto.response.ProductRanking;
import com.example.shopping.domain.product.dto.response.ProductInfoResponse;
import com.example.shopping.domain.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    //상품 생성
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> creatProduct(@Valid @RequestBody ProductRequest request) {

        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("상품 등록", response));
    }

    //상품 단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductInfoResponse>> getProduct(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long productId
    ) {
        ProductInfoResponse response = productService.getProduct(userId, productId);
        return ResponseEntity.status(HttpStatus.OK)
            .body((ApiResponse.success("상품 조회 ", response)));
    }

    //상품 목록 조회
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<ProductInfoResponse>>> getAllProductsPaged(
        Pageable pageable
    ) {
        Page<ProductInfoResponse> response = productService.getAllProducts(pageable);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("상품 목록 조회", response));
    }

    //상품 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductInfoResponse>> patchProduct(
        @PathVariable Long productId,
        @Valid @RequestBody ProductPatchRequest request) {
        ProductInfoResponse response = productService.updateProduct(productId, request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("상품 수정", response));
    }

    //상품 삭제 → 이벤트 생성 필요 (상품 삭제 시 타 도메인에서 자동 삭제 되도록)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("상품 삭제", null));
    }

    //상품 랭킹 TOP 10
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<ProductRanking>>> getProductRanking() {

        List<ProductRanking> response = productService.getProductRanking();

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("상품 랭킹 TOP 10", response));
    }

    // 일일 상품 랭킹 TOP 10
    @GetMapping("/daily-ranking")
    public ResponseEntity<ApiResponse<List<ProductRanking>>> getDailyProductRanking() {

        List<ProductRanking> response = productService.getRedisProductRanking();

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("일일 상품 랭킹 TOP 10", response));
    }

    // 일일 조회수 정산 (Redis → DB)
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> syncTest() {
        productService.syncTest();
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success(" 조회 수 정산", null));
    }

    // 이벤트 상품 추가
    @PostMapping("/event/{productId}")
    public ResponseEntity<ApiResponse<Void>> addEventProduct(
        @PathVariable Long productId,
        @Valid @RequestBody AddEventProductRequest request
    ) {
        productService.addEventProduct(productId, request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("이벤트 상품 생성", null));
    }

    // 이벤트 상품 조회
    @GetMapping("/event")
    public ResponseEntity<ApiResponse<List<EventProduct>>> getEventProducts() {

        List<EventProduct> response = productService.getEventProducts();
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("이벤트 상품 조회", response));
    }
}
