package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.UpdateNotificationSettingsPatchRequest;
import com.pcms.customerportal.dto.request.UpdateNotificationSettingsRequest;
import com.pcms.customerportal.dto.response.NotificationSettingsResponse;

import java.util.UUID;

/**
 * TICKET-704 (Notification settings) - Cài đặt thông báo.
 * FR14.26.
 */
public interface NotificationSettingsService {

    /**
     * Lazy-creates a default row if one doesn't exist for the customer.
     * Callers can therefore always rely on a non-null response.
     */
    NotificationSettingsResponse get(UUID currentCustomerId);

    /** Full replace (all flags must be non-null). */
    NotificationSettingsResponse update(UUID currentCustomerId, UpdateNotificationSettingsRequest request);

    /** Partial update (only non-null fields are applied). */
    NotificationSettingsResponse patch(UUID currentCustomerId, UpdateNotificationSettingsPatchRequest request);
}
