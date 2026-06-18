package com.pcms.notificationservice.service;

import com.pcms.notificationservice.dto.request.ExpiryAlertNotificationRequest;
import com.pcms.notificationservice.dto.request.LowStockNotificationRequest;
import com.pcms.notificationservice.dto.request.OrderPaidNotificationRequest;

import java.util.Map;
import java.util.UUID;

public interface OutboxConsumerService {

    Map<String, Object> handleOrderPaid(UUID eventId, OrderPaidNotificationRequest request);

    Map<String, Object> handleLowStock(UUID eventId, LowStockNotificationRequest request);

    Map<String, Object> handleExpiryAlert(UUID eventId, ExpiryAlertNotificationRequest request);
}