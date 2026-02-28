package com.example.food_app.repository;

import com.example.food_app.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface DistrictRepository extends JpaRepository<District, BigInteger> {
}
