package com.pcms.inventoryservice.dto.response;

import java.util.List;

public record BulkImportResultResponse(
        int total,
        int successCount,
        int failedCount,
        List<String> errors) {
}