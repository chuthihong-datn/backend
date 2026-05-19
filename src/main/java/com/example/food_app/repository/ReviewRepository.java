package com.example.food_app.repository;

import com.example.food_app.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMenu_MenuId(Long menuId);
    List<Review> findByMenu_MenuIdIn(List<Long> menuIds);
    boolean existsByAccountAndOrderAndMenu(
            Account account,
            Order order,
            Menu menu
    );
    List<Review> findByMenu_MenuIdAndRating(Long menuId, Float rating);
    List<Review> findByRating(Float rating);
    long countByAccountAndOrder(
            Account account,
            Order order
    );
}
