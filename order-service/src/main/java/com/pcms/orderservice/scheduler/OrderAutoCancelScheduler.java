package com.pcms.orderservice.scheduler;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderStatusHistory;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import com.pcms.orderservice.repository.OrderStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;

/**
 * NSF-01: Auto-cancel unpaid orders every 15 minutes
 * BR01: Cancel orders in PENDING_PAYMENT > 24h
 */
@Component
public class OrderAutoCancelScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelScheduler.class);

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Value("${order.pending-payment-timeout-hours:24}")
    private int timeoutHours;

    public OrderAutoCancelScheduler(OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Scheduled(fixedRate = 900_000) // 15 min = 900_000 ms
    @Transactional
    public void autoCancelStaleOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(timeoutHours);
        List<Order> stale = orderRepository.findStalePendingOrders(cutoff);
        for (Order order : stale) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            orderStatusHistoryRepository.save(new OrderStatusHistory(
                    order.getId(), OrderStatus.CANCELLED, Instant.now(), null,
                    "Tu dong huy do qua han thanh toan"));
            log.info("NSF-01/BR01: Auto-cancelled order {} (created {})",
                    order.getOrderNumber(), order.getCreatedAt());
            // TODO(PCMS-UC13): emit notification to customer (UC13, MSG19)
        }
        if (!stale.isEmpty()) {
            log.info("NSF-01: Cancelled {} stale pending orders", stale.size());
        }
    }
}
