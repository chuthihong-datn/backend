package com.example.food_app.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToppingResponse {
    private BigInteger toppingId;
    private String name;
    private BigDecimal price;
    private Boolean isActive;
    private Boolean outOfStock;
}
