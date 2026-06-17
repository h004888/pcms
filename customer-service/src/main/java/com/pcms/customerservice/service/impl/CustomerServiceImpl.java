package com.pcms.customerservice.service.impl;

import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerservice.enums.LoyaltyTier;
import com.pcms.customerservice.dto.CreateCustomerRequest;
import com.pcms.customerservice.dto.CustomerResponse;
import com.pcms.customerservice.dto.UpdateCustomerRequest;
import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.entity.LoyaltyTransaction;
import com.pcms.customerservice.repository.CustomerRepository;
import com.pcms.customerservice.repository.LoyaltyTransactionRepository;
import com.pcms.customerservice.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link CustomerService}.
 * FR8.2: auto-generates code CUST-yyyy####.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LoyaltyTransactionRepository loyaltyRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                              LoyaltyTransactionRepository loyaltyRepository) {
        this.customerRepository = customerRepository;
        this.loyaltyRepository = loyaltyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return customerRepository.search(search, pageable).map(CustomerResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        return customerRepository.findById(id)
            .map(CustomerResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByCode(String code) {
        return customerRepository.findByCode(code)
            .map(CustomerResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", code));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByPhone(String phone) {
        return customerRepository.findByPhone(phone)
            .map(CustomerResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Customer with phone", phone));
    }

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Phone", request.phone());
        }
        Customer c = new Customer();
        c.setName(request.name());
        c.setPhone(request.phone());
        c.setEmail(request.email());
        c.setAddress(request.address());
        c.setDob(request.dob());
        c.setGender(request.gender());
        c.setPoints(0);
        c.setTier(LoyaltyTier.BRONZE);
        c.setCode(generateCode());
        return CustomerResponse.from(customerRepository.save(c));
    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        // Phone uniqueness: if changed, check collision
        if (!c.getPhone().equals(request.phone()) && customerRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Phone", request.phone());
        }
        c.setName(request.name());
        c.setPhone(request.phone());
        c.setEmail(request.email());
        c.setAddress(request.address());
        c.setDob(request.dob());
        c.setGender(request.gender());
        // code and points preserved
        return CustomerResponse.from(customerRepository.save(c));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        // B-15: Soft delete — set status INACTIVE instead of physical delete (preserves audit trail).
        c.setStatus(com.pcms.customerservice.enums.CustomerStatus.INACTIVE);
        customerRepository.save(c);
    }

    @Override
    @Transactional
    public CustomerResponse addPoints(UUID id, int points, UUID refOrderId, String reason) {
        if (points < 0) {
            throw new InvalidOperationException(
                "Points to add must be non-negative",
                "Số điểm cần cộng phải không âm");
        }

        // Idempotency: if a transaction with the same refOrderId already exists, skip
        if (refOrderId != null) {
            var existing = loyaltyRepository.findByRefOrderId(refOrderId);
            if (existing.isPresent()) {
                Customer c = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
                return CustomerResponse.from(c);
            }
        }

        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        int newBalance = (c.getPoints() == null ? 0 : c.getPoints()) + points;
        c.setPoints(newBalance);
        c.setTier(LoyaltyTier.of(newBalance));
        Customer saved = customerRepository.save(c);

        // Audit + idempotency record
        LoyaltyTransaction tx = new LoyaltyTransaction(
                id, points, newBalance, refOrderId, reason);
        loyaltyRepository.save(tx);

        return CustomerResponse.from(saved);
    }

    /**
     * FR8.2: Generate next customer code CUST-yyyy#### by scanning the largest
     * existing code for the current year prefix.
     */
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
