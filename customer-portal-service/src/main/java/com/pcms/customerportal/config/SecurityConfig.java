package com.pcms.customerportal.config;

import com.pcms.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Security configuration for customer-portal-service.
 * Inherits base rules from BaseSecurityConfig (B-02).
 * Gateway validates JWT; downstream services trust upstream.
 *
 * <p>Adds:
 * <ul>
 *   <li>Enable {@code @PreAuthorize} method-level security (UC14 admin endpoints).</li>
 *   <li>Lock down {@code /api/v1/admin/**} to ADMIN role at HTTP layer as well.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
        );
        // anyRequest() ở BaseSecurityConfig.securityFilterChain() được gọi sau customize()
    }
}
