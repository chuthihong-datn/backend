package com.example.food_app.repository;

import com.example.food_app.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByIsActiveIsTrue();
    List<Menu> findByCategory_CategoryIdAndIsActiveTrue(Long categoryId);
    Optional<Menu> findByMenuIdAndIsActiveTrue(Long id);
    List<Menu> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);
    @Modifying
    @Query("UPDATE Menu m SET m.isActive = :isActive WHERE m.category.categoryId = :categoryId")
    void updateIsActiveByCategoryId(Long categoryId, Boolean isActive);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndMenuIdNot(String name, Long menuId);
}
