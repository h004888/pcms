package com.pcms.pharmacistworkbench.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.pharmacistworkbench.client.CustomerClient;
import com.pcms.pharmacistworkbench.dto.response.CustomerProfile360Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregates customer data from multiple services for the 360° pharmacist view.
 */
@Service
public class Customer360Service {

    private static final Logger log = LoggerFactory.getLogger(Customer360Service.class);

    private final CustomerClient customerClient;

    public Customer360Service(CustomerClient customerClient) {
        this.customerClient = customerClient;
    }

    @Transactional(readOnly = true)
    public CustomerProfile360Response get360(UUID customerId) {
        Map<String, Object> customer;
        try {
            customer = customerClient.getById(customerId.toString());
        } catch (Exception e) {
            log.warn("Failed to fetch customer {}: {}", customerId, e.getMessage());
            customer = Map.of();
        }
        if (customer == null || customer.isEmpty()) {
            throw new ResourceNotFoundException("Customer", customerId);
        }

        // Fetch orders + history
        Map<String, Object> orders, history;
        try {
            orders = customerClient.getOrders(customerId.toString());
        } catch (Exception e) {
            log.warn("Failed to fetch orders for {}: {}", customerId, e.getMessage());
            orders = Map.of("data", List.of());
        }
        try {
            history = customerClient.getHistory(customerId.toString());
        } catch (Exception e) {
            log.warn("Failed to fetch history for {}: {}", customerId, e.getMessage());
            history = Map.of();
        }

        @SuppressWarnings("unchecked")
        List<Object> recentOrders = (List<Object>) orders.getOrDefault("data", List.of());
        @SuppressWarnings("unchecked")
        List<Object> recentPrescriptions = (List<Object>) history.getOrDefault("prescriptions", List.of());

        return new CustomerProfile360Response(
                customerId,
                (String) customer.getOrDefault("name", ""),
                (String) customer.getOrDefault("phone", ""),
                (String) customer.getOrDefault("email", ""),
                (String) customer.getOrDefault("tier", "BRONZE"),
                (Integer) customer.getOrDefault("points", 0),
                List.of(),  // allergies (extend later)
                List.of(),  // chronicConditions (extend later)
                recentOrders,
                recentPrescriptions,
                (String) customer.getOrDefault("preferredPharmacist", null),
                "AI summary disabled",
                null
        );
    }
}
