package com.pcms.orderservice.saga;

/**
 * Lifecycle states for an Order Fulfillment Saga instance.
 * Transitions:
 *   STARTED → IN_PROGRESS → COMPLETED
 *                       ↘ COMPENSATING → COMPENSATED
 *                       ↘ FAILED (terminal, manual intervention)
 */
public enum SagaStatus {
    STARTED,         // Saga created but no steps executed yet
    IN_PROGRESS,     // At least one forward step completed
    COMPLETED,       // All forward steps succeeded (terminal)
    COMPENSATING,    // At least one forward step failed; running compensations
    COMPENSATED,     // All compensations completed (terminal)
    FAILED           // Compensation failed, manual intervention needed (terminal)
}