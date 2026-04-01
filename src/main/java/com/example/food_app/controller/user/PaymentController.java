package com.example.food_app.controller.user;

import com.example.food_app.config.VNPayConfig;
import com.example.food_app.entity.Cart;
import com.example.food_app.entity.Menu;
import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentStatus;
import com.example.food_app.repository.*;
import com.example.food_app.util.VNPayUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final VNPayConfig config;
    private final MenuRepository menuRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {

        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signData = VNPayUtil.buildQuery(params);
        String checkHash = VNPayUtil.hmacSHA512(config.getHashSecret(), signData);

        if (!checkHash.equals(secureHash)) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        Order order = orderRepository.findById(new BigInteger(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.ok("Order already processed");
        }

        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
        if (order.getFinalAmount().longValue() != vnpAmount) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        if ("00".equals(responseCode)) {

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);

            List<OrderDetail> details = orderDetailRepository.findByOrder(order);

            for (OrderDetail detail : details) {
                Menu menu = detail.getMenu();
                int quantity = detail.getQuantity();

                if (menu.getAmount() < quantity) {
                    throw new RuntimeException("Hết hàng khi thanh toán");
                }

                menu.setAmount(menu.getAmount() - quantity);
                menuRepository.save(menu);
            }

            Cart cart = cartRepository.findByAccount(order.getAccount())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            cartItemRepository.deleteByCart(cart);

        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);

        return ResponseEntity.ok("Payment processed");
    }
}