package com.pcms.common.idempotency;

/**
 * Constants for the Idempotency-Key contract (CR-05).
 *
 * <p>PCMS adopts the IETF draft-ietf-httpapi-idempotency-key-header convention:
 * <ul>
 *   <li>Clients send {@code Idempotency-Key: <uuid>} on POST/PUT/DELETE.</li>
 *   <li>Servers persist the (key, request-hash, response) tuple for 24h.</li>
 *   <li>Replay with same key + same hash returns the stored response.</li>
 *   <li>Replay with same key + different hash returns 409 Conflict.</li>
 * </ul>
 */
public final class IdempotencyHeaders {

    /** HTTP header carrying the idempotency key. */
    public static final String HEADER = "Idempotency-Key";

    /** Conflict status when same key is used with different payload. */
    public static final int STATUS_CONFLICT = 409;

    /** Error code mapped to MSG09. */
    public static final String CODE_CONFLICT = "MSG09";

    /** TTL for stored idempotency records (24h in seconds). */
    public static final long TTL_SECONDS = 24 * 60 * 60L;

    private IdempotencyHeaders() {
        // utility class
    }
}
