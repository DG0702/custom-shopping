package com.example.shopping.domain.cart.service;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.service.ProductService;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.service.UserQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    private final UserQueryService userQueryService;
    private final ProductService productService;

    @Transactional
    public void create(CartCreateRequestDto request, Long userId) {
        User user = userQueryService.findByIdOrElseThrow(userId);
        Product product = productService.findByIdOrElseThrow(request.getProductId());

        CartItem cartItem = CartItem.createCartItem(user, product, request.getQuantity());
        cartRepository.save(cartItem);
    }
}
