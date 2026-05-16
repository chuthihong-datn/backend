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

    // add to cart
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size không tồn tại"));
        } else {
            menuSize = menuItem.getSizes() == null ? null :
                    menuItem.getSizes().stream()
                            .min(Comparator.comparing(MenuSize::getExtraPrice))
                            .orElse(null);
        }

        List<Topping> toppings = request.getToppingIds() != null
                ? toppingRepository.findAllById(request.getToppingIds())
                : new ArrayList<>();

        if (request.getQuantity() > menuItem.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vượt quá tồn kho");
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setMenu(menuItem);
        newItem.setMenuSize(menuSize);
        newItem.setQuantity(request.getQuantity());
        newItem.setToppings(toppings);

        cartItemRepository.save(newItem);
    }

    // get cart
    public CartResponse getCart(Account account) {

        Cart cart = cartRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy giỏ"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();

        // tổng tiền
        BigDecimal rawTotal = cartItems.stream()
                .map(i -> getOriginalPrice(i).multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean usedFlashSale = false;
        boolean flashSaleEligible = true;
        boolean hasFlashSaleItem = false;

        BigDecimal minOrderAmount = BigDecimal.ZERO;

        List<CartItemResponse> responses = new ArrayList<>();
        BigDecimal finalTotal = BigDecimal.ZERO;

        for (CartItem item : cartItems) {

            BigDecimal originalPrice = getOriginalPrice(item);
            FlashSale fs = flashSaleMap.get(item.getMenu().getMenuId());

            if (fs != null) {
                hasFlashSaleItem = true;

                if (fs.getMinOrderAmount() != null) {
                    minOrderAmount = minOrderAmount.max(fs.getMinOrderAmount());
                }
            }

            boolean eligible = fs == null
                    || fs.getMinOrderAmount() == null
                    || rawTotal.compareTo(fs.getMinOrderAmount()) >= 0;

            if (fs != null && !eligible) {
                flashSaleEligible = false;
            }

            var result = FlashSaleEngine.calculate(
                    originalPrice,
                    item.getQuantity(),
                    fs,
                    eligible,
                    usedFlashSale
            );

            if (result.isFlashSaleApplied()) {
                usedFlashSale = true;
            }

            finalTotal = finalTotal.add(result.getFinalPrice());

            String image = (item.getMenu().getImages() != null && !item.getMenu().getImages().isEmpty())
                    ? item.getMenu().getImages().get(0)
                    : "";

            // list topping
            List<String> toppingNames = item.getToppings() != null
                    ? item.getToppings().stream()
                    .map(Topping::getName)
                    .toList()
                    : new ArrayList<>();

            // size
            String sizeName = item.getMenuSize() != null
                    ? item.getMenuSize().getSizeName()
                    : null;

            responses.add(CartItemResponse.builder()
                    .cartItemId(item.getCartItemId())
                    .menuId(item.getMenu().getMenuId())
                    .menuName(item.getMenu().getName())
                    .image(image)
                    .quantity(item.getQuantity())
                    .price(originalPrice)
                    .itemTotal(result.getFinalPrice())
                    .salePrice(result.getSalePrice())
                    .saleQuantity(result.getSaleQuantity())
                    .isFlashSaleApplied(result.isFlashSaleApplied())
                    .toppings(toppingNames)
                    .sizeName(sizeName)

                    .build());
        }

        // trả về message nếu đơn chưa đạt giá trị tối thiểu để áp dụng flash sale
        String message = null;

        if (hasFlashSaleItem && !flashSaleEligible && minOrderAmount.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal missing = minOrderAmount.subtract(rawTotal).max(BigDecimal.ZERO);
            long missingK = (long) Math.ceil(missing.doubleValue() / 1000);

            message = "Mua thêm " + missingK + "k để đạt giá trị tối thiểu áp dụng flash sale.";
        }

        return CartResponse.builder()
                .items(responses)
                .totalAmount(finalTotal)
                .flashSaleEligible(flashSaleEligible)
                .minOrderAmount(minOrderAmount)
                .flashSaleMessage(message)
                .build();
    }

    // flash sale map
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

    // giá ban đầu
    private BigDecimal getOriginalPrice(CartItem item) {

        BigDecimal base = item.getMenu().getBasePrice();

        BigDecimal size = item.getMenuSize() != null
                ? item.getMenuSize().getExtraPrice()
                : BigDecimal.ZERO;

        BigDecimal topping = item.getToppings().stream()
                .map(Topping::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return base.add(size).add(topping);
    }

    // update số lượng item
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

    // xóa món trong giỏ
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