package com.example.food_app.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartRequest {
    private BigInteger menuId;
    private BigInteger menuSizeId;
    private Integer quantity;
    private List<BigInteger> toppingIds;
}
