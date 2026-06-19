package com.pcms.ecomops.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request to create a flash sale.
 */
public record CreateFlashSaleRequest(
        @NotBlank String name,
        String description,
        @NotNull LocalDateTime startsAt,
        @NotNull LocalDateTime endsAt,
        BigDecimal discountPct,
        Integer maxQtyPerUser,
        List<FlashSaleItemRequest> items
) {}
