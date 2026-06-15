package com.pcms.customerservice.controller;

import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC08 - Customer Profile & Loyalty
 * BR07: Award loyalty points when order is paid
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<Customer> customers = customerRepository.search(search, pageable);
        return ResponseEntity.ok(Map.of(
            "data", customers.getContent(),
            "page", customers.getNumber(),
            "size", customers.getSize(),
            "total", customers.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable UUID id) {
        return customerRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Customer> getByPhone(@PathVariable String phone) {
        return customerRepository.findByPhone(phone).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/customers - Step 5-9 of UC08 main flow
     * Auto-generates code CUST-yyyy####
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Customer customer) {
        if (customerRepository.existsByPhone(customer.getPhone())) {
            return ResponseEntity.status(409).body(Map.of("code", "MSG24", "message", "Phone already exists"));
        }
        // Step 8: Generate customer code CUST-yyyy####
        customer.setCode(generateCode());
        if (customer.getPoints() == null) customer.setPoints(0);
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable UUID id, @RequestBody Customer details) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Customer c = optional.get();
        c.setName(details.getName());
        c.setPhone(details.getPhone());
        c.setEmail(details.getEmail());
        c.setAddress(details.getAddress());
        c.setDob(details.getDob());
        c.setGender(details.getGender());
        // code and points preserved
        return ResponseEntity.ok(customerRepository.save(c));
    }

    /**
     * PUT /api/v1/customers/{id}/points - Called by payment-service (BR07)
     * Award 1 point per 1000 VND of order total
     */
    @PutMapping("/{id}/points/add")
    @Transactional
    public ResponseEntity<?> addPoints(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Customer c = optional.get();
        int added = body.getOrDefault("points", 0);
        c.setPoints(c.getPoints() + added);
        customerRepository.save(c);
        return ResponseEntity.ok(Map.of("customerId", c.getId(), "addedPoints", added, "totalPoints", c.getPoints()));
    }

    private String generateCode() {
        String year = String.valueOf(LocalDate.now().getYear());
        Pageable limit = PageRequest.of(0, 1, Sort.by("code").descending());
        List<Customer> latest = customerRepository.findByYearPrefix(year, limit);
        int nextNum = 1;
        if (!latest.isEmpty()) {
            String latestCode = latest.get(0).getCode();
            String numPart = latestCode.substring(latestCode.lastIndexOf('-') + 1);
            try { nextNum = Integer.parseInt(numPart) + 1; } catch (NumberFormatException ignored) {}
        }
        return String.format("CUST-%s%04d", year, nextNum);
    }
}
