package com.pcms.notificationservice.service.impl;

import com.pcms.notificationservice.dto.request.ExpiryAlertNotificationRequest;
import com.pcms.notificationservice.dto.request.CreateNotificationRequest;
import com.pcms.notificationservice.dto.request.LowStockNotificationRequest;
import com.pcms.notificationservice.dto.request.OrderPaidNotificationRequest;
import com.pcms.notificationservice.entity.OutboxLog;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.repository.OutboxLogRepository;
import com.pcms.notificationservice.service.NotificationSenderService;
import com.pcms.notificationservice.service.OutboxConsumerService;
import com.pcms.notificationservice.service.TemplateResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class OutboxConsumerServiceImpl implements OutboxConsumerService {

        private final NotificationSenderService senderService;
        private final OutboxLogRepository outboxLogRepository;
        private final TemplateResolver templateResolver;

        public OutboxConsumerServiceImpl(NotificationSenderService senderService,
                        OutboxLogRepository outboxLogRepository,
                        TemplateResolver templateResolver) {
                this.senderService = senderService;
                this.outboxLogRepository = outboxLogRepository;
                this.templateResolver = templateResolver;
        }

        @Override
        public Map<String, Object> handleOrderPaid(UUID eventId, OrderPaidNotificationRequest request) {
                UUID effectiveEventId = eventId != null ? eventId : request.orderId();
                if (effectiveEventId != null && outboxLogRepository.existsByEventId(effectiveEventId)) {
                        return Map.of("status", "duplicate", "eventId", effectiveEventId);
                }

                var variables = Map.<String, Object>of(
                                "order_id", request.orderId(),
                                "order_number", request.orderNumber(),
                                "customer_id", request.customerId(),
                                "branch_id", request.branchId() == null ? "" : request.branchId(),
                                "total", request.total(),
                                "paid_at", request.paidAt() == null ? "" : request.paidAt());
                var resolved = templateResolver.resolve("ORDER_PAID", NotificationChannel.IN_APP,
                                "Thanh toán đơn hàng thành công",
                                "Đơn hàng " + request.orderNumber() + " đã thanh toán thành công. Tổng tiền: "
                                                + request.total() + " VND.",
                                variables);
                senderService.createAndSend(new CreateNotificationRequest(
                                request.customerId(),
                                NotificationChannel.IN_APP,
                                "ORDER_PAID",
                                resolved.title(),
                                resolved.body(),
                                LocalDateTime.now()));

                if (effectiveEventId != null) {
                        outboxLogRepository.save(
                                        new OutboxLog(effectiveEventId, "ORDER_PAID", "PROCESSED", request.orderId()));
                }
                return Map.of("status", "processed", "eventId", effectiveEventId);
        }

        @Override
        public Map<String, Object> handleLowStock(UUID eventId, LowStockNotificationRequest request) {
                UUID effectiveEventId = eventId != null ? eventId : UUID.randomUUID();
                if (outboxLogRepository.existsByEventId(effectiveEventId)) {
                        return Map.of("status", "duplicate", "eventId", effectiveEventId);
                }

                UUID recipientId = request.recipientId() != null ? request.recipientId() : request.branchId();
                String medicineName = request.medicineName() != null ? request.medicineName()
                                : String.valueOf(request.medicineId());
                var variables = Map.<String, Object>of(
                                "medicine_id", request.medicineId(),
                                "medicine_name", medicineName,
                                "branch_id", request.branchId(),
                                "qty_on_hand", request.qtyOnHand(),
                                "min_qty", request.minQty());
                var resolved = templateResolver.resolve("LOW_STOCK", NotificationChannel.IN_APP,
                                "Cảnh báo tồn kho thấp",
                                "Thuốc " + medicineName + " tại chi nhánh " + request.branchId()
                                                + " chỉ còn " + request.qtyOnHand() + " (ngưỡng tối thiểu "
                                                + request.minQty() + ").",
                                variables);
                senderService.createAndSend(new CreateNotificationRequest(
                                recipientId,
                                NotificationChannel.IN_APP,
                                "LOW_STOCK",
                                resolved.title(),
                                resolved.body(),
                                LocalDateTime.now()));

                outboxLogRepository
                                .save(new OutboxLog(effectiveEventId, "LOW_STOCK", "PROCESSED", request.medicineId()));
                return Map.of("status", "processed", "eventId", effectiveEventId);
        }

        @Override
        public Map<String, Object> handleExpiryAlert(UUID eventId, ExpiryAlertNotificationRequest request) {
                UUID effectiveEventId = eventId != null ? eventId : UUID.randomUUID();
                if (outboxLogRepository.existsByEventId(effectiveEventId)) {
                        return Map.of("status", "duplicate", "eventId", effectiveEventId);
                }

                UUID recipientId = request.recipientId() != null ? request.recipientId() : request.branchId();
                String medicineName = request.medicineName() != null ? request.medicineName()
                                : String.valueOf(request.medicineId());
                var variables = Map.<String, Object>of(
                                "medicine_id", request.medicineId(),
                                "medicine_name", medicineName,
                                "branch_id", request.branchId(),
                                "batch_no", request.batchNo(),
                                "expiry_date", request.expiryDate());
                var resolved = templateResolver.resolve("EXPIRY_ALERT", NotificationChannel.IN_APP,
                                "Cảnh báo thuốc sắp hết hạn",
                                "Lô " + request.batchNo() + " của thuốc " + medicineName + " sẽ hết hạn vào "
                                                + request.expiryDate() + ".",
                                variables);
                senderService.createAndSend(new CreateNotificationRequest(
                                recipientId,
                                NotificationChannel.IN_APP,
                                "EXPIRY_ALERT",
                                resolved.title(),
                                resolved.body(),
                                LocalDateTime.now()));

                outboxLogRepository.save(
                                new OutboxLog(effectiveEventId, "EXPIRY_ALERT", "PROCESSED", request.medicineId()));
                return Map.of("status", "processed", "eventId", effectiveEventId);
        }
}