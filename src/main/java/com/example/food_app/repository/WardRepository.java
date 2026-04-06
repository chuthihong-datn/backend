package com.example.food_app.repository;

import com.example.food_app.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface WardRepository extends JpaRepository<Ward, BigInteger> {
    List<Ward> findByIsDeliveryTrue();
    List<Ward> findByNameContainingIgnoreCase(String name);
    boolean existsByWardCode(String wardCode);
}
