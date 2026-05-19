package com.example.food_app.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WardResponse {
    private Long wardId;
    private String wardCode;
    private String name;
    private Integer shippingFee;
    private Boolean isDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
