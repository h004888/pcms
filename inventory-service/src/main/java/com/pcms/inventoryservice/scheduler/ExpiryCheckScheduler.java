package com.pcms.inventoryservice.scheduler;

import com.pcms.inventoryservice.entity.InventoryBatch;
import com.pcms.inventoryservice.repository.InventoryBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * NSF-03: Batch expiry check - runs daily at 00:00 (BR03)
 * Alerts items expiring <= 30 days
 */
@Component
public class ExpiryCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckScheduler.class);

    @Autowired
    private InventoryBatchRepository batchRepository;

    @Value("${inventory.expiry-alert-days:30}")
    private int expiryAlertDays;

    @Scheduled(cron = "0 0 0 * * *")  // Daily at 00:00
    public void checkExpiringBatches() {
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(expiryAlertDays);
        List<InventoryBatch> expiring = batchRepository.findExpiringBatches(today, alertDate);
        if (!expiring.isEmpty()) {
            log.warn("NSF-03: Found {} batches expiring within {} days", expiring.size(), expiryAlertDays);
            for (InventoryBatch batch : expiring) {
                log.warn("  - Batch {} (medicine={}, branch={}) expires on {}",
                    batch.getBatchNo(), batch.getMedicineId(), batch.getBranchId(), batch.getExpiryDate());
            }
            // TODO: emit notification via notification-service (UC13)
        }
    }
}
