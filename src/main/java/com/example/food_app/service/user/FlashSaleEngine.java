package com.example.food_app.service.user;

import com.example.food_app.entity.FlashSale;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

public class FlashSaleEngine {
    @Data
    @Builder
    public static class PriceResult {
        private BigDecimal originalPrice;
        private BigDecimal salePrice;
        private BigDecimal finalPrice;
        private int saleQuantity;
        private boolean isFlashSaleApplied;
        private boolean eligible;
        private String message;
        private BigDecimal minOrderAmount;
    }

    public static PriceResult calculate(
            BigDecimal originalPrice,
            int quantity,
            FlashSale fs,
            boolean isEligible,
            boolean usedFlashSale
    ) {

        // không có flash sale
        if (fs == null) {
            return PriceResult.builder()
                    .originalPrice(originalPrice)
                    .finalPrice(originalPrice.multiply(BigDecimal.valueOf(quantity)))
                    .saleQuantity(0)
                    .isFlashSaleApplied(false)
                    .eligible(true)
                    .build();
        }

        // chưa đạt min order
        if (!isEligible) {
            return PriceResult.builder()
                    .originalPrice(originalPrice)
                    .finalPrice(originalPrice.multiply(BigDecimal.valueOf(quantity)))
                    .saleQuantity(0)
                    .isFlashSaleApplied(false)
                    .eligible(false)
                    .minOrderAmount(fs.getMinOrderAmount())
                    .message("Đơn tối thiểu " + fs.getMinOrderAmount() + "đ để áp dụng Flash Sale")
                    .build();
        }

        // đã dùng flash sale
        if (usedFlashSale) {
            return PriceResult.builder()
                    .originalPrice(originalPrice)
                    .finalPrice(originalPrice.multiply(BigDecimal.valueOf(quantity)))
                    .saleQuantity(0)
                    .isFlashSaleApplied(false)
                    .eligible(true)
                    .build();
        }

        // áp dụng flash sale
        BigDecimal salePrice = applyDiscount(originalPrice, fs);

        int saleQty = 1;
        int normalQty = Math.max(quantity - 1, 0);

        BigDecimal finalPrice = salePrice.multiply(BigDecimal.valueOf(saleQty))
                .add(originalPrice.multiply(BigDecimal.valueOf(normalQty)));

        return PriceResult.builder()
                .originalPrice(originalPrice)
                .salePrice(salePrice)
                .finalPrice(finalPrice)
                .saleQuantity(saleQty)
                .isFlashSaleApplied(true)
                .eligible(true)
                .build();
    }

    private static BigDecimal applyDiscount(BigDecimal price, FlashSale fs) {

        if ("PERCENT".equals(fs.getDiscountType().name())) {
            BigDecimal discount = price
                    .multiply(fs.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));

            if (fs.getMaxDiscount() != null) {
                discount = discount.min(fs.getMaxDiscount());
            }

            return price.subtract(discount);
        }

        return price.subtract(fs.getDiscountValue());
    }
}