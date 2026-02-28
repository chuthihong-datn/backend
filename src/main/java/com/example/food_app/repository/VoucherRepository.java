package com.example.food_app.repository;

import com.example.food_app.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface VoucherRepository extends JpaRepository<Voucher, BigInteger> {
}
