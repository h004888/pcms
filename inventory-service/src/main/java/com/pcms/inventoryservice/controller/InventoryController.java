package com.pcms.inventoryservice.controller;

import com.pcms.inventoryservice.entity.InventoryBatch;
import com.pcms.inventoryservice.entity.InventoryTransaction;
import com.pcms.inventoryservice.enums.TransactionType;
import com.pcms.inventoryservice.repository.InventoryBatchRepository;
import com.pcms.inventoryservice.repository.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC05 - Manage Inventory (Import, Export, Transfer, List)
 * Implements FIFO batch picking (NSF-05)
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryBatchRepository batchRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    /** GET /api/v1/inventory - List stock per branch */
    @GetMapping
    public List<InventoryBatch> list(@RequestParam(required = false) UUID branchId) {
        return branchId != null ? batchRepository.findByBranchId(branchId)
                                : batchRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryBatch> getById(@PathVariable UUID id) {
        return batchRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/branch/{branchId}/medicine/{medicineId}")
    public List<InventoryBatch> getByBranchAndMedicine(@PathVariable UUID branchId, @PathVariable UUID medicineId) {
        return batchRepository.findByMedicineIdAndBranchId(medicineId, branchId);
    }

    /**
     * POST /api/v1/inventory/import - UC05 main flow Import
     * AT4: validate qty > 0, batch unique per medicine, expiry in future
     */
    @PostMapping("/import")
    @Transactional
    public ResponseEntity<?> importStock(@RequestBody ImportRequest request) {
        // AT4: Validate
        if (request.qty() == null || request.qty() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG15", "message", "Quantity must be greater than 0"));
        }
        if (request.expiryDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG17", "message", "Expiry date must be in the future"));
        }
        // Check duplicate batch
        if (batchRepository.findByMedicineIdAndBranchIdAndBatchNo(request.medicineId(), request.branchId(), request.batchNo()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG16", "message", "Batch number already exists"));
        }

        // Persist
        InventoryBatch batch = new InventoryBatch(request.medicineId(), request.branchId(), request.batchNo(), request.qty(), request.expiryDate());
        InventoryBatch saved = batchRepository.save(batch);

        InventoryTransaction txn = new InventoryTransaction(saved.getId(), TransactionType.IMPORT, request.qty(), request.actorId());
        txn.setRefId(request.poRef());
        transactionRepository.save(txn);

        // AT3: Check if resulting stock < min_level (BR02) - emit notification
        if (saved.getQtyOnHand() < saved.getMinStockLevel()) {
            // TODO: call notification-service
        }

        return ResponseEntity.ok(Map.of("code", "MSG14", "message", "Stock imported successfully", "batch", saved));
    }

    /**
     * POST /api/v1/inventory/export - UC05 AT1
     * Decrement stock FIFO by batch
     */
    @PostMapping("/export")
    @Transactional
    public ResponseEntity<?> exportStock(@RequestBody ExportRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "Reason is required for export"));
        }
        return consumeStock(request.medicineId(), request.branchId(), request.qty(), request.actorId(), TransactionType.EXPORT, request.reason(), null);
    }

    /**
     * POST /api/v1/inventory/consume - Called by order-service when order paid
     * Implements NSF-05: FIFO batch consumption
     */
    @PostMapping("/consume")
    @Transactional
    public ResponseEntity<?> consumeStock(@RequestBody ConsumeRequest request) {
        return consumeStock(request.medicineId(), request.branchId(), request.qty(), request.actorId(), TransactionType.SALE, "Order fulfillment", request.orderId());
    }

    /**
     * Common FIFO stock consumption logic
     */
    private ResponseEntity<?> consumeStock(UUID medicineId, UUID branchId, int qtyNeeded, UUID actorId, TransactionType type, String reason, UUID refId) {
        if (qtyNeeded <= 0) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG15", "message", "Quantity must be greater than 0"));
        }
        List<InventoryBatch> batches = batchRepository.findAvailableBatchesFifo(medicineId, branchId, LocalDate.now());
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
            return ResponseEntity.status(409).body(Map.of("code", "MSG20", "message", "Insufficient stock", "shortage", remaining));
        }
        return ResponseEntity.ok(Map.of("message", "Stock consumed successfully", "qty", qtyNeeded));
    }

    /** GET /api/v1/inventory/low-stock - BR02 alerts */
    @GetMapping("/low-stock")
    public List<InventoryBatch> lowStock() {
        return batchRepository.findLowStockBatches();
    }

    /** GET /api/v1/inventory/expiring - BR03 alerts */
    @GetMapping("/expiring")
    public List<InventoryBatch> expiring(@RequestParam(defaultValue = "30") int days) {
        return batchRepository.findExpiringBatches(LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public record ImportRequest(UUID medicineId, UUID branchId, String batchNo, Integer qty, LocalDate expiryDate, UUID actorId, UUID poRef) {}
    public record ExportRequest(UUID medicineId, UUID branchId, Integer qty, String reason, UUID actorId) {}
    public record ConsumeRequest(UUID medicineId, UUID branchId, Integer qty, UUID actorId, UUID orderId) {}
}
