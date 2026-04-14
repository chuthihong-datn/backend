package com.example.food_app.repository;

import com.example.food_app.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface FlashSaleRepository extends JpaRepository<FlashSale, BigInteger> {
}
