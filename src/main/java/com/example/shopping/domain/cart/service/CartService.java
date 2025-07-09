package com.example.shopping.domain.cart.service;

import com.example.shopping.domain.cart.dto.CartCreateRequestDto;
import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(CartCreateRequestDto request, Long userId) {
        //TODO : user service 에서 조회
        User user = new User();
        //TODO : product service 에서 조회
        Product product = new Product();
        CartItem cartItem = CartItem.createCartItem(user, product, request.getQuantity());
        cartRepository.save(cartItem);
    }
}
