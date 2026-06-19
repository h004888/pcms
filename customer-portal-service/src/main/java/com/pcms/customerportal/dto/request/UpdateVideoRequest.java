package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request for VideoAdmin to update an existing video. All fields optional.
 */
public record UpdateVideoRequest(
        @Size(max = 200) String title,
        @Pattern(regexp = "^[A-Za-z0-9_-]{11}$", message = "YouTube ID must be 11 chars") String youtubeId,
        String thumbnailUrl,
        String source,
        Integer durationSec,
        String category,
        String status
) {}