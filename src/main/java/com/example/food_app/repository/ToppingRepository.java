package com.example.food_app.repository;

import com.example.food_app.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Long> {
    @Query("""
        SELECT t FROM Topping t
        JOIN t.menus m
        WHERE m.menuId = :menuId
        AND t.outOfStock = false
        AND t.isActive = true
    """)
    List<Topping> findAvailableToppingsByMenuId(Long menuId);
    List<Topping> findByNameContainingIgnoreCase(String keyword);
}
