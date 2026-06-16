-- ============================================================================
-- PCMS Branch Service — Seed data
-- 5 chi nhanh mau (HCM Quan 1, HCM Quan 3, HN Hoan Kiem, DN Hai Chau, HCM Tan Binh)
-- ============================================================================
INSERT INTO branches (id, code, name, address, phone, manager_id, status, created_at, updated_at) VALUES
(UUID(), 'HCM-Q1', N'Chi nhánh Quận 1', N'123 Nguyễn Huệ, Quận 1, TP.HCM', '0281234567', NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'HCM-Q3', N'Chi nhánh Quận 3', N'456 Võ Văn Tần, Quận 3, TP.HCM', '0282345678', NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'HN-HK',  N'Chi nhánh Hoàn Kiếm', N'789 Tràng Tiền, Hoàn Kiếm, Hà Nội', '0243456789', NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'DN-HC',  N'Chi nhánh Hải Châu', N'321 Lê Duẩn, Hải Châu, Đà Nẵng', '0236456789', NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'HCM-TB', N'Chi nhánh Tân Bình', N'654 Cộng Hòa, Tân Bình, TP.HCM', '0284567890', NULL, 'ACTIVE', NOW(), NOW());
