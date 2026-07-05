package com.pcms.ecomops.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FlashSaleItemResponse(
        UUID id,
        UUID flashSaleId,
        UUID medicineId,
        String medicineName,
        String imageUrl,
        BigDecimal originalPrice,
        BigDecimal salePrice,
        Integer discountPercent,
        Integer qtyLimit,
        Integer soldQty
) {}
