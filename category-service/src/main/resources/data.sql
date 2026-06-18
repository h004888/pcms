-- ============================================================================
-- PCMS Category Service — Seed data
-- 10 categories mau
-- ============================================================================
INSERT INTO categories (id, name, description, status, created_at, updated_at) VALUES
(UUID(), N'Giảm đau - Hạ sốt',    N'Thuốc giảm đau, hạ sốt thông dụng', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Kháng sinh',          N'Thuốc kháng sinh các loại', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Vitamin - Khoáng chất', N'Bổ sung vitamin và khoáng chất', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Cảm cúm - Ho',        N'Thuốc trị cảm cúm, ho, sổ mũi', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Tiêu hóa',            N'Thuốc trị các bệnh tiêu hóa', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Tim mạch',            N'Thuốc tim mạch, huyết áp', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Tiểu đường',          N'Thuốc trị tiểu đường', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Da liễu',             N'Thuốc bôi, trị các bệnh ngoài da', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Dụng cụ y tế',        N'Nhiệt kế, máy đo, bông băng', 'ACTIVE', NOW(), NOW()),
(UUID(), N'Thực phẩm chức năng', N'Bổ sung dinh dưỡng, sức khỏe', 'ACTIVE', NOW(), NOW());
