package com.example.food_app.repository;

import com.example.food_app.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, BigInteger> {
    @Query("""
    SELECT t FROM Topping t
    JOIN t.menus m
    WHERE m.menuId = :menuId
    AND t.outOfStock = false
""")
    List<Topping> findAvailableToppingsByMenuId(BigInteger menuId);
}
