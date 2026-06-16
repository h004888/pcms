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
}
