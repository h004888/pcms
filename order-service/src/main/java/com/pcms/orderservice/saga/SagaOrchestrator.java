package com.pcms.orderservice.saga;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderItem;
import com.pcms.orderservice.entity.OutboxEvent;
import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.OutboxEventRepository;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Central saga orchestrator.
 *
 * <p>Defines the step sequence for each SagaType, persists the SagaInstance
 * and its SagaSteps in one transaction, then emits OutboxEvents for each step.
 *
 * <p>Compensation logic is delegated to {@link SagaCompensationHandler}.
 * Step execution status is monitored by {@code SagaTimeoutScheduler}.
 *
 * <p>B-17: Saga pattern - orchestration flavor.
 */
@Service
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final SagaInstanceRepository sagaRepo;
    private final SagaStepRepository stepRepo;
    private final OutboxEventRepository outboxRepo;

    public SagaOrchestrator(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            OutboxEventRepository outboxRepo) {
        this.sagaRepo = sagaRepo;
        this.stepRepo = stepRepo;
        this.outboxRepo = outboxRepo;
    }

    /**
     * Start a new Order Fulfillment Saga. Persists the saga and all forward
     * steps + their compensations, then emits one OutboxEvent per forward step.
     */
    @Transactional
    public SagaInstance startOrderFulfillment(Order order, UUID actorId) {
        // Idempotency: skip if a non-terminal saga for this order already exists
        var existing = sagaRepo.findByAggregateTypeAndAggregateId("Order", order.getId());
        if (existing.isPresent() && existing.get().getStatus() != SagaStatus.COMPENSATED
                && existing.get().getStatus() != SagaStatus.FAILED) {
            log.info("[saga] Saga already exists for order {}, status={}",
                    order.getId(), existing.get().getStatus());
            return existing.get();
        }

        SagaInstance saga = new SagaInstance();
        saga.setSagaType(SagaType.ORDER_FULFILLMENT);
        saga.setStatus(SagaStatus.STARTED);
        saga.setAggregateType("Order");
        saga.setAggregateId(order.getId());
        saga.setCorrelationId(UUID.randomUUID().toString());
        saga.setStartedAt(LocalDateTime.now());
        saga = sagaRepo.save(saga);

        int stepOrder = 1;

        // Forward step 1: stock consume (bulk)
        StringBuilder itemsJson = new StringBuilder("[");
        for (OrderItem item : order.getItems()) {
            if (itemsJson.length() > 1) itemsJson.append(",");
            itemsJson.append(String.format(
                    "{\"medicineId\":\"%s\",\"branchId\":\"%s\",\"qty\":%d,\"actorId\":\"%s\",\"orderId\":\"%s\"}",
                    item.getMedicineId(), order.getBranchId(), item.getQty(), actorId, order.getId()));
        }
        itemsJson.append("]");

        SagaStep stockStep = createStep(saga, stepOrder++, "STOCK_CONSUMED",
                "inventory-service", "/inventory/orders/" + order.getId() + "/paid-bulk",
                "{\"items\":" + itemsJson + "}", false, null);

        // Forward step 2: points award (only if total >= 1000 VND worth of points)
        int points = order.getTotal().divide(java.math.BigDecimal.valueOf(1000), 0,
                java.math.RoundingMode.FLOOR).intValue();
        if (points > 0) {
            SagaStep pointsStep = createStep(saga, stepOrder++, "POINTS_AWARDED",
                    "customer-service", "/customers/" + order.getCustomerId() + "/points/add",
                    String.format("{\"points\":%d,\"refOrderId\":\"%s\",\"reason\":\"ORDER_PAID:%s\"}",
                            points, order.getId(), order.getOrderNumber()),
                    false, null);
            registerCompensation(pointsStep, "POINTS_REVERSAL",
                    "customer-service", "/customers/" + order.getCustomerId() + "/points/reverse",
                    String.format("{\"points\":%d,\"refOrderId\":\"%s\",\"reason\":\"ORDER_CANCELLED:%s\"}",
                            points, order.getId(), order.getOrderNumber()));
        }

        // Forward step 3: notification
        SagaStep notificationStep = createStep(saga, stepOrder++, "ORDER_PAID_NOTIFICATION",
                "notification-service", "/notifications/orders/paid",
                String.format(
                        "{\"orderId\":\"%s\",\"orderNumber\":\"%s\",\"customerId\":\"%s\",\"branchId\":\"%s\",\"staffId\":\"%s\",\"total\":%s}",
                        order.getId(), order.getOrderNumber(), order.getCustomerId(), order.getBranchId(),
                        actorId, order.getTotal()),
                false, null);
        registerCompensation(notificationStep, "ORDER_CANCELLED_NOTIFICATION",
                "notification-service", "/notifications/orders/cancelled",
                String.format("{\"orderId\":\"%s\",\"orderNumber\":\"%s\"}",
                        order.getId(), order.getOrderNumber()));

        saga.setStatus(SagaStatus.IN_PROGRESS);
        saga = sagaRepo.save(saga);

        // Emit OutboxEvent for each forward step
        for (SagaStep step : saga.getSteps()) {
            if (Boolean.FALSE.equals(step.getCompensation())) {
                OutboxEvent event = new OutboxEvent(
                        "SagaInstance", saga.getId(), step.getStepName(),
                        step.getTargetService(), step.getEndpoint(), step.getPayload());
                event = outboxRepo.save(event);
                step.setOutboxEventId(event.getId());
                step.setStatus(SagaStepStatus.IN_PROGRESS);
                step.setExecutedAt(LocalDateTime.now());
                stepRepo.save(step);
            }
        }

        log.info("[saga] Started Order Fulfillment saga={} for order={} with {} steps",
                saga.getId(), order.getId(), saga.getSteps().size());
        return saga;
    }

    private SagaStep createStep(SagaInstance saga, int order, String name, String targetService,
            String endpoint, String payload, boolean compensation, UUID compensatesStepId) {
        SagaStep step = new SagaStep();
        step.setSaga(saga);
        step.setStepOrder(order);
        step.setStepName(name);
        step.setTargetService(targetService);
        step.setEndpoint(endpoint);
        step.setPayload(payload);
        step.setCompensation(compensation);
        step.setCompensatesStepId(compensatesStepId);
        step.setStatus(SagaStepStatus.PENDING);
        return stepRepo.save(step);
    }

    private void registerCompensation(SagaStep forwardStep, String compName,
            String targetService, String endpoint, String payload) {
        SagaInstance saga = forwardStep.getSaga();
        SagaStep comp = createStep(saga, forwardStep.getStepOrder() + 1000, compName,
                targetService, endpoint, payload, true, forwardStep.getId());
        comp.setCompensatesStepId(forwardStep.getId());
        stepRepo.save(comp);
    }
}