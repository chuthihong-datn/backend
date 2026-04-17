package com.example.food_app.controller.user;

import com.example.food_app.dto.response.user.Voucher1Response;
import com.example.food_app.entity.Account;
import com.example.food_app.service.user.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    public List<Voucher1Response> getAvailableVouchers() {
        return voucherService.getAvailableVouchers();
    }

    @PostMapping("/{voucherId}/save")
    public Voucher1Response saveVoucher(
            @PathVariable Long voucherId,
            @AuthenticationPrincipal Account account
    ) {
        return voucherService.saveVoucher(account, voucherId);
    }
}
