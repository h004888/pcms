package com.pcms.customerportal.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for payment-service.
 * <p>
 * Used to create pending payments for VietQR checkout and query payment status.
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @PostMapping("/payments")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackCreatePayment")
    Map<String, Object> createPayment(@RequestBody Map<String, Object> request);

    @GetMapping("/payments/status/{orderNumber}")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackGetStatus")
    Map<String, Object> getPaymentStatusByOrderNumber(@PathVariable String orderNumber);

    default Map<String, Object> fallbackCreatePayment(Map<String, Object> request, Throwable t) {
        return Map.of("status", "DEFERRED", "message", "Payment service unavailable");
    }

    default Map<String, Object> fallbackGetStatus(String orderNumber, Throwable t) {
        return Map.of("status", "UNKNOWN", "message", "Payment service unavailable");
    }
}
