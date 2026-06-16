package com.pcms.reportservice.dto;

import java.util.List;
import java.util.Map;

public record RevenueReportResponse(
        List<Map<String, Object>> data,
        Map<String, Object> total
) {
}
