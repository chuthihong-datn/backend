package com.example.food_app.controller.user;

import com.example.food_app.config.VNPayConfig;
import com.example.food_app.entity.Cart;
import com.example.food_app.entity.Menu;
import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentStatus;
import com.example.food_app.repository.*;
import com.example.food_app.service.notification.OrderNotificationService;
import com.example.food_app.util.VNPayUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    private final OrderNotificationService orderNotificationService;

    @GetMapping("/vnpay-return")
    @Transactional
    public void vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signData = VNPayUtil.buildQuery(params);
        String checkHash = VNPayUtil.hmacSHA512(config.getHashSecret(), signData);

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        Order order = orderRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            response.sendRedirect("http://localhost:3000/?payment-success=true&orderId=" + orderId);
            return;
        }

        OrderStatus previousOrderStatus = order.getOrderStatus();

        boolean isValid = checkHash.equals(secureHash);
        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;

        boolean isSuccess = isValid
                && "00".equals(responseCode)
                && order.getFinalAmount().longValue() == vnpAmount;

        if (isSuccess) {

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);

            List<OrderDetail> details = orderDetailRepository.findByOrder(order);

            for (OrderDetail detail : details) {
                Menu menu = detail.getMenu();

                if (menu.getAmount() < detail.getQuantity()) {
                    throw new RuntimeException("Hết hàng khi thanh toán");
                }

                menu.setAmount(menu.getAmount() - detail.getQuantity());
                menuRepository.save(menu);
            }

            Cart cart = cartRepository.findByAccount(order.getAccount())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            cartItemRepository.deleteByCart(cart);

            orderRepository.save(order);
            orderNotificationService.notifyAdminPaymentUpdated(order);
            orderNotificationService.notifyCustomerOrderStatusChanged(order, previousOrderStatus);

            response.sendRedirect("http://localhost:3000/?payment-success=true&orderId=" + orderId);

        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            orderNotificationService.notifyAdminPaymentUpdated(order);
            orderNotificationService.notifyCustomerOrderStatusChanged(order, previousOrderStatus);

            response.sendRedirect("http://localhost:3000/?payment-failed=true&orderId=" + orderId + "&code=" + (responseCode != null ? responseCode : "99"));
        }
    }
}
