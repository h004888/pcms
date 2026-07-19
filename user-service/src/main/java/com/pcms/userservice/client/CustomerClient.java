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
     * Provision Customer.id = User.id after a CUSTOMER User is persisted.
     */
    @PostMapping("/customers/internal/provision")
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackRegister")
    Map<String, Object> register(@RequestBody CustomerRegisterRequest request);

    /**
     * Registration must not issue an authenticated CUSTOMER account without
     * a matching customer profile.
     */
    default Map<String, Object> fallbackRegister(CustomerRegisterRequest request, Throwable t) {
        throw new IllegalStateException("Customer profile provisioning is unavailable", t);
    }
}
