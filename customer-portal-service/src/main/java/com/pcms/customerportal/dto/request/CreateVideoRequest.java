package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request for VideoAdmin (admin) to create a new video.
 */
public record CreateVideoRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{11}$", message = "YouTube ID must be 11 chars") String youtubeId,
        String thumbnailUrl,
        String source,
        Integer durationSec,
        String category
) {}