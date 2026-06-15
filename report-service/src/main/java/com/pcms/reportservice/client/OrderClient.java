package com.pcms.reportservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign client to aggregate data from order-service for reports
 */
@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/orders")
    Map<String, Object> getOrders(@RequestParam(required = false) UUID customerId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "100") int size);
}
