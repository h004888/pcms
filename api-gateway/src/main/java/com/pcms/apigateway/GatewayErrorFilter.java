package com.pcms.apigateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class GatewayErrorFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorFilter.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String hostname;

    public GatewayErrorFilter(@Value("${server.hostname:-}") String hostname) {
        this.hostname = hostname;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            if (response.isCommitted()) {
                log.warn("[gateway] Response already committed, cannot send 503 for {} -> {}",
                        request.getRequestURI(), e.getMessage());
                return;
            }

            log.error("[gateway] Routing failure for {} -> {}", request.getRequestURI(), e.getMessage(), e);
            writeServiceUnavailable(request, response);
        }
    }

    private void writeServiceUnavailable(HttpServletRequest request,
                                         HttpServletResponse response) {
        String path = request.getRequestURI();
        String origin = request.getHeader("Origin");
        String allowedOrigin = origin != null && ApiGatewayApplication.ALLOWED_ORIGINS.contains(origin)
                ? origin
                : "http://localhost:5173";

        try {
            response.reset();
        } catch (IllegalStateException ignored) {
            // Buffer already flushed
        }

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Vary", "Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "MSG35");
        body.put("status", 503);
        body.put("message", "Service Unavailable");
        body.put("messageVi", "Dịch vụ tạm thời chưa sẵn sàng");
        body.put("path", path);
        body.put("timestamp", Instant.now().toString());

        try {
            objectMapper.writeValue(response.getOutputStream(), body);
        } catch (Exception e) {
            log.error("[gateway] Failed to write 503 body for {}", path, e);
        }
    }
}
