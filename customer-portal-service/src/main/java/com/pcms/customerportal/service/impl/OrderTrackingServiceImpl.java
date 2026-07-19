package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.response.OrderDetailResponse;
import com.pcms.customerportal.dto.response.OrderHistoryItemResponse;
import com.pcms.customerportal.dto.response.OrderTrackingResponse;
import com.pcms.customerportal.service.OrderTrackingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderTrackingServiceImpl implements OrderTrackingService {
    private final OrderClient orderClient;

    public OrderTrackingServiceImpl(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public OrderTrackingResponse track(UUID orderId, UUID customerId) {
        Map<String, Object> order = requireOwner(orderId, customerId);
        List<OrderTrackingResponse.TimelineEntry> timeline = orderClient.getStatusHistory(orderId.toString()).stream()
                .map(event -> new OrderTrackingResponse.TimelineEntry(
                        stringValue(event, "status"),
                        parseInstant(event.get("occurredAt")),
                        nullableString(event.get("note"))))
                .toList();
        return new OrderTrackingResponse(orderId, stringValue(order, "orderNumber"),
                stringValue(order, "status"), timeline);
    }

    @Override
    public PageResponse<OrderHistoryItemResponse> history(UUID customerId, String status, String dateFrom,
                                                           String dateTo, int page, int size) {
        Map<String, Object> orderPage = orderClient.listForHistory(customerId.toString(), status, dateFrom, dateTo, page, size);
        List<Map<String, Object>> data = maps(orderPage.get("data"));
        List<OrderHistoryItemResponse> items = data.stream().map(this::historyItem).toList();
        return new PageResponse<>(items, intValue(orderPage.get("page"), page), intValue(orderPage.get("size"), size),
                longValue(orderPage.get("totalElements"), longValue(orderPage.get("total"), items.size())),
                intValue(orderPage.get("totalPages"), 0));
    }

    @Override
    public OrderDetailResponse detail(UUID orderId, UUID customerId) {
        Map<String, Object> order = requireOwner(orderId, customerId);
        return new OrderDetailResponse(orderId, stringValue(order, "orderNumber"), stringValue(order, "status"),
                uuidValue(order.get("branchId")), uuidValue(order.get("prescriptionId")), nullableString(order.get("couponCode")),
                decimalValue(order.get("subtotal")), decimalValue(order.get("discount")), decimalValue(order.get("total")),
                parseInstant(order.get("createdAt")), maps(order.get("items")).stream().map(item ->
                        new OrderDetailResponse.Item(uuidValue(item.get("medicineId")), nullableString(item.get("medicineName")),
                                intValue(item.get("quantity"), 0), decimalValue(item.get("unitPrice")),
                                decimalValue(item.get("discount")), decimalValue(item.get("subtotal")))).toList());
    }

    private OrderHistoryItemResponse historyItem(Map<String, Object> order) {
        List<Map<String, Object>> sourceItems = maps(order.get("items"));
        return new OrderHistoryItemResponse(uuidValue(order.get("id")), stringValue(order, "orderNumber"),
                stringValue(order, "status"), decimalValue(order.get("total")), sourceItems.size(),
                parseInstant(order.get("createdAt")), sourceItems.stream().limit(3).map(item ->
                        new OrderHistoryItemResponse.ItemPreview(uuidValue(item.get("medicineId")),
                                nullableString(item.get("medicineName")), intValue(item.get("quantity"), 0))).toList());
    }

    private Map<String, Object> requireOwner(UUID orderId, UUID customerId) {
        Map<String, Object> order = orderClient.getById(orderId.toString());
        UUID owner = uuidValue(order.get("customerId"));
        if (order.isEmpty() || owner == null || !owner.equals(customerId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return order;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().filter(Map.class::isInstance).map(item -> (Map<String, Object>) item).toList();
    }

    private static String stringValue(Map<String, Object> map, String key) { return stringValue(map.get(key)); }
    private static String stringValue(Object value) { return value == null ? null : value.toString(); }
    private static String nullableString(Object value) { return value == null ? null : value.toString(); }
    private static int intValue(Object value, int fallback) { return value instanceof Number n ? n.intValue() : fallback; }
    private static long longValue(Object value, long fallback) { return value instanceof Number n ? n.longValue() : fallback; }
    private static BigDecimal decimalValue(Object value) { return value instanceof Number n ? new BigDecimal(n.toString()) : null; }
    private static UUID uuidValue(Object value) { try { return value == null ? null : UUID.fromString(value.toString()); } catch (RuntimeException e) { return null; } }

    private static Instant parseInstant(Object value) {
        if (value instanceof Instant instant) return instant;
        if (value == null) return null;
        try { return Instant.parse(value.toString()); }
        catch (RuntimeException ignored) { return LocalDateTime.parse(value.toString()).toInstant(ZoneOffset.UTC); }
    }
}
