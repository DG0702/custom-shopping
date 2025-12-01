package com.example.shopping.domain.order.util;

import com.example.shopping.domain.cart.entity.CartItem;
import com.example.shopping.domain.cart.repository.CartRepository;
import com.example.shopping.domain.product.entity.Product;
import com.example.shopping.domain.product.repository.ProductRepository;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    public User createUser(String email, String password, String name, String address) {
        return userRepository.save(new User(email, password, name, address, UserRole.USER));
    }

    public Product createProduct(String name, String description, Integer price, Integer stock) {
        return productRepository.save(new Product(name, description, price, stock));
    }

    public List<CartItem> cartItems(User user, List<Product> products, int quantity) {
        List<CartItem> cartItems = new ArrayList<>();

        for (Product product : products) {
            CartItem cartItem = CartItem.createCartItem(user, product, quantity);
            cartRepository.save(cartItem);
            cartItems.add(cartItem);
        }

        return cartItems;
    }

}
