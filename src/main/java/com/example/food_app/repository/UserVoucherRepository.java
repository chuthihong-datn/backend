package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.UserVoucher;
import com.example.food_app.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    Optional<UserVoucher> findByAccountAndVoucher(Account account, Voucher voucher);
    @Query("""
        SELECT uv FROM UserVoucher uv
        JOIN uv.voucher v
        WHERE uv.account = :account
        AND uv.isUsed = false
        AND v.isActive = true
    """)
    List<UserVoucher> findValidVouchers(@Param("account") Account account);
    long countByVoucher(Voucher voucher);
    boolean existsByAccountAndVoucher(Account account, Voucher voucher);
}
