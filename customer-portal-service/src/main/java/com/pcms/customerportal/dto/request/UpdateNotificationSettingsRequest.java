package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.AssertTrue;

import java.util.UUID;

/**
 * Body of PUT /notif-settings (full replace).
 * FR14.26.
 */
public record UpdateNotificationSettingsRequest(
        Boolean pushEnabled,
        Boolean emailEnabled,
        Boolean smsEnabled,
        Boolean marketingEnabled,
        Boolean orderUpdates,
        Boolean lowStockAlert,
        Boolean expiryAlert
) {
    @AssertTrue(message = "all flags must be non-null for full update")
    public boolean isComplete() {
        return pushEnabled != null && emailEnabled != null
            && smsEnabled != null && marketingEnabled != null
            && orderUpdates != null && lowStockAlert != null
            && expiryAlert != null;
    }
}
