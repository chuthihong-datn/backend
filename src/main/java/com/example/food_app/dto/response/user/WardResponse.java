package com.example.food_app.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WardResponse {
    private Long wardId;
    private String name;
    private boolean isDelivery;
    private Integer shippingFee;
}
