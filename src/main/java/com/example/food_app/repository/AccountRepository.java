package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, BigInteger> {
    boolean existsByEmail(String email);
    Optional<Account> findByEmailAndIsActiveIsTrue(String email);
    Optional<Account> findByEmailAndRoleAndIsActiveIsTrue(String email, Role role);
    Optional<Account> findByEmail(String email);
}
