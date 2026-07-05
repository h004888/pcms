package com.pcms.customerportal.dto.request;

import com.pcms.customerportal.enums.BannerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Admin payload for creating a home banner.
 * Hero carousel trang chủ - admin upload.
 */
public record CreateHomeBannerRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String imageUrl,
        @Size(max = 500) String linkUrl,
        Integer sortOrder,
        BannerStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
