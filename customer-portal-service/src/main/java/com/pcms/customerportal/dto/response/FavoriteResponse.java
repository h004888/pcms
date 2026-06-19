package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.CustomerFavorite;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteResponse(
        UUID id,
        UUID customerId,
        UUID medicineId,
        LocalDateTime createdAt
) {
    public static FavoriteResponse from(CustomerFavorite f) {
        return new FavoriteResponse(
                f.getId(), f.getCustomerId(), f.getMedicineId(), f.getCreatedAt());
    }
}
