package com.pcms.customerportal.dto.request;

import com.pcms.customerportal.enums.BannerStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Admin partial-update payload. Tất cả field optional - chỉ field có giá trị mới update.
 */
public record UpdateHomeBannerRequest(
        @Size(max = 200) String title,
        @Size(max = 500) String imageUrl,
        @Size(max = 500) String linkUrl,
        Integer sortOrder,
        BannerStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
