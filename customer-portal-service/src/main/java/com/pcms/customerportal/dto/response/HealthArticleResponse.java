package com.pcms.customerportal.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for SHOP-HEALTH-ARTICLE endpoints.
 */
public record HealthArticleResponse(
        UUID id,
        String title,
        String slug,
        String category,
        String author,
        String bodyMarkdown,
        LocalDateTime publishedAt,
        Long viewCount
) {}