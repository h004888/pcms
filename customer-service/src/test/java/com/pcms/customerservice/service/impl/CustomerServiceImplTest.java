package com.pcms.customerservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.entity.LoyaltyTransaction;
import com.pcms.customerservice.repository.CustomerRepository;
import com.pcms.customerservice.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test scaffold for CustomerServiceImpl (B-18).
 * Covers addPoints idempotency (B-08 fix from earlier work).
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private LoyaltyTransactionRepository loyaltyRepository;

    private CustomerServiceImpl customerService;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, loyaltyRepository);
        customerId = UUID.randomUUID();
    }

    @Test
    void addPoints_withValidRequest_creditsCustomerAndPersistsTransaction() {
        // Arrange
        Customer customer = new Customer("CUST-2026-0001", "Nguyen Van A", "0901234567");
        customer.setId(customerId);
        customer.setPoints(50);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loyaltyRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        UUID orderId = UUID.randomUUID();

        // Act
        var response = customerService.addPoints(customerId, 100, orderId, "Test award");

        // Assert
        assertThat(response.points()).isEqualTo(150);
        ArgumentCaptor<LoyaltyTransaction> txCaptor = ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(loyaltyRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getRefOrderId()).isEqualTo(orderId);
    }

    @Test
    void addPoints_withDuplicateRefOrderId_doesNotDoubleCredit() {
        // Arrange — simulate that a prior transaction exists for the same orderId
        UUID orderId = UUID.randomUUID();
        when(loyaltyRepository.findByRefOrderId(orderId))
                .thenReturn(Optional.of(new LoyaltyTransaction(customerId, 100, 150, orderId, "previous")));

        // ... but the customer lookup is also needed for the "already processed" return path
        Customer customer = new Customer("CUST-2026-0001", "Nguyen Van A", "0901234567");
        customer.setId(customerId);
        customer.setPoints(150);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Act
        var response = customerService.addPoints(customerId, 100, orderId, "Test retry");

        // Assert — points unchanged, no NEW transaction persisted
        assertThat(response.points()).isEqualTo(150);
        verify(loyaltyRepository, never()).save(any());
    }

    @Test
    void addPoints_withNegativeAmount_throwsInvalidOperationException() {
        // Act + Assert
        assertThatThrownBy(() -> customerService.addPoints(customerId, -1, null, "invalid"))
                .isInstanceOf(com.pcms.common.exception.InvalidOperationException.class);
    }

    // -------------------------------------------------------------------------
    // create() tests
    // -------------------------------------------------------------------------

    @Test
    void create_withValidRequest_generatesCodeAndSetsDefaults() {
        // Arrange
        var request = new com.pcms.customerservice.dto.CreateCustomerRequest(
                "Tran Thi B", "0909999888", null, null, null, null);
        when(customerRepository.existsByPhone("0909999888")).thenReturn(false);
        when(customerRepository.findByYearPrefix(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(java.util.List.of());
        when(customerRepository.save(any(com.pcms.customerservice.entity.Customer.class)))
                .thenAnswer(inv -> {
                    com.pcms.customerservice.entity.Customer c = inv.getArgument(0);
                    c.setId(UUID.randomUUID());
                    return c;
                });

        // Act
        var response = customerService.create(request);

        // Assert
        assertThat(response.code()).startsWith("CUST-");
        assertThat(response.points()).isEqualTo(0);
        assertThat(response.tier()).isEqualTo(com.pcms.customerservice.enums.LoyaltyTier.BRONZE);
        assertThat(response.status()).isEqualTo(com.pcms.customerservice.enums.CustomerStatus.ACTIVE);
    }

    @Test
    void create_withDuplicatePhone_throwsDuplicateResourceException() {
        // Arrange
        var request = new com.pcms.customerservice.dto.CreateCustomerRequest(
                "Nguyen Van C", "0901234567", null, null, null, null);
        when(customerRepository.existsByPhone("0901234567")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(com.pcms.common.exception.DuplicateResourceException.class);
        verify(customerRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // softDelete() tests
    // -------------------------------------------------------------------------

    @Test
    void softDelete_setsStatusInactive() {
        // Arrange
        com.pcms.customerservice.entity.Customer customer =
                new com.pcms.customerservice.entity.Customer("CUST-20260001", "Le Van D", "0911111111");
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(customer));
        when(customerRepository.save(any(com.pcms.customerservice.entity.Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        customerService.softDelete(customerId);

        // Assert
        ArgumentCaptor<com.pcms.customerservice.entity.Customer> captor =
                ArgumentCaptor.forClass(com.pcms.customerservice.entity.Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus())
                .isEqualTo(com.pcms.customerservice.enums.CustomerStatus.INACTIVE);
    }

    // -------------------------------------------------------------------------
    // addPoints() tier promotion tests
    // -------------------------------------------------------------------------

    @Test
    void addPoints_autoUpdatesTierToSilver_when1000Points() {
        // Arrange: customer has 900 points; adding 100 should reach 1000 → SILVER
        com.pcms.customerservice.entity.Customer customer =
                new com.pcms.customerservice.entity.Customer("CUST-20260002", "Pham Thi E", "0922222222");
        customer.setId(customerId);
        customer.setPoints(900);
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(customer));
        when(customerRepository.save(any(com.pcms.customerservice.entity.Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(loyaltyRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var response = customerService.addPoints(customerId, 100, null, "Silver threshold");

        // Assert
        assertThat(response.points()).isEqualTo(1000);
        assertThat(response.tier()).isEqualTo(com.pcms.customerservice.enums.LoyaltyTier.SILVER);
    }

    @Test
    void addPoints_autoUpdatesTierToGold_when5000Points() {
        // Arrange: customer has 4900 points; adding 100 should reach 5000 → GOLD
        com.pcms.customerservice.entity.Customer customer =
                new com.pcms.customerservice.entity.Customer("CUST-20260003", "Hoang Van F", "0933333333");
        customer.setId(customerId);
        customer.setPoints(4900);
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(customer));
        when(customerRepository.save(any(com.pcms.customerservice.entity.Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(loyaltyRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var response = customerService.addPoints(customerId, 100, null, "Gold threshold");

        // Assert
        assertThat(response.points()).isEqualTo(5000);
        assertThat(response.tier()).isEqualTo(com.pcms.customerservice.enums.LoyaltyTier.GOLD);
    }
}
