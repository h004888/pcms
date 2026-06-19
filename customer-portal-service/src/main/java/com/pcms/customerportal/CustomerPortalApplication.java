package com.pcms.customerportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Customer Portal (B2C) Service - aggregates catalog, branch, inventory, etc.
 * to serve B2C use cases (UC14, UC15, UC18, UC19):
 * - SHOP-HOME, SHOP-CAT-*, SHOP-PDP, SHOP-SEARCH
 * - STORE-LOCATOR, STORE-LIST-PROVINCE, STORE-DETAIL
 * - SHOP-LOOKUP-DRUG, SHOP-LOOKUP-INGREDIENT, SHOP-LOOKUP-HERB
 * - VACCINE-HOME, VACCINE-BOOKING, VACCINE-LEDGER
 * - HEALTH-ARTICLE, DISEASE-INFO, VIDEOS
 * - VERIFY-ORIGIN
 *
 * Runs on port 8093.
 */
@SpringBootApplication(scanBasePackages = "com.pcms")
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
public class CustomerPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerPortalApplication.class, args);
    }
}
