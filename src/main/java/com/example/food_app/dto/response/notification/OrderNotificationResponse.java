package com.example.food_app.dto.response.notification;

import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentMethod;
import com.example.food_app.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderNotificationResponse {
    private String type;
    private Long orderId;
    private Long accountId;
    private String customerName;
    private BigDecimal finalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private OrderStatus previousOrderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime eventTime;
    private String title;
    private String message;
}
