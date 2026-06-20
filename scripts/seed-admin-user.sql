-- ============================================================================
-- PCMS Admin User Seed Script
-- Creates default admin user for testing
-- Password: admin123 (BCrypt hash)
-- ============================================================================
USE pcms_user;

INSERT INTO users (id, email, password_hash, full_name, phone, role, status, email_verified, created_at, updated_at)
VALUES (
    UUID_TO_BIN(UUID(), 1),
    'admin@pcms.vn',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Administrator',
    '0901234567',
    'ADMIN',
    'ACTIVE',
    1,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO users (id, email, password_hash, full_name, phone, role, status, email_verified, created_at, updated_at)
VALUES (
    UUID_TO_BIN(UUID(), 1),
    'pharmacist01@pcms.vn',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Nguyễn Văn A',
    '0901234567',
    'PHARMACIST',
    'ACTIVE',
    1,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();

SELECT 'Admin user seeded successfully' AS status;
