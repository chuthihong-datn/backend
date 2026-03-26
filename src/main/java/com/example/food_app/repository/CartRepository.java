package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, BigInteger> {
    Optional<Cart> findByAccount(Account account);
}
