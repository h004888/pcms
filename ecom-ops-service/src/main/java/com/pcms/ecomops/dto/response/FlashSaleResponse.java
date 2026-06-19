package com.pcms.ecomops.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FlashSaleResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        BigDecimal discountPct,
        Integer maxQtyPerUser,
        String status,
        List<FlashSaleItemResponse> items
) {}
