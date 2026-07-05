package com.pcms.orderservice.controller;

import com.pcms.orderservice.dto.TopMedicineResponse;
import com.pcms.orderservice.repository.OrderItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Analytics endpoints for order data.
 * Used by customer-portal-service for SHOP-HOME best-sellers aggregation.
 */
@RestController
@RequestMapping("/orders/analytics")
@Tag(name = "Order Analytics")
public class OrderAnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(OrderAnalyticsController.class);

    private final OrderItemRepository orderItemRepository;

    public OrderAnalyticsController(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping("/top-medicines")
    @Operation(summary = "Top N medicines by sold quantity in the last N days")
    public ResponseEntity<List<TopMedicineResponse>> getTopMedicines(
            @RequestParam(name = "periodDays", defaultValue = "30") int periodDays,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        LocalDateTime since = LocalDateTime.now().minusDays(periodDays);
        List<Object[]> raw = orderItemRepository.findTopMedicines(since, limit);

        List<TopMedicineResponse> result = raw.stream().map(row -> {
            UUID medicineId = row[0] != null ? UUID.fromString(row[0].toString()) : null;
            String name = row[1] != null ? row[1].toString() : "Unknown";
            Long soldCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal price = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            return new TopMedicineResponse(medicineId, name, price, soldCount);
        }).toList();

        log.debug("Top-medicines: {} results for period={}d, limit={}", result.size(), periodDays, limit);
        return ResponseEntity.ok(result);
    }
}
