package com.pcms.inventoryservice.controller;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.request.BulkImportBatchItemRequest;
import com.pcms.inventoryservice.dto.request.CreateBatchRequest;
import com.pcms.inventoryservice.dto.request.ExportBatchRequest;
import com.pcms.inventoryservice.dto.request.TransferBatchRequest;
import com.pcms.inventoryservice.dto.response.BatchResponse;
import com.pcms.inventoryservice.dto.response.BulkImportResultResponse;
import com.pcms.inventoryservice.dto.response.StockOperationResult;
import com.pcms.inventoryservice.dto.response.TransactionResponse;
import com.pcms.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * UC05 - Manage Inventory (Import, Export, Transfer, List)
 * Implements FIFO batch picking (NSF-05)
 * All business validation is delegated to the service which throws
 * BusinessException
 * subtypes; errors are mapped by
 * {@code com.pcms.common.exception.GlobalExceptionHandler}.
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
    public List<BatchResponse> list(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return inventoryService.listBatches(resolveBranchId(branchId, currentBranchId));
    }

    /**
     * GET /api/v1/inventory/batches - SDD §6.7 alias for /inventory.
     * List stock per branch (paginated when supported).
     * Used by SCR-INV-LIST to align with SRS screen definition.
     */
    @GetMapping("/batches")
    public List<BatchResponse> listBatches(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return inventoryService.listBatches(resolveBranchId(branchId, currentBranchId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getBatchById(id));
    }

    /**
     * GET /api/v1/inventory/batches/{id} - SDD §6.7 alias for /inventory/{id}.
     * Get batch detail.
     */
    @GetMapping("/batches/{id}")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getBatchById(id));
    }

    @GetMapping("/batches/scan/{code}")
    public ResponseEntity<BatchResponse> scan(@PathVariable String code) {
        return ResponseEntity.ok(inventoryService.scanByCode(code));
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
    public ResponseEntity<BatchResponse> importStock(
            @Valid @RequestBody CreateBatchRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(inventoryService.importStock(applyActorContext(request, userId)));
    }

    /**
     * POST /api/v1/inventory/export - UC05 AT1
     * Decrement stock FIFO by batch
     */
    @PostMapping("/export")
    public ResponseEntity<StockOperationResult> exportStock(
            @Valid @RequestBody ExportBatchRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(inventoryService.exportStock(applyActorContext(request, userId)));
    }

    /**
     * POST /api/v1/inventory/consume - Called by order-service when order paid
     * Implements NSF-05: FIFO batch consumption
     */
    @PostMapping("/consume")
    public ResponseEntity<StockOperationResult> consumeStock(
            @Valid @RequestBody ConsumeBatchRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(inventoryService.consumeStock(applyActorContext(request, userId)));
    }

    /**
     * POST /api/v1/inventory/transfer - UC05 AT2
     * Atomic: TRANSFER_OUT on source + TRANSFER_IN on destination.
     */
    @PostMapping("/transfer")
    public ResponseEntity<StockOperationResult> transfer(
            @Valid @RequestBody TransferBatchRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(inventoryService.transfer(applyActorContext(request, userId)));
    }

    @PostMapping(value = "/bulk/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkImportResultResponse> bulkImport(
            @RequestBody List<@Valid BulkImportBatchItemRequest> items) {
        return ResponseEntity.ok(inventoryService.bulkImport(items));
    }

    @PostMapping(value = "/bulk/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkImportResultResponse> bulkImportMultipart(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) UUID actorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(inventoryService.bulkImportCsv(file, resolveActorId(actorId, userId)));
    }

    @PostMapping(value = "/bulk/import-file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BulkImportResultResponse> bulkImportFile(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) UUID actorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("file (multipart) is required");
        }
        return ResponseEntity.ok(inventoryService.bulkImportCsv(file, resolveActorId(actorId, userId)));
    }

    @GetMapping("/bulk/export")
    public ResponseEntity<String> bulkExport(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-batches.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(inventoryService.bulkExportCsv(resolveBranchId(branchId, currentBranchId)));
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

    /** GET /api/v1/inventory/alerts/low-stock - Alias used by SCR-INV-LIST. */
    @GetMapping("/alerts/low-stock")
    public List<BatchResponse> lowStockAlerts() {
        return inventoryService.lowStockAlerts();
    }

    /** GET /api/v1/inventory/expiring - BR03 alerts */
    @GetMapping("/expiring")
    public List<BatchResponse> expiring(@RequestParam(defaultValue = "30") int days) {
        return inventoryService.expiringAlerts(days);
    }

    /** GET /api/v1/inventory/alerts/expiry - Alias used by SCR-INV-LIST. */
    @GetMapping("/alerts/expiry")
    public List<BatchResponse> expiryAlerts(@RequestParam(defaultValue = "30") int days) {
        return inventoryService.expiringAlerts(days);
    }

    @GetMapping("/report/stock-level")
    public ResponseEntity<List<Map<String, Object>>> stockLevelReport(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(inventoryService.stockLevelReport(resolveBranchId(branchId, currentBranchId)));
    }

    @GetMapping("/report/movement")
    public ResponseEntity<List<Map<String, Object>>> movementReport(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(
                inventoryService.movementReport(resolveBranchId(branchId, currentBranchId), type, fromDate, toDate));
    }

    private CreateBatchRequest applyActorContext(CreateBatchRequest request, UUID userId) {
        if (request.actorId() != null || userId == null) {
            return request;
        }
        return new CreateBatchRequest(
                request.medicineId(),
                request.branchId(),
                request.batchNo(),
                request.barcode(),
                request.qty(),
                request.expiryDate(),
                userId,
                request.supplierId(),
                request.minStockLevel());
    }

    private ExportBatchRequest applyActorContext(ExportBatchRequest request, UUID userId) {
        if (request.actorId() != null || userId == null) {
            return request;
        }
        return new ExportBatchRequest(request.medicineId(), request.branchId(), request.qty(), request.reason(),
                userId);
    }

    private ConsumeBatchRequest applyActorContext(ConsumeBatchRequest request, UUID userId) {
        if (request.actorId() != null || userId == null) {
            return request;
        }
        return new ConsumeBatchRequest(request.medicineId(), request.branchId(), request.qty(), userId,
                request.orderId());
    }

    private TransferBatchRequest applyActorContext(TransferBatchRequest request, UUID userId) {
        if (request.actorId() != null || userId == null) {
            return request;
        }
        return new TransferBatchRequest(
                request.medicineId(),
                request.fromBranchId(),
                request.toBranchId(),
                request.qty(),
                request.reason(),
                userId);
    }

    private UUID resolveBranchId(UUID requestedBranchId, UUID currentBranchId) {
        if (requestedBranchId != null) {
            return requestedBranchId;
        }
        if (currentBranchId != null) {
            return currentBranchId;
        }
        return null;
    }

    private UUID resolveActorId(UUID actorId, UUID userId) {
        if (actorId != null) {
            return actorId;
        }
        if (userId != null) {
            return userId;
        }
        throw new InvalidOperationException(
                "Actor ID is required",
                "Thiếu thông tin người thao tác hiện tại");
    }
}
