package com.pcms.ecomops.config;

import com.pcms.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;

/**
 * E-commerce Ops Security Config.
 * Extends BaseSecurityConfig to permit all requests since API Gateway
 * has already validated the JWT and forwarded identity headers.
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {
}
