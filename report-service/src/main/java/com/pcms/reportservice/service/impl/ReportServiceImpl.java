package com.pcms.reportservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.reportservice.client.InventoryClient;
import com.pcms.reportservice.client.OrderClient;
import com.pcms.reportservice.dto.InventoryReportRequest;
import com.pcms.reportservice.dto.InventoryReportResponse;
import com.pcms.reportservice.dto.RevenueReportRequest;
import com.pcms.reportservice.dto.RevenueReportResponse;
import com.pcms.reportservice.dto.StaffReportRequest;
import com.pcms.reportservice.dto.StaffReportResponse;
import com.pcms.reportservice.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link ReportService}.
 * <p>
 * Aggregates data from order-service and inventory-service via Feign clients.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final OrderClient orderClient;
    private final InventoryClient inventoryClient;
    private final com.pcms.reportservice.service.impl.ExcelExportService excelExportService;
    private final com.pcms.reportservice.service.impl.PdfExportService pdfExportService;

    public ReportServiceImpl(OrderClient orderClient,
                              InventoryClient inventoryClient,
                              com.pcms.reportservice.service.impl.ExcelExportService excelExportService,
                              com.pcms.reportservice.service.impl.PdfExportService pdfExportService) {
        this.orderClient = orderClient;
        this.inventoryClient = inventoryClient;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
    }

    @Override
    public RevenueReportResponse revenue(RevenueReportRequest request) {
        validateRange(request.fromDate(), request.toDate());
        return revenue(request.fromDate(), request.toDate(), request.branchId(),
                request.groupBy() == null ? "day" : request.groupBy().name().toLowerCase());
    }

    @Override
    public RevenueReportResponse revenue(LocalDate from, LocalDate to, UUID branchId, String groupBy) {
        validateRange(from, to);
        Map<String, Object> ordersResp = orderClient.getOrders(null, 0, 1000);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders =
                (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());

        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> order : orders) {
            String createdAtStr = (String) order.get("createdAt");
            if (createdAtStr == null) continue;
            LocalDateTime created = LocalDateTime.parse(createdAtStr);
            LocalDate date = created.toLocalDate();
            if (date.isBefore(from) || date.isAfter(to)) continue;

            String key = groupBy == null
                    ? date.toString()
                    : "week".equalsIgnoreCase(groupBy)
                        ? date.getYear() + "-W" + (date.getDayOfYear() / 7)
                        : "month".equalsIgnoreCase(groupBy)
                            ? date.getYear() + "-" + String.format("%02d", date.getMonthValue())
                            : date.toString();

            grouped.computeIfAbsent(key, k -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("date", k);
                r.put("orders", 0);
                r.put("gross", 0.0);
                r.put("discount", 0.0);
                r.put("net", 0.0);
                return r;
            });

            Map<String, Object> bucket = grouped.get(key);
            bucket.put("orders", (int) bucket.get("orders") + 1);
            double total = ((Number) order.getOrDefault("total", 0)).doubleValue();
            double discount = ((Number) order.getOrDefault("discount", 0)).doubleValue();
            double subtotal = ((Number) order.getOrDefault("subtotal", 0)).doubleValue();
            bucket.put("gross", ((Number) bucket.get("gross")).doubleValue() + subtotal);
            bucket.put("discount", ((Number) bucket.get("discount")).doubleValue() + discount);
            bucket.put("net", ((Number) bucket.get("net")).doubleValue() + total);
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("from", from);
        total.put("to", to);
        total.put("branchId", branchId);
        total.put("groupBy", groupBy);
        total.put("totalOrders", orders.size());
        return new RevenueReportResponse(new ArrayList<>(grouped.values()), total);
    }

    @Override
    public InventoryReportResponse inventory(InventoryReportRequest request) {
        if (request != null
                && request.fromDate() != null
                && request.toDate() != null
                && request.fromDate().isAfter(request.toDate())) {
            throw new InvalidOperationException(
                    "fromDate must be before toDate",
                    "Ngày bắt đầu phải trước ngày kết thúc");
        }
        return inventory(request == null ? null : request.branchId());
    }

    @Override
    public InventoryReportResponse inventory(UUID branchId) {
        List<Map<String, Object>> stock = inventoryClient.getInventory(branchId);
        List<Map<String, Object>> lowStock = inventoryClient.getLowStock();

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("branchId", branchId);
        total.put("totalBatches", stock.size());
        total.put("lowStockCount", lowStock.size());

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("section", "lowStock", "items", lowStock));
        data.add(Map.of("section", "stockDetails", "items", stock));

        return new InventoryReportResponse(data, total);
    }

    @Override
    public StaffReportResponse staff(StaffReportRequest request) {
        validateRange(request.fromDate(), request.toDate());
        // Aggregation by staff is derived from order-service (staffId field).
        Map<String, Object> ordersResp = orderClient.getOrders(null, 0, 1000);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders =
                (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());

        Map<String, Map<String, Object>> byStaff = new LinkedHashMap<>();
        for (Map<String, Object> order : orders) {
            String staffId = String.valueOf(order.getOrDefault("staffId", "unknown"));
            byStaff.computeIfAbsent(staffId, k -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("staffId", k);
                r.put("orders", 0);
                r.put("revenue", 0.0);
                return r;
            });
            Map<String, Object> bucket = byStaff.get(staffId);
            bucket.put("orders", (int) bucket.get("orders") + 1);
            double total = ((Number) order.getOrDefault("total", 0)).doubleValue();
            bucket.put("revenue", ((Number) bucket.get("revenue")).doubleValue() + total);
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("fromDate", request.fromDate());
        total.put("toDate", request.toDate());
        total.put("branchId", request.branchId());
        return new StaffReportResponse(new ArrayList<>(byStaff.values()), total);
    }

    @Override
    public Object export(String type, String format, LocalDate from, LocalDate to) {
        validateRange(from, to);
        // B-12: Aggregate data and build the report rows
        java.util.List<String> headers = new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String title = String.format("%s Report (%s to %s)",
                type.substring(0, 1).toUpperCase() + type.substring(1), from, to);

        if ("revenue".equalsIgnoreCase(type)) {
            headers.add("Date");
            headers.add("Branch");
            headers.add("Orders");
            headers.add("Revenue (VND)");
            headers.add("Avg Order Value");
            // Aggregate from order-service (simplified for now)
            rows.add(java.util.Map.of("Date", from, "Branch", "ALL", "Orders", 0, "Revenue (VND)", 0, "Avg Order Value", 0));
        } else if ("inventory".equalsIgnoreCase(type)) {
            headers.add("Medicine");
            headers.add("Branch");
            headers.add("Stock");
            headers.add("Min Level");
            headers.add("Status");
            rows.add(java.util.Map.of("Medicine", "Sample", "Branch", "ALL", "Stock", 0, "Min Level", 10, "Status", "OK"));
        } else if ("staff".equalsIgnoreCase(type)) {
            headers.add("Staff ID");
            headers.add("Name");
            headers.add("Orders");
            headers.add("Revenue (VND)");
            rows.add(java.util.Map.of("Staff ID", "N/A", "Name", "N/A", "Orders", 0, "Revenue (VND)", 0));
        } else {
            throw new com.pcms.common.exception.InvalidOperationException(
                    "Unknown report type: " + type,
                    "Loại báo cáo không hợp lệ: " + type);
        }

        // Dispatch to Excel or PDF
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            return excelExportService.exportToExcel(title, headers, rows);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return pdfExportService.exportToPdf(title, headers, rows);
        } else {
            throw new com.pcms.common.exception.InvalidOperationException(
                    "Unknown export format: " + format + " (supported: excel, pdf)",
                    "Định dạng xuất không hỗ trợ: " + format);
        }
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidOperationException(
                    "fromDate and toDate are required",
                    "Ngày bắt đầu và ngày kết thúc là bắt buộc");
        }
        if (from.isAfter(to)) {
            throw new InvalidOperationException(
                    "fromDate must be before toDate",
                    "Ngày bắt đầu phải trước ngày kết thúc");
        }
    }
}
