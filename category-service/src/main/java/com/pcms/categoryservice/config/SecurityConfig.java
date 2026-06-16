package com.pcms.categoryservice.config;

import com.pcms.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for category-service.
 * Inherits base rules from BaseSecurityConfig (B-02).
 * Gateway validates JWT; downstream services trust upstream.
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {
}
