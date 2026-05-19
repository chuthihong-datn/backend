package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.ProfileUpdateRequest;
import com.example.food_app.dto.response.user.OrderByUserResponse;
import com.example.food_app.dto.response.user.ProfileResponse;
import com.example.food_app.dto.response.user.VoucherResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(userService.getProfile(account));
    }

    @PutMapping(value = "/profile", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal Account account,
            @RequestPart(required = false) ProfileUpdateRequest request,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(
                userService.updateProfile(account, request, file)
        );
    }

    @GetMapping("/order")
    public List<OrderByUserResponse> getMyOrders(
            @AuthenticationPrincipal Account account
    ) {
        return userService.getMyOrders(account);
    }

    @GetMapping("/order/{id}")
    public OrderByUserResponse getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Account account
    ) {
        return userService.getMyOrderDetail(id, account);
    }

    @GetMapping("/vouchers")
    public List<VoucherResponse> getMyVouchers(
            @AuthenticationPrincipal Account account
    ) {
        return userService.getMyVouchers(account);
    }
}