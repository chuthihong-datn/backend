package com.example.food_app.service.user;

import com.example.food_app.dto.response.user.Voucher1Response;
import com.example.food_app.entity.Account;
import com.example.food_app.entity.UserVoucher;
import com.example.food_app.entity.Voucher;
import com.example.food_app.entity.enums.VoucherType;
import com.example.food_app.repository.UserVoucherRepository;
import com.example.food_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    // Lấy list voucher
    public List<Voucher1Response> getAvailableVouchers() {
        LocalDateTime now = LocalDateTime.now();

        List<Voucher> vouchers =
                voucherRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now);

        return vouchers.stream()
                .filter(v -> v.getVoucherType() == VoucherType.PUBLIC)
                .map(voucher -> {

                    long usedCount = userVoucherRepository.countByVoucher(voucher);

                    Voucher1Response res = new Voucher1Response();

                    res.setVoucherId(voucher.getVoucherId());
                    res.setCode(voucher.getCode());
                    res.setTitle(voucher.getTitle());
                    res.setDescription(voucher.getDescription());
                    res.setDiscountType(voucher.getDiscountType().name());
                    res.setDiscountValue(voucher.getDiscountValue());
                    res.setMaxDiscount(voucher.getMaxDiscount());
                    res.setMinOrderAmount(voucher.getMinOrderAmount());
                    res.setStartDate(voucher.getStartDate());
                    res.setEndDate(voucher.getEndDate());
                    res.setOutOfStock(usedCount >= voucher.getUsageLimit());

                    return res;
                })
                .toList();
    }

    public Voucher1Response saveVoucher(Account account, Long voucherId) {

        Voucher voucher = voucherRepository.findById(
                java.math.BigInteger.valueOf(voucherId)
        ).orElseThrow(() -> new RuntimeException("Voucher not found"));

        long usedCount = userVoucherRepository.countByVoucher(voucher);

        Voucher1Response res = new Voucher1Response();
        res.setVoucherId(voucher.getVoucherId());
        res.setCode(voucher.getCode());
        res.setTitle(voucher.getTitle());

        if (usedCount >= voucher.getUsageLimit()) {
            res.setOutOfStock(true);
            return res;
        }

        if (userVoucherRepository.existsByAccountAndVoucher(account, voucher)) {
            return res;
        }

        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setAccount(account);
        userVoucher.setVoucher(voucher);
        userVoucher.setUsed(false);

        userVoucherRepository.save(userVoucher);
        res.setOutOfStock(false);
        return res;
    }
}