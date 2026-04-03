package com.example.food_app.repository;

import com.example.food_app.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, BigInteger> {
    List<Menu> findAllByIsActiveIsTrue();
    List<Menu> findByCategory_CategoryIdAndIsActiveTrue(BigInteger categoryId);
    Optional<Menu> findByMenuIdAndIsActiveTrue(BigInteger id);
    List<Menu> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);
    List<Menu> findAllByMenuIdInAndIsActiveTrue(List<BigInteger> menuIds);
    @Modifying
    @Query("UPDATE Menu m SET m.isActive = :isActive WHERE m.category.categoryId = :categoryId")
    void updateIsActiveByCategoryId(BigInteger categoryId, Boolean isActive);
}
