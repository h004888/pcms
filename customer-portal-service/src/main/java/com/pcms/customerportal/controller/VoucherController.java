package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.ApplyVoucherRequest;
import com.pcms.customerportal.dto.response.ApplyVoucherResponse;
import com.pcms.customerportal.dto.response.VoucherResponse;
import com.pcms.customerportal.dto.response.VoucherUsageResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * UC14 / UC19 - Voucher / Coupon (3 endpoints).
 * Authorization: Customer (authenticated).
 */
@RestController
@RequestMapping("/vouchers")
@Tag(name = "UC14/UC19 - Vouchers")
public class VoucherController {

    private final VoucherService service;

    public VoucherController(VoucherService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List active vouchers available to the current customer")
    public ResponseEntity<List<VoucherResponse>> list() {
        return ResponseEntity.ok(service.listActive());
    }

    @PostMapping("/apply")
    @Operation(summary = "Preview voucher discount (does NOT consume voucher)")
    public ResponseEntity<ApplyVoucherResponse> apply(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @RequestParam(name = "cartTotal", defaultValue = "0") BigDecimal cartTotal,
            @Valid @RequestBody ApplyVoucherRequest request) {
        return ResponseEntity.ok(
                service.apply(CurrentUser.requireCustomerId(userId), request, cartTotal));
    }

    @GetMapping("/history")
    @Operation(summary = "Voucher usage history for the current customer")
    public ResponseEntity<List<VoucherUsageResponse>> history(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(service.history(CurrentUser.requireCustomerId(userId)));
    }
}