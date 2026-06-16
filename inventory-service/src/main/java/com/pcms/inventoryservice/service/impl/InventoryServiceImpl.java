package com.pcms.inventoryservice.service.impl;

import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InsufficientStockException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.inventoryservice.client.BranchClient;
import com.pcms.inventoryservice.client.CatalogClient;
import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.request.CreateBatchRequest;
import com.pcms.inventoryservice.dto.request.ExportBatchRequest;
import com.pcms.inventoryservice.dto.request.TransferBatchRequest;
import com.pcms.inventoryservice.dto.response.BatchResponse;
import com.pcms.inventoryservice.dto.response.StockOperationResult;
import com.pcms.inventoryservice.dto.response.TransactionResponse;
import com.pcms.inventoryservice.entity.InventoryBatch;
import com.pcms.inventoryservice.entity.InventoryTransaction;
import com.pcms.inventoryservice.enums.TransactionType;
import com.pcms.inventoryservice.repository.InventoryBatchRepository;
import com.pcms.inventoryservice.repository.InventoryTransactionRepository;
import com.pcms.inventoryservice.service.InventoryService;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final CatalogClient catalogClient;
    private final BranchClient branchClient;

    public InventoryServiceImpl(InventoryBatchRepository batchRepository,
                                InventoryTransactionRepository transactionRepository,
                                CatalogClient catalogClient,
                                BranchClient branchClient) {
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.catalogClient = catalogClient;
        this.branchClient = branchClient;
    }

    @Override
    @Transactional
    public BatchResponse importStock(CreateBatchRequest request) {
        if (request.qty() == null || request.qty() <= 0) {
            throw new InvalidOperationException(
                    "Quantity must be greater than 0",
                    "Số lượng phải lớn hơn 0");
        }
        if (!request.expiryDate().isAfter(LocalDate.now())) {
            throw new InvalidOperationException(
                    "Expiry date must be in the future",
                    "Ngày hết hạn phải lớn hơn hôm nay");
        }
        // B-06: Validate medicine exists via catalog-service
        try {
            var medicine = catalogClient.getMedicineById(request.medicineId());
            if (medicine == null || medicine.get("status") == null
                    || "UNREACHABLE".equals(medicine.get("status"))) {
                throw new ResourceNotFoundException("Medicine", request.medicineId());
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Medicine", request.medicineId());
        }
        // B-06: Validate branch exists via branch-service
        try {
            var branch = branchClient.getBranchById(request.branchId());
            if (branch == null || branch.get("status") == null
                    || "UNREACHABLE".equals(branch.get("status"))) {
                throw new ResourceNotFoundException("Branch", request.branchId());
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Branch", request.branchId());
        }
        if (batchRepository.findByMedicineIdAndBranchIdAndBatchNo(
                request.medicineId(), request.branchId(), request.batchNo()).isPresent()) {
            throw new DuplicateResourceException("Batch number", request.batchNo());
        }

        InventoryBatch batch = new InventoryBatch(
                request.medicineId(), request.branchId(),
                request.batchNo(), request.qty(), request.expiryDate());
        if (request.minStockLevel() != null) {
            batch.setMinStockLevel(request.minStockLevel());
        }
        InventoryBatch saved = batchRepository.save(batch);

        InventoryTransaction txn = new InventoryTransaction(
                saved.getId(), TransactionType.IMPORT, request.qty(), request.actorId());
        if (request.supplierId() != null) {
            txn.setRefId(request.supplierId());
        }
        transactionRepository.save(txn);

        if (saved.getQtyOnHand() < saved.getMinStockLevel()) {
            // TODO: emit notification via notification-service (UC13)
        }

        return BatchResponse.from(saved);
    }

    @Override
    @Transactional
    public StockOperationResult exportStock(ExportBatchRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            throw new InvalidOperationException(
                    "Reason is required for export",
                    "Lý do xuất kho là bắt buộc");
        }
        return consumeStockInternal(request.medicineId(), request.branchId(), request.qty(),
                request.actorId(), TransactionType.EXPORT, request.reason(), null,
                "Stock exported successfully");
    }

    @Override
    @Transactional
    public StockOperationResult consumeStock(ConsumeBatchRequest request) {
        return consumeStockInternal(request.medicineId(), request.branchId(), request.qty(),
                request.actorId(), TransactionType.SALE, "Order fulfillment", request.orderId(),
                "Stock consumed successfully");
    }

    /**
     * Common FIFO stock consumption logic.
     * Throws InsufficientStockException if not enough stock across batches.
     */
    private StockOperationResult consumeStockInternal(UUID medicineId, UUID branchId, int qtyNeeded,
                                                      UUID actorId, TransactionType type,
                                                      String reason, UUID refId, String successMessage) {
        if (qtyNeeded <= 0) {
            throw new InvalidOperationException(
                    "Quantity must be greater than 0",
                    "Số lượng phải lớn hơn 0");
        }
        List<InventoryBatch> batches = batchRepository.findAvailableBatchesFifo(
                medicineId, branchId, LocalDate.now());
        int remaining = qtyNeeded;
        for (InventoryBatch batch : batches) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, batch.getQtyOnHand());
            batch.setQtyOnHand(batch.getQtyOnHand() - take);
            batchRepository.save(batch);
            InventoryTransaction txn = new InventoryTransaction(batch.getId(), type, -take, actorId);
            txn.setReason(reason);
            txn.setRefId(refId);
            transactionRepository.save(txn);
            remaining -= take;
        }
        if (remaining > 0) {
            int available = qtyNeeded - remaining;
            throw new InsufficientStockException(medicineId.toString(), qtyNeeded, available);
        }
        return StockOperationResult.of(successMessage, qtyNeeded);
    }

    @Override
    @Transactional
    public StockOperationResult transfer(TransferBatchRequest request) {
        if (request.fromBranchId().equals(request.toBranchId())) {
            throw new InvalidOperationException(
                    "fromBranchId and toBranchId must differ",
                    "Chi nhánh nguồn và chi nhánh đích phải khác nhau");
        }
        if (request.qty() <= 0) {
            throw new InvalidOperationException(
                    "Quantity must be greater than 0",
                    "Số lượng phải lớn hơn 0");
        }

        // TRANSFER_OUT: pick FIFO from source branch
        List<InventoryBatch> sourceBatches = batchRepository.findAvailableBatchesFifo(
                request.medicineId(), request.fromBranchId(), LocalDate.now());
        int remaining = request.qty();
        InventoryBatch firstSourceBatch = null;
        for (InventoryBatch batch : sourceBatches) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, batch.getQtyOnHand());
            batch.setQtyOnHand(batch.getQtyOnHand() - take);
            batchRepository.save(batch);
            InventoryTransaction txn = new InventoryTransaction(batch.getId(),
                    TransactionType.TRANSFER_OUT, -take, request.actorId());
            txn.setReason(request.reason());
            txn.setRefId(request.toBranchId());
            transactionRepository.save(txn);
            if (firstSourceBatch == null) firstSourceBatch = batch;
            remaining -= take;
        }
        if (remaining > 0) {
            int available = request.qty() - remaining;
            throw new InsufficientStockException(request.medicineId().toString(), request.qty(), available);
        }

        // TRANSFER_IN: credit destination branch with a new batch carrying the same batchNo/expiry
        if (firstSourceBatch != null) {
            InventoryBatch destBatch = new InventoryBatch(
                    request.medicineId(),
                    request.toBranchId(),
                    firstSourceBatch.getBatchNo(),
                    request.qty(),
                    firstSourceBatch.getExpiryDate());
            InventoryBatch savedDest = batchRepository.save(destBatch);
            InventoryTransaction txnIn = new InventoryTransaction(savedDest.getId(),
                    TransactionType.TRANSFER_IN, request.qty(), request.actorId());
            txnIn.setReason(request.reason());
            txnIn.setRefId(request.fromBranchId());
            transactionRepository.save(txnIn);
        }

        return StockOperationResult.of("Transfer completed", request.qty());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> listBatches(UUID branchId) {
        List<InventoryBatch> batches = branchId != null
                ? batchRepository.findByBranchId(branchId)
                : batchRepository.findAll();
        return batches.stream().map(BatchResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(UUID id) {
        InventoryBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
        return BatchResponse.from(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByBranchAndMedicine(UUID branchId, UUID medicineId) {
        return batchRepository.findByMedicineIdAndBranchId(medicineId, branchId)
                .stream().map(BatchResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(UUID batchId) {
        return transactionRepository.findByBatchId(batchId)
                .stream().map(TransactionResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> lowStockAlerts() {
        return batchRepository.findLowStockBatches()
                .stream().map(BatchResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> expiringAlerts(int days) {
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(days);
        return batchRepository.findExpiringBatches(today, alertDate)
                .stream().map(BatchResponse::from).toList();
    }
}
