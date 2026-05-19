package com.example.food_app.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToppingRequest {
    private Long toppingId;
    private String name;
    private BigDecimal price;
    private Boolean isActive;
    private Boolean outOfStock;
}
