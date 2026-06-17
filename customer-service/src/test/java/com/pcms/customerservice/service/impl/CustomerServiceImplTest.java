package com.pcms.customerservice.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerservice.client.OrderClient;
import com.pcms.customerservice.dto.response.CustomerHistoryResponse;
import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.repository.CustomerRepository;
import com.pcms.customerservice.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerServiceImplTest {

    @Test
    void shouldBuildCustomerHistory() {
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        LoyaltyTransactionRepository loyaltyRepository = Mockito.mock(LoyaltyTransactionRepository.class);
        OrderClient orderClient = Mockito.mock(OrderClient.class);

        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCode("CUST-20260001");
        customer.setName("Test Customer");
        customer.setPhone("0123456789");

        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Mockito.when(customerRepository.existsById(customerId)).thenReturn(true);
        Mockito.when(loyaltyRepository.findByCustomerId(Mockito.eq(customerId), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Mockito.when(orderClient.getOrdersByCustomer(customerId, null, null, null, null, 0, 20))
                .thenReturn(PageResponse.empty(0, 20));

        CustomerServiceImpl service = new CustomerServiceImpl(customerRepository, loyaltyRepository, orderClient);
        CustomerHistoryResponse history = service.getHistory(customerId, 0, 20);

        assertNotNull(history);
        assertNotNull(history.customer());
    }
}