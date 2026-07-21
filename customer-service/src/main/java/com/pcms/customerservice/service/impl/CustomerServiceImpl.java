package com.pcms.customerservice.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerservice.client.OrderClient;
import com.pcms.customerservice.client.UserClient;
import com.pcms.customerservice.dto.request.CreateCustomerRequest;
import com.pcms.customerservice.dto.request.CustomerPortalRegisterRequest;
import com.pcms.customerservice.dto.request.CustomerProvisionRequest;
import com.pcms.customerservice.dto.request.UpdateCustomerRequest;
import com.pcms.customerservice.dto.response.CustomerHistoryResponse;
import com.pcms.customerservice.dto.response.CustomerOrderSummaryResponse;
import com.pcms.customerservice.dto.response.CustomerResponse;
import com.pcms.customerservice.dto.response.LoyaltyTransactionResponse;
import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.entity.LoyaltyTransaction;
import com.pcms.customerservice.enums.LoyaltyTier;
import com.pcms.customerservice.repository.CustomerRepository;
import com.pcms.customerservice.repository.LoyaltyTransactionRepository;
import com.pcms.customerservice.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of {@link CustomerService}.
 * FR8.2: auto-generates code CUST-yyyy####.
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final LoyaltyTransactionRepository loyaltyRepository;
    private final OrderClient orderClient;
    private final UserClient userClient;

    public CustomerServiceImpl(CustomerRepository customerRepository,
            LoyaltyTransactionRepository loyaltyRepository,
            OrderClient orderClient,
            UserClient userClient) {
        this.customerRepository = customerRepository;
        this.loyaltyRepository = loyaltyRepository;
        this.orderClient = orderClient;
        this.userClient = userClient;
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
    @Transactional(readOnly = true)
    public CustomerResponse getByEmail(String email) {
        return customerRepository.findFirstByEmailIgnoreCase(email)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with email", email));
    }

    @Override
    @Transactional(readOnly = true)
    public String getTier(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return resolveTier(customer).name();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerOrderSummaryResponse> getOrders(UUID id, int page, int size) {
        ensureCustomerExists(id);
        return orderClient.getOrdersByCustomer(id, null, null, null, null, page, Math.min(size, 100));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoyaltyTransactionResponse> getPoints(UUID id, int page, int size) {
        ensureCustomerExists(id);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return PageResponse.of(loyaltyRepository.findByCustomerId(id, pageable), LoyaltyTransactionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerHistoryResponse getHistory(UUID id, int page, int size) {
        CustomerResponse customer = getById(id);
        return new CustomerHistoryResponse(
                customer,
                getOrders(id, page, size),
                getPoints(id, page, size));
    }

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Phone", request.phone());
        }
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
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
        // email is intentionally NOT updated — mirrors users.email which is locked
        c.setAddress(request.address());
        c.setDob(request.dob());
        c.setGender(request.gender());
        // code, email, points preserved
        return CustomerResponse.from(customerRepository.save(c));
    }

    @Override
    public CustomerResponse register(CustomerPortalRegisterRequest request) {
        return create(new CreateCustomerRequest(
                request.name(),
                request.phone(),
                request.email(),
                request.address(),
                request.dob(),
                request.gender()));
    }

    @Override
    @Transactional
    public CustomerResponse provisionFromUser(CustomerProvisionRequest request) {
        return customerRepository.findById(request.userId())
                .map(CustomerResponse::from)
                .orElseGet(() -> createProvisionedCustomer(request));
    }

    @Override
    public CustomerResponse updatePortalProfile(UUID id, CustomerPortalRegisterRequest request) {
        CustomerResponse response = update(id, new UpdateCustomerRequest(
                request.name(),
                request.phone(),
                request.address(),
                request.dob(),
                request.gender()));

        try {
            Map<String, Object> syncPayload = new HashMap<>();
            syncPayload.put("fullName", request.name());
            if (request.phone() != null) {
                syncPayload.put("phone", request.phone());
            }
            userClient.syncProfile(id, syncPayload);
        } catch (Exception e) {
            log.warn("Failed to sync profile to user-service for customer {}: {}", id, e.getMessage());
        }

        return response;
    }

    @Override
    public CustomerResponse updatePortalProfileByEmail(String email, CustomerPortalRegisterRequest request) {
        Customer customer = customerRepository.findFirstByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with email", email));
        return updatePortalProfile(customer.getId(), request);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        // B-15: Soft delete — set status INACTIVE instead of physical delete (preserves
        // audit trail).
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
        c.setTier(LoyaltyTier.fromPoints(newBalance));
        Customer saved = customerRepository.save(c);

        // Audit + idempotency record
        LoyaltyTransaction tx = new LoyaltyTransaction(
                id, points, newBalance, refOrderId, reason);
        loyaltyRepository.save(tx);

        return CustomerResponse.from(saved);
    }

    private LoyaltyTier resolveTier(Customer customer) {
        LoyaltyTier tier = LoyaltyTier.fromPoints(customer.getPoints());
        if (customer.getTier() != tier) {
            customer.setTier(tier);
        }
        return tier;
    }

    private void ensureCustomerExists(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }
    }

    private CustomerResponse createProvisionedCustomer(CustomerProvisionRequest request) {
        if (customerRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Phone", request.phone());
        }
        Customer customer = new Customer();
        customer.setId(request.userId());
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setDob(request.dob());
        customer.setGender(request.gender());
        customer.setPoints(0);
        customer.setTier(LoyaltyTier.BRONZE);
        customer.setCode(generateCode());
        return CustomerResponse.from(customerRepository.save(customer));
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
            if (numPart.startsWith(year)) {
                numPart = numPart.substring(year.length());
            }
            try {
                nextNum = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("CUST-%s%04d", year, nextNum);
    }
}
