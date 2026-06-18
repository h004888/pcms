package com.pcms.prescriptionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    Logger log = LoggerFactory.getLogger(CustomerClient.class);

    @GetMapping("/customers/{id}")
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackGetCustomer")
    Map<String, Object> getCustomerById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetCustomer(UUID id, Throwable throwable) {
        log.warn("Customer service unavailable for patient {}: {}", id, throwable.getMessage());
        return Map.of("id", id.toString(), "status", "UNREACHABLE");
    }
}