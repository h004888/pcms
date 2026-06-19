package com.pcms.customerportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for customer-portal-service.
 * Aligned with PCMS-wide standards from CODE_RULES.md.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PCMS Customer Portal API")
                        .description("B2C Customer Portal (UC14, UC18) - E-commerce, lookup, store locator, vaccine, health articles, videos")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PCMS Team")
                                .email("pcms@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Via API Gateway"),
                        new Server().url("http://localhost:8093").description("Direct (dev only)")
                ));
    }
}
