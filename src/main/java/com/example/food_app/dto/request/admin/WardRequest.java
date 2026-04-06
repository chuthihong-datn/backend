package com.example.food_app.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WardRequest {
    private Integer shippingFee;
    private Boolean isDelivery;
}
