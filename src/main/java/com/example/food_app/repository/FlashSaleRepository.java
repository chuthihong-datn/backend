package com.example.food_app.repository;

import com.example.food_app.entity.FlashSale;
import com.example.food_app.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface FlashSaleRepository extends JpaRepository<FlashSale, BigInteger> {
    List<FlashSale> findAllByOrderByCreatedAtDesc();
}