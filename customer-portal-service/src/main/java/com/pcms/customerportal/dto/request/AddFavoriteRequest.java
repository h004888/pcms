package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Body of POST /favorites.
 * FR14.25.
 */
public record AddFavoriteRequest(
        @NotNull(message = "medicineId is required")
        UUID medicineId
) {}
