package com.example.food_app.repository;

import com.example.food_app.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, BigInteger> {
}
