package com.pcms.orderservice.service.impl;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.orderservice.entity.DeadLetterEvent;
import com.pcms.orderservice.entity.OutboxEvent;
import com.pcms.orderservice.repository.DeadLetterEventRepository;
import com.pcms.orderservice.repository.OutboxEventRepository;
import com.pcms.orderservice.service.OutboxAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class OutboxAdminServiceImpl implements OutboxAdminService {

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxAdminServiceImpl(DeadLetterEventRepository deadLetterEventRepository,
            OutboxEventRepository outboxEventRepository) {
        this.deadLetterEventRepository = deadLetterEventRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public UUID retryDeadLetter(UUID deadLetterEventId) {
        DeadLetterEvent deadLetterEvent = deadLetterEventRepository.findById(deadLetterEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Dead letter event", deadLetterEventId));

        OutboxEvent outboxEvent = new OutboxEvent(
                deadLetterEvent.getAggregateType(),
                deadLetterEvent.getAggregateId(),
                deadLetterEvent.getEventType(),
                deadLetterEvent.getTargetService(),
                deadLetterEvent.getEndpoint(),
                deadLetterEvent.getPayload());
        outboxEvent.setNextAttemptAt(LocalDateTime.now());
        outboxEvent.setRetryCount(0);
        OutboxEvent saved = outboxEventRepository.save(outboxEvent);

        deadLetterEvent.setResolved(true);
        deadLetterEvent.setResolvedAt(LocalDateTime.now());
        deadLetterEventRepository.save(deadLetterEvent);
        return saved.getId();
    }
}