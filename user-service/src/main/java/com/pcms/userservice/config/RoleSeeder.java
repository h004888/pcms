package com.pcms.userservice.config;

import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seed admin user khi service start.
 * Idempotent: check existsByEmail trước khi insert.
 * Default credentials: admin@pcms.local / admin123 (change in production).
 */
@Component
public class RoleSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleSeeder.class);

    private static final String ADMIN_EMAIL = "admin@pcms.local";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NAME = "System Admin";

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public RoleSeeder(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
    }

    private void seedAdmin() {
        if (userRepo.existsByEmail(ADMIN_EMAIL)) {
            log.info("Admin user already exists: {}", ADMIN_EMAIL);
            return;
        }
        User admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setFullName(ADMIN_NAME);
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setBranchId(null);
        userRepo.save(admin);
        log.warn("Seeded admin user: {} / {} (CHANGE PASSWORD IN PRODUCTION)",
                ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
