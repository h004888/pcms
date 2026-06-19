package com.pcms.customerportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client to payment-service (port 8089).
 * Used by CheckoutService to initialize a payment session after creating an order.
 */
@FeignClient(name = "payment-service", fallback = PaymentClient.Fallback.class)
public interface PaymentClient {

    /**
     * POST /payments - Initialize a payment for an order.
     * Returns {id, status, paymentUrl} from payment-service.
     */
    @PostMapping("/api/v1/payments")
    Map<String, Object> init(@RequestBody Map<String, Object> request);

    /**
     * Fallback when payment-service is unavailable.
     * Returns a stub response so checkout flow can continue (order will be PENDING_PAYMENT).
     */
    class Fallback implements PaymentClient {
        @Override
        public Map<String, Object> init(Map<String, Object> request) {
            return Map.of(
                    "id", UUID.randomUUID().toString(),
                    "status", "PENDING",
                    "paymentUrl", "",
                    "fallback", true
            );
        }
    }
}
