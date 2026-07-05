package com.pcms.userservice.client;

import com.pcms.userservice.dto.request.CustomerRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.Map;

/**
 * Feign client for customer-service (UC08).
 * Used during self-registration to create the customer record
 * synchronously with the user account.
 */
@FeignClient(name = "customer-service")
public interface CustomerClient {

    /**
     * Create a customer profile in customer-service.
     * Called right after the User account is persisted.
     */
    @PostMapping("/customers/register")
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackRegister")
    Map<String, Object> register(@RequestBody CustomerRegisterRequest request);

    /**
     * Fallback when customer-service is unavailable.
     * Returns DEFERRED status so the registration can still succeed —
     * the customer record will be lazily created on first profile access.
     */
    default Map<String, Object> fallbackRegister(CustomerRegisterRequest request, Throwable t) {
        return Map.of(
            "status", "DEFERRED",
            "message", "Customer service unavailable, profile will be created on first access"
        );
    }
}
