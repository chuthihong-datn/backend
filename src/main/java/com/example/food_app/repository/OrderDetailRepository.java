package com.example.food_app.repository;

import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
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
}
