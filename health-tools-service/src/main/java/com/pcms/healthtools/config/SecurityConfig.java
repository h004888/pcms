package com.pcms.healthtools.config;

import com.pcms.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Health Tools Security Config.
 * Extends BaseSecurityConfig to permit all requests since API Gateway
 * has already validated the JWT and forwarded identity headers.
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {
}
