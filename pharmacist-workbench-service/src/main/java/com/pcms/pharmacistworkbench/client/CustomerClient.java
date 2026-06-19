package com.pcms.pharmacistworkbench.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "customer-service", fallback = CustomerClient.Fallback.class)
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{id}")
    Map<String, Object> getById(@PathVariable("id") String id);

    @GetMapping("/api/v1/customers/{id}/orders")
    Map<String, Object> getOrders(@PathVariable("id") String id);

    @GetMapping("/api/v1/customers/{id}/history")
    Map<String, Object> getHistory(@PathVariable("id") String id);

    class Fallback implements CustomerClient {
        @Override
        public Map<String, Object> getById(String id) { return Map.of(); }
        @Override
        public Map<String, Object> getOrders(String id) { return Map.of("data", java.util.List.of()); }
        @Override
        public Map<String, Object> getHistory(String id) { return Map.of(); }
    }
}
