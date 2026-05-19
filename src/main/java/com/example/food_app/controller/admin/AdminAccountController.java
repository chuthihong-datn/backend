package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.AccountUpdateRequest;
import com.example.food_app.dto.response.admin.AccountResponse;
import com.example.food_app.service.admin.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll() {
        return ResponseEntity.ok(adminAccountService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminAccountService.getDetail(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AccountResponse> update(
            @PathVariable Long id,
            @RequestPart("data") AccountUpdateRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return ResponseEntity.ok(
                adminAccountService.update(id, request, avatar)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> disable(
            @PathVariable Long id
    ) {
        adminAccountService.disable(id);

        return ResponseEntity.ok("Vô hiệu hóa tài khoản thành công");
    }
}