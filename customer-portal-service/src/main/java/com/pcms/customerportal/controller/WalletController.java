package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.RedeemPointsRequest;
import com.pcms.customerportal.dto.response.RedeemResponse;
import com.pcms.customerportal.dto.response.WalletResponse;
import com.pcms.customerportal.dto.response.WalletTransactionResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@Tag(name = "UC14 - Customer Account / Health Wallet (Ví Khỏe Nhà Ta)")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get my health wallet (balance + tier + perks + recent tx)")
    public ResponseEntity<WalletResponse> get(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(service.getWallet(CurrentUser.requireCustomerId(userId)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Paginated wallet transaction history")
    public ResponseEntity<PageResponse<WalletTransactionResponse>> transactions(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                service.getTransactions(CurrentUser.requireCustomerId(userId), page, size));
    }

    @PostMapping("/redeem")
    @Operation(summary = "Redeem points for a reward")
    public ResponseEntity<RedeemResponse> redeem(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.ok(service.redeem(CurrentUser.requireCustomerId(userId), request));
    }
}
