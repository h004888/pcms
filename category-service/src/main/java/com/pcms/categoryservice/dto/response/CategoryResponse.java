package com.pcms.categoryservice.dto.response;

import com.pcms.categoryservice.enums.CategoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
                UUID id,
                String name,
                String slug,
                String description,
                String imageUrl,
                CategoryStatus status,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
