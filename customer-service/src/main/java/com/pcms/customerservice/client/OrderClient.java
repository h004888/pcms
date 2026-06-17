package com.pcms.customerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/** B5: Feign client to fetch order history from order-service. */
@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/orders")
    Map<String, Object> getOrdersByCustomer(
            @RequestParam UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size);
}
