package com.example.food_app.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayMenuStatisticResponse {
    private BigInteger menuId;
    private String menuName;
    private Long totalQuantity;
}
