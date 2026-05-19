package com.example.food_app.repository;

import com.example.food_app.entity.MenuSize;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MenuSizeRepository extends JpaRepository<MenuSize, Long> {
}