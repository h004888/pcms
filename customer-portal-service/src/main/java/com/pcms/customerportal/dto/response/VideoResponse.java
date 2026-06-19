package com.pcms.customerportal.dto.response;

import java.util.UUID;

/**
 * Response for video endpoints.
 */
public record VideoResponse(
        UUID id,
        String title,
        String youtubeId,
        String thumbnailUrl,
        String source,
        Integer durationSec,
        String category,
        Long viewCount,
        String status
) {}