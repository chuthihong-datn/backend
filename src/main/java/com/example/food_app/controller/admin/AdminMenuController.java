package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.MenuRequest;
import com.example.food_app.service.admin.AdminMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService menuService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(menuService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getDetail(id));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> create(
            @RequestPart("data") MenuRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.ok(menuService.create(request, files));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("data") MenuRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.ok(menuService.update(id, request, files));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }
}
