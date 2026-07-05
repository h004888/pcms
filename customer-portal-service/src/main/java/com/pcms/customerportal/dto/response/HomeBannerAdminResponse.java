package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.enums.BannerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin view của home banner (có status + timestamps).
 */
public record HomeBannerAdminResponse(
        UUID id,
        String title,
        String imageUrl,
        String linkUrl,
        Integer sortOrder,
        BannerStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
