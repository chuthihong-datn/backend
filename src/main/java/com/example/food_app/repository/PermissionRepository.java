package com.example.food_app.repository;

import com.example.food_app.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface PermissionRepository extends JpaRepository<Permission, BigInteger> {
}
