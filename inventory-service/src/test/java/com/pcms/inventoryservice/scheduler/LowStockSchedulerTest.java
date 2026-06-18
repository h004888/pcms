package com.pcms.inventoryservice.scheduler;

import com.pcms.inventoryservice.client.BranchClient;
import com.pcms.inventoryservice.client.CatalogClient;
import com.pcms.inventoryservice.client.NotificationClient;
import com.pcms.inventoryservice.entity.InventoryBatch;
import com.pcms.inventoryservice.repository.InventoryBatchRepository;
import com.pcms.inventoryservice.repository.OutboxLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

class LowStockSchedulerTest {

        @Test
        void shouldPublishLowStockAlertsForDetectedBatches() {
                InventoryBatchRepository batchRepository = Mockito.mock(InventoryBatchRepository.class);
                OutboxLogRepository outboxLogRepository = Mockito.mock(OutboxLogRepository.class);
                NotificationClient notificationClient = Mockito.mock(NotificationClient.class);
                BranchClient branchClient = Mockito.mock(BranchClient.class);
                CatalogClient catalogClient = Mockito.mock(CatalogClient.class);

                InventoryBatch batch = new InventoryBatch(UUID.randomUUID(), UUID.randomUUID(), "B001", 2,
                                LocalDate.now().plusDays(90));
                batch.setId(UUID.randomUUID());
                batch.setMinStockLevel(10);

                Mockito.when(batchRepository.findLowStockBatches()).thenReturn(List.of(batch));
                Mockito.when(outboxLogRepository.existsByEventId(any())).thenReturn(false);
                Mockito.when(branchClient.getBranchById(batch.getBranchId()))
                                .thenReturn(Map.of("managerId", UUID.randomUUID().toString()));
                Mockito.when(catalogClient.getMedicineById(batch.getMedicineId()))
                                .thenReturn(Map.of("name", "Paracetamol"));

                LowStockScheduler scheduler = new LowStockScheduler(batchRepository, outboxLogRepository,
                                notificationClient,
                                branchClient, catalogClient);
                scheduler.publishLowStockAlerts();

                Mockito.verify(notificationClient, times(1)).lowStock(anyString(), any());
                Mockito.verify(outboxLogRepository, times(1)).save(any());
        }

        @Test
        void shouldSkipDuplicateLowStockEvent() {
                InventoryBatchRepository batchRepository = Mockito.mock(InventoryBatchRepository.class);
                OutboxLogRepository outboxLogRepository = Mockito.mock(OutboxLogRepository.class);
                NotificationClient notificationClient = Mockito.mock(NotificationClient.class);
                BranchClient branchClient = Mockito.mock(BranchClient.class);
                CatalogClient catalogClient = Mockito.mock(CatalogClient.class);

                InventoryBatch batch = new InventoryBatch(UUID.randomUUID(), UUID.randomUUID(), "B001", 2,
                                LocalDate.now().plusDays(90));
                batch.setId(UUID.randomUUID());
                batch.setMinStockLevel(10);

                Mockito.when(batchRepository.findLowStockBatches()).thenReturn(List.of(batch));
                Mockito.when(outboxLogRepository.existsByEventId(any())).thenReturn(true);

                LowStockScheduler scheduler = new LowStockScheduler(batchRepository, outboxLogRepository,
                                notificationClient,
                                branchClient, catalogClient);
                scheduler.publishLowStockAlerts();

                Mockito.verify(notificationClient, times(0)).lowStock(anyString(), any());
        }
}