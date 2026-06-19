package com.pcms.healthtools.dto.response;

import java.util.UUID;

public record QuizResponse(
        UUID id,
        String slug,
        String name,
        String description,
        Integer questionCount,
        String scoringLogic
) {}
