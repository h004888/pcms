-- =====================================================
-- PCMS - Seed data for ecom-ops-service (flash sales)
-- Run: mysql -u pcms_user -ppcms_pass pcms_ecom_ops < seed-ecom-ops-data.sql
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE flash_sale_items;
TRUNCATE TABLE flash_sales;
SET FOREIGN_KEY_CHECKS=1;

-- Flash sale 1: ACTIVE (Vitamin)
INSERT INTO flash_sales (id, name, description, discount_pct, starts_at, ends_at, status, max_qty_per_user, created_at, updated_at) VALUES
(UUID_TO_BIN('fa111111-1111-1111-1111-111111111111'),
 'Vitamin & TPCN giảm đến 50%',
 'Vitamin, thực phẩm chức năng giảm giá sốc',
 50.00, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 6 HOUR, 'ACTIVE', 5, NOW(), NOW()),

-- Flash sale 2: UPCOMING (Thuốc cảm cúm)
(UUID_TO_BIN('fa222222-2222-2222-2222-222222222222'),
 'Thuốc cảm cúm — Flash 3 giờ',
 'Paracetamol, Ibuprofen, Cetirizine',
 35.00, NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 1 DAY + INTERVAL 3 HOUR, 'UPCOMING', 3, NOW(), NOW()),

-- Flash sale 3: ACTIVE (Dược mỹ phẩm)
(UUID_TO_BIN('fa333333-3333-3333-3333-333333333333'),
 'Chăm sóc da mùa hè',
 'Kem chống nắng, kem dưỡng',
 40.00, NOW() - INTERVAL 30 MINUTE, NOW() + INTERVAL 4 HOUR, 'ACTIVE', 2, NOW(), NOW());

-- Flash sale items
INSERT INTO flash_sale_items (id, flash_sale_id, medicine_id, medicine_name, image_url,
                               sale_price, original_price, qty_limit, sold_qty, created_at, updated_at) VALUES
(UUID_TO_BIN('fb111111-1111-1111-1111-111111111111'),
 UUID_TO_BIN('fa111111-1111-1111-1111-111111111111'),
 UUID_TO_BIN('0aaaaaaa-0002-0002-0002-000000000002'),
 'Vitamin C 1000mg', '/products/vitamin-c.png',
 85000, 110000, 100, 12, NOW(), NOW()),
(UUID_TO_BIN('fb111111-2222-2222-2222-222222222222'),
 UUID_TO_BIN('fa111111-1111-1111-1111-111111111111'),
 UUID_TO_BIN('0aaaaaaa-0003-0003-0003-000000000003'),
 'Canxi+ Vitamin D3', '/products/calcium-d3.png',
 320000, 420000, 50, 8, NOW(), NOW()),

(UUID_TO_BIN('fb222222-1111-1111-1111-111111111111'),
 UUID_TO_BIN('fa222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('0aaaaaaa-0001-0001-0001-000000000001'),
 'Paracetamol 500mg', '/products/paracetamol.png',
 18000, 25000, 200, 45, NOW(), NOW()),
(UUID_TO_BIN('fb222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('fa222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('0aaaaaaa-0006-0006-0006-000000000006'),
 'Cetirizine 10mg', '/products/cetirizine.png',
 45000, 65000, 100, 23, NOW(), NOW()),
(UUID_TO_BIN('fb222222-3333-3333-3333-333333333333'),
 UUID_TO_BIN('fa222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('0aaaaaaa-0008-0008-0008-000000000008'),
 'Ibuprofen 400mg', '/products/ibuprofen.png',
 35000, 50000, 80, 19, NOW(), NOW()),

(UUID_TO_BIN('fb333333-1111-1111-1111-111111111111'),
 UUID_TO_BIN('fa333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('0aaaaaaa-0005-0005-0005-000000000005'),
 'Kem chống nắng SPF50', '/products/sunscreen.png',
 280000, 360000, 60, 14, NOW(), NOW());

SELECT '✅ Ecom-ops seeded' AS status;
SELECT 'flash_sales' AS tbl, COUNT(*) AS cnt FROM flash_sales
UNION SELECT 'flash_sale_items', COUNT(*) FROM flash_sale_items;