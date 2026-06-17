package com.pcms.notificationservice.controller;

import com.pcms.notificationservice.dto.CreateNotificationRequest;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.service.NotificationSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * B6 + B7: Outbox event consumers.
 * Called by order-service (order.paid) and inventory-service (low-stock).
 */
@RestController
@RequestMapping("/notifications")
public class OutboxConsumerController {

    private static final Logger log = LoggerFactory.getLogger(OutboxConsumerController.class);

    private final NotificationSenderService senderService;

    public OutboxConsumerController(NotificationSenderService senderService) {
        this.senderService = senderService;
    }

    /**
     * B6: POST /notifications/orders/paid
     * Triggered by order-service outbox when an order status transitions to PAID.
     * Expected body: { "customerId", "orderId", "orderNumber", "total" }
     */
    @PostMapping("/orders/paid")
    public ResponseEntity<Map<String, Object>> orderPaid(@RequestBody Map<String, Object> event) {
        log.info("Received order.paid event: {}", event);
        String customerIdStr = String.valueOf(event.getOrDefault("customerId", ""));
        String orderNumber   = String.valueOf(event.getOrDefault("orderNumber", "N/A"));
        String total         = String.valueOf(event.getOrDefault("total", "0"));

        if (customerIdStr.isBlank() || customerIdStr.equals("null")) {
            log.warn("order.paid event missing customerId, skipping");
            return ResponseEntity.ok(Map.of("status", "skipped", "reason", "no customerId"));
        }

        UUID customerId = UUID.fromString(customerIdStr);
        CreateNotificationRequest req = new CreateNotificationRequest(
                customerId,
                NotificationChannel.IN_APP,
                "NTPL-ORDER-PAID",
                "Đơn hàng " + orderNumber + " đã thanh toán",
                "Đơn hàng " + orderNumber + " tổng " + total + " VND đã được xác nhận thanh toán thành công.",
                null
        );
        senderService.createAndSend(req);
        return ResponseEntity.ok(Map.of("status", "queued", "orderId", event.get("orderId")));
    }

    /**
     * B7: POST /notifications/inventory/low-stock
     * Triggered by inventory-service when stock falls below min_stock_level.
     * Expected body: { "medicineId", "medicineName", "branchId", "qtyOnHand", "minStockLevel", "managerId" }
     */
    @PostMapping("/inventory/low-stock")
    public ResponseEntity<Map<String, Object>> lowStock(@RequestBody Map<String, Object> event) {
        log.info("Received inventory.low-stock event: {}", event);
        String managerIdStr  = String.valueOf(event.getOrDefault("managerId", ""));
        String medicineName  = String.valueOf(event.getOrDefault("medicineName", "Unknown"));
        String qtyOnHand     = String.valueOf(event.getOrDefault("qtyOnHand", "0"));
        String minLevel      = String.valueOf(event.getOrDefault("minStockLevel", "10"));

        if (managerIdStr.isBlank() || managerIdStr.equals("null")) {
            log.warn("low-stock event missing managerId, skipping");
            return ResponseEntity.ok(Map.of("status", "skipped", "reason", "no managerId"));
        }

        UUID managerId = UUID.fromString(managerIdStr);
        CreateNotificationRequest req = new CreateNotificationRequest(
                managerId,
                NotificationChannel.IN_APP,
                "NTPL-LOW-STOCK",
                "Cảnh báo tồn kho thấp: " + medicineName,
                "Thuốc " + medicineName + " còn " + qtyOnHand + " đơn vị (mức tối thiểu: " + minLevel + ").",
                null
        );
        senderService.createAndSend(req);
        return ResponseEntity.ok(Map.of("status", "queued", "medicineId", event.get("medicineId")));
    }
}
