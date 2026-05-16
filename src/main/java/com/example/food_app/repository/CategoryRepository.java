package com.example.food_app.repository;

import com.example.food_app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, BigInteger> {
    List<Category> findAllByIsActiveIsTrue();
    Boolean existsByCategoryIdAndIsActiveIsTrue(BigInteger id);
    List<Category> findByNameContainingIgnoreCase(String keyword);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndCategoryIdNot(String name, BigInteger categoryId);
}