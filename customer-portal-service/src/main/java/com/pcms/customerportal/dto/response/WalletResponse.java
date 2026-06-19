package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.WalletTier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for GET /wallet.
 * FR14.23.
 */
public record WalletResponse(
        UUID customerId,
        int balance,
        BigDecimal lifetimeSpend,
        WalletTierResponse currentTier,
        WalletTierResponse nextTier,
        int pointsToNextTier,
        List<String> perks,
        List<WalletTransactionResponse> recentTransactions
) {
    public record WalletTierResponse(
            UUID id, String name, BigDecimal minSpend,
            BigDecimal discountPct, int sortOrder
    ) {
        public static WalletTierResponse from(WalletTier t) {
            return new WalletTierResponse(
                    t.getId(), t.getName(), t.getMinSpend(),
                    t.getDiscountPct(), t.getSortOrder());
        }
    }
}
