package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.FlashSaleRequest;
import com.example.food_app.dto.request.admin.VoucherRequest;
import com.example.food_app.dto.response.admin.FlashSaleResponse;
import com.example.food_app.dto.response.admin.VoucherResponse;
import com.example.food_app.service.admin.FlashSaleService;
import com.example.food_app.service.admin.AdminVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final AdminVoucherService voucherService;
    private final FlashSaleService flashSaleService;

    @PostMapping("/voucher")
    public VoucherResponse create(@RequestBody VoucherRequest request) {
        return voucherService.create(request);
    }

    @PutMapping("/voucher/{id}")
    public VoucherResponse update(
            @PathVariable BigInteger id,
            @RequestBody VoucherRequest request
    ) {
        return voucherService.update(id, request);
    }

    @DeleteMapping("/voucher/{id}")
    public void delete(@PathVariable BigInteger id) {
        voucherService.delete(id);
    }

    @GetMapping("/voucher")
    public List<VoucherResponse> getAll() {
        return voucherService.getAll();
    }

    @PostMapping("/flash-sale")
    public FlashSaleResponse create1(@RequestBody FlashSaleRequest request) {
        return flashSaleService.create(request);
    }

    @PutMapping("/flash-sale/{id}")
    public FlashSaleResponse update1(
            @PathVariable BigInteger id,
            @RequestBody FlashSaleRequest request
    ) {
        return flashSaleService.update(id, request);
    }

    @DeleteMapping("/flash-sale/{id}")
    public void delete1(@PathVariable BigInteger id) {
        flashSaleService.delete(id);
    }

    @GetMapping("/flash-sale")
    public List<FlashSaleResponse> getAll1() {
        return flashSaleService.getAll();
    }
}
