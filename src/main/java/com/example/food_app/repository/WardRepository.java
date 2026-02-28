package com.example.food_app.repository;

import com.example.food_app.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface WardRepository extends JpaRepository<Ward, BigInteger> {
}
