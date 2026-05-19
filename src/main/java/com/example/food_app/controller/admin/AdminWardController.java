package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.WardRequest;
import com.example.food_app.service.admin.AdminWardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/wards")
@RequiredArgsConstructor
public class AdminWardController {

    private final AdminWardService wardService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(wardService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String keyword) {
        return ResponseEntity.ok(wardService.search(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody WardRequest request
    ) {
        return ResponseEntity.ok(wardService.update(id, request));
    }
}