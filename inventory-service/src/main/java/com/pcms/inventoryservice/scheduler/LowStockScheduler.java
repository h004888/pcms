package com.pcms.inventoryservice.scheduler;

import com.pcms.inventoryservice.client.NotificationClient;
import com.pcms.inventoryservice.client.BranchClient;
import com.pcms.inventoryservice.client.CatalogClient;
import com.pcms.inventoryservice.entity.InventoryBatch;
import com.pcms.inventoryservice.entity.OutboxLog;
import com.pcms.inventoryservice.repository.InventoryBatchRepository;
import com.pcms.inventoryservice.repository.OutboxLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
public class LowStockScheduler {
    private static final Logger log = LoggerFactory.getLogger(LowStockScheduler.class);
    private static final String EVENT_TYPE = "LOW_STOCK_NOTIFICATION";

    private final InventoryBatchRepository batchRepository;
    private final OutboxLogRepository outboxLogRepository;
    private final NotificationClient notificationClient;
    private final BranchClient branchClient;
    private final CatalogClient catalogClient;

    public LowStockScheduler(InventoryBatchRepository batchRepository,
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

    @Scheduled(fixedRateString = "${inventory.low-stock-scan-ms:300000}")
    @Transactional
    public void publishLowStockAlerts() {
        var lowStockBatches = batchRepository.findLowStockBatches();
        if (lowStockBatches.isEmpty()) {
            return;
        }
        log.info("Found {} low-stock batches", lowStockBatches.size());
        for (InventoryBatch batch : lowStockBatches) {
            publishBatchAlert(batch);
        }
    }

    private void publishBatchAlert(InventoryBatch batch) {
        UUID eventId = buildDailyEventId(batch);
        if (outboxLogRepository.existsByEventId(eventId)) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "branchId", batch.getBranchId(),
                "medicineId", batch.getMedicineId(),
                "medicineName", resolveMedicineName(batch),
                "qtyOnHand", batch.getQtyOnHand(),
                "minQty", batch.getMinStockLevel(),
                "recipientId", resolveRecipientId(batch));
        notificationClient.lowStock(eventId.toString(), payload);
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
            log.warn("Failed to resolve branch manager for branch {}: {}", batch.getBranchId(), ex.getMessage());
        }
        return batch.getBranchId();
    }

    private String resolveMedicineName(InventoryBatch batch) {
        try {
            Map<String, Object> medicine = catalogClient.getMedicineById(batch.getMedicineId());
            Object medicineName = medicine.get("name");
            if (medicineName instanceof String name && !name.isBlank()) {
                return name;
            }
        } catch (Exception ex) {
            log.warn("Failed to resolve medicine name for medicine {}: {}", batch.getMedicineId(), ex.getMessage());
        }
        return String.valueOf(batch.getMedicineId());
    }

    private UUID buildDailyEventId(InventoryBatch batch) {
        String key = EVENT_TYPE + ":" + batch.getId() + ":" + LocalDate.now();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}