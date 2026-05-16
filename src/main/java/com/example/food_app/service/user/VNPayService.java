package com.example.food_app.service.user;

import com.example.food_app.config.VNPayConfig;
import com.example.food_app.entity.Order;
import com.example.food_app.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VNPayService {
    private final VNPayConfig config;

    public String createPaymentUrl(Order order, String ip) {

        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", config.getTmnCode());

        long amount = order.getFinalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderId().toString());
        params.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getOrderId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_IpAddr", ip);

        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        params.put("vnp_CreateDate", createDate);

        String query = VNPayUtil.buildQuery(params);
        String hash = VNPayUtil.hmacSHA512(config.getHashSecret(), query);

        return config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + hash;
    }
}