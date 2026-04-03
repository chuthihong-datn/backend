package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.ToppingRequest;
import com.example.food_app.dto.response.admin.ToppingResponse;
import com.example.food_app.service.admin.AdminToppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/admin/toppings")
@RequiredArgsConstructor
public class AdminToppingController {

    private final AdminToppingService toppingService;

    @GetMapping
    public ResponseEntity<List<ToppingResponse>> getAll() {
        return ResponseEntity.ok(toppingService.getAll());
    }

    @PostMapping
    public ResponseEntity<ToppingResponse> create(@RequestBody ToppingRequest request) {
        return ResponseEntity.ok(toppingService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ToppingResponse> update(
            @PathVariable BigInteger id,
            @RequestBody ToppingRequest request
    ) {
        return ResponseEntity.ok(toppingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable BigInteger id) {
        toppingService.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ToppingResponse>> search(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(toppingService.search(keyword));
    }
}