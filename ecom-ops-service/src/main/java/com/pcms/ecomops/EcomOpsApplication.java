package com.pcms.ecomops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.pcms")
@EnableJpaAuditing
public class EcomOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcomOpsApplication.class, args);
    }
}
