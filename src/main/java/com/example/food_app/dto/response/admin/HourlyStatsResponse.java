package com.example.food_app.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HourlyStatsResponse {
    private int hour;
    private BigDecimal revenue;
    private long orderCount;
}
