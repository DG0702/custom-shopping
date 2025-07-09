package com.example.shopping.domain.product.controller;

import com.example.shopping.domain.product.dto.request.ProductRequestDto;
import com.example.shopping.domain.product.dto.response.ProductReadByIdDto;
import com.example.shopping.domain.product.dto.response.ProductResponseDto;
import com.example.shopping.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto request) {

        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping("/productId")
    public ResponseEntity<ProductReadByIdDto> readProduct(@PathVariable Long productId) {

        return ResponseEntity.ok(productService.readProductById(productId));
    }

    @PutMapping("/productId")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductRequestDto request
    ) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/productId")
    public ResponseEntity<ProductResponseDto> deleteProduct(@PathVariable Long productId) {

        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
