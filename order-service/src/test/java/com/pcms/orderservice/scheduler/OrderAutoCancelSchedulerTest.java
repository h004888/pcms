package com.pcms.orderservice.scheduler;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderStatusHistory;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import com.pcms.orderservice.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAutoCancelSchedulerTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @InjectMocks private OrderAutoCancelScheduler scheduler;

    @Test
    void autoCancelStaleOrders_recordsAutomaticCancellation() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findStalePendingOrders(any())).thenReturn(List.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        scheduler.autoCancelStaleOrders();

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }
}
