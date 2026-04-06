package com.example.food_app.dto.response.admin;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuResponse {
    private BigInteger menuId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer amount;
    private Boolean isActive;

    private List<String> images;

    private String categoryName;

    private List<String> toppings;

    private List<SizeResponse> sizes;

    @Getter
    @Builder
    public static class SizeResponse {
        private String sizeName;
        private BigDecimal extraPrice;
    }

    private boolean outOfStock;

    private boolean isDeleted;
}
