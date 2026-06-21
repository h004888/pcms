package com.pcms.orderservice.saga;

/**
 * Status of a single SagaStep. Each step records both its forward status
 * and the id of the compensating step (if any) for reversal.
 */
public enum SagaStepStatus {
    PENDING,         // Step not yet executed
    IN_PROGRESS,     // Step execution started (e.g., outbox event published)
    COMPLETED,       // Step succeeded (consumer acknowledged or synchronous OK)
    FAILED,          // Step forward execution failed after retries
    COMPENSATED,     // Compensating step succeeded
    COMPENSATION_FAILED  // Compensating step failed - saga becomes FAILED
}