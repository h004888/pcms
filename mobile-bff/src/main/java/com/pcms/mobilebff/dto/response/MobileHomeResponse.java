package com.pcms.mobilebff.dto.response;

import java.util.List;

/**
 * Aggregated home page response for mobile app.
 * Combines notifications, recent orders, and active reminders.
 */
public record MobileHomeResponse(
        String userId,
        String role,
        List<Object> recentNotifications,
        List<Object> recentOrders,
        List<ReminderResponse> activeReminders,
        List<Object> nearbyPharmacies,
        Object aiChatContext
) {}
