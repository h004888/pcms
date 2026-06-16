package com.pcms.common.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Correlation ID filter (CR-06).
 * <ul>
 *   <li>Reads {@code X-Correlation-Id} header from incoming request.</li>
 *   <li>Generates a new UUID if absent.</li>
 *   <li>Stores it in MDC for log enrichment.</li>
 *   <li>Sets it on response header for client visibility.</li>
 *   <li>Exposes it as a request attribute for downstream filters / services.</li>
 * </ul>
 *
 * <p>Registered automatically via component scan ({@code scanBasePackages = "com.pcms"}).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    /** Request attribute key (used by GlobalExceptionHandler to inject into ErrorResponse). */
    public static final String REQUEST_ATTR = "pcms.correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CorrelationContext.HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        try {
            MDC.put(CorrelationContext.MDC_KEY, correlationId);
            request.setAttribute(REQUEST_ATTR, correlationId);
            response.setHeader(CorrelationContext.HEADER, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationContext.MDC_KEY);
        }
    }
}
