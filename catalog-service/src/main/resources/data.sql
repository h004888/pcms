-- ============================================================================
-- PCMS Catalog Service — Seed data
-- 22 medicines mau (Panadol, Amoxicillin, Vitamin C, ...)
-- Note: category_id and supplier_id are placeholder UUIDs.
-- Update them via API or DB if cross-service references are needed.
-- ============================================================================
INSERT INTO medicines (id, sku, name, category_id, supplier_id, price, unit, prescription_required, image_url, status, created_at, updated_at) VALUES
(UUID(), 'MED-0001', N'Panadol Extra 500mg (hộp 20 viên)',  UUID(), UUID(), 45000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0002', N'Paracetamol 500mg (hộp 100 viên)',   UUID(), UUID(), 35000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0003', N'Efferalgan 500mg (tuýp 16 viên sủi)', UUID(), UUID(), 55000, N'tuýp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0004', N'Amoxicillin 500mg (hộp 30 viên)',    UUID(), UUID(), 85000,  N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0005', N'Augmentin 1g (hộp 14 viên)',         UUID(), UUID(), 185000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0006', N'Cefuroxime 500mg (hộp 14 viên)',     UUID(), UUID(), 165000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0007', N'Vitamin C 500mg (tuýp 20 viên sủi)', UUID(), UUID(), 38000,  N'tuýp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0008', N'Vitamin B Complex (hộp 100 viên)',   UUID(), UUID(), 45000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0009', N'Calcium D3 (hộp 60 viên)',           UUID(), UUID(), 120000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0010', N'Glucosamine (hộp 60 viên)',          UUID(), UUID(), 250000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0011', N'Thuốc ho Prospan (chai 100ml)',      UUID(), UUID(), 95000,  N'chai', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0012', N'Siro ho Pectol (chai 90ml)',         UUID(), UUID(), 45000,  N'chai', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0013', N'Smecta (hộp 30 gói)',               UUID(), UUID(), 95000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0014', N'Enterogermina (hộp 20 ống)',         UUID(), UUID(), 85000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0015', N'Nexium 20mg (hộp 14 viên)',          UUID(), UUID(), 165000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0016', N'Concor 5mg (hộp 30 viên)',           UUID(), UUID(), 145000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0017', N'Glucophage 500mg (hộp 50 viên)',     UUID(), UUID(), 95000,  N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0018', N'Metformin 500mg (hộp 60 viên)',      UUID(), UUID(), 75000,  N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0019', N'Nhiệt kế điện tử Omron',             UUID(), UUID(), 220000, N'cái',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0020', N'Bông y tế Bạch Tuyết (gói 100g)',    UUID(), UUID(), 18000,  N'gói',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0021', N'Omega-3 Fish Oil (hộp 100 viên)',    UUID(), UUID(), 280000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID(), 'MED-0022', N'Multivitamin Centrum (hộp 30 viên)', UUID(), UUID(), 195000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW());