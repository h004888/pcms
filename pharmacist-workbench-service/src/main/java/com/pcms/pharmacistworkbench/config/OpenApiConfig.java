package com.pcms.pharmacistworkbench.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Pharmacist Workbench Service")
                .version("1.0.0")
                .description("UC16 - Pharmacist Workbench APIs (RX-CONSOLE, consultation, follow-up, VIP)"));
    }
}
