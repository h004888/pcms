package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "prescription-service")
public interface PrescriptionClient {

    Logger log = LoggerFactory.getLogger(PrescriptionClient.class);

    @GetMapping("/prescriptions/{id}")
    @CircuitBreaker(name = "prescriptionService", fallbackMethod = "fallbackGetPrescription")
    Map<String, Object> getPrescriptionById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetPrescription(UUID id, Throwable throwable) {
        log.warn("Prescription service unavailable for prescription {}: {}", id, throwable.getMessage());
        return Map.of("id", id.toString(), "status", "UNREACHABLE");
    }
}