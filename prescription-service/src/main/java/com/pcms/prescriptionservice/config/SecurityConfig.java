package com.pcms.prescriptionservice.config;

import com.pcms.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for prescription-service.
 * Inherits base rules from BaseSecurityConfig (B-02).
 * Gateway validates JWT; downstream services trust upstream.
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {
}
