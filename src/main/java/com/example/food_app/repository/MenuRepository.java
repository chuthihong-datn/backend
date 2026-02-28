package com.example.food_app.repository;

import com.example.food_app.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, BigInteger> {
    List<Menu> findAllByIsActiveIsTrue();
    List<Menu> findByCategory_CategoryIdAndIsActiveTrue(BigInteger categoryId);
    Optional<Menu> findByMenuIdAndIsActiveTrue(BigInteger id);
}
