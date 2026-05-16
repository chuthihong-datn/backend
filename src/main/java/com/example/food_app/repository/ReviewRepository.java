package com.example.food_app.repository;

import com.example.food_app.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    List<Review> findByMenu_MenuId(BigInteger menuId);
    List<Review> findByMenu_MenuIdIn(List<BigInteger> menuIds);
    boolean existsByAccountAndOrderAndMenu(
            Account account,
            Order order,
            Menu menu
    );
    List<Review> findByMenu_MenuIdAndRating(BigInteger menuId, Float rating);
    List<Review> findByRating(Float rating);
    long countByAccountAndOrder(
            Account account,
            Order order
    );
}
