package com.pcms.catalogservice.dto.response;

import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.enums.MedicineStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicineResponse(
        UUID id,
        String sku,
        String name,
        UUID categoryId,
        UUID supplierId,
        BigDecimal price,
        String unit,
        Boolean prescriptionRequired,
        String imageUrl,
        MedicineStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MedicineResponse from(Medicine m) {
        return new MedicineResponse(
                m.getId(),
                m.getSku(),
                m.getName(),
                m.getCategoryId(),
                m.getSupplierId(),
                m.getPrice(),
                m.getUnit(),
                m.getPrescriptionRequired(),
                m.getImageUrl(),
                m.getStatus(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
