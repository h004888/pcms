package com.pcms.inventoryservice.scheduler;

import com.pcms.inventoryservice.entity.InventoryBatch;
import com.pcms.inventoryservice.entity.OutboxLog;
import com.pcms.inventoryservice.repository.InventoryBatchRepository;
import com.pcms.inventoryservice.repository.OutboxLogRepository;
import com.pcms.inventoryservice.client.BranchClient;
import com.pcms.inventoryservice.client.CatalogClient;
import com.pcms.inventoryservice.client.NotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * NSF-03: Batch expiry check - runs daily at 00:00 (BR03)
 * Alerts items expiring <= 30 days
 */
@Component
public class ExpiryCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckScheduler.class);
    private static final String EVENT_TYPE = "EXPIRY_ALERT_NOTIFICATION";

    private final InventoryBatchRepository batchRepository;
    private final OutboxLogRepository outboxLogRepository;
    private final NotificationClient notificationClient;
    private final BranchClient branchClient;
    private final CatalogClient catalogClient;

    @Value("${inventory.expiry-alert-days:30}")
    private int expiryAlertDays;

    public ExpiryCheckScheduler(InventoryBatchRepository batchRepository,
            OutboxLogRepository outboxLogRepository,
            NotificationClient notificationClient,
            BranchClient branchClient,
            CatalogClient catalogClient) {
        this.batchRepository = batchRepository;
        this.outboxLogRepository = outboxLogRepository;
        this.notificationClient = notificationClient;
        this.branchClient = branchClient;
        this.catalogClient = catalogClient;
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily at 00:00
    public void checkExpiringBatches() {
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(expiryAlertDays);
        List<InventoryBatch> expiring = batchRepository.findExpiringBatches(today, alertDate);
        if (!expiring.isEmpty()) {
            log.warn("NSF-03: Found {} batches expiring within {} days", expiring.size(), expiryAlertDays);
            for (InventoryBatch batch : expiring) {
                log.warn("  - Batch {} (medicine={}, branch={}) expires on {}",
                        batch.getBatchNo(), batch.getMedicineId(), batch.getBranchId(), batch.getExpiryDate());
                publishExpiryAlert(batch);
            }
        }
    }

    private void publishExpiryAlert(InventoryBatch batch) {
        UUID eventId = buildDailyEventId(batch);
        if (outboxLogRepository.existsByEventId(eventId)) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "branchId", batch.getBranchId(),
                "medicineId", batch.getMedicineId(),
                "medicineName", resolveMedicineName(batch),
                "batchNo", batch.getBatchNo(),
                "expiryDate", batch.getExpiryDate().toString(),
                "recipientId", resolveRecipientId(batch));
        notificationClient.expiry(eventId.toString(), payload);
        outboxLogRepository.save(new OutboxLog(eventId, EVENT_TYPE, "PROCESSED", batch.getId()));
    }

    private UUID resolveRecipientId(InventoryBatch batch) {
        try {
            Map<String, Object> branch = branchClient.getBranchById(batch.getBranchId());
            Object managerId = branch.get("managerId");
            if (managerId instanceof String managerIdString && !managerIdString.isBlank()) {
                return UUID.fromString(managerIdString);
            }
        } catch (Exception ex) {
            log.warn("Failed to resolve expiry recipient for branch {}: {}", batch.getBranchId(), ex.getMessage());
        }
        return batch.getBranchId();
    }

    private UUID buildDailyEventId(InventoryBatch batch) {
        String key = EVENT_TYPE + ":" + batch.getId() + ":" + LocalDate.now();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveMedicineName(InventoryBatch batch) {
        try {
            Map<String, Object> medicine = catalogClient.getMedicineById(batch.getMedicineId());
            Object medicineName = medicine.get("name");
            if (medicineName instanceof String name && !name.isBlank()) {
                return name;
            }
        } catch (Exception ex) {
            log.warn("Failed to resolve expiry medicine name for medicine {}: {}", batch.getMedicineId(),
                    ex.getMessage());
        }
        return String.valueOf(batch.getMedicineId());
    }
}
