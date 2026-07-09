package com.pcms.reportservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.reportservice.client.InventoryClient;
import com.pcms.reportservice.client.OrderClient;
import com.pcms.reportservice.dto.CreateScheduleRequest;
import com.pcms.reportservice.dto.InventoryReportRequest;
import com.pcms.reportservice.dto.InventoryReportResponse;
import com.pcms.reportservice.dto.RevenueReportRequest;
import com.pcms.reportservice.dto.RevenueReportResponse;
import com.pcms.reportservice.dto.ScheduleResponse;
import com.pcms.reportservice.dto.StaffReportRequest;
import com.pcms.reportservice.dto.StaffReportResponse;
import com.pcms.reportservice.entity.ReportSchedule;
import com.pcms.reportservice.repository.ReportScheduleRepository;
import com.pcms.reportservice.service.ReportService;
import org.springframework.cache.annotation.Cacheable;
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
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final ReportScheduleRepository scheduleRepository;

    public ReportServiceImpl(OrderClient orderClient,
                              InventoryClient inventoryClient,
                              ExcelExportService excelExportService,
                              PdfExportService pdfExportService,
                              ReportScheduleRepository scheduleRepository) {
        this.orderClient = orderClient;
        this.inventoryClient = inventoryClient;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
        this.scheduleRepository = scheduleRepository;
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
        Map<String, Object> ordersResp = orderClient.getOrders(null, "PAID", branchId, from, to, 0, 1000);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());

        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> order : orders) {
            String createdAtStr = (String) order.get("createdAt");
            if (createdAtStr == null)
                continue;
            LocalDateTime created = LocalDateTime.parse(createdAtStr);
            LocalDate date = created.toLocalDate();
            if (date.isBefore(from) || date.isAfter(to))
                continue;

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
        total.put("totalOrders", grouped.values().stream()
                .mapToInt(row -> ((Number) row.get("orders")).intValue())
                .sum());
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
        Map<String, Object> ordersResp = orderClient.getOrders(null, "PAID", request.branchId(),
                request.fromDate(), request.toDate(), 0, 1000);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());

        Map<String, Map<String, Object>> byStaff = new LinkedHashMap<>();
        for (Map<String, Object> order : orders) {
            String createdAtStr = (String) order.get("createdAt");
            if (createdAtStr != null) {
                LocalDate date = LocalDateTime.parse(createdAtStr).toLocalDate();
                if (date.isBefore(request.fromDate()) || date.isAfter(request.toDate())) {
                    continue;
                }
            }
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
    @Cacheable(value = "realtimeStats", key = "#branchId == null ? 'ALL' : #branchId.toString()")
    public Map<String, Object> realtimeStats(UUID branchId) {
        LocalDate today = LocalDate.now();
        RevenueReportResponse revenue = revenue(today, today, branchId, "day");
        InventoryReportResponse inventory = inventory(branchId);
        List<Map<String, Object>> recentOrders = recentOrders(branchId, 5);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("branchId", branchId);
        stats.put("todayRevenue", revenue.data().stream()
                .mapToDouble(row -> ((Number) row.getOrDefault("net", 0)).doubleValue())
                .sum());
        stats.put("todayOrders", revenue.data().stream()
                .mapToInt(row -> ((Number) row.getOrDefault("orders", 0)).intValue())
                .sum());
        stats.put("lowStockCount", inventory.total().getOrDefault("lowStockCount", 0));
        stats.put("totalBatches", inventory.total().getOrDefault("totalBatches", 0));
        stats.put("recentOrders", recentOrders);
        return stats;
    }

    @Override
    public List<Map<String, Object>> recentOrders(UUID branchId, int limit) {
        Map<String, Object> ordersResp = orderClient.getOrders(null, null, branchId, null, null, 0,
                Math.min(limit, 20));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());
        return orders.stream().limit(Math.max(limit, 0)).toList();
    }

    @Override
    public Object export(String type, String format, LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        String title = String.format("%s Report (%s to %s)",
                type.substring(0, 1).toUpperCase() + type.substring(1), from, to);

        if ("revenue".equalsIgnoreCase(type)) {
            headers.add("Date");
            headers.add("Orders");
            headers.add("Gross");
            headers.add("Discount");
            headers.add("Revenue (VND)");
            RevenueReportResponse response = revenue(from, to, null, "day");
            for (Map<String, Object> row : response.data()) {
                Map<String, Object> exportRow = new LinkedHashMap<>();
                exportRow.put("Date", valueOrBlank(row.get("date")));
                exportRow.put("Orders", valueOrZero(row.get("orders")));
                exportRow.put("Gross", valueOrZero(row.get("gross")));
                exportRow.put("Discount", valueOrZero(row.get("discount")));
                exportRow.put("Revenue (VND)", valueOrZero(row.get("net")));
                rows.add(exportRow);
            }
        } else if ("inventory".equalsIgnoreCase(type)) {
            headers.add("Medicine");
            headers.add("Branch");
            headers.add("Stock");
            headers.add("Min Level");
            headers.add("Status");
            appendInventoryRowsFromReport(inventoryClient.getStockLevelReport(null), rows);
        } else if ("staff".equalsIgnoreCase(type)) {
            headers.add("Staff ID");
            headers.add("Orders");
            headers.add("Revenue (VND)");
            StaffReportResponse response = staff(new StaffReportRequest(from, to, null));
            for (Map<String, Object> row : response.data()) {
                Map<String, Object> exportRow = new LinkedHashMap<>();
                exportRow.put("Staff ID", valueOrBlank(row.get("staffId")));
                exportRow.put("Orders", valueOrZero(row.get("orders")));
                exportRow.put("Revenue (VND)", valueOrZero(row.get("revenue")));
                rows.add(exportRow);
            }
        } else {
            throw new InvalidOperationException(
                    "Unknown report type: " + type,
                    "Loại báo cáo không hợp lệ: " + type);
        }

        // Dispatch to Excel or PDF
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            return excelExportService.exportToExcel(title, headers, rows);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return pdfExportService.exportToPdf(title, headers, rows);
        } else {
            throw new InvalidOperationException(
                    "Unknown export format: " + format + " (supported: excel, pdf)",
                    "Định dạng xuất không hỗ trợ: " + format);
        }
    }

    @Override
    public Map<String, Object> realtimeStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> ordersResp = orderClient.getOrders(null, 0, 1000);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allOrders =
                (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());

        long todayOrders = 0;
        double todayRevenue = 0.0;
        for (Map<String, Object> order : allOrders) {
            String createdAtStr = (String) order.get("createdAt");
            if (createdAtStr == null) continue;
            try {
                LocalDateTime created = LocalDateTime.parse(createdAtStr);
                if (created.toLocalDate().equals(today)) {
                    todayOrders++;
                    todayRevenue += ((Number) order.getOrDefault("total", 0)).doubleValue();
                }
            } catch (Exception ignored) {}
        }

        List<Map<String, Object>> lowStock = inventoryClient.getLowStock();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", today.toString());
        result.put("todayOrders", todayOrders);
        result.put("todayRevenue", todayRevenue);
        result.put("totalOrders", allOrders.size());
        result.put("lowStockCount", lowStock.size());
        return result;
    }

    @Override
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        ReportSchedule schedule = new ReportSchedule();
        schedule.setType(request.reportType());
        schedule.setFormat(request.format());
        schedule.setBranchId(request.branchId());
        schedule.setCronExpression("0 0 * * *"); // default daily midnight
        schedule.setRecipientEmail(request.recipients());
        schedule.setActive(true);
        schedule.setNextRunAt(LocalDateTime.now().plusDays(1));
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Override
    public List<ScheduleResponse> listSchedules() {
        return scheduleRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(ScheduleResponse::from)
                .toList();
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

    private void appendInventoryRowsFromReport(List<Map<String, Object>> stockItems, List<Map<String, Object>> rows) {
        for (Map<String, Object> item : stockItems) {
            Number qty = (Number) item.getOrDefault("qtyOnHand", 0);
            Number min = (Number) item.getOrDefault("minStockLevel", 0);
            Map<String, Object> exportRow = new LinkedHashMap<>();
            exportRow.put("Medicine", valueOrBlank(item.get("medicineId")));
            exportRow.put("Branch", valueOrBlank(item.get("branchId")));
            exportRow.put("Stock", qty);
            exportRow.put("Min Level", min);
            exportRow.put("Status",
                    valueOrBlank(item.getOrDefault("status", qty.intValue() < min.intValue() ? "LOW" : "OK")));
            rows.add(exportRow);
        }
    }

    private Object valueOrBlank(Object value) {
        return value == null ? "" : value;
    }

    private Object valueOrZero(Object value) {
        return value == null ? 0 : value;
    }
}
