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
public class MenuDetailResponse {

    private BigInteger id;
    private String name;
    private String description;
    private List<String> images;

    private BigDecimal minPrice;
    private Integer prepareTime;
    private Integer amount;

    private List<MenuSizeResponse> sizes;
    private List<ToppingResponse> toppings;

    private Float rating;
    private Integer reviewCount;
    private List<ReviewResponse> reviews;
}
