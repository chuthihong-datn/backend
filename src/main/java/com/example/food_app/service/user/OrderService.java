package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.OrderRequest;
import com.example.food_app.entity.*;
import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentMethod;
import com.example.food_app.entity.enums.PaymentStatus;
import com.example.food_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final WardRepository wardRepository;
    private final MenuRepository menuRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final VNPayService vnpayService;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Transactional
    public Object createOrder(OrderRequest request, Account account, String ip) {

        // get cart
        Cart cart = cartRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy giỏ hàng"
                ));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Giỏ hàng trống"
            );
        }

        // check khu vực giao
        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy khu vực"
                ));

        if (!Boolean.TRUE.equals(ward.getIsDelivery())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Khu vực không hỗ trợ giao hàng"
            );
        }

        // tạo order
        Order order = new Order();
        order.setAccount(account);
        order.setWard(ward);
        order.setAddressDetail(request.getAddressDetail());
        order.setShippingFee(BigDecimal.valueOf(ward.getShippingFee()));
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();

        BigDecimal rawTotal = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            BigDecimal originalPrice = calculateOriginalPrice(item);

            rawTotal = rawTotal.add(
                    originalPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        // áp dụng flash sale
        boolean usedFlashSale = false;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {

            Menu menu = item.getMenu();
            int quantity = item.getQuantity();

            if (quantity <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Số lượng không hợp lệ"
                );
            }

            if (menu.getAmount() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Món " + menu.getName() + " không đủ số lượng"
                );
            }

            BigDecimal originalPrice = calculateOriginalPrice(item);
            FlashSale fs = flashSaleMap.get(menu.getMenuId());

            BigDecimal itemTotal;

            if (fs != null && !usedFlashSale) {

                boolean eligible = fs.getMinOrderAmount() == null
                        || rawTotal.compareTo(fs.getMinOrderAmount()) >= 0;

                if (eligible) {

                    BigDecimal salePrice = applyDiscount(originalPrice, fs);

                    int saleQty = 1;
                    int normalQty = Math.max(quantity - 1, 0);

                    itemTotal = salePrice.multiply(BigDecimal.valueOf(saleQty))
                            .add(originalPrice.multiply(BigDecimal.valueOf(normalQty)));

                    usedFlashSale = true;

                } else {
                    itemTotal = originalPrice.multiply(BigDecimal.valueOf(quantity));
                }

            } else {
                itemTotal = originalPrice.multiply(BigDecimal.valueOf(quantity));
            }

            totalAmount = totalAmount.add(itemTotal);

            // lưu chi tiết đơn hàng
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setMenu(menu);
            detail.setMenuSize(item.getMenuSize());
            detail.setQuantity(quantity);
            detail.setBasePrice(menu.getBasePrice());
            detail.setSizeExtraPrice(
                    item.getMenuSize() != null ? item.getMenuSize().getExtraPrice() : BigDecimal.ZERO
            );
            detail.setToppingTotalPrice(
                    item.getToppings().stream()
                            .map(Topping::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
            detail.setItemTotalPrice(itemTotal);
            detail.setToppings(new HashSet<>(item.getToppings()));

            orderDetailRepository.save(detail);
        }
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {

            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Voucher không tồn tại"
                    ));

            // check active
            if (!Boolean.TRUE.equals(voucher.getIsActive())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Voucher không khả dụng"
                );
            }

            LocalDateTime now = LocalDateTime.now();

            if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Voucher chưa bắt đầu"
                );
            }

            if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Voucher đã hết hạn"
                );
            }

            // check min order
            if (voucher.getMinOrderAmount() != null &&
                    totalAmount.compareTo(voucher.getMinOrderAmount()) < 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher"
                );
            }

            // check user có voucher không
            UserVoucher userVoucher = userVoucherRepository
                    .findByAccountAndVoucher(account, voucher)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Bạn không sở hữu voucher này"
                    ));

            if (userVoucher.isUsed()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Voucher đã được sử dụng"
                );
            }

            // tính giảm giá
            if (voucher.getDiscountType() != null) {

                switch (voucher.getDiscountType()) {

                    case PERCENT -> {
                        if (voucher.getDiscountValue() == null) {
                            throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST, "Voucher không hợp lệ"
                            );
                        }

                        discountAmount = totalAmount
                                .multiply(voucher.getDiscountValue())
                                .divide(BigDecimal.valueOf(100));

                        if (voucher.getMaxDiscount() != null) {
                            discountAmount = discountAmount.min(voucher.getMaxDiscount());
                        }
                    }

                    case FIXED -> {
                        if (voucher.getDiscountValue() == null) {
                            throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST, "Voucher không hợp lệ"
                            );
                        }

                        discountAmount = voucher.getDiscountValue();

                        if (discountAmount.compareTo(totalAmount) > 0) {
                            discountAmount = totalAmount;
                        }
                    }
                }
            }

            // đánh dấu đã dùng
            userVoucher.setUsed(true);
            userVoucher.setUsedAt(LocalDateTime.now());
            userVoucherRepository.save(userVoucher);
        }

        BigDecimal finalAmount = totalAmount
                .subtract(discountAmount)
                .add(order.getShippingFee());

        order.setTotalAmount(totalAmount);
        order.setFinalAmount(finalAmount);
        order.setDiscountAmount(discountAmount);

        orderRepository.save(order);

        // VNPay
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {

            String paymentUrl = vnpayService.createPaymentUrl(order, ip);

            return Map.of(
                    "orderId", order.getOrderId(),
                    "paymentUrl", paymentUrl,
                    "totalAmount", totalAmount,
                    "finalAmount", finalAmount,
                    "flashSaleApplied", usedFlashSale
            );
        }

        // cập nhật tồn kho
        for (CartItem item : cartItems) {
            Menu menu = item.getMenu();
            menu.setAmount(menu.getAmount() - item.getQuantity());
            menuRepository.save(menu);
        }

        // xóa giỏ hàng sau khi order thành công
        cartItemRepository.deleteByCart(cart);

        return Map.of(
                "orderId", order.getOrderId(),
                "message", "Đặt hàng thành công",
                "totalAmount", totalAmount,
                "finalAmount", finalAmount,
                "flashSaleApplied", usedFlashSale
        );
    }

    private BigDecimal calculateOriginalPrice(CartItem item) {

        BigDecimal base = item.getMenu().getBasePrice();

        BigDecimal size = item.getMenuSize() != null
                ? item.getMenuSize().getExtraPrice()
                : BigDecimal.ZERO;

        BigDecimal topping = item.getToppings().stream()
                .map(Topping::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return base.add(size).add(topping);
    }

    private Map<BigInteger, FlashSale> buildFlashSaleMap() {

        LocalDateTime now = LocalDateTime.now();
        Map<BigInteger, FlashSale> map = new HashMap<>();

        flashSaleRepository.findAll().stream()
                .filter(fs -> Boolean.TRUE.equals(fs.getIsActive()))
                .filter(fs -> fs.getStartTime() != null && fs.getEndTime() != null)
                .filter(fs -> now.isAfter(fs.getStartTime()) && now.isBefore(fs.getEndTime()))
                .forEach(fs -> {
                    if (fs.getItems() != null) {
                        fs.getItems().forEach(m -> map.put(m.getMenuId(), fs));
                    }
                });

        return map;
    }

    private BigDecimal applyDiscount(BigDecimal price, FlashSale fs) {

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