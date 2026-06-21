package com.pcms.inventoryservice.service;

import com.pcms.inventoryservice.dto.BulkConsumeRequest;
import com.pcms.inventoryservice.dto.BulkRestoreRequest;
import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.request.BulkImportBatchItemRequest;
import com.pcms.inventoryservice.dto.request.CreateBatchRequest;
import com.pcms.inventoryservice.dto.request.ExportBatchRequest;
import com.pcms.inventoryservice.dto.request.TransferBatchRequest;
import com.pcms.inventoryservice.dto.response.BatchResponse;
import com.pcms.inventoryservice.dto.response.BulkImportResultResponse;
import com.pcms.inventoryservice.dto.response.StockOperationResult;
import com.pcms.inventoryservice.dto.response.TransactionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface InventoryService {

    /** Import stock - creates new batch + IMPORT transaction. */
    BatchResponse importStock(CreateBatchRequest request);

    /** Export stock - decrements FIFO + EXPORT transaction. */
    StockOperationResult exportStock(ExportBatchRequest request);

    /**
     * Consume stock (called by order-service on order paid) - FIFO + SALE
     * transaction.
     */
    StockOperationResult consumeStock(ConsumeBatchRequest request);

    /** Restore previously consumed order stock. */
    StockOperationResult restoreStock(ConsumeBatchRequest request);

    /** Bulk consume stock across multiple line items for a saga step. */
    List<StockOperationResult> bulkConsumeStock(BulkConsumeRequest request);

    /** Precise restore: look up all SALE transactions for an order and reverse them. */
    List<StockOperationResult> restoreStockByOrder(UUID orderId);

    /** Bulk restore using provided ConsumeBatchRequest list. */
    List<StockOperationResult> bulkRestoreStock(BulkRestoreRequest request);

    /**
     * Transfer stock atomically - TRANSFER_OUT source + TRANSFER_IN destination.
     */
    StockOperationResult transfer(TransferBatchRequest request);

    /** List all batches (optionally filtered by branch). */
    List<BatchResponse> listBatches(UUID branchId);

    BatchResponse getBatchById(UUID id);

    List<BatchResponse> getBatchesByBranchAndMedicine(UUID branchId, UUID medicineId);

    BatchResponse scanByCode(String code);

    BulkImportResultResponse bulkImport(List<BulkImportBatchItemRequest> items);

    BulkImportResultResponse bulkImportCsv(MultipartFile file, UUID actorId);

    String bulkExportCsv(UUID branchId);

    List<TransactionResponse> getTransactions(UUID batchId);

    /** BR02 - low stock alerts. */
    List<BatchResponse> lowStockAlerts();

    /** BR03 - expiring alerts within N days. */
    List<BatchResponse> expiringAlerts(int days);

    List<Map<String, Object>> stockLevelReport(UUID branchId);

    List<Map<String, Object>> movementReport(UUID branchId);

    List<Map<String, Object>> movementReport(UUID branchId, String type, LocalDate fromDate, LocalDate toDate);
}
