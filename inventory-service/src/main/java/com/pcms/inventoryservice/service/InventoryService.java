package com.pcms.inventoryservice.service;

import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.request.CreateBatchRequest;
import com.pcms.inventoryservice.dto.request.ExportBatchRequest;
import com.pcms.inventoryservice.dto.request.TransferBatchRequest;
import com.pcms.inventoryservice.dto.response.BatchResponse;
import com.pcms.inventoryservice.dto.response.StockOperationResult;
import com.pcms.inventoryservice.dto.response.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    /** Import stock - creates new batch + IMPORT transaction. */
    BatchResponse importStock(CreateBatchRequest request);

    /** Export stock - decrements FIFO + EXPORT transaction. */
    StockOperationResult exportStock(ExportBatchRequest request);

    /** Consume stock (called by order-service on order paid) - FIFO + SALE transaction. */
    StockOperationResult consumeStock(ConsumeBatchRequest request);

    /** Transfer stock atomically - TRANSFER_OUT source + TRANSFER_IN destination. */
    StockOperationResult transfer(TransferBatchRequest request);

    /** List all batches (optionally filtered by branch). */
    List<BatchResponse> listBatches(UUID branchId);

    BatchResponse getBatchById(UUID id);

    List<BatchResponse> getBatchesByBranchAndMedicine(UUID branchId, UUID medicineId);

    List<TransactionResponse> getTransactions(UUID batchId);

    /** BR02 - low stock alerts. */
    List<BatchResponse> lowStockAlerts();

    /** BR03 - expiring alerts within N days. */
    List<BatchResponse> expiringAlerts(int days);
}
