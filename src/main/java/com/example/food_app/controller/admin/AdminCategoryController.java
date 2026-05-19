package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.CategoryRequest;
import com.example.food_app.dto.response.admin.CategoryResponse;
import com.example.food_app.service.admin.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(adminCategoryService.getAll());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CategoryResponse> create(
            @RequestPart("data") CategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(adminCategoryService.create(request, file));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @RequestPart("data") CategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(adminCategoryService.update(id, request, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        adminCategoryService.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> search(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(adminCategoryService.search(keyword));
    }
}