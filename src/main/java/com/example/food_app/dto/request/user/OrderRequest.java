package com.example.food_app.dto.request.user;

import com.example.food_app.entity.enums.PaymentMethod;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private PaymentMethod paymentMethod;
    private String fullName;
    private String phone;
    private Long wardId;
    private String addressDetail;
    private String note;
    private String voucherCode;
}
