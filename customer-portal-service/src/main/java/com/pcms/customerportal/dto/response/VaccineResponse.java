package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.Vaccine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 - VACCINE-HOME. Public-facing vaccine summary card.
 */
public record VaccineResponse(
        UUID id,
        String name,
        String manufacturer,
        Integer dosesRequired,
        Integer daysBetweenDoses,
        BigDecimal price,
        String description,
        String status,
        LocalDateTime createdAt
) {
    public static VaccineResponse from(Vaccine v) {
        return new VaccineResponse(
                v.getId(),
                v.getName(),
                v.getManufacturer(),
                v.getDosesRequired(),
                v.getDaysBetweenDoses(),
                v.getPrice(),
                v.getDescription(),
                v.getStatus(),
                v.getCreatedAt()
        );
    }
}