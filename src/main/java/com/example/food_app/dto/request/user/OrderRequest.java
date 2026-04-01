package com.example.food_app.dto.request.user;

import com.example.food_app.entity.enums.PaymentMethod;
import lombok.*;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private PaymentMethod paymentMethod;
    private String fullName;
    private String phone;
    private BigInteger wardId;
    private String addressDetail;
    private String note;
    private String voucherCode;
}
