package com.pcms.catalogservice.dto.response;

import com.pcms.catalogservice.entity.Medicine;

import java.util.UUID;

public record MedicineMediaSummaryResponse(
        UUID id,
        String slug,
        String imageUrl,
        String description
) {
    public static MedicineMediaSummaryResponse from(Medicine m) {
        return new MedicineMediaSummaryResponse(
                m.getId(),
                m.getSlug(),
                m.getImageUrl(),
                m.getDescription()
        );
    }
}