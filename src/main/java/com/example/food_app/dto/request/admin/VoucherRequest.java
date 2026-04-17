package com.example.food_app.dto.request.admin;

import com.example.food_app.entity.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherRequest {
    private String code;
    private String title;
    private String description;

    private DiscountType discountType;
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer usageLimit;
    private Boolean isActive;
}
