package com.example.shopping.domain.product.controller;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.dto.PageResponseDto;
import com.example.shopping.domain.common.dto.ResponseDto;
import com.example.shopping.domain.product.dto.request.AddEventProductRequestDto;
import com.example.shopping.domain.product.dto.request.ProductPatchRequestDto;
import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.EventProductDto;
import com.example.shopping.domain.product.dto.response.ProductRankingDto;
import com.example.shopping.domain.product.dto.response.ReadProductDto;
import com.example.shopping.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    //상품생성
    @PostMapping
    public ResponseEntity<ResponseDto<Void>> creatProduct(@Valid @RequestBody ProductRequestDto request) {
        productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품이 등록되었습니다.", null));
    }

    //상품 단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ResponseDto<ReadProductDto>> readProduct(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long productId
    ) {
        ReadProductDto result = productService.readProductById(user, productId);
        return ResponseEntity.status(HttpStatus.OK).body((new ResponseDto<>("조회한 상품입니다.", result)));
    }

    //상품목록조회
    @GetMapping()
    public ResponseEntity<ResponseDto<PageResponseDto<ReadProductDto>>> getAllProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PageResponseDto<ReadProductDto> pagedResult = productService.getAllProductsPaged(page, size);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품목록 조회 성공",pagedResult));
    }

    //상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<ResponseDto<Void>> patchProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductPatchRequestDto request
    ) {
        productService.updateProduct(productId, request);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품이 수정되었습니다", null));
    }

    //상품삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ResponseDto<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품이 삭제되었습니다", null));
    }

    //상품랭킹
    @GetMapping("/ranking")
    public ResponseEntity<ResponseDto<List<ProductRankingDto>>> getProductRanking(
            @RequestParam(defaultValue = "10") Long size) {
        if (size <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품 랭킹 입니다", productService.getProductRanking(size)));
    }

    // 일일상품랭킹
    @GetMapping("/daily-ranking")
    public ResponseEntity<ResponseDto<List<ProductRankingDto>>> getDailyProductRanking(
            @RequestParam(defaultValue = "10") Long size) {
        if (size <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ResponseDto<List<ProductRankingDto>> response =
                new ResponseDto<>("일일 상품 랭킹 입니다", productService.getRedisProductRanking(size));
        return ResponseEntity.ok(response);
    }

    // 조회수 정산
    @PostMapping("/sync")
    public ResponseEntity<ResponseDto<Void>> syncTest() {
        productService.syncTest();
        ResponseDto<Void> response = new ResponseDto<>("조회수 싱크", null);
        return ResponseEntity.ok(response);
    }

    // 이벤트 상품 추가
    @PostMapping("/event/{productId}")
    public ResponseEntity<ResponseDto<Void>> addEventProduct(
            @PathVariable Long productId,
            @Valid @RequestBody AddEventProductRequestDto request
    ) {
        productService.addEventProduct(productId, request);
        ResponseDto<Void> response = new ResponseDto<>("이벤트 상품 생성", null);
        return ResponseEntity.ok(response);
    }

    // 이벤트 상품 조회
    @GetMapping("/event")
    public ResponseEntity<ResponseDto<List<EventProductDto>>> getEventProducts() {
        List<EventProductDto> responseData = productService.getEventProducts();
        ResponseDto<List<EventProductDto>> response =
                new ResponseDto<>("이벤트 상품 조회", responseData);
        return ResponseEntity.ok(response);
    }
}
