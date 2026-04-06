package com.example.food_app.dto.request.admin;

import com.example.food_app.entity.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    private OrderStatus orderStatus;
}