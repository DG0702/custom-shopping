package com.example.shopping.domain.product.controller;

import com.example.shopping.domain.product.dto.request.ProductPatchRequestDto;
import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.ReadProductDto;
import com.example.shopping.domain.product.dto.response.ProductResponseDto;
import com.example.shopping.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid@RequestBody ProductRequestDto request) {

        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ReadProductDto> readProduct(@PathVariable Long productId) {

        return ResponseEntity.ok(productService.readProductById(productId));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> patchProduct (
            @PathVariable Long productId,
            @Valid @RequestBody ProductPatchRequestDto request
            ) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> deleteProduct(@PathVariable Long productId) {

        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
