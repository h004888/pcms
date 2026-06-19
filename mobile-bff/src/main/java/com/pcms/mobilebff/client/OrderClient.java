package com.pcms.mobilebff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "order-service", fallback = OrderClient.Fallback.class)
public interface OrderClient {
    @GetMapping("/api/v1/orders")
    Map<String, Object> list(@RequestParam(name = "customerId") String customerId,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "5") int size);

    class Fallback implements OrderClient {
        @Override
        public Map<String, Object> list(String customerId, int page, int size) {
            return Map.of("data", java.util.List.of());
        }
    }
}
