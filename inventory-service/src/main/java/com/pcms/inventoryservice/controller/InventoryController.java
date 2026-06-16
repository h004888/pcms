package com.pcms.inventoryservice.controller;

import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.request.CreateBatchRequest;
import com.pcms.inventoryservice.dto.request.ExportBatchRequest;
import com.pcms.inventoryservice.dto.request.TransferBatchRequest;
import com.pcms.inventoryservice.dto.response.BatchResponse;
import com.pcms.inventoryservice.dto.response.StockOperationResult;
import com.pcms.inventoryservice.dto.response.TransactionResponse;
import com.pcms.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * UC05 - Manage Inventory (Import, Export, Transfer, List)
 * Implements FIFO batch picking (NSF-05)
 * All business validation is delegated to the service which throws BusinessException
 * subtypes; errors are mapped by {@code com.pcms.common.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/v1/inventory - List stock per branch */
    @GetMapping
    public List<BatchResponse> list(@RequestParam(required = false) UUID branchId) {
        return inventoryService.listBatches(branchId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getBatchById(id));
    }

    @GetMapping("/branch/{branchId}/medicine/{medicineId}")
    public List<BatchResponse> getByBranchAndMedicine(@PathVariable UUID branchId,
                                                      @PathVariable UUID medicineId) {
        return inventoryService.getBatchesByBranchAndMedicine(branchId, medicineId);
    }

    /**
     * POST /api/v1/inventory/import - UC05 main flow Import
     * AT4: validate qty > 0, batch unique per medicine, expiry in future
     */
    @PostMapping("/import")
    public ResponseEntity<BatchResponse> importStock(@Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.ok(inventoryService.importStock(request));
    }

    /**
     * POST /api/v1/inventory/export - UC05 AT1
     * Decrement stock FIFO by batch
     */
    @PostMapping("/export")
    public ResponseEntity<StockOperationResult> exportStock(@Valid @RequestBody ExportBatchRequest request) {
        return ResponseEntity.ok(inventoryService.exportStock(request));
    }

    /**
     * POST /api/v1/inventory/consume - Called by order-service when order paid
     * Implements NSF-05: FIFO batch consumption
     */
    @PostMapping("/consume")
    public ResponseEntity<StockOperationResult> consumeStock(@Valid @RequestBody ConsumeBatchRequest request) {
        return ResponseEntity.ok(inventoryService.consumeStock(request));
    }

    /**
     * POST /api/v1/inventory/transfer - UC05 AT2
     * Atomic: TRANSFER_OUT on source + TRANSFER_IN on destination.
     */
    @PostMapping("/transfer")
    public ResponseEntity<StockOperationResult> transfer(@Valid @RequestBody TransferBatchRequest request) {
        return ResponseEntity.ok(inventoryService.transfer(request));
    }

    /** GET /api/v1/inventory/transactions?batchId=... - audit trail */
    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions(@RequestParam UUID batchId) {
        return inventoryService.getTransactions(batchId);
    }

    /** GET /api/v1/inventory/low-stock - BR02 alerts */
    @GetMapping("/low-stock")
    public List<BatchResponse> lowStock() {
        return inventoryService.lowStockAlerts();
    }

    /** GET /api/v1/inventory/expiring - BR03 alerts */
    @GetMapping("/expiring")
    public List<BatchResponse> expiring(@RequestParam(defaultValue = "30") int days) {
        return inventoryService.expiringAlerts(days);
    }
}
