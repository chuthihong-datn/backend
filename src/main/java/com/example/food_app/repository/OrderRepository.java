package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, BigInteger> {
    List<Order> findByAccountOrderByCreatedAtDesc(Account account);
    Optional<Order> findByOrderIdAndAccount(BigInteger orderId, Account account);
}
