package com.example.shopping.domain.cart.service;

import com.example.shopping.domain.cart.dto.cartRequest.CartCreateRequest;
import com.example.shopping.domain.cart.dto.cartResponse.CartResponse;
import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.global.common.exception.CustomException;
import com.example.shopping.global.common.exception.ErrorCode;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.service.ProductService;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.service.UserQueryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserQueryService userQueryService;
    private final ProductService productService;

    @Transactional
    public void create(CartCreateRequest request, Long userId) {
        User user = userQueryService.findByIdOrElseThrow(userId);
        Product product = productService.findByIdOrElseThrow(request.getProductId());

        CartItem cartItem = CartItem.createCartItem(user, product, request.getQuantity());
        cartRepository.save(cartItem);
    }

    @Transactional
    public Page<CartResponse> getAllCartItems(Long userId, Pageable pageable) {
        userQueryService.findByIdOrElseThrow(userId);
        return cartRepository.getCartPage(userId, pageable);

    }

    @Transactional
    public void delete(Long userId, Long cartId) {
        CartItem cartItem = cartRepository.findById(cartId)
            .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        cartRepository.delete(cartItem);
    }
}
