package com.example.shopping.domain.product.controller;

import com.example.shopping.domain.common.dto.ResponseDto;
import com.example.shopping.domain.product.dto.request.ProductPatchRequestDto;
import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.ProductListResponseDto;
import com.example.shopping.domain.product.dto.response.ProductRankingDto;
import com.example.shopping.domain.product.dto.response.ReadProductDto;
import com.example.shopping.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> creatProduct(@Valid@RequestBody ProductRequestDto request) {
        productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품이 등록되었습니다.", null));
    }
    //상품 단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ResponseDto<ReadProductDto>> readProduct(@PathVariable Long productId) {
        ReadProductDto result = productService.readProductById(productId);
        return ResponseEntity.status(HttpStatus.OK).body((new ResponseDto<>("조회한 상품입니다.",result)));
    }
    //상품목록조회
    @GetMapping()
    public ResponseEntity<ResponseDto<ProductListResponseDto>> getAllProductsPaged (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        ProductListResponseDto response = productService.getAllProductsPaged(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품 목록 페이징 조회 성공", response));
    }
    //상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<ResponseDto<Void>> patchProduct (
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
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto<>("상품 랭킹 입니다",productService.getProductRanking(size)));
    }
}
