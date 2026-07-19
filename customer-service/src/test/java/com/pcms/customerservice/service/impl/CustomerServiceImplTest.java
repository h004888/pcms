package com.pcms.customerservice.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.customerservice.client.OrderClient;
import com.pcms.customerservice.dto.request.CreateCustomerRequest;
import com.pcms.customerservice.dto.request.CustomerProvisionRequest;
import com.pcms.customerservice.dto.response.CustomerHistoryResponse;
import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.entity.LoyaltyTransaction;
import com.pcms.customerservice.enums.CustomerStatus;
import com.pcms.customerservice.enums.LoyaltyTier;
import com.pcms.customerservice.repository.CustomerRepository;
import com.pcms.customerservice.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    private CustomerRepository customerRepository;
    private LoyaltyTransactionRepository loyaltyRepository;
    private OrderClient orderClient;
    private CustomerServiceImpl customerService;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerRepository = Mockito.mock(CustomerRepository.class);
        loyaltyRepository = Mockito.mock(LoyaltyTransactionRepository.class);
        orderClient = Mockito.mock(OrderClient.class);
        customerService = new CustomerServiceImpl(customerRepository, loyaltyRepository, orderClient);
        customerId = UUID.randomUUID();
    }

    @Test
    void shouldBuildCustomerHistory() {
        UUID cid = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(cid);
        customer.setCode("CUST-20260001");
        customer.setName("Test Customer");
        customer.setPhone("0123456789");

        Mockito.when(customerRepository.findById(cid)).thenReturn(Optional.of(customer));
        Mockito.when(customerRepository.existsById(cid)).thenReturn(true);
        Mockito.when(loyaltyRepository.findByCustomerId(Mockito.eq(cid), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Mockito.when(orderClient.getOrdersByCustomer(cid, null, null, null, null, 0, 20))
                .thenReturn(PageResponse.empty(0, 20));

        CustomerServiceImpl service = new CustomerServiceImpl(customerRepository, loyaltyRepository, orderClient);
        CustomerHistoryResponse history = service.getHistory(cid, 0, 20);

        assertNotNull(history);
        assertNotNull(history.customer());
    }

    private void assertNotNull(Object obj) {
        assertThat(obj).isNotNull();
    }

    @Test
    void addPoints_withDuplicateRefOrderId_doesNotDoubleCredit() {
        UUID orderId = UUID.randomUUID();
        when(loyaltyRepository.findByRefOrderId(orderId))
                .thenReturn(Optional.of(new LoyaltyTransaction(customerId, 100, 150, orderId, "previous")));

        Customer customer = new Customer("CUST-2026-0001", "Nguyen Van A", "0901234567");
        customer.setId(customerId);
        customer.setPoints(150);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        var response = customerService.addPoints(customerId, 100, orderId, "Test retry");

        assertThat(response.points()).isEqualTo(150);
        verify(loyaltyRepository, never()).save(any());
    }

    @Test
    void addPoints_withNegativeAmount_throwsInvalidOperationException() {
        assertThatThrownBy(() -> customerService.addPoints(customerId, -1, null, "invalid"))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void create_withValidRequest_generatesCodeAndSetsDefaults() {
        var request = new CreateCustomerRequest(
                "Tran Thi B", "0909999888", null, null, null, null);
        when(customerRepository.existsByPhone("0909999888")).thenReturn(false);
        when(customerRepository.findByYearPrefix(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(java.util.List.of());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = customerService.create(request);

        assertThat(response.code()).startsWith("CUST-");
        assertThat(response.points()).isEqualTo(0);
        assertThat(response.tier()).isEqualTo(LoyaltyTier.BRONZE);
        assertThat(response.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(response.id()).isNotNull();
    }

    @Test
    void provisionFromUser_withNewUserId_createsCustomerWithUserId() {
        UUID userId = UUID.randomUUID();
        var request = new CustomerProvisionRequest(
                userId, "Nguyen Van A", "0901234567", null, null, null, null);
        when(customerRepository.findById(userId)).thenReturn(Optional.empty());
        when(customerRepository.existsByPhone(request.phone())).thenReturn(false);
        when(customerRepository.findByYearPrefix(anyString(), any())).thenReturn(List.of());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = customerService.provisionFromUser(request);

        assertThat(response.id()).isEqualTo(userId);
        verify(customerRepository).save(argThat(customer -> customer.getId().equals(userId)));
    }

    @Test
    void provisionFromUser_withExistingUserId_returnsExistingCustomerWithoutSaving() {
        UUID userId = UUID.randomUUID();
        Customer existing = new Customer("CUST-20260001", "Existing", "0901234567");
        existing.setId(userId);
        var request = new CustomerProvisionRequest(
                userId, "Changed", "0909999888", null, null, null, null);
        when(customerRepository.findById(userId)).thenReturn(Optional.of(existing));

        var response = customerService.provisionFromUser(request);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Existing");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void provisionFromUser_withDuplicatePhone_throwsDuplicateResourceException() {
        UUID userId = UUID.randomUUID();
        var request = new CustomerProvisionRequest(
                userId, "Nguyen Van A", "0901234567", null, null, null, null);
        when(customerRepository.findById(userId)).thenReturn(Optional.empty());
        when(customerRepository.existsByPhone(request.phone())).thenReturn(true);

        assertThatThrownBy(() -> customerService.provisionFromUser(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void create_withDuplicatePhone_throwsDuplicateResourceException() {
        var request = new CreateCustomerRequest(
                "Nguyen Van C", "0901234567", null, null, null, null);
        when(customerRepository.existsByPhone("0901234567")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void softDelete_setsStatusInactive() {
        Customer customer = new Customer("CUST-20260001", "Le Van D", "0911111111");
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        customerService.softDelete(customerId);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void addPoints_autoUpdatesTierToSilver_when1000Points() {
        Customer customer = new Customer("CUST-20260002", "Pham Thi E", "0922222222");
        customer.setId(customerId);
        customer.setPoints(900);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(loyaltyRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = customerService.addPoints(customerId, 100, null, "Silver threshold");

        assertThat(response.points()).isEqualTo(1000);
        assertThat(response.tier()).isEqualTo(LoyaltyTier.SILVER);
    }

    @Test
    void addPoints_autoUpdatesTierToGold_when5000Points() {
        Customer customer = new Customer("CUST-20260003", "Hoang Van F", "0933333333");
        customer.setId(customerId);
        customer.setPoints(4900);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(loyaltyRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = customerService.addPoints(customerId, 100, null, "Gold threshold");

        assertThat(response.points()).isEqualTo(5000);
        assertThat(response.tier()).isEqualTo(LoyaltyTier.GOLD);
    }
}
