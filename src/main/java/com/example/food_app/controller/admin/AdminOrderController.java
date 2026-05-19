package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.UpdateOrderRequest;
import com.example.food_app.dto.response.admin.OrderResponse;
import com.example.food_app.service.admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public List<OrderResponse> getAll() {
        return adminOrderService.getAll();
    }

    @GetMapping("/{id}")
    public OrderResponse getDetail(@PathVariable Long id) {
        return adminOrderService.getDetail(id);
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderRequest request
    ) {
        return adminOrderService.updateStatus(id, request);
    }
}