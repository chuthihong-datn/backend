package com.example.food_app.repository;

import com.example.food_app.entity.Order;
import com.example.food_app.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
    List<Voucher> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDateTime now1,
            LocalDateTime now2
    );
    List<Voucher> findAllByOrderByCreatedAtDesc();
}
