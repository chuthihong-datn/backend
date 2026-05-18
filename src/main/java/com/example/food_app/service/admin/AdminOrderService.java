package com.example.food_app.service.admin;

import com.example.food_app.dto.request.admin.UpdateOrderRequest;
import com.example.food_app.dto.response.admin.OrderResponse;
import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import com.example.food_app.entity.Topping;
import com.example.food_app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;

    public List<OrderResponse> getAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getDetail(BigInteger orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"
                ));

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(
            BigInteger orderId,
            UpdateOrderRequest request
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"
                ));

        if (request.getOrderStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái không hợp lệ"
            );
        }

        // update order status
        order.setOrderStatus(request.getOrderStatus());
        if (request.getOrderStatus().name().equals("COMPLETED")) {

            // chỉ set nếu chưa phải PAID
            if (order.getPaymentStatus() == null ||
                    !order.getPaymentStatus().name().equals("PAID")) {

                order.setPaymentStatus(
                        com.example.food_app.entity.enums.PaymentStatus.PAID
                );
            }
        }
        orderRepository.save(order);
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderResponse.OrderItemResponse> items =
                order.getOrderDetails().stream()
                        .map(this::mapItem)
                        .toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(order.getAccount().getFullName())
                .phone(order.getAccount().getPhone())
                .address(order.getAddressDetail())
                .wardName(order.getWard().getName())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderResponse.OrderItemResponse mapItem(OrderDetail detail) {

        String sizeName = detail.getMenuSize() != null
                ? detail.getMenuSize().getSizeName()
                : null;

        List<String> toppings = detail.getToppings()
                .stream()
                .map(Topping::getName)
                .toList();

        return OrderResponse.OrderItemResponse.builder()
                .menuName(detail.getMenu().getName())
                .sizeName(sizeName)
                .toppings(toppings)
                .quantity(detail.getQuantity())
                .itemTotal(detail.getItemTotalPrice())
                .build();
    }
}