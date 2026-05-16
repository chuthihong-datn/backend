package com.example.food_app.dto.response.user;

import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentMethod;
import com.example.food_app.entity.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderByUserResponse {
    private BigInteger orderId;
    private String address;
    private String wardName;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private Boolean isReviewed;
    private List<OrderItemResponse> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemResponse {
        private BigInteger orderDetailId;
        private String menuName;
        private String sizeName;
        private List<String> toppings;
        private Integer quantity;
        private BigDecimal itemTotal;
    }
}