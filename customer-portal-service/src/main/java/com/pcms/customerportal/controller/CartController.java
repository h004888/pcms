package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.AddCartItemRequest;
import com.pcms.customerportal.dto.request.CheckoutConfirmRequest;
import com.pcms.customerportal.dto.request.CheckoutPreviewRequest;
import com.pcms.customerportal.dto.request.UpdateCartItemRequest;
import com.pcms.customerportal.dto.response.CartResponse;
import com.pcms.customerportal.dto.response.CheckoutConfirmResponse;
import com.pcms.customerportal.dto.response.CheckoutPreviewResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.CartService;
import com.pcms.customerportal.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * UC14 - Shopping Cart + Checkout (5 + 2 = 7 endpoints)
 * Authorization: Customer (authenticated, X-User-Id header).
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "UC14 - Cart + Checkout")
public class CartController {

    private final CartService cartService;
    private final CheckoutService checkoutService;

    public CartController(CartService cartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    @Operation(summary = "Get current cart")
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(cartService.getCart(CurrentUser.requireCustomerId(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart (auto-create cart if needed)")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(CurrentUser.requireCustomerId(userId), request));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item qty (recompute totals + apply BR04 5% bulk discount)")
    public ResponseEntity<CartResponse> updateItem(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(
                cartService.updateItem(CurrentUser.requireCustomerId(userId), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(cartService.removeItem(CurrentUser.requireCustomerId(userId), itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from cart")
    public ResponseEntity<CartResponse> clearCart(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(cartService.clearCart(CurrentUser.requireCustomerId(userId)));
    }

    @PostMapping("/checkout/preview")
    @Operation(summary = "Preview checkout totals (subtotal/discount/shipping/total)")
    public ResponseEntity<CheckoutPreviewResponse> preview(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody CheckoutPreviewRequest request) {
        return ResponseEntity.ok(
                checkoutService.preview(CurrentUser.requireCustomerId(userId), request));
    }

    @PostMapping("/checkout/confirm")
    @Operation(summary = "Confirm checkout - creates order via order-service")
    public ResponseEntity<CheckoutConfirmResponse> confirm(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody CheckoutConfirmRequest request) {
        return ResponseEntity.ok(
                checkoutService.confirm(CurrentUser.requireCustomerId(userId), request));
    }
}
