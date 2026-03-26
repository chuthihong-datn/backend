package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.AddToCartRequest;
import com.example.food_app.dto.request.user.UpdateCartItemRequest;
import com.example.food_app.dto.response.user.CartResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.service.user.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal Account account,
            @RequestBody AddToCartRequest request
    ) {
        cartService.addToCart(account, request);
        return ResponseEntity.ok("Added to cart successfully");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(cartService.getCart(account));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateQuantity(
            @AuthenticationPrincipal Account account,
            @PathVariable BigInteger itemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        cartService.updateQuantity(account, itemId, request.getQuantity());
        return ResponseEntity.ok(cartService.getCart(account));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteItem(
            @AuthenticationPrincipal Account account,
            @PathVariable BigInteger itemId
    ) {
        cartService.deleteCartItem(account, itemId);
        return ResponseEntity.ok(cartService.getCart(account));
    }
}
