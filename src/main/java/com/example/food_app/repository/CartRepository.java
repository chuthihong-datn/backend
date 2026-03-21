package com.example.food_app.repository;

import com.example.food_app.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface CartRepository extends JpaRepository<Cart, BigInteger> {

}
