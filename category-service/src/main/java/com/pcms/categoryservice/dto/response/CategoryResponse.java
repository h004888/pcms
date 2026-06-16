package com.pcms.categoryservice.dto.response;

import com.pcms.categoryservice.enums.CategoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
                UUID id,
                String name,
                String description,
                CategoryStatus status,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
