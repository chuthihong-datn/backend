package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.AddToCartRequest;
import com.example.food_app.dto.response.user.CartItemResponse;
import com.example.food_app.dto.response.user.CartResponse;
import com.example.food_app.entity.*;
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
public class CartService {

    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final MenuSizeRepository menuSizeRepository;
    private final ToppingRepository toppingRepository;
    private final CartItemRepository cartItemRepository;
    private final FlashSaleRepository flashSaleRepository;

    // ================= ADD TO CART =================
    @Transactional
    public void addToCart(Account account, AddToCartRequest request) {

        Cart cart = cartRepository.findByAccount(account)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setAccount(account);
                    return cartRepository.save(newCart);
                });

        Menu menuItem = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu không tồn tại"));

        MenuSize menuSize;

        if (request.getMenuSizeId() != null) {

            menuSize = menuSizeRepository.findById(request.getMenuSizeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Size không tồn tại"
                    ));

        } else {

            // 👉 AUTO chọn size nhỏ nhất nếu có
            if (menuItem.getSizes() != null && !menuItem.getSizes().isEmpty()) {

                menuSize = menuItem.getSizes()
                        .stream()
                        .min(Comparator.comparing(MenuSize::getExtraPrice))
                        .orElse(null);

            } else {
                menuSize = null;
            }
        }

        List<Topping> toppings = request.getToppingIds() != null
                ? toppingRepository.findAllById(request.getToppingIds())
                : new ArrayList<>();

        if (request.getQuantity() > menuItem.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vượt quá tồn kho");
        }

        // ❌ KHÔNG gộp item → mỗi lần add là 1 dòng riêng
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setMenu(menuItem);
        newItem.setMenuSize(menuSize);
        newItem.setQuantity(request.getQuantity());
        newItem.setToppings(toppings);

        cartItemRepository.save(newItem);
    }

    // ================= GET CART =================
    public CartResponse getCart(Account account) {

        Cart cart = cartRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng không tồn tại"));

        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();

        boolean[] usedFlashSale = {false};

        List<CartItemResponse> items = cart.getCartItems()
                .stream()
                .map(item -> mapToResponse(item, flashSaleMap, usedFlashSale))
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    // ================= MAP RESPONSE =================
    private CartItemResponse mapToResponse(
            CartItem item,
            Map<BigInteger, FlashSale> flashSaleMap,
            boolean[] usedFlashSale
    ) {

        // ===== IMAGE (GIỮ NGUYÊN LOGIC) =====
        String image = null;
        if (item.getMenu().getImages() != null && !item.getMenu().getImages().isEmpty()) {
            image = item.getMenu().getImages().get(0);
        }

        String sizeName = item.getMenuSize() != null
                ? item.getMenuSize().getSizeName()
                : null;

        List<String> toppingNames = item.getToppings()
                .stream()
                .map(Topping::getName)
                .toList();

        BigDecimal basePrice = item.getMenu().getBasePrice();

        BigDecimal sizePrice = item.getMenuSize() != null
                ? item.getMenuSize().getExtraPrice()
                : BigDecimal.ZERO;

        BigDecimal toppingPrice = item.getToppings()
                .stream()
                .map(Topping::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal originalPrice = basePrice.add(sizePrice).add(toppingPrice);

        FlashSale fs = flashSaleMap.get(item.getMenu().getMenuId());

        int quantity = item.getQuantity();

        BigDecimal itemTotal;

        int saleQty = 0;
        BigDecimal salePrice = null;

        // ===== FLASH SALE: chỉ 1 item được giảm =====
        if (fs != null && !usedFlashSale[0]) {

            salePrice = applyDiscount(originalPrice, fs);
            saleQty = 1;

            int normalQty = quantity - 1;

            itemTotal = salePrice
                    .add(originalPrice.multiply(BigDecimal.valueOf(Math.max(normalQty, 0))));

            usedFlashSale[0] = true;

        } else {
            itemTotal = originalPrice.multiply(BigDecimal.valueOf(quantity));
        }

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .image(image) // ✅ FIX IMAGE
                .menuId(item.getMenu().getMenuId())
                .menuName(item.getMenu().getName())
                .sizeName(sizeName)
                .toppings(toppingNames)
                .quantity(quantity)
                .price(originalPrice)
                .itemTotal(itemTotal)
                .saleQuantity(saleQty)
                .salePrice(salePrice)
                .isFlashSaleApplied(saleQty > 0)
                .build();
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

    // ================= UPDATE =================
    @Transactional
    public void updateQuantity(Account account, BigInteger cartItemId, Integer quantity) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy item"));

        if (!item.getCart().getAccount().getAccountId().equals(account.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        if (quantity > item.getMenu().getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vượt tồn kho");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    // ================= DELETE =================
    @Transactional
    public void deleteCartItem(Account account, BigInteger cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy item"));

        if (!item.getCart().getAccount().getAccountId().equals(account.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền");
        }

        cartItemRepository.delete(item);
    }
}