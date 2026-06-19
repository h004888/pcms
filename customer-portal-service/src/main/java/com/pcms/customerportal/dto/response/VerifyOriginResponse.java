package com.pcms.customerportal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for SHOP-VERIFY-ORIGIN QR scan result.
 */
public record VerifyOriginResponse(
        boolean verified,
        String warning,
        UUID medicineId,
        String medicineName,
        String batchNo,
        String manufacturer,
        LocalDate manufacturedAt,
        LocalDateTime verifiedAt,
        String status
) {}