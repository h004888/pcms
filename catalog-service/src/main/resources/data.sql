-- ============================================================================
-- PCMS Catalog Service — Seed data
-- 22 medicines mau (Panadol, Amoxicillin, Vitamin C, ...)
-- ============================================================================
INSERT INTO medicines (id, sku, name, category_id, supplier_id, price, unit, prescription_required, image_url, status, created_at, updated_at) VALUES
(UUID(), 'MED-0001', N'Panadol Extra 500mg (hộp 20 viên)',
    (SELECT id FROM categories WHERE name=N'Giảm đau - Hạ sốt' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Imexpharm%' LIMIT 1),
    45000, N'hộp', FALSE, 'https://example.com/panadol.jpg', 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0002', N'Paracetamol 500mg (hộp 100 viên)',
    (SELECT id FROM categories WHERE name=N'Giảm đau - Hạ sốt' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Traphaco%' LIMIT 1),
    35000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0003', N'Efferalgan 500mg (tuýp 16 viên sủi)',
    (SELECT id FROM categories WHERE name=N'Giảm đau - Hạ sốt' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Sanofi%' LIMIT 1),
    55000, N'tuýp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0004', N'Amoxicillin 500mg (hộp 30 viên)',
    (SELECT id FROM categories WHERE name=N'Kháng sinh' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Imexpharm%' LIMIT 1),
    85000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0005', N'Augmentin 1g (hộp 14 viên)',
    (SELECT id FROM categories WHERE name=N'Kháng sinh' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%AstraZeneca%' LIMIT 1),
    185000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0006', N'Cefuroxime 500mg (hộp 14 viên)',
    (SELECT id FROM categories WHERE name=N'Kháng sinh' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Traphaco%' LIMIT 1),
    165000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0007', N'Vitamin C 500mg (tuýp 20 viên sủi)',
    (SELECT id FROM categories WHERE name=N'Vitamin - Khoáng chất' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Hậu Giang%' LIMIT 1),
    38000, N'tuýp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0008', N'Vitamin B Complex (hộp 100 viên)',
    (SELECT id FROM categories WHERE name=N'Vitamin - Khoáng chất' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Traphaco%' LIMIT 1),
    45000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0009', N'Calcium D3 (hộp 60 viên)',
    (SELECT id FROM categories WHERE name=N'Vitamin - Khoáng chất' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Sanofi%' LIMIT 1),
    120000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0010', N'Tiffy-D (hộp 20 viên) - trị cảm cúm',
    (SELECT id FROM categories WHERE name=N'Cảm cúm - Ho' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Hậu Giang%' LIMIT 1),
    25000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0011', N'Decolgen (hộp 20 viên) - trị cảm',
    (SELECT id FROM categories WHERE name=N'Cảm cúm - Ho' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Imexpharm%' LIMIT 1),
    28000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0012', N'Siro Prospan (chai 100ml) - trị ho',
    (SELECT id FROM categories WHERE name=N'Cảm cúm - Ho' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%AstraZeneca%' LIMIT 1),
    95000, N'chai', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0013', N'Smecta (hộp 30 gói) - trị tiêu chảy',
    (SELECT id FROM categories WHERE name=N'Tiêu hóa' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Sanofi%' LIMIT 1),
    110000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0014', N'Maalox (hộp 40 viên) - trị đau dạ dày',
    (SELECT id FROM categories WHERE name=N'Tiêu hóa' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Sanofi%' LIMIT 1),
    78000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0015', N'Omeprazole 20mg (hộp 28 viên)',
    (SELECT id FROM categories WHERE name=N'Tiêu hóa' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Traphaco%' LIMIT 1),
    65000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0016', N'Concor 5mg (hộp 30 viên) - tim mạch',
    (SELECT id FROM categories WHERE name=N'Tim mạch' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%AstraZeneca%' LIMIT 1),
    145000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0017', N'Amlodipine 5mg (hộp 30 viên)',
    (SELECT id FROM categories WHERE name=N'Tim mạch' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Hậu Giang%' LIMIT 1),
    55000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0018', N'Glucophage 500mg (hộp 50 viên) - tiểu đường',
    (SELECT id FROM categories WHERE name=N'Tiểu đường' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Sanofi%' LIMIT 1),
    125000, N'hộp', TRUE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0019', N'Nhiệt kế điện tử Omron',
    (SELECT id FROM categories WHERE name=N'Dụng cụ y tế' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%AstraZeneca%' LIMIT 1),
    220000, N'cái', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0020', N'Bông y tế Bạch Tuyết (gói 100g)',
    (SELECT id FROM categories WHERE name=N'Dụng cụ y tế' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Traphaco%' LIMIT 1),
    18000, N'gói', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0021', N'Omega-3 Fish Oil (hộp 100 viên)',
    (SELECT id FROM categories WHERE name=N'Thực phẩm chức năng' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%Hậu Giang%' LIMIT 1),
    285000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),

(UUID(), 'MED-0022', N'Sữa Ensure Gold (hộp 800g)',
    (SELECT id FROM categories WHERE name=N'Thực phẩm chức năng' LIMIT 1),
    (SELECT id FROM suppliers WHERE name LIKE N'%AstraZeneca%' LIMIT 1),
    720000, N'hộp', FALSE, NULL, 'ACTIVE', NOW(), NOW());
