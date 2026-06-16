package com.pcms.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Base security configuration shared by all PCMS services (B-02).
 *
 * <p>The API Gateway validates JWT and forwards identity via headers
 * (X-User-Id, X-User-Role, X-Branch-Id) — see
 * {@code api-gateway/JwtAuthenticationFilter}. Each downstream service
 * can therefore <b>trust the upstream gateway</b> and just permit all.
 *
 * <p>To enable per-endpoint authorization at service level, extend this
 * class and override {@link #customize(HttpSecurity)}:
 *
 * <pre>{@code
 * @Configuration
 * public class SecurityConfig extends BaseSecurityConfig {
 *     @Override
 *     protected void customize(HttpSecurity http) throws Exception {
 *         http.authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/admin/**").hasRole("ADMIN")
 *             .anyRequest().permitAll());
 *     }
 * }
 * }</pre>
 */
@Configuration
public abstract class BaseSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/healthz", "/readyz").permitAll()
                .anyRequest().permitAll()
            );
        customize(http);
        return http.build();
    }

    /**
     * Hook for subclasses to add per-endpoint rules.
     * Default: no-op.
     */
    protected void customize(HttpSecurity http) throws Exception {
        // no-op
    }
}
