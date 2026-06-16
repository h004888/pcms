package com.pcms.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Default in-memory event publisher — logs to SLF4J (CR-08).
 *
 * <p>Active when no other {@link EventPublisher} bean is defined.
 * Swap with a Kafka-backed implementation when the event bus is provisioned.
 */
@Component
@ConditionalOnMissingBean(EventPublisher.class)
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(String topic, DomainEvent event) {
        log.info("[event] topic={} type={} id={} aggregateId={} correlationId={} payload={}",
                topic,
                event.eventType(),
                event.eventId(),
                event.aggregateId(),
                event.correlationId(),
                event.payload());
    }
}
