package com.pcms.common.event;

/**
 * Generic event publisher contract (CR-08).
 *
 * <p>All PCMS services that emit domain events depend on this interface
 * (not on Kafka/RabbitMQ APIs directly) so the underlying transport can be
 * swapped without touching producers.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code InMemoryEventPublisher} — logs events, used in dev / unit tests.</li>
 *   <li>{@code KafkaEventPublisher} — production, added in a follow-up sprint.</li>
 * </ul>
 *
 * <p>Usage in service:
 * <pre>{@code
 * eventPublisher.publish("pcms.order.paid", DomainEvents.orderPaid(...));
 * }</pre>
 */
public interface EventPublisher {

    /**
     * Publish a domain event to the given topic.
     *
     * @param topic the topic / channel name (e.g. {@code "pcms.order.paid"})
     * @param event the event envelope
     */
    void publish(String topic, DomainEvent event);
}
