package com.pcms.reportservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Feign client to aggregate data from order-service for reports
 */
@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/orders")
    Map<String, Object> getOrders(
            @RequestParam(value = "customerId", required = false) UUID customerId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "branchId", required = false) UUID branchId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "100") int size);
}
