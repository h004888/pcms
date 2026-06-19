package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.response.OrderHistoryItemResponse;
import com.pcms.customerportal.dto.response.OrderTrackingResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * SHOP-ORDER-TRACK + SHOP-ORDER-HISTORY (2 endpoints).
 * Authorization: Customer (authenticated).
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "UC14 - Order Tracking B2C")
public class OrderTrackingController {

    private final OrderTrackingService service;

    public OrderTrackingController(OrderTrackingService service) {
        this.service = service;
    }

    @GetMapping("/{id}/track")
    @Operation(summary = "Track order status with timeline + estimated delivery")
    public ResponseEntity<OrderTrackingResponse> track(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID orderId) {
        return ResponseEntity.ok(
                service.track(orderId, CurrentUser.requireCustomerId(userId)));
    }

    @GetMapping("/history")
    @Operation(summary = "B2C order history (paginated, own orders only)")
    public ResponseEntity<PageResponse<OrderHistoryItemResponse>> history(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                service.history(CurrentUser.requireCustomerId(userId), page, size));
    }
}