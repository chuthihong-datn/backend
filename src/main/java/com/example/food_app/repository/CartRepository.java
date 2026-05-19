package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByAccount(Account account);
}
