package com.example.food_app.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayMenuStatisticResponse {
    private Long menuId;
    private String menuName;
    private Long totalQuantity;
}
