package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.OrderRequest;
import com.example.food_app.entity.Account;
import com.example.food_app.service.user.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    @PostMapping
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal Account account,
            @RequestBody OrderRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        Object response = orderService.createOrder(request, account, ip);
        return ResponseEntity.ok(response);
    }
}