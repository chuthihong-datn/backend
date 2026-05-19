package com.example.food_app.dto.request.admin;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuRequest {
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer amount;

    private List<String> images;

    private List<Long> toppingIds;

    private List<MenuSizeRequest> sizes;

    @Getter
    @Setter
    public static class MenuSizeRequest {
        private String sizeName;
        private BigDecimal extraPrice;
    }
}
