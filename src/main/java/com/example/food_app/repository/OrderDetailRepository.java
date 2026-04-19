package com.example.food_app.repository;

import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, BigInteger> {
    @Query("""
                SELECT od.menu.menuId, SUM(od.quantity)
                FROM OrderDetail od
                WHERE od.order.orderStatus = com.example.food_app.entity.enums.OrderStatus.COMPLETED
                GROUP BY od.menu.menuId
            """)
    List<Object[]> findTotalSoldByMenuCompleted();

    List<OrderDetail> findByOrder(Order order);

    @Query("""
        SELECT od.menu.menuId, od.menu.name, SUM(od.quantity)
        FROM OrderDetail od
        WHERE od.order.orderStatus = 'COMPLETED'
          AND od.order.paymentStatus = 'PAID'
          AND od.order.createdAt BETWEEN :start AND :end
        GROUP BY od.menu.menuId, od.menu.name
        ORDER BY SUM(od.quantity) DESC
    """)
    List<Object[]> getTodaySoldMenu(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT od.menu.menuId, od.menu.name, SUM(od.quantity)
        FROM OrderDetail od
        WHERE od.order.orderStatus = 'COMPLETED'
          AND od.order.paymentStatus = 'PAID'
          AND od.order.createdAt BETWEEN :start AND :end
        GROUP BY od.menu.menuId, od.menu.name
        ORDER BY SUM(od.quantity) DESC
    """)
    List<Object[]> getSoldMenuByRange(LocalDateTime start, LocalDateTime end);
}
