package com.pcms.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * API Gateway entry point.
 *
 * <p>Responsibilities (CR-03):
 * <ul>
 *   <li>Single entry point for all 12 business services.</li>
 *   <li>Routes requests based on path prefix (see {@code application.yml}).</li>
 *   <li>Forwards {@code Authorization} + {@code X-Correlation-Id} + {@code Idempotency-Key} headers.</li>
 *   <li>Applies CORS policy for the frontend dev server.</li>
 * </ul>
 *
 * <p>JWT validation and rate limiting will be added in a follow-up sprint.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /** Allowed origins (frontend dev servers). */
    public static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",   // Vite default
            "http://localhost:3000",   // Create React App / Next.js
            "http://localhost:8080"    // Self
    );

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * CORS configuration for the API Gateway (CR-03, dev-friendly).
     *
     * <p>For production, replace {@code ALLOWED_ORIGINS} with the actual
     * production domain. The {@code *} value is NOT allowed in production.
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Correlation-Id",
                "Idempotency-Key",
                "X-Requested-With"
        ));
        config.setExposedHeaders(List.of(
                "X-Correlation-Id",
                "Location"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
