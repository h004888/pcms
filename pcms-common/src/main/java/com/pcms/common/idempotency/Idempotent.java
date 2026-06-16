package com.pcms.common.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method as requiring an {@code Idempotency-Key} header (CR-05).
 *
 * <p>When this annotation is present:
 * <ul>
 *   <li>The request MUST include a non-blank {@code Idempotency-Key} header.</li>
 *   <li>If absent, the {@link com.pcms.common.idempotency.IdempotencyKeyFilter} returns 400 / MSG33.</li>
 *   <li>If present, the same response will be replayed for subsequent calls with the same key.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @PostMapping
 * @Idempotent
 * public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest req) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /**
     * Optional TTL override (seconds). Default 24h.
     */
    long ttlSeconds() default IdempotencyHeaders.TTL_SECONDS;
}
