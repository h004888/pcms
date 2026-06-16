package com.pcms.common.correlation;

/**
 * Constants for correlation-id propagation across services (CR-06).
 * <p>Header name and MDC key for end-to-end request tracing.
 */
public final class CorrelationContext {

    /** HTTP header carrying the correlation id. */
    public static final String HEADER = "X-Correlation-Id";

    /** MDC key used by SLF4J / Logback. */
    public static final String MDC_KEY = "correlationId";

    /** Field name in {@link com.pcms.common.dto.ErrorResponse}. */
    public static final String FIELD = "correlationId";

    private CorrelationContext() {
        // utility class
    }
}
