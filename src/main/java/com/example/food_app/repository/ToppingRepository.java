package com.example.food_app.repository;

import com.example.food_app.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, BigInteger> {
}
