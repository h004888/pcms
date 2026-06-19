package com.pcms.customerportal.dto.response;

import java.util.UUID;

/**
 * Response for SHOP-DISEASE-INFO endpoints.
 */
public record DiseaseInfoResponse(
        UUID id,
        String name,
        String slug,
        String targetAudience,
        String season,
        String severity,
        String body,
        Long viewCount
) {}