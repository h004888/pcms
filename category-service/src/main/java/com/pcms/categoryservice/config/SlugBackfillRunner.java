package com.pcms.categoryservice.config;

import com.pcms.categoryservice.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Backfill missing slug cho record Category hiện có khi service khởi động.
 * Chạy 1 lần duy nhất nhờ JPA ddl-auto + service init.
 */
@Component
@Order(Integer.MIN_VALUE + 10)
public class SlugBackfillRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SlugBackfillRunner.class);

    private final CategoryService categoryService;

    public SlugBackfillRunner(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        try {
            int updated = categoryService.backfillSlugs();
            if (updated > 0) {
                log.info("Slug backfill: cập nhật {} category", updated);
            }
        } catch (Exception ex) {
            log.warn("Slug backfill thất bại (không ảnh hưởng startup): {}", ex.getMessage());
        }
    }
}
