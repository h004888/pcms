package com.pcms.customerportal.config;

import com.pcms.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for customer-portal-service.
 * Inherits base rules from BaseSecurityConfig (B-02).
 * Gateway validates JWT; downstream services trust upstream.
 *
 * <p>This service is primarily B2C, so most endpoints accept guest access.
 * Endpoints requiring authentication are guarded at the service level
 * (e.g. /cart, /favorites) using @PreAuthorize or JwtClaims inspection.
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {
}
