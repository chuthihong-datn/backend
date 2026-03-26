package com.example.food_app.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private BigInteger cartItemId;
    private BigInteger menuId;
    private String image;
    private String menuName;
    private String sizeName;
    private List<String> toppings;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal itemTotal;
}
