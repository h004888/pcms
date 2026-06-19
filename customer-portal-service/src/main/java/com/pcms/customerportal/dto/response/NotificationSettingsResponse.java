package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.CustomerNotificationSetting;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationSettingsResponse(
        UUID id,
        UUID customerId,
        Boolean pushEnabled,
        Boolean emailEnabled,
        Boolean smsEnabled,
        Boolean marketingEnabled,
        Boolean orderUpdates,
        Boolean lowStockAlert,
        Boolean expiryAlert,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationSettingsResponse from(CustomerNotificationSetting s) {
        return new NotificationSettingsResponse(
                s.getId(), s.getCustomerId(),
                s.getPushEnabled(), s.getEmailEnabled(), s.getSmsEnabled(),
                s.getMarketingEnabled(), s.getOrderUpdates(),
                s.getLowStockAlert(), s.getExpiryAlert(),
                s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}
