package com.example.shopping.domain.cart.service;

import com.example.shopping.domain.cart.dto.cartRequest.CartCreateRequest;
import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.service.ProductService;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.service.UserQueryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserQueryService userQueryService;

    @Test
    void 장바구니_아이템_저장_성공_테스트() {
        // given
        Long userId = 1L;
        Long productId = 10L;
        int quantity = 3;

        CartCreateRequest requestDto = new CartCreateRequest();
        ReflectionTestUtils.setField(requestDto, "productId", productId);
        ReflectionTestUtils.setField(requestDto, "quantity", quantity);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", productId);
        CartItem cartItem = CartItem.createCartItem(user, product, quantity);

        given(userQueryService.findByIdOrElseThrow(userId)).willReturn(user);
        given(productService.findByIdOrElseThrow(productId)).willReturn(product);
        given(cartRepository.save(any(CartItem.class))).willReturn(cartItem);

        // when
        cartService.create(requestDto, userId);

        // then
        verify(userQueryService).findByIdOrElseThrow(userId);
        verify(productService).findByIdOrElseThrow(productId);
        verify(cartRepository).save(any(CartItem.class));
    }

}