package com.pcms.customerportal.dto.request;

/**
 * Body of PATCH /notif-settings (partial update - only set fields are applied).
 * FR14.26.
 */
public record UpdateNotificationSettingsPatchRequest(
        Boolean pushEnabled,
        Boolean emailEnabled,
        Boolean smsEnabled,
        Boolean marketingEnabled,
        Boolean orderUpdates,
        Boolean lowStockAlert,
        Boolean expiryAlert
) {}
