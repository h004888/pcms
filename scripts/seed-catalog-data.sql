-- =====================================================
-- PCMS - Seed data for catalog-service
-- Run: mysql -u pcms_user -ppcms_pass pcms_catalog < seed-catalog-data.sql
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE medicines;
SET FOREIGN_KEY_CHECKS=1;

-- Categories (embedded in catalog DB) - need to reference via foreign key
-- Insert medicines with category_id = one of the L1 categories

-- Use stable UUIDs for medicines
INSERT INTO medicines (id, sku, name, category_id, supplier_id, price, unit, prescription_required, image_url, status, created_at, updated_at) VALUES
(UUID_TO_BIN('0aaaaaaa-0001-0001-0001-000000000001'), 'MD001', 'Paracetamol 500mg (Hộp 20 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 18000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1584308666744-24fb5e417b15?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0002-0002-0002-000000000002'), 'MD002', 'Vitamin C 1000mg (Hộp 30 viên sủi)', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 85000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1550572017-edd951b55104?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0003-0003-0003-000000000003'), 'MD003', 'Omega-3 1000mg (Hộp 60 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 320000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0004-0004-0004-000000000004'), 'MD004', 'Glucosamine 1500mg (Hộp 90 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 380000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1559663172-c0b89e6c89d1?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0005-0005-0005-000000000005'), 'MD005', 'Kem chống nắng SPF 50+ PA++++', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 280000.00, 'Tuýp', 0, 'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0006-0006-0006-000000000006'), 'MD006', 'Ibuprofen 400mg (Hộp 30 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 45000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1587854692152-cbe660dbde88?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0007-0007-0007-000000000007'), 'MD007', 'Amoxicillin 500mg (Hộp 21 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 85000.00, 'Hộp', 1, 'https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0008-0008-0008-000000000008'), 'MD008', 'Cetirizine 10mg (Hộp 30 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 35000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1584308666744-24fb5e417b15?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0009-0009-0009-000000000009'), 'MD009', 'Khẩu trang y tế 4 lớp (Hộp 50 cái)', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 45000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1585848728479-c8ce0d5e5e8e?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0010-0010-0010-000000000010'), 'MD010', 'Nhiệt kế điện tử Omron', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 250000.00, 'Cái', 0, 'https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0011-0011-0011-000000000011'), 'MD011', 'Magie B6 (Hộp 50 viên)', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 120000.00, 'Hộp', 0, 'https://images.unsplash.com/photo-1559663172-c0b89e6c89d1?w=400', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0012-0012-0012-000000000012'), 'MD012', 'Siro ho Prospan (100ml)', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 95000.00, 'Chai', 0, 'https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=400', 'ACTIVE', NOW(), NOW());

SELECT '✅ Catalog seeded' AS status;
SELECT COUNT(*) AS medicines FROM medicines;
SELECT name, price, unit FROM medicines LIMIT 5;