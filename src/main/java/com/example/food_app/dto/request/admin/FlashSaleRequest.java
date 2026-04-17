package com.example.food_app.dto.request.admin;

import com.example.food_app.entity.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlashSaleRequest {

    private String title;
    private String description;

    private DiscountType discountType;
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;

    private Integer maxQuantityPerOrder;
    private Integer minStock;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean isActive;

    private List<BigInteger> menuIds;
}