package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.OrderRequest;
import com.example.food_app.entity.*;
import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.entity.enums.PaymentStatus;
import com.example.food_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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
    private final VNPayService vnpayService;

    @Transactional
    public Object createOrder(OrderRequest request, Account account, String ip) {

        Cart cart = cartRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy giỏ hàng"
                ));

        var cartItems = cartItemRepository.findByCart(cart);

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

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {

            Menu menu = item.getMenu();
            Integer quantity = item.getQuantity();

            if (quantity == null || quantity <= 0) {
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

            Set<Topping> toppings = new HashSet<>(toppingList);

            BigDecimal toppingTotal = toppingList.stream()
                    .map(Topping::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal basePrice = menu.getBasePrice();

            BigDecimal itemPrice = basePrice
                    .add(sizePrice)
                    .add(toppingTotal);

            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(itemTotal);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setMenu(menu);
            detail.setMenuSize(size);
            detail.setQuantity(quantity);
            detail.setBasePrice(basePrice);
            detail.setSizeExtraPrice(sizePrice);
            detail.setToppingTotalPrice(toppingTotal);
            detail.setItemTotalPrice(itemTotal);
            detail.setToppings(toppings);

            orderDetailRepository.save(detail);
        }

        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal finalAmount = totalAmount
                .add(order.getShippingFee())
                .subtract(discount);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discount);
        order.setFinalAmount(finalAmount);

        orderRepository.save(order);

        if (request.getPaymentMethod().name().equals("VNPAY")) {

            String paymentUrl = vnpayService.createPaymentUrl(order, ip);

            return Map.of(
                    "orderId", order.getOrderId(),
                    "paymentUrl", paymentUrl
            );
        }

        for (CartItem item : cartItems) {
            Menu menu = item.getMenu();
            menu.setAmount(menu.getAmount() - item.getQuantity());
            menuRepository.save(menu);
        }

        cartItemRepository.deleteByCart(cart);

        return Map.of(
                "orderId", order.getOrderId(),
                "message", "Đặt hàng thành công"
        );
    }
}