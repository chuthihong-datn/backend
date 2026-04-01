package com.example.food_app.repository;

import com.example.food_app.entity.Cart;
import com.example.food_app.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, BigInteger> {
    void deleteByCart(Cart cart);
    List<CartItem> findByCart(Cart cart);
}
