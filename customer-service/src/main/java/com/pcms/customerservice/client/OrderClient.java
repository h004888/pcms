package com.pcms.customerservice.client;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerservice.dto.response.CustomerOrderSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping("/orders")
    PageResponse<CustomerOrderSummaryResponse> getOrdersByCustomer(
            @RequestParam("customerId") UUID customerId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "branchId", required = false) UUID branchId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size);
}