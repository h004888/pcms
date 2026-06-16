-- ============================================================================
-- PCMS Category Service — Seed data
-- 10 categories mau
-- ============================================================================
INSERT INTO categories (id, name, description, created_at) VALUES
(UUID(), N'Giảm đau - Hạ sốt',    N'Thuốc giảm đau, hạ sốt thông dụng', NOW()),
(UUID(), N'Kháng sinh',          N'Thuốc kháng sinh các loại', NOW()),
(UUID(), N'Vitamin - Khoáng chất', N'Bổ sung vitamin và khoáng chất', NOW()),
(UUID(), N'Cảm cúm - Ho',        N'Thuốc trị cảm cúm, ho, sổ mũi', NOW()),
(UUID(), N'Tiêu hóa',            N'Thuốc trị các bệnh tiêu hóa', NOW()),
(UUID(), N'Tim mạch',            N'Thuốc tim mạch, huyết áp', NOW()),
(UUID(), N'Tiểu đường',          N'Thuốc trị tiểu đường', NOW()),
(UUID(), N'Da liễu',             N'Thuốc bôi, trị các bệnh ngoài da', NOW()),
(UUID(), N'Dụng cụ y tế',        N'Nhiệt kế, máy đo, bông băng', NOW()),
(UUID(), N'Thực phẩm chức năng', N'Bổ sung dinh dưỡng, sức khỏe', NOW());
