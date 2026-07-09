package com.pcms.reportservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.reportservice.client.InventoryClient;
import com.pcms.reportservice.client.OrderClient;
import com.pcms.reportservice.repository.ReportScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportServiceImpl.
 * OrderClient and InventoryClient are mocked — no network calls.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private OrderClient orderClient;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private ExcelExportService excelExportService;

    @Mock
    private PdfExportService pdfExportService;

    @Mock
    private ReportScheduleRepository scheduleRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(orderClient, inventoryClient, excelExportService, pdfExportService, scheduleRepository);
    }

    // -------------------------------------------------------------------------
    // Helper: build a single order Map with today's date
    // -------------------------------------------------------------------------

    private Map<String, Object> buildOrder(LocalDate date, double total) {
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("createdAt", LocalDateTime.of(date, LocalTime.NOON).toString());
        order.put("total", total);
        order.put("subtotal", total);
        order.put("discount", 0.0);
        order.put("staffId", UUID.randomUUID().toString());
        return order;
    }

    private Map<String, Object> ordersResponse(List<Map<String, Object>> orders) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("data", orders);
        return resp;
    }

    @Test
    void shouldBuildRealtimeStats() {
        LocalDate today = LocalDate.now();
        when(orderClient.getOrders(null, "PAID", null, today, today, 0, 1000))
                .thenReturn(Map.of("data", List.of()));
        when(orderClient.getOrders(null, null, null, null, null, 0, 5))
                .thenReturn(Map.of("data", List.of()));
        when(inventoryClient.getInventory(null)).thenReturn(List.of());
        when(inventoryClient.getLowStock()).thenReturn(List.of());

        Map<String, Object> stats = reportService.realtimeStats(null);

        assertThat(stats).isNotEmpty();
        assertThat(stats.get("todayRevenue")).isEqualTo(0.0);
        assertThat(stats.get("todayOrders")).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // revenue() tests
    // -------------------------------------------------------------------------

    @Test
    void revenue_withValidRange_returnsRows() {
        LocalDate today = LocalDate.now();
        Map<String, Object> order = buildOrder(today, 100_000.0);
        when(orderClient.getOrders(isNull(), anyInt(), anyInt()))
                .thenReturn(ordersResponse(List.of(order)));

        var response = reportService.revenue(today, today, null, "day");

        assertThat(response).isNotNull();
        assertThat(response.data()).hasSize(1);

        Map<String, Object> row = response.data().get(0);
        assertThat(row.get("orders")).isEqualTo(1);
        assertThat(((Number) row.get("net")).doubleValue()).isEqualTo(100_000.0);

        assertThat(response.total().get("totalOrders")).isEqualTo(1);
    }

    @Test
    void revenue_fromAfterTo_throwsInvalidOperationException() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.minusDays(1);

        assertThatThrownBy(() -> reportService.revenue(from, to, null, "day"))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void revenue_withRequest_filtersOrdersOutsideRange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tenDaysAgo = today.minusDays(10);

        Map<String, Object> todayOrder = buildOrder(today, 50_000.0);
        Map<String, Object> oldOrder = buildOrder(tenDaysAgo, 200_000.0);

        when(orderClient.getOrders(isNull(), anyInt(), anyInt()))
                .thenReturn(ordersResponse(List.of(todayOrder, oldOrder)));

        var response = reportService.revenue(yesterday, today, null, "day");

        assertThat(response.data()).hasSize(1);
        Map<String, Object> row = response.data().get(0);
        assertThat(((Number) row.get("net")).doubleValue()).isEqualTo(50_000.0);
    }

    // -------------------------------------------------------------------------
    // realtimeStats() tests
    // -------------------------------------------------------------------------

    @Test
    void realtimeStats_returnsTodaySummary() {
        LocalDate today = LocalDate.now();
        Map<String, Object> todayOrder = buildOrder(today, 75_000.0);
        Map<String, Object> oldOrder = buildOrder(today.minusDays(5), 99_000.0);

        when(orderClient.getOrders(isNull(), anyInt(), anyInt()))
                .thenReturn(ordersResponse(List.of(todayOrder, oldOrder)));
        when(inventoryClient.getLowStock()).thenReturn(List.of());

        Map<String, Object> stats = reportService.realtimeStats();

        assertThat(stats).isNotNull();
        assertThat(stats.get("date")).isEqualTo(today.toString());
        assertThat(((Number) stats.get("todayOrders")).longValue()).isEqualTo(1L);
        assertThat(((Number) stats.get("todayRevenue")).doubleValue()).isEqualTo(75_000.0);
        assertThat(((Number) stats.get("totalOrders")).longValue()).isEqualTo(2L);
        assertThat(((Number) stats.get("lowStockCount")).intValue()).isEqualTo(0);
    }

    @Test
    void realtimeStats_withLowStockItems_reflectsCount() {
        LocalDate today = LocalDate.now();
        when(orderClient.getOrders(isNull(), anyInt(), anyInt()))
                .thenReturn(ordersResponse(List.of()));

        Map<String, Object> lowStockItem = Map.of("medicine", "Aspirin", "stock", 2);
        when(inventoryClient.getLowStock()).thenReturn(List.of(lowStockItem, lowStockItem));

        Map<String, Object> stats = reportService.realtimeStats();

        assertThat(((Number) stats.get("lowStockCount")).intValue()).isEqualTo(2);
        assertThat(((Number) stats.get("todayOrders")).longValue()).isEqualTo(0L);
    }
}
