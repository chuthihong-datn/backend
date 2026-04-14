package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.OrderRequest;
import com.example.food_app.entity.*;
import com.example.food_app.entity.enums.OrderStatus;
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

    @Transactional
    public Object createOrder(OrderRequest request, Account account, String ip) {

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

        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy khu vực"
                ));

        if (!Boolean.TRUE.equals(ward.getIsDelivery())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Khu vực không hỗ trợ giao hàng"
            );
        }

        Order order = new Order();
        order.setAccount(account);
        order.setWard(ward);
        order.setAddressDetail(request.getAddressDetail());
        order.setShippingFee(BigDecimal.valueOf(ward.getShippingFee()));
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        // ================= FLASH SALE MAP =================
        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();

        boolean usedFlashSale = false;
        FlashSale activeFlashSale = null;

        BigDecimal totalAmount = BigDecimal.ZERO;

        // ================= CALCULATE CART TOTAL FIRST =================
        for (CartItem item : cartItems) {

            Menu menu = item.getMenu();
            int quantity = item.getQuantity();

            MenuSize size = item.getMenuSize();
            BigDecimal sizePrice = size != null ? size.getExtraPrice() : BigDecimal.ZERO;

            List<Topping> toppingList = item.getToppings() != null
                    ? item.getToppings()
                    : new ArrayList<>();

            BigDecimal toppingTotal = toppingList.stream()
                    .map(Topping::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal basePrice = menu.getBasePrice();

            BigDecimal originalPrice = basePrice
                    .add(sizePrice)
                    .add(toppingTotal);

            totalAmount = totalAmount.add(originalPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        // ================= ORDER DETAILS =================
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

            MenuSize size = item.getMenuSize();
            BigDecimal sizePrice = size != null ? size.getExtraPrice() : BigDecimal.ZERO;

            List<Topping> toppingList = item.getToppings() != null
                    ? item.getToppings()
                    : new ArrayList<>();

            BigDecimal toppingTotal = toppingList.stream()
                    .map(Topping::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal basePrice = menu.getBasePrice();

            BigDecimal originalPrice = basePrice
                    .add(sizePrice)
                    .add(toppingTotal);

            FlashSale fs = flashSaleMap.get(menu.getMenuId());

            BigDecimal itemTotal;

            // ================= APPLY FLASH SALE =================
            if (fs != null && !usedFlashSale) {

                activeFlashSale = fs;

                boolean eligible = fs.getMinOrderAmount() == null
                        || totalAmount.compareTo(fs.getMinOrderAmount()) >= 0;

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

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setMenu(menu);
            detail.setMenuSize(size);
            detail.setQuantity(quantity);
            detail.setBasePrice(basePrice);
            detail.setSizeExtraPrice(sizePrice);
            detail.setToppingTotalPrice(toppingTotal);
            detail.setItemTotalPrice(itemTotal);
            detail.setToppings(new HashSet<>(toppingList));

            orderDetailRepository.save(detail);
        }

        // ================= FINAL AMOUNT =================
        BigDecimal finalAmount = totalAmount
                .add(order.getShippingFee());

        order.setTotalAmount(totalAmount);
        order.setFinalAmount(finalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);

        orderRepository.save(order);

        // ================= PAYMENT =================
        if (request.getPaymentMethod().name().equals("VNPAY")) {

            String paymentUrl = vnpayService.createPaymentUrl(order, ip);

            return Map.of(
                    "orderId", order.getOrderId(),
                    "paymentUrl", paymentUrl,
                    "flashSaleApplied", usedFlashSale
            );
        }

        // ================= UPDATE STOCK =================
        for (CartItem item : cartItems) {
            Menu menu = item.getMenu();
            menu.setAmount(menu.getAmount() - item.getQuantity());
            menuRepository.save(menu);
        }

        cartItemRepository.deleteByCart(cart);

        return Map.of(
                "orderId", order.getOrderId(),
                "message", "Đặt hàng thành công",
                "flashSaleApplied", usedFlashSale
        );
    }

    // ================= FLASH SALE MAP =================
    private Map<BigInteger, FlashSale> buildFlashSaleMap() {

        LocalDateTime now = LocalDateTime.now();

        List<FlashSale> activeSales = flashSaleRepository.findAll().stream()
                .filter(fs -> Boolean.TRUE.equals(fs.getIsActive()))
                .filter(fs -> fs.getStartTime() != null && fs.getEndTime() != null)
                .filter(fs -> now.isAfter(fs.getStartTime()) && now.isBefore(fs.getEndTime()))
                .toList();

        Map<BigInteger, FlashSale> map = new HashMap<>();

        for (FlashSale fs : activeSales) {
            if (fs.getItems() == null) continue;

            for (Menu item : fs.getItems()) {
                map.put(item.getMenuId(), fs);
            }
        }

        return map;
    }

    // ================= DISCOUNT =================
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