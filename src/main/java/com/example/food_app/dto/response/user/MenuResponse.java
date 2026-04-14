package com.example.food_app.dto.response.user;

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
public class MenuResponse {
    private BigInteger id;
    private String name;
    private List<String> images;
    private float rating;
    private BigDecimal minPrice;
    private Integer amount;
    private boolean outOfStock;
    private boolean isFlashSale;
    private BigDecimal discountedPrice;
    private Integer discountPercent;
    private LocalDateTime flashSaleEndTime;
    private Long totalSold;
}
