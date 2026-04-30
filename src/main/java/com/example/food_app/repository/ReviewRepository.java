package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.Menu;
import com.example.food_app.entity.OrderDetail;
import com.example.food_app.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, BigInteger> {
    List<Review> findByMenu_MenuId(BigInteger menuId);
    List<Review> findByMenu_MenuIdIn(List<BigInteger> menuIds);
    boolean existsByAccountAndOrderDetail(Account account, OrderDetail orderDetail);
    List<Review> findByMenu(Menu menu);
    List<Review> findByIsDeletedFalse();
    List<Review> findByMenu_MenuIdAndIsDeletedFalse(BigInteger menuId);
    List<Review> findByMenu_MenuIdAndRating(BigInteger menuId, Float rating);
    List<Review> findByRating(Float rating);
}
