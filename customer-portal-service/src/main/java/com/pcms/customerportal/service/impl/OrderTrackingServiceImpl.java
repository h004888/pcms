package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.response.OrderHistoryItemResponse;
import com.pcms.customerportal.dto.response.OrderTrackingResponse;
import com.pcms.customerportal.service.OrderTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks B2C orders by aggregating data from order-service.
 */
@Service
public class OrderTrackingServiceImpl implements OrderTrackingService {

    private static final Logger log = LoggerFactory.getLogger(OrderTrackingServiceImpl.class);

    private final OrderClient orderClient;

    public OrderTrackingServiceImpl(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public OrderTrackingResponse track(UUID orderId, UUID customerId) {
        Map<String, Object> orderData;
        try {
            orderData = orderClient.getById(orderId.toString());
        } catch (Exception e) {
            log.warn("Failed to fetch order {} for tracking: {}", orderId, e.getMessage());
            orderData = Map.of();
        }

        if (orderData.isEmpty()) {
            // Return minimal response so FE can still render
            return new OrderTrackingResponse(
                    null,
                    "UNKNOWN",
                    "UNKNOWN",
                    List.of(),
                    null,
                    Instant.now().plus(3, ChronoUnit.DAYS)
            );
        }

        String orderNumber = (String) orderData.getOrDefault("orderNumber", "");
        String status = (String) orderData.getOrDefault("status", "PENDING");
        Object createdAtRaw = orderData.get("createdAt");
        Instant createdAt = parseInstant(createdAtRaw);

        List<OrderTrackingResponse.TimelineEntry> timeline = buildTimeline(status, createdAt);

        return new OrderTrackingResponse(
                null,
                orderNumber,
                status,
                timeline,
                null,
                createdAt.plus(3, ChronoUnit.DAYS)
        );
    }

    @Override
    public PageResponse<OrderHistoryItemResponse> history(UUID customerId, int page, int size) {
        Map<String, Object> orderPage;
        try {
            orderPage = orderClient.listForHistory(customerId.toString(), page, size);
        } catch (Exception e) {
            log.warn("Failed to fetch order history for customer {}: {}", customerId, e.getMessage());
            return PageResponse.of(List.<OrderHistoryItemResponse>of(), o -> o);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) orderPage.getOrDefault("data", List.of());

        List<OrderHistoryItemResponse> items = new ArrayList<>();
        for (Map<String, Object> o : data) {
            UUID id = parseUUID(o.get("id"));
            String orderNumber = (String) o.getOrDefault("orderNumber", "");
            String status = (String) o.getOrDefault("status", "PENDING");
            Object total = o.get("total");
            Object itemCount = o.get("itemCount");
            Object createdAt = o.get("createdAt");

            items.add(new OrderHistoryItemResponse(
                    id != null ? id : UUID.randomUUID(),
                    orderNumber,
                    status,
                    total instanceof Number n ? new java.math.BigDecimal(n.toString()) : java.math.BigDecimal.ZERO,
                    itemCount instanceof Number n ? n.intValue() : 0,
                    parseInstant(createdAt)
            ));
        }

        return PageResponse.of(items, o -> o);
    }

    private List<OrderTrackingResponse.TimelineEntry> buildTimeline(String status, Instant baseTime) {
        List<OrderTrackingResponse.TimelineEntry> timeline = new ArrayList<>();
        Instant t0 = baseTime != null ? baseTime : Instant.now();

        timeline.add(new OrderTrackingResponse.TimelineEntry("CONFIRMED", t0, "Đã xác nhận", null));
        if ("PAID".equals(status) || "SHIPPING".equals(status) || "DELIVERED".equals(status) || "COMPLETED".equals(status)) {
            timeline.add(new OrderTrackingResponse.TimelineEntry("PAID", t0.plus(5, ChronoUnit.MINUTES), "Đã thanh toán", null));
        }
        if ("SHIPPING".equals(status) || "DELIVERED".equals(status) || "COMPLETED".equals(status)) {
            timeline.add(new OrderTrackingResponse.TimelineEntry("SHIPPING", t0.plus(1, ChronoUnit.HOURS), "Đang giao", "Kho Hà Nội"));
        }
        if ("DELIVERED".equals(status) || "COMPLETED".equals(status)) {
            timeline.add(new OrderTrackingResponse.TimelineEntry("DELIVERED", t0.plus(2, ChronoUnit.DAYS), "Đã giao", null));
        }
        if ("CANCELLED".equals(status)) {
            timeline.add(new OrderTrackingResponse.TimelineEntry("CANCELLED", t0, "Đã huỷ", null));
        }

        return timeline;
    }

    private Instant parseInstant(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof Instant i) return i;
            if (o instanceof String s) return Instant.parse(s);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private UUID parseUUID(Object o) {
        if (o == null) return null;
        try {
            return o instanceof UUID u ? u : UUID.fromString(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}