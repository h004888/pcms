package com.pcms.healthtools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.pcms")
@EnableJpaAuditing
public class HealthToolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthToolsApplication.class, args);
    }
}
