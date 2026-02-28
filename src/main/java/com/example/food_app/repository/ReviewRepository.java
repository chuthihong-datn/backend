package com.example.food_app.repository;

import com.example.food_app.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    List<Review> findByMenu_MenuId(BigInteger menuId);
    List<Review> findByMenu_MenuIdIn(List<BigInteger> menuIds);
}
