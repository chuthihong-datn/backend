package com.example.food_app.dto.response.admin;

import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentStatus;
import com.example.food_app.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private BigInteger orderId;
    private String customerName;
    private String phone;
    private String address;
    private String wardName;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String menuName;
        private String sizeName;
        private List<String> toppings;
        private Integer quantity;
        private BigDecimal itemTotal;
    }
}