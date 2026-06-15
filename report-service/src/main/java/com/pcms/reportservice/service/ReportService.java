package com.pcms.reportservice.service;

import com.pcms.reportservice.client.InventoryClient;
import com.pcms.reportservice.client.OrderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * UC09 - Reports
 * FR9.1, FR9.2 - Revenue, Inventory reports
 * FR9.3, FR9.4 - Excel/PDF export (TBD)
 * FR9.5 - Schedule delivery (TBD)
 */
@Service
public class ReportService {

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private InventoryClient inventoryClient;

    /**
     * Revenue report: aggregate orders by day/week/month in date range
     */
    public Map<String, Object> generateRevenueReport(LocalDate from, LocalDate to, UUID branchId, String groupBy) {
        Map<String, Object> ordersResp = orderClient.getOrders(null, 0, 1000);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) ordersResp.getOrDefault("data", List.of());

        // Group by day
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> order : orders) {
            String createdAtStr = (String) order.get("createdAt");
            if (createdAtStr == null) continue;
            LocalDateTime created = LocalDateTime.parse(createdAtStr);
            LocalDate date = created.toLocalDate();
            if (date.isBefore(from) || date.isAfter(to)) continue;

            String key = groupBy == null ? date.toString()
                       : "week".equalsIgnoreCase(groupBy) ? date.getYear() + "-W" + (date.getDayOfYear() / 7)
                       : "month".equalsIgnoreCase(groupBy) ? date.getYear() + "-" + String.format("%02d", date.getMonthValue())
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from);
        result.put("to", to);
        result.put("branchId", branchId);
        result.put("groupBy", groupBy);
        result.put("rows", new ArrayList<>(grouped.values()));
        result.put("totalOrders", orders.size());
        return result;
    }

    /**
     * Inventory report: current stock per branch, with low-stock alerts
     */
    public Map<String, Object> generateInventoryReport(UUID branchId) {
        List<Map<String, Object>> stock = inventoryClient.getInventory(branchId);
        List<Map<String, Object>> lowStock = inventoryClient.getLowStock();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("branchId", branchId);
        result.put("totalBatches", stock.size());
        result.put("lowStockCount", lowStock.size());
        result.put("lowStockItems", lowStock);
        result.put("stockDetails", stock);
        return result;
    }
}
