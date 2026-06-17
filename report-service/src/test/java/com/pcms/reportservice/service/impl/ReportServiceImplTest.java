package com.pcms.reportservice.service.impl;

import com.pcms.reportservice.client.InventoryClient;
import com.pcms.reportservice.client.OrderClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReportServiceImplTest {

        @Test
        void shouldBuildRealtimeStats() {
                OrderClient orderClient = Mockito.mock(OrderClient.class);
                InventoryClient inventoryClient = Mockito.mock(InventoryClient.class);
                ExcelExportService excelExportService = Mockito.mock(ExcelExportService.class);
                PdfExportService pdfExportService = Mockito.mock(PdfExportService.class);

                Mockito.when(orderClient.getOrders(null, "PAID", null, LocalDate.now(), LocalDate.now(), 0, 1000))
                                .thenReturn(Map.of("data", List.of()));
                Mockito.when(orderClient.getOrders(null, null, null, null, null, 0, 5))
                                .thenReturn(Map.of("data", List.of()));
                Mockito.when(inventoryClient.getInventory(null)).thenReturn(List.of());
                Mockito.when(inventoryClient.getLowStock()).thenReturn(List.of());

                ReportServiceImpl service = new ReportServiceImpl(orderClient, inventoryClient, excelExportService,
                                pdfExportService);
                Map<String, Object> stats = service.realtimeStats(null);

                assertFalse(stats.isEmpty());
                assertEquals(0.0, stats.get("todayRevenue"));
                assertEquals(0, stats.get("todayOrders"));
        }
}