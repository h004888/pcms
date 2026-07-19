package com.pcms.catalogservice.config;

import com.pcms.catalogservice.service.MedicineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Backfill missing slug cho record Medicine hiện có khi service khởi động.
 * Có thể chạy sau category-service (cùng @Order), hoặc độc lập.
 */
@Component
@Order(Integer.MIN_VALUE + 10)
public class SlugBackfillRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SlugBackfillRunner.class);

    private final MedicineService medicineService;

    public SlugBackfillRunner(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Override
    public void run(String... args) {
        try {
            int updated = medicineService.backfillSlugs();
            if (updated > 0) {
                log.info("Slug backfill: cập nhật {} medicine", updated);
            }
        } catch (Exception ex) {
            log.error("Slug backfill thất bại (không ảnh hưởng startup): {}", ex.getMessage(), ex);
        }
    }
}
