package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.WalletTransaction;
import com.pcms.customerportal.enums.WalletTransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        UUID customerId,
        WalletTransactionType type,
        int amount,
        int balanceAfter,
        String source,
        UUID sourceId,
        String note,
        LocalDateTime createdAt
) {
    public static WalletTransactionResponse from(WalletTransaction t) {
        return new WalletTransactionResponse(
                t.getId(), t.getCustomerId(), t.getType(),
                t.getAmount(), t.getBalanceAfter(),
                t.getSource(), t.getSourceId(), t.getNote(),
                t.getCreatedAt()
        );
    }
}
