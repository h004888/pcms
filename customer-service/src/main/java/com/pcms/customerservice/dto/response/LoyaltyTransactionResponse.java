package com.pcms.customerservice.dto.response;

import com.pcms.customerservice.entity.LoyaltyTransaction;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoyaltyTransactionResponse(
        UUID id,
        UUID customerId,
        Integer points,
        Integer balanceAfter,
        UUID refOrderId,
        String reason,
        LocalDateTime createdAt) {
    public static LoyaltyTransactionResponse from(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
                transaction.getId(),
                transaction.getCustomerId(),
                transaction.getPoints(),
                transaction.getBalanceAfter(),
                transaction.getRefOrderId(),
                transaction.getReason(),
                transaction.getCreatedAt());
    }
}