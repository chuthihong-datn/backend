package com.example.food_app.repository;

import com.example.food_app.entity.MenuSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface MenuSizeRepository extends JpaRepository<MenuSize, BigInteger> {
}
