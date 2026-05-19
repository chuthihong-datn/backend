package com.example.food_app.service.notification;

import com.example.food_app.dto.response.notification.OrderNotificationResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.entity.Order;
import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
    private static final String ADMIN_ORDER_TOPIC = "/topic/admin/orders";
    private static final String CUSTOMER_ORDER_TOPIC_PREFIX = "/topic/customers/";

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyAdminNewOrder(Order order) {
        OrderNotificationResponse payload = buildPayload(
                "NEW_ORDER",
                order,
                null,
                "Đơn hàng mới",
                "Đơn #" + order.getOrderId() + " mới từ " + getCustomerName(order) + "."
        );

        sendAfterCommit(ADMIN_ORDER_TOPIC, payload);
    }

    public void notifyAdminPaymentUpdated(Order order) {
        String title = order.getPaymentStatus() == PaymentStatus.PAID
                ? "Đơn hàng đã thanh toán"
                : "Thanh toán đơn hàng thay đổi";

        String message = order.getPaymentStatus() == PaymentStatus.PAID
                ? "Đơn #" + order.getOrderId() + " đã thanh toán thành công."
                : "Đơn #" + order.getOrderId() + " có trạng thái thanh toán "
                        + formatPaymentStatus(order.getPaymentStatus()) + ".";

        OrderNotificationResponse payload = buildPayload(
                "PAYMENT_UPDATED",
                order,
                null,
                title,
                message
        );

        sendAfterCommit(ADMIN_ORDER_TOPIC, payload);
    }

    public void notifyCustomerOrderStatusChanged(Order order, OrderStatus previousStatus) {
        if (order == null || order.getAccount() == null) {
            return;
        }

        if (Objects.equals(previousStatus, order.getOrderStatus())) {
            return;
        }

        OrderNotificationResponse payload = buildPayload(
                "ORDER_STATUS_CHANGED",
                order,
                previousStatus,
                "Trạng thái đơn hàng thay đổi",
                "Đơn #" + order.getOrderId() + " chuyển sang "
                        + formatOrderStatus(order.getOrderStatus()) + "."
        );

        sendAfterCommit(getCustomerOrderTopic(order.getAccount().getAccountId()), payload);
    }

    private OrderNotificationResponse buildPayload(
            String type,
            Order order,
            OrderStatus previousStatus,
            String title,
            String message
    ) {
        Account account = order.getAccount();

        return OrderNotificationResponse.builder()
                .type(type)
                .orderId(order.getOrderId())
                .accountId(account != null ? account.getAccountId() : null)
                .customerName(getCustomerName(order))
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .previousOrderStatus(previousStatus)
                .createdAt(order.getCreatedAt())
                .eventTime(LocalDateTime.now())
                .title(title)
                .message(message)
                .build();
    }

    private String getCustomerName(Order order) {
        Account account = order.getAccount();
        if (account == null || account.getFullName() == null || account.getFullName().isBlank()) {
            return "khách hàng";
        }

        return account.getFullName();
    }

    private String formatOrderStatus(OrderStatus status) {
        if (status == null) {
            return "trạng thái mới";
        }

        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case DELIVERING -> "Đang giao";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }

    private String formatPaymentStatus(PaymentStatus status) {
        if (status == null) {
            return "chưa xác định";
        }

        return switch (status) {
            case PENDING -> "Đang xử lý";
            case PAID -> "Đã thanh toán";
            case FAILED -> "Thanh toán thất bại";
        };
    }

    private String getCustomerOrderTopic(Long accountId) {
        return CUSTOMER_ORDER_TOPIC_PREFIX + accountId + "/orders";
    }

    private void sendAfterCommit(String destination, OrderNotificationResponse payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(destination, payload);
                }
            });
            return;
        }

        messagingTemplate.convertAndSend(destination, payload);
    }
}
