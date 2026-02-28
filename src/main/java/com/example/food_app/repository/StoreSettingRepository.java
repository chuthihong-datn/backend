package com.example.food_app.repository;

import com.example.food_app.entity.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface StoreSettingRepository extends JpaRepository<StoreSetting, BigInteger> {
}
