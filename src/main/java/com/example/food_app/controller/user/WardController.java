package com.example.food_app.controller.user;
import com.example.food_app.dto.response.user.WardResponse;
import com.example.food_app.service.user.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @GetMapping("delivery")
    public ResponseEntity<List<WardResponse>> getAllWardDelivery() {
        List<WardResponse> wards = wardService.getAllWardDelivery();
        return ResponseEntity.ok(wards);
    }
}