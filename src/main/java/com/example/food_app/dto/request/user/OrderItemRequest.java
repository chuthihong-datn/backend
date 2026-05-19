package com.example.food_app.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    private Long menuId;
    private Long menuSizeId;
    private List<Long> toppingIds;
    private Integer quantity;
}
