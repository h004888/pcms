package com.pcms.inventoryservice.service.impl;

import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InsufficientStockException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.inventoryservice.client.BranchClient;
import com.pcms.inventoryservice.client.CatalogClient;
import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.request.BulkImportBatchItemRequest;
import com.pcms.inventoryservice.dto.request.CreateBatchRequest;
import com.pcms.inventoryservice.dto.request.ExportBatchRequest;
import com.pcms.inventoryservice.dto.request.TransferBatchRequest;
import com.pcms.inventoryservice.dto.response.BatchResponse;
import com.pcms.inventoryservice.dto.response.BulkImportResultResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
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
        batch.setBarcode(resolveBarcode(request.barcode(), request.branchId(), request.batchNo()));
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

    @Override
    @Transactional
    public StockOperationResult restoreStock(ConsumeBatchRequest request) {
        if (request.qty() == null || request.qty() <= 0) {
            throw new InvalidOperationException(
                    "Quantity must be greater than 0",
                    "Số lượng phải lớn hơn 0");
        }
        List<InventoryBatch> batches = batchRepository.findByMedicineIdAndBranchId(
                request.medicineId(), request.branchId());
        InventoryBatch targetBatch = batches.stream()
                .filter(batch -> batch.getExpiryDate().isAfter(LocalDate.now()))
                .sorted(java.util.Comparator.comparing(InventoryBatch::getExpiryDate))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Inventory batch", request.medicineId()));

        targetBatch.setQtyOnHand(targetBatch.getQtyOnHand() + request.qty());
        batchRepository.save(targetBatch);

        InventoryTransaction txn = new InventoryTransaction(
                targetBatch.getId(), TransactionType.SALE_RESTORE, request.qty(), request.actorId());
        txn.setReason("Restore stock for cancelled paid order");
        txn.setRefId(request.orderId());
        transactionRepository.save(txn);

        return StockOperationResult.of("Stock restored successfully", request.qty());
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
            if (remaining <= 0)
                break;
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
            if (remaining <= 0)
                break;
            int take = Math.min(remaining, batch.getQtyOnHand());
            batch.setQtyOnHand(batch.getQtyOnHand() - take);
            batchRepository.save(batch);
            InventoryTransaction txn = new InventoryTransaction(batch.getId(),
                    TransactionType.TRANSFER_OUT, -take, request.actorId());
            txn.setReason(request.reason());
            txn.setRefId(request.toBranchId());
            transactionRepository.save(txn);
            if (firstSourceBatch == null)
                firstSourceBatch = batch;
            remaining -= take;
        }
        if (remaining > 0) {
            int available = request.qty() - remaining;
            throw new InsufficientStockException(request.medicineId().toString(), request.qty(), available);
        }

        // TRANSFER_IN: credit destination branch with a new batch carrying the same
        // batchNo/expiry
        if (firstSourceBatch != null) {
            InventoryBatch destBatch = new InventoryBatch(
                    request.medicineId(),
                    request.toBranchId(),
                    firstSourceBatch.getBatchNo(),
                    request.qty(),
                    firstSourceBatch.getExpiryDate());
            destBatch.setBarcode(resolveBarcode(null, request.toBranchId(), firstSourceBatch.getBatchNo()));
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
    public BatchResponse scanByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidOperationException(
                    "Scan code is required",
                    "Mã quét không được để trống");
        }
        List<InventoryBatch> batches = batchRepository.findAll();
        return batches.stream()
                .filter(batch -> batch.getBatchNo().equalsIgnoreCase(code)
                        || batch.getBarcode().equalsIgnoreCase(code)
                        || batch.getId().toString().equalsIgnoreCase(code))
                .findFirst()
                .map(BatchResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory batch", code));
    }

    @Override
    public BulkImportResultResponse bulkImport(List<BulkImportBatchItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOperationException(
                    "Bulk import items are required",
                    "Danh sách nhập kho hàng loạt không được để trống");
        }
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        for (int index = 0; index < items.size(); index++) {
            BulkImportBatchItemRequest item = items.get(index);
            try {
                importStock(new CreateBatchRequest(
                        item.medicineId(),
                        item.branchId(),
                        item.batchNo(),
                        item.barcode(),
                        item.qty(),
                        item.expiryDate(),
                        item.actorId(),
                        item.supplierId(),
                        item.minStockLevel()));
                successCount++;
            } catch (RuntimeException ex) {
                errors.add("Dòng " + (index + 1) + ": " + ex.getMessage());
            }
        }
        return new BulkImportResultResponse(items.size(), successCount, items.size() - successCount, errors);
    }

    @Override
    public BulkImportResultResponse bulkImportCsv(MultipartFile file, UUID actorId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException(
                    "CSV file is required",
                    "File CSV nhập kho là bắt buộc");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".csv")) {
            throw new InvalidOperationException(
                    "Only CSV file is supported",
                    "Chỉ hỗ trợ file CSV");
        }
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<String> lines = content.lines().filter(line -> !line.isBlank()).toList();
            if (lines.size() <= 1) {
                throw new InvalidOperationException(
                        "CSV file has no data rows",
                        "File CSV không có dữ liệu");
            }
            List<BulkImportBatchItemRequest> items = new ArrayList<>();
            for (int index = 1; index < lines.size(); index++) {
                items.add(parseBulkCsvLine(lines.get(index), index + 1, actorId));
            }
            return bulkImport(items);
        } catch (IOException exception) {
            throw new InvalidOperationException(
                    "Cannot read CSV file",
                    "Không thể đọc file CSV");
        }
    }

    private BulkImportBatchItemRequest parseBulkCsvLine(String line, int lineNumber, UUID actorId) {
        List<String> columns = Arrays.stream(line.split(",", -1))
                .map(String::trim)
                .toList();
        if (columns.size() < 6) {
            throw new InvalidOperationException(
                    "Invalid CSV row at line " + lineNumber,
                    "Dòng CSV không hợp lệ tại dòng " + lineNumber);
        }
        try {
            return new BulkImportBatchItemRequest(
                    UUID.fromString(columns.get(0)),
                    UUID.fromString(columns.get(1)),
                    columns.get(2),
                    columns.size() > 3 ? columns.get(3) : null,
                    Integer.parseInt(columns.get(4)),
                    LocalDate.parse(columns.get(5)),
                    parseInteger(columns.size() > 6 ? columns.get(6) : null),
                    parseUuid(columns.size() > 7 ? columns.get(7) : null),
                    actorId);
        } catch (RuntimeException exception) {
            throw new InvalidOperationException(
                    "Invalid CSV row at line " + lineNumber + ": " + exception.getMessage(),
                    "Dữ liệu CSV không hợp lệ tại dòng " + lineNumber);
        }
    }

    private Integer parseInteger(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    private UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    @Override
    @Transactional(readOnly = true)
    public String bulkExportCsv(UUID branchId) {
        List<BatchResponse> batches = listBatches(branchId);
        StringBuilder csv = new StringBuilder(
                "batchId,medicineId,branchId,batchNo,barcode,qtyOnHand,expiryDate,minStockLevel\n");
        batches.forEach(batch -> csv.append(batch.id()).append(',')
                .append(batch.medicineId()).append(',')
                .append(batch.branchId()).append(',')
                .append(escapeCsv(batch.batchNo())).append(',')
                .append(escapeCsv(batch.barcode())).append(',')
                .append(batch.qtyOnHand()).append(',')
                .append(batch.expiryDate()).append(',')
                .append(batch.minStockLevel()).append('\n'));
        return csv.toString();
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

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> stockLevelReport(UUID branchId) {
        List<InventoryBatch> batches = branchId != null
                ? batchRepository.findByBranchId(branchId)
                : batchRepository.findAll();
        return batches.stream()
                .map(batch -> {
                    Map<String, Object> medicine = safeGetMedicine(batch.getMedicineId());
                    Map<String, Object> branch = safeGetBranch(batch.getBranchId());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("batchId", batch.getId());
                    row.put("medicineId", batch.getMedicineId());
                    row.put("medicineName", medicine.getOrDefault("name", "Unknown Medicine"));
                    row.put("branchId", batch.getBranchId());
                    row.put("branchName", branch.getOrDefault("name", "Unknown Branch"));
                    row.put("batchNo", batch.getBatchNo());
                    row.put("barcode", batch.getBarcode());
                    row.put("qtyOnHand", batch.getQtyOnHand());
                    row.put("minStockLevel", batch.getMinStockLevel());
                    row.put("expiryDate", batch.getExpiryDate());
                    row.put("status", batch.getQtyOnHand() < batch.getMinStockLevel() ? "LOW" : "OK");
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> movementReport(UUID branchId) {
        return movementReport(branchId, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> movementReport(UUID branchId, String type, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new InvalidOperationException(
                    "fromDate must be before toDate",
                    "Ngày bắt đầu phải trước ngày kết thúc");
        }
        List<InventoryTransaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .filter(transaction -> matchesBranch(branchId, transaction))
                .filter(transaction -> matchesType(type, transaction))
                .filter(transaction -> matchesDateRange(fromDate, toDate, transaction))
                .map(this::toMovementRow)
                .toList();
    }

    private Map<String, Object> toMovementRow(InventoryTransaction transaction) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("transactionId", transaction.getId());
        row.put("batchId", transaction.getBatchId());
        row.put("type", transaction.getType().name());
        row.put("qty", transaction.getQty());
        row.put("reason", transaction.getReason());
        row.put("refId", transaction.getRefId());
        row.put("actorId", transaction.getActorId());
        row.put("quantityDirection", transaction.getQty() >= 0 ? "IN" : "OUT");
        row.put("createdAt", transaction.getCreatedAt());
        batchRepository.findById(transaction.getBatchId()).ifPresent(batch -> {
            Map<String, Object> medicine = safeGetMedicine(batch.getMedicineId());
            Map<String, Object> branch = safeGetBranch(batch.getBranchId());
            row.put("medicineId", batch.getMedicineId());
            row.put("medicineName", medicine.getOrDefault("name", "Unknown Medicine"));
            row.put("branchId", batch.getBranchId());
            row.put("branchName", branch.getOrDefault("name", "Unknown Branch"));
            row.put("batchNo", batch.getBatchNo());
            row.put("barcode", batch.getBarcode());
            row.put("expiryDate", batch.getExpiryDate());
        });
        return row;
    }

    private boolean matchesBranch(UUID branchId, InventoryTransaction transaction) {
        if (branchId == null) {
            return true;
        }
        return batchRepository.findById(transaction.getBatchId())
                .map(batch -> branchId.equals(batch.getBranchId()))
                .orElse(false);
    }

    private boolean matchesType(String type, InventoryTransaction transaction) {
        if (type == null || type.isBlank()) {
            return true;
        }
        return transaction.getType().name().equalsIgnoreCase(type);
    }

    private boolean matchesDateRange(LocalDate fromDate, LocalDate toDate, InventoryTransaction transaction) {
        LocalDateTime createdAt = transaction.getCreatedAt();
        if (createdAt == null) {
            return fromDate == null && toDate == null;
        }
        LocalDate date = createdAt.toLocalDate();
        if (fromDate != null && date.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && date.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String resolveBarcode(String barcode, UUID branchId, String batchNo) {
        String resolved = barcode == null || barcode.isBlank()
                ? branchId + "-" + batchNo
                : barcode.trim();
        batchRepository.findByBarcodeIgnoreCase(resolved).ifPresent(existing -> {
            throw new DuplicateResourceException("Barcode", resolved);
        });
        return resolved;
    }

    private Map<String, Object> safeGetMedicine(UUID medicineId) {
        try {
            return catalogClient.getMedicineById(medicineId);
        } catch (RuntimeException exception) {
            return Map.of("id", medicineId.toString(), "name", "Unknown Medicine");
        }
    }

    private Map<String, Object> safeGetBranch(UUID branchId) {
        try {
            return branchClient.getBranchById(branchId);
        } catch (RuntimeException exception) {
            return Map.of("id", branchId.toString(), "name", "Unknown Branch");
        }
    }
}
