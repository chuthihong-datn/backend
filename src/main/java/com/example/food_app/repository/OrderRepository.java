package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, BigInteger> {
    List<Order> findByAccountOrderByCreatedAtDesc(Account account);
    Optional<Order> findByOrderIdAndAccount(BigInteger orderId, Account account);
    // doanh thu theo ngày
    @Query("""
        SELECT DATE(o.createdAt), SUM(o.finalAmount), COUNT(o)
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.paymentStatus = 'PAID'
        GROUP BY DATE(o.createdAt)
        ORDER BY DATE(o.createdAt)
    """)
    List<Object[]> getRevenueByDay();

    // doanh thu theo giờ
    @Query("""
        SELECT FUNCTION('HOUR', o.createdAt), SUM(o.finalAmount), COUNT(o)
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.paymentStatus = 'PAID'
        GROUP BY FUNCTION('HOUR', o.createdAt)
        ORDER BY FUNCTION('HOUR', o.createdAt)
    """)
    List<Object[]> getRevenueByHour();

    // doanh thu theo tháng
    @Query("""
        SELECT FUNCTION('YEAR', o.createdAt),
               FUNCTION('MONTH', o.createdAt),
               SUM(o.finalAmount),
               COUNT(o)
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.paymentStatus = 'PAID'
        GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)
        ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)
    """)
    List<Object[]> getRevenueByMonth();
}
