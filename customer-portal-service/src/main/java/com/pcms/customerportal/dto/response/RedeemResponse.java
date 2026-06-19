package com.pcms.customerportal.dto.response;

import java.util.UUID;

/**
 * Response DTO for POST /wallet/redeem.
 * MVP returns a generated voucher code; future iterations will
 * integrate with the voucher module for proper discount coupons.
 */
public record RedeemResponse(
        String status,
        int pointsRedeemed,
        int balanceAfter,
        String voucherCode,
        UUID transactionId,
        String message
) {}
