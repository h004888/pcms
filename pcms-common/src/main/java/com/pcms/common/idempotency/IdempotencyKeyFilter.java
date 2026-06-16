package com.pcms.common.idempotency;

import com.pcms.common.correlation.CorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Idempotency-Key filter with actual dedup (B-14).
 *
 * <p>On mutating methods (POST/PUT/PATCH/DELETE), checks the {@code Idempotency-Key} header:
 * <ul>
 *   <li>Missing → log warn and pass through (soft mode)</li>
 *   <li>Same key + same request hash → replay cached response (200/201/etc.)</li>
 *   <li>Same key + different request hash → 409 Conflict (MSG09)</li>
 *   <li>New key → call controller, cache the response</li>
 * </ul>
 *
 * <p>Response caching is done by {@link CachingResponseWrapper} — the controller
 * writes to a buffer, and after the filter chain returns we serialize that
 * buffer to the store.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class IdempotencyKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyKeyFilter.class);
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final IdempotencyStore store;

    public IdempotencyKeyFilter(IdempotencyStore store) {
        this.store = store;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && (uri.contains("/healthz") || uri.contains("/readyz") || uri.contains("/actuator"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!MUTATING_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader(IdempotencyHeaders.HEADER);
        if (key == null || key.isBlank()) {
            log.warn("[CR-05] Idempotency-Key header missing on {} {} (recommend adding for safety)",
                    request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Read body once and hash it
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String body = cachedRequest.getCachedBody();
        String requestHash = store.hashRequest(body);

        // Idempotency check
        var existing = store.get(key);
        if (existing.isPresent()) {
            IdempotencyStore.Entry entry = existing.get();
            if (entry.requestHash().equals(requestHash)) {
                log.info("[CR-05] Replaying cached response for key={} (status={})",
                        key, entry.statusCode());
                response.setStatus(entry.statusCode());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(entry.responseBody());
                return;
            } else {
                log.warn("[CR-05] Key reuse with different payload for key={} (expected hash={} but got {})",
                        key, entry.requestHash(), requestHash);
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"code\":\"" + IdempotencyHeaders.CODE_CONFLICT
                                + "\",\"status\":409,\"message\":\"Idempotency-Key already used with different payload\""
                                + ",\"messageVi\":\"Idempotency-Key đã được sử dụng với payload khác\""
                                + ",\"path\":\"" + request.getRequestURI() + "\"}");
                return;
            }
        }

        // First time — process and cache
        CachingResponseWrapper cachingResponse = new CachingResponseWrapper(response);
        filterChain.doFilter(cachedRequest, cachingResponse);

        // Cache successful (2xx) responses
        int status = cachingResponse.getStatus();
        if (status >= 200 && status < 300) {
            String responseBody = cachingResponse.getCaptureAsString();
            store.put(key, requestHash, responseBody, status);
            log.debug("[CR-05] Cached response for key={} (status={})", key, status);
        }
        cachingResponse.flushBuffer();
    }

    /**
     * Wraps HttpServletRequest to allow body to be read multiple times.
     */
    static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        String getCachedBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return bais.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener readListener) { /* no-op */ }
                @Override public int read() { return bais.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Wraps HttpServletResponse to buffer the body so we can read it
     * after the controller writes, and replay it on idempotent calls.
     */
    static class CachingResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private final java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        private final jakarta.servlet.ServletOutputStream stream;
        private final java.io.PrintWriter writer;

        CachingResponseWrapper(HttpServletResponse response) {
            super(response);
            this.stream = new jakarta.servlet.ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) { /* no-op */ }
                @Override public void write(int b) throws IOException { buffer.write(b); }
                @Override public void write(byte[] b) throws IOException { buffer.write(b); }
                @Override public void write(byte[] b, int off, int len) throws IOException { buffer.write(b, off, len); }
            };
            this.writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(buffer, StandardCharsets.UTF_8));
        }

        @Override
        public jakarta.servlet.ServletOutputStream getOutputStream() { return stream; }

        @Override
        public java.io.PrintWriter getWriter() { return writer; }

        String getCaptureAsString() {
            writer.flush();
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
