-- ============================================================================
-- PCMS Catalog Service — Seed data
-- 22 medicines mẫu (Panadol, Amoxicillin, Vitamin C, ...)
-- Note: category_id and supplier_id are placeholder UUIDs.
-- Update them via API or DB if cross-service references are needed.
-- ============================================================================
INSERT INTO medicines (id, sku, slug, name, category_id, supplier_id, price, unit, prescription_required, image_url, status, created_at, updated_at) VALUES
(UUID_TO_BIN(UUID(), 1), 'MED-0001', 'panadol-extra-500mg-hop-20-vien',       N'Panadol Extra 500mg (hộp 20 viên)',  UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 45000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0002', 'paracetamol-500mg-hop-100-vien',         N'Paracetamol 500mg (hộp 100 viên)',   UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 35000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0003', 'efferalgan-500mg-tuyp-16-vien-sui',      N'Efferalgan 500mg (tuýp 16 viên sủi)', UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 55000, N'tuýp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0004', 'amoxicillin-500mg-hop-30-vien',          N'Amoxicillin 500mg (hộp 30 viên)',    UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 85000,  N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0005', 'augmentin-1g-hop-14-vien',               N'Augmentin 1g (hộp 14 viên)',         UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 185000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0006', 'cefuroxime-500mg-hop-14-vien',           N'Cefuroxime 500mg (hộp 14 viên)',     UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 165000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0007', 'vitamin-c-500mg-tuyp-20-vien-sui',       N'Vitamin C 500mg (tuýp 20 viên sủi)', UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 38000,  N'tuýp', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0008', 'vitamin-b-complex-hop-100-vien',         N'Vitamin B Complex (hộp 100 viên)',   UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 45000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0009', 'calcium-d3-hop-60-vien',                 N'Calcium D3 (hộp 60 viên)',           UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 120000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0010', 'glucosamine-hop-60-vien',                N'Glucosamine (hộp 60 viên)',          UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 250000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0011', 'thuoc-ho-prospan-chai-100ml',            N'Thuốc ho Prospan (chai 100ml)',      UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 95000,  N'chai', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0012', 'siro-ho-pectol-chai-90ml',               N'Siro ho Pectol (chai 90ml)',         UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 45000,  N'chai', FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0013', 'smecta-hop-30-goi',                      N'Smecta (hộp 30 gói)',               UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 95000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0014', 'enterogermina-hop-20-ong',               N'Enterogermina (hộp 20 ống)',         UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 85000,  N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0015', 'nexium-20mg-hop-14-vien',                N'Nexium 20mg (hộp 14 viên)',          UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 165000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0016', 'concor-5mg-hop-30-vien',                 N'Concor 5mg (hộp 30 viên)',           UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 145000, N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0017', 'glucophage-500mg-hop-50-vien',           N'Glucophage 500mg (hộp 50 viên)',     UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 95000,  N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0018', 'metformin-500mg-hop-60-vien',            N'Metformin 500mg (hộp 60 viên)',      UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 75000,  N'hộp',  TRUE,  NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0019', 'nhiet-ke-dien-tu-omron',                 N'Nhiệt kế điện tử Omron',             UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 220000, N'cái',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0020', 'bong-y-te-bach-tuyet-goi-100g',          N'Bông y tế Bạch Tuyết (gói 100g)',    UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 18000,  N'gói',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0021', 'omega-3-fish-oil-hop-100-vien',          N'Omega-3 Fish Oil (hộp 100 viên)',    UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 280000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), 'MED-0022', 'multivitamin-centrum-hop-30-vien',       N'Multivitamin Centrum (hộp 30 viên)', UUID_TO_BIN(UUID(), 1), UUID_TO_BIN(UUID(), 1), 195000, N'hộp',  FALSE, NULL, 'ACTIVE', NOW(), NOW());
