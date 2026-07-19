package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.client.OrderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTrackingServiceImplTest {
    @Mock
    private OrderClient orderClient;

    @Test
    void historyBuildsPreviewFromItemsAndKeepsSourceMetadata() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Map<String, Object> order = Map.of(
                "id", orderId.toString(), "orderNumber", "ORD-1", "status", "PAID",
                "total", 120000, "createdAt", "2026-07-19T10:00:00Z",
                "items", List.of(
                        Map.of("medicineId", UUID.randomUUID().toString(), "medicineName", "A", "quantity", 2),
                        Map.of("medicineId", UUID.randomUUID().toString(), "medicineName", "B", "quantity", 1),
                        Map.of("medicineId", UUID.randomUUID().toString(), "medicineName", "C", "quantity", 4),
                        Map.of("medicineId", UUID.randomUUID().toString(), "medicineName", "D", "quantity", 3)));
        when(orderClient.listForHistory(customerId.toString(), "PAID", "2026-07-01", "2026-07-19", 2, 10))
                .thenReturn(Map.of("data", List.of(order), "page", 2, "size", 10, "totalElements", 31, "totalPages", 4));

        var response = new OrderTrackingServiceImpl(orderClient)
                .history(customerId, "PAID", "2026-07-01", "2026-07-19", 2, 10);

        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.total()).isEqualTo(31);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.data()).singleElement().satisfies(item -> {
            assertThat(item.itemCount()).isEqualTo(4);
            assertThat(item.items()).hasSize(3);
            assertThat(item.total()).isEqualByComparingTo(BigDecimal.valueOf(120000));
        });
    }

    @Test
    void detailAndTrackingRejectNonOwnerAndTrackingUsesPersistedEvents() {
        UUID orderId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Map<String, Object> order = Map.of("id", orderId.toString(), "customerId", ownerId.toString(),
                "orderNumber", "ORD-2", "status", "COMPLETED", "items", List.of());
        when(orderClient.getById(orderId.toString())).thenReturn(order);
        when(orderClient.getStatusHistory(orderId.toString())).thenReturn(List.of(
                Map.of("status", "PENDING_PAYMENT", "occurredAt", "2026-07-19T10:00:00Z", "note", "created"),
                Map.of("status", "COMPLETED", "occurredAt", "2026-07-19T11:00:00Z", "note", "done")));
        var service = new OrderTrackingServiceImpl(orderClient);

        var tracking = service.track(orderId, ownerId);

        assertThat(tracking.orderId()).isEqualTo(orderId);
        assertThat(tracking.timeline()).extracting("status")
                .containsExactly("PENDING_PAYMENT", "COMPLETED");
        assertThat(tracking.timeline().get(1).occurredAt()).isEqualTo(Instant.parse("2026-07-19T11:00:00Z"));
        assertThatThrownBy(() -> service.detail(orderId, UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class);
        verify(orderClient).getStatusHistory(orderId.toString());
    }

    @Test
    void upstreamFailureIsNotConvertedToEmptyOrUnknown() {
        UUID customerId = UUID.randomUUID();
        when(orderClient.listForHistory(customerId.toString(), null, null, null, 0, 20))
                .thenThrow(new IllegalStateException("unavailable"));

        assertThatThrownBy(() -> new OrderTrackingServiceImpl(orderClient)
                .history(customerId, null, null, null, 0, 20))
                .isInstanceOf(IllegalStateException.class);
    }
}
