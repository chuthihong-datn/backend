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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final MenuSizeRepository menuSizeRepository;
    private final ToppingRepository toppingRepository;
    private final CartItemRepository cartItemRepository;

    //thêm vào giỏ hàng
    @Transactional
    public void addToCart(Account account, AddToCartRequest request) {
        Cart cart = cartRepository.findByAccount(account)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setAccount(account);
                    return cartRepository.save(newCart);
                });

        Menu menuItem = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Menu không tồn tại"
                ));

        final MenuSize menuSize = request.getMenuSizeId() != null
                ? menuSizeRepository.findById(request.getMenuSizeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Size không tồn tại"
                ))
                : null;

        final List<Topping> toppings =
                request.getToppingIds() != null && !request.getToppingIds().isEmpty()
                        ? toppingRepository.findAllById(request.getToppingIds())
                        : new ArrayList<>();

        Optional<CartItem> existingItem = cart.getCartItems()
                .stream()
                .filter(item -> isSameItem(item, menuItem, menuSize, toppings))
                .findFirst();

        int availableStock = menuItem.getAmount();

        int newQuantity = existingItem
                .map(item -> item.getQuantity() + request.getQuantity())
                .orElse(request.getQuantity());

        if (newQuantity > availableStock) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số lượng vượt quá tồn kho"
            );
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setMenu(menuItem);
            newItem.setMenuSize(menuSize);
            newItem.setQuantity(request.getQuantity());
            newItem.setToppings(toppings);

            cartItemRepository.save(newItem);
            cart.getCartItems().add(newItem);
        }
    }

    //kiểm tra món giống nhau
    private boolean isSameItem(CartItem item, Menu menu, MenuSize size, List<Topping> toppings) {
        if (!item.getMenu().getMenuId().equals(menu.getMenuId())) return false;

        if (!Objects.equals(
                item.getMenuSize() != null ? item.getMenuSize().getMenuSizeId() : null,
                size != null ? size.getMenuSizeId() : null
        )) return false;

        Set<BigInteger> itemToppingIds = item.getToppings()
                .stream().map(Topping::getToppingId)
                .collect(Collectors.toSet());

        Set<BigInteger> requestToppingIds = toppings
                .stream().map(Topping::getToppingId)
                .collect(Collectors.toSet());

        return itemToppingIds.equals(requestToppingIds);
    }

    //lấy danh sách món trong giỏ
    public CartResponse getCart(Account account) {

        Cart cart = cartRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Giỏ hàng không tồn tại"
                ));

        List<CartItemResponse> items = cart.getCartItems()
                .stream()
                .map(this::mapToResponse)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse mapToResponse(CartItem item) {

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

        BigDecimal price = basePrice
                .add(sizePrice)
                .add(toppingPrice);

        BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .image(image)
                .menuId(item.getMenu().getMenuId())
                .menuName(item.getMenu().getName())
                .sizeName(sizeName)
                .toppings(toppingNames)
                .quantity(item.getQuantity())
                .price(price)
                .itemTotal(itemTotal)
                .build();
    }

    //sửa số lượng món
    @Transactional
    public void updateQuantity(Account account, BigInteger cartItemId, int quantity) {

        if (account == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!item.getCart().getAccount().getAccountId().equals(account.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền");
        }

        int availableStock = item.getMenu().getAmount();

        int totalOtherItems = item.getCart().getCartItems().stream()
                .filter(i -> i.getMenu().getMenuId().equals(item.getMenu().getMenuId()))
                .filter(i -> !i.getCartItemId().equals(cartItemId))
                .mapToInt(CartItem::getQuantity)
                .sum();

        int newTotal = totalOtherItems + quantity;

        if (newTotal > availableStock) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tổng số lượng vượt quá tồn kho"
            );
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    //xóa món trong giỏ
    @Transactional
    public void deleteCartItem(Account account, BigInteger cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Giỏ hàng không tồn tại"
                ));

        if (!item.getCart().getAccount().getAccountId().equals(account.getAccountId())) {
            throw new RuntimeException("Access denied");
        }

        cartItemRepository.delete(item);
    }
}