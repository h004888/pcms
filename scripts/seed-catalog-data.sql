-- =====================================================
-- PCMS - Seed data for catalog-service
-- Generated with real product images
-- Date: 2026-07-14 23:35:46
-- Images: 0 Unsplash + 97 SVG
-- Run: mysql -u pcms_user -ppcms_pass pcms_catalog < seed-catalog-data.sql
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE medicines;
SET FOREIGN_KEY_CHECKS=1;

INSERT INTO medicines (id, sku, name, category_id, supplier_id, price, unit, prescription_required, image_url, description, status, created_at, updated_at) VALUES
(UUID_TO_BIN('0aaaaaaa-0001-0001-0001-000000000001'), 'MD001', 'Paracetamol 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 18000, 'Hộp 20 viên', 0, '/images/med/paracetamol-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0002-0002-0002-000000000002'), 'MD002', 'Paracetamol 650mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 25000, 'Hộp 10 viên', 0, '/images/med/paracetamol-650mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0003-0003-0003-000000000003'), 'MD003', 'Efferalgan 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 55000, 'Hộp 16 viên sủi', 0, '/images/med/efferalgan-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0004-0004-0004-000000000004'), 'MD004', 'Panadol Extra', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 35000, 'Hộp 20 viên', 0, '/images/med/panadol-extra.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0005-0005-0005-000000000005'), 'MD005', 'Ibuprofen 400mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 45000, 'Hộp 30 viên', 0, '/images/med/ibuprofen-400mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0006-0006-0006-000000000006'), 'MD006', 'Diclofenac 50mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 25000, 'Hộp 10 viên', 1, '/images/med/diclofenac-50mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0007-0007-0007-000000000007'), 'MD007', 'Celecoxib 200mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 85000, 'Hộp 10 viên', 1, '/images/med/celecoxib-200mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0008-0008-0008-000000000008'), 'MD008', 'Meloxicam 7.5mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 35000, 'Hộp 30 viên', 1, '/images/med/meloxicam-75mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0009-0009-0009-000000000009'), 'MD009', 'Etoricoxib 60mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 180000, 'Hộp 10 viên', 1, '/images/med/etoricoxib-60mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0010-0010-0010-000000000010'), 'MD010', 'Paracetamol 120mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 15000, 'Hộp 10 gói', 0, '/images/med/paracetamol-120mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0011-0011-0011-000000000011'), 'MD011', 'Amoxicillin 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 65000, 'Hộp 20 viên', 1, '/images/med/amoxicillin-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0012-0012-0012-000000000012'), 'MD012', 'Amoxicillin 250mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 45000, 'Hộp 12 gói', 1, '/images/med/amoxicillin-250mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0013-0013-0013-000000000013'), 'MD013', 'Augmentin 625mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 250000, 'Hộp 14 viên', 1, '/images/med/augmentin-625mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0014-0014-0014-000000000014'), 'MD014', 'Cephalexin 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 55000, 'Hộp 20 viên', 1, '/images/med/cephalexin-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0015-0015-0015-000000000015'), 'MD015', 'Ciprofloxacin 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 75000, 'Hộp 10 viên', 1, '/images/med/ciprofloxacin-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0016-0016-0016-000000000016'), 'MD016', 'Azithromycin 250mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 85000, 'Hộp 6 viên', 1, '/images/med/azithromycin-250mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0017-0017-0017-000000000017'), 'MD017', 'Doxycycline 100mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 35000, 'Hộp 10 viên', 1, '/images/med/doxycycline-100mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0018-0018-0018-000000000018'), 'MD018', 'Metronidazole 250mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 25000, 'Hộp 10 viên', 1, '/images/med/metronidazole-250mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0019-0019-0019-000000000019'), 'MD019', 'Clindamycin 300mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 120000, 'Hộp 10 viên', 1, '/images/med/clindamycin-300mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0020-0020-0020-000000000020'), 'MD020', 'Cefuroxime 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 150000, 'Hộp 10 viên', 1, '/images/med/cefuroxime-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0021-0021-0021-000000000021'), 'MD021', 'Levofloxacin 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 180000, 'Hộp 5 viên', 1, '/images/med/levofloxacin-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0022-0022-0022-000000000022'), 'MD022', 'Erythromycin 250mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 35000, 'Hộp 10 viên', 1, '/images/med/erythromycin-250mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0023-0023-0023-000000000023'), 'MD023', 'Vitamin C 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 25000, 'Hộp 20 viên', 0, '/images/med/vitamin-c-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0024-0024-0024-000000000024'), 'MD024', 'Vitamin C 1000mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 85000, 'Hộp 10 ống sủi', 0, '/images/med/vitamin-c-1000mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0025-0025-0025-000000000025'), 'MD025', 'Vitamin B12 1000mcg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 35000, 'Hộp 30 viên', 0, '/images/med/vitamin-b12-1000mcg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0026-0026-0026-000000000026'), 'MD026', 'Vitamin B Complex', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 45000, 'Hộp 30 viên', 0, '/images/med/vitamin-b-complex.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0027-0027-0027-000000000027'), 'MD027', 'Vitamin E 400IU', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 38000, 'Hộp 30 viên', 0, '/images/med/vitamin-e-400iu.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0028-0028-0028-000000000028'), 'MD028', 'Calcium + Vitamin D3', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 120000, 'Hộp 60 viên', 0, '/images/med/calcium-vitamin-d3.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0029-0029-0029-000000000029'), 'MD029', 'Sắt + Acid Folic', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 45000, 'Hộp 30 viên', 0, '/images/med/sắt-acid-folic.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0030-0030-0030-000000000030'), 'MD030', 'Magnesi B6', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 120000, 'Hộp 30 viên', 0, '/images/med/magnesi-b6.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0031-0031-0031-000000000031'), 'MD031', 'Vitamin 3B', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 25000, 'Hộp 20 viên', 0, '/images/med/vitamin-3b.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0032-0032-0032-000000000032'), 'MD032', 'Neurobion', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 65000, 'Hộp 20 viên', 0, '/images/med/neurobion.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0033-0033-0033-000000000033'), 'MD033', 'Piracetam 800mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 85000, 'Hộp 60 viên', 1, '/images/med/piracetam-800mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0034-0034-0034-000000000034'), 'MD034', 'Ginkgo Biloba 40mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 75000, 'Hộp 30 viên', 0, '/images/med/ginkgo-biloba-40mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0035-0035-0035-000000000035'), 'MD035', 'Coldacmin Flu', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 25000, 'Hộp 10 viên', 0, '/images/med/coldacmin-flu.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0036-0036-0036-000000000036'), 'MD036', 'Tiffy', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 15000, 'Hộp 10 viên', 0, '/images/med/tiffy.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0037-0037-0037-000000000037'), 'MD037', 'Decolgen Forte', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 25000, 'Hộp 10 viên', 0, '/images/med/decolgen-forte.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0038-0038-0038-000000000038'), 'MD038', 'Clorpheniramin 4mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 12000, 'Hộp 10 viên', 0, '/images/med/clorpheniramin-4mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0039-0039-0039-000000000039'), 'MD039', 'Cetirizin 10mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 35000, 'Hộp 30 viên', 0, '/images/med/cetirizin-10mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0040-0040-0040-000000000040'), 'MD040', 'Loratadin 10mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 45000, 'Hộp 10 viên', 0, '/images/med/loratadin-10mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0041-0041-0041-000000000041'), 'MD041', 'Fexofenadin 180mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 85000, 'Hộp 10 viên', 0, '/images/med/fexofenadin-180mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0042-0042-0042-000000000042'), 'MD042', 'Prospan', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 95000, 'Chai 100ml', 0, '/images/med/prospan.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0043-0043-0043-000000000043'), 'MD043', 'Bổ Phế Nam Hà', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 45000, 'Chai 125ml', 0, '/images/med/bổ-phế-nam-hà.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0044-0044-0044-000000000044'), 'MD044', 'Atussin', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 35000, 'Hộp 10 gói', 0, '/images/med/atussin.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0045-0045-0045-000000000045'), 'MD045', 'Siro ho trẻ em', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 38000, 'Chai 100ml', 0, '/images/med/siro-ho-trẻ-em.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0046-0046-0046-000000000046'), 'MD046', 'Omeprazole 20mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 35000, 'Hộp 20 viên', 0, '/images/med/omeprazole-20mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0047-0047-0047-000000000047'), 'MD047', 'Esomeprazole 40mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 85000, 'Hộp 14 viên', 0, '/images/med/esomeprazole-40mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0048-0048-0048-000000000048'), 'MD048', 'Mebeverine 135mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 65000, 'Hộp 30 viên', 0, '/images/med/mebeverine-135mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0049-0049-0049-000000000049'), 'MD049', 'Loperamid 2mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 15000, 'Hộp 10 viên', 0, '/images/med/loperamid-2mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0050-0050-0050-000000000050'), 'MD050', 'Smecta', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 65000, 'Hộp 10 gói', 0, '/images/med/smecta.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0051-0051-0051-000000000051'), 'MD051', 'Domperidon 10mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 25000, 'Hộp 10 viên', 0, '/images/med/domperidon-10mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0052-0052-0052-000000000052'), 'MD052', 'Rennie', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 55000, 'Hộp 24 viên', 0, '/images/med/rennie.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0053-0053-0053-000000000053'), 'MD053', 'Bisacodyl 5mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 15000, 'Hộp 10 viên', 0, '/images/med/bisacodyl-5mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0054-0054-0054-000000000054'), 'MD054', 'Simethicon 80mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 25000, 'Hộp 10 viên', 0, '/images/med/simethicon-80mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0055-0055-0055-000000000055'), 'MD055', 'Amlodipine 5mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 45000, 'Hộp 30 viên', 1, '/images/med/amlodipine-5mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0056-0056-0056-000000000056'), 'MD056', 'Losartan 50mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 65000, 'Hộp 30 viên', 1, '/images/med/losartan-50mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0057-0057-0057-000000000057'), 'MD057', 'Atorvastatin 20mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 120000, 'Hộp 30 viên', 1, '/images/med/atorvastatin-20mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0058-0058-0058-000000000058'), 'MD058', 'Atorvastatin 10mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 85000, 'Hộp 30 viên', 1, '/images/med/atorvastatin-10mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0059-0059-0059-000000000059'), 'MD059', 'Enalapril 5mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 35000, 'Hộp 30 viên', 1, '/images/med/enalapril-5mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0060-0060-0060-000000000060'), 'MD060', 'Carvedilol 6.25mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 55000, 'Hộp 30 viên', 1, '/images/med/carvedilol-625mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0061-0061-0061-000000000061'), 'MD061', 'Furosemide 40mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 15000, 'Hộp 10 viên', 1, '/images/med/furosemide-40mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0062-0062-0062-000000000062'), 'MD062', 'Nifedipine 10mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 25000, 'Hộp 10 viên', 1, '/images/med/nifedipine-10mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0063-0063-0063-000000000063'), 'MD063', 'Ramipril 5mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 65000, 'Hộp 30 viên', 1, '/images/med/ramipril-5mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0064-0064-0064-000000000064'), 'MD064', 'Telmisartan 40mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 85000, 'Hộp 30 viên', 1, '/images/med/telmisartan-40mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0065-0065-0065-000000000065'), 'MD065', 'Rosuvastatin 10mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 150000, 'Hộp 30 viên', 1, '/images/med/rosuvastatin-10mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0066-0066-0066-000000000066'), 'MD066', 'Clopidogrel 75mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 250000, 'Hộp 14 viên', 1, '/images/med/clopidogrel-75mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0067-0067-0067-000000000067'), 'MD067', 'Aspirin 81mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 15000, 'Hộp 20 viên', 0, '/images/med/aspirin-81mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0068-0068-0068-000000000068'), 'MD068', 'Metformin 500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 35000, 'Hộp 28 viên', 1, '/images/med/metformin-500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0069-0069-0069-000000000069'), 'MD069', 'Metformin 850mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 45000, 'Hộp 30 viên', 1, '/images/med/metformin-850mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0070-0070-0070-000000000070'), 'MD070', 'Gliclazide 30mg MR', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 65000, 'Hộp 30 viên', 1, '/images/med/gliclazide-30mg-mr.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0071-0071-0071-000000000071'), 'MD071', 'Gliclazide 80mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 45000, 'Hộp 30 viên', 1, '/images/med/gliclazide-80mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0072-0072-0072-000000000072'), 'MD072', 'Insulin Mixtard 30', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 350000, 'Lọ 10ml', 1, '/images/med/insulin-mixtard-30.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0073-0073-0073-000000000073'), 'MD073', 'Fucidin H kem', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 55000, 'Tuýp 15g', 0, '/images/med/fucidin-h-kem.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0074-0074-0074-000000000074'), 'MD074', 'Gentrison kem', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 25000, 'Tuýp 10g', 0, '/images/med/gentrison-kem.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0075-0075-0075-000000000075'), 'MD075', 'Nizoral kem', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 65000, 'Tuýp 15g', 0, '/images/med/nizoral-kem.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0076-0076-0076-000000000076'), 'MD076', 'Kem chống nắng SPF50', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 280000, 'Tuýp 50ml', 0, '/images/med/kem-chống-nắng-spf50.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0077-0077-0077-000000000077'), 'MD077', 'Bôi da B.S', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 15000, 'Tuýp 10g', 0, '/images/med/bôi-da-bs.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0078-0078-0078-000000000078'), 'MD078', 'Nhiệt kế điện tử', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 250000, 'Cái', 0, '/images/med/nhiệt-kế-điện-tử.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0079-0079-0079-000000000079'), 'MD079', 'Máy đo huyết áp Omron', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 890000, 'Bộ', 0, '/images/med/máy-đo-huyết-áp-omron.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0080-0080-0080-000000000080'), 'MD080', 'Máy đo đường huyết', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 650000, 'Bộ', 0, '/images/med/máy-đo-đường-huyết.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0081-0081-0081-000000000081'), 'MD081', 'Bông y tế', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 25000, 'Gói 200g', 0, '/images/med/bông-y-tế.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0082-0082-0082-000000000082'), 'MD082', 'Gạc y tế', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 15000, 'Hộp 10 gói', 0, '/images/med/gạc-y-tế.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0083-0083-0083-000000000083'), 'MD083', 'Băng dính vết thương', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 25000, 'Hộp 100 miếng', 0, '/images/med/băng-dính-vết-thương.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0084-0084-0084-000000000084'), 'MD084', 'Dầu cá Omega-3', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 180000, 'Hộp 60 viên', 0, '/images/med/dầu-cá-omega-3.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0085-0085-0085-000000000085'), 'MD085', 'Men vi sinh Probio', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 250000, 'Hộp 10 ống', 0, '/images/med/men-vi-sinh-probio.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0086-0086-0086-000000000086'), 'MD086', 'Glucosamine 1500mg', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 380000, 'Hộp 60 viên', 0, '/images/med/glucosamine-1500mg.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0087-0087-0087-000000000087'), 'MD087', 'Collagen Type II', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 450000, 'Hộp 30 viên', 0, '/images/med/collagen-type-ii.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0088-0088-0088-000000000088'), 'MD088', 'Viên khớp JEX', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 220000, 'Hộp 30 viên', 0, '/images/med/viên-khớp-jex.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0089-0089-0089-000000000089'), 'MD089', 'Berocca Performance', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 150000, 'Hộp 15 viên sủi', 0, '/images/med/berocca-performance.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0090-0090-0090-000000000090'), 'MD090', 'Sữa Ensure', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 650000, 'Hộp 850g', 0, '/images/med/sữa-ensure.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0091-0091-0091-000000000091'), 'MD091', 'Trà xanh Olong', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 35000, 'Hộp 20 gói', 0, '/images/med/trà-xanh-olong.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0092-0092-0092-000000000092'), 'MD092', 'Glucosamine Chondroitin', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 350000, 'Hộp 60 viên', 0, '/images/med/glucosamine-chondroitin.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0093-0093-0093-000000000093'), 'MD093', 'Vitamin D3 K2', UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), NULL, 150000, 'Hộp 30 viên', 0, '/images/med/vitamin-d3-k2.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0094-0094-0094-000000000094'), 'MD094', 'Canxi Cơm', UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), NULL, 35000, 'Hộp 60 viên', 0, '/images/med/canxi-cơm.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0095-0095-0095-000000000095'), 'MD095', 'Trà gừng mật ong', UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), NULL, 25000, 'Hộp 20 gói', 0, '/images/med/trà-gừng-mật-ong.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0096-0096-0096-000000000096'), 'MD096', 'Kẽm + Vitamin C', UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), NULL, 85000, 'Hộp 30 viên', 0, '/images/med/kẽm-vitamin-c.jpg', '', 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('0aaaaaaa-0097-0097-0097-000000000097'), 'MD097', 'Liver Detox', UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), NULL, 250000, 'Hộp 30 viên', 0, '/images/med/liver-detox.jpg', '', 'ACTIVE', NOW(), NOW());

-- ============= UPDATE DESCRIPTIONS =============
UPDATE medicines SET description = 'Giảm đau, hạ sốt hiệu quả. Dùng cho người lớn và trẻ em trên 12 tuổi.' WHERE id = UUID_TO_BIN('0aaaaaaa-0001-0001-0001-000000000001');
UPDATE medicines SET description = 'Giảm đau, hạ sốt liều cao. Dùng khi cần giảm đau nhanh.' WHERE id = UUID_TO_BIN('0aaaaaaa-0002-0002-0002-000000000002');
UPDATE medicines SET description = 'Thuốc sủi giảm đau, hạ sốt dạng sủi bọt. Hấp thu nhanh, dễ uống.' WHERE id = UUID_TO_BIN('0aaaaaaa-0003-0003-0003-000000000003');
UPDATE medicines SET description = 'Giảm đau nhanh, hạ sốt hiệu quả với công thức Extra. Dùng cho người lớn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0004-0004-0004-000000000004');
UPDATE medicines SET description = 'Kháng viêm, giảm đau không steroid. Điều trị viêm khớp, đau cơ.' WHERE id = UUID_TO_BIN('0aaaaaaa-0005-0005-0005-000000000005');
UPDATE medicines SET description = 'Kháng viêm mạnh, giảm đau xương khớp. Cần kê đơn bác sĩ.' WHERE id = UUID_TO_BIN('0aaaaaaa-0006-0006-0006-000000000006');
UPDATE medicines SET description = 'Ức chế COX-2 chọn lọc, giảm đau viêm khớp. Ít tác dụng phụ dạ dày.' WHERE id = UUID_TO_BIN('0aaaaaaa-0007-0007-0007-000000000007');
UPDATE medicines SET description = 'Kháng viêm, giảm đau trong viêm khớp dạng thấp và thoái hóa khớp.' WHERE id = UUID_TO_BIN('0aaaaaaa-0008-0008-0008-000000000008');
UPDATE medicines SET description = 'Giảm đau viêm khớp thế hệ mới. Hiệu quả kéo dài 24 giờ.' WHERE id = UUID_TO_BIN('0aaaaaaa-0009-0009-0009-000000000009');
UPDATE medicines SET description = 'Hạ sốt, giảm đau dạng gói cho trẻ em. Hương vị trái cây dễ uống.' WHERE id = UUID_TO_BIN('0aaaaaaa-0010-0010-0010-000000000010');
UPDATE medicines SET description = 'Kháng sinh phổ rộng nhóm Penicillin. Điều trị nhiễm khuẩn hô hấp.' WHERE id = UUID_TO_BIN('0aaaaaaa-0011-0011-0011-000000000011');
UPDATE medicines SET description = 'Kháng sinh Amoxicillin hàm lượng thấp dạng gói. Dùng cho trẻ em.' WHERE id = UUID_TO_BIN('0aaaaaaa-0012-0012-0012-000000000012');
UPDATE medicines SET description = 'Kháng sinh kết hợp Amoxicillin + Acid Clavulanic. Trị nhiễm khuẩn nặng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0013-0013-0013-000000000013');
UPDATE medicines SET description = 'Kháng sinh Cephalosporin thế hệ 1. Điều trị nhiễm khuẩn da và hô hấp.' WHERE id = UUID_TO_BIN('0aaaaaaa-0014-0014-0014-000000000014');
UPDATE medicines SET description = 'Kháng sinh Quinolon phổ rộng. Trị nhiễm khuẩn tiết niệu và tiêu hóa.' WHERE id = UUID_TO_BIN('0aaaaaaa-0015-0015-0015-000000000015');
UPDATE medicines SET description = 'Kháng sinh Macrolid. Điều trị viêm họng, viêm phế quản, viêm xoang.' WHERE id = UUID_TO_BIN('0aaaaaaa-0016-0016-0016-000000000016');
UPDATE medicines SET description = 'Kháng sinh Tetracyclin. Trị mụn trứng cá, nhiễm khuẩn đường hô hấp.' WHERE id = UUID_TO_BIN('0aaaaaaa-0017-0017-0017-000000000017');
UPDATE medicines SET description = 'Kháng sinh trị nhiễm khuẩn kỵ khí. Dùng trong nhiễm khuẩn răng miệng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0018-0018-0018-000000000018');
UPDATE medicines SET description = 'Kháng sinh Lincosamid. Điều trị nhiễm khuẩn xương, khớp, ổ bụng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0019-0019-0019-000000000019');
UPDATE medicines SET description = 'Kháng sinh Cephalosporin thế hệ 2. Phổ rộng, trị nhiều loại nhiễm khuẩn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0020-0020-0020-000000000020');
UPDATE medicines SET description = 'Kháng sinh Quinolon thế hệ mới. Điều trị viêm phổi, viêm xoang nặng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0021-0021-0021-000000000021');
UPDATE medicines SET description = 'Kháng sinh Macrolid cổ điển. Dùng cho bệnh nhân dị ứng Penicillin.' WHERE id = UUID_TO_BIN('0aaaaaaa-0022-0022-0022-000000000022');
UPDATE medicines SET description = 'Tăng cường sức đề kháng, chống oxy hóa. Hỗ trợ hấp thu sắt.' WHERE id = UUID_TO_BIN('0aaaaaaa-0023-0023-0023-000000000023');
UPDATE medicines SET description = 'Vitamin C liều cao dạng ống sủi. Tăng sức đề kháng, làm đẹp da.' WHERE id = UUID_TO_BIN('0aaaaaaa-0024-0024-0024-000000000024');
UPDATE medicines SET description = 'Bổ sung Vitamin B12 cho người thiếu máu, mệt mỏi, suy nhược thần kinh.' WHERE id = UUID_TO_BIN('0aaaaaaa-0025-0025-0025-000000000025');
UPDATE medicines SET description = 'Tổng hợp Vitamin nhóm B. Hỗ trợ chuyển hóa năng lượng, giảm căng thẳng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0026-0026-0026-000000000026');
UPDATE medicines SET description = 'Chống oxy hóa mạnh, làm đẹp da, chống lão hóa. Hỗ trợ sinh sản.' WHERE id = UUID_TO_BIN('0aaaaaaa-0027-0027-0027-000000000027');
UPDATE medicines SET description = 'Bổ sung Canxi và Vitamin D3. Phòng ngừa loãng xương, chắc khỏe xương.' WHERE id = UUID_TO_BIN('0aaaaaaa-0028-0028-0028-000000000028');
UPDATE medicines SET description = 'Bổ sung sắt và acid folic cho phụ nữ mang thai. Phòng thiếu máu.' WHERE id = UUID_TO_BIN('0aaaaaaa-0029-0029-0029-000000000029');
UPDATE medicines SET description = 'Bổ sung Magie và Vitamin B6. Giảm căng thẳng, chống chuột rút.' WHERE id = UUID_TO_BIN('0aaaaaaa-0030-0030-0030-000000000030');
UPDATE medicines SET description = 'Kết hợp Vitamin B1, B6, B12. Điều trị đau dây thần kinh, tê bì chân tay.' WHERE id = UUID_TO_BIN('0aaaaaaa-0031-0031-0031-000000000031');
UPDATE medicines SET description = 'Vitamin nhóm B liều cao. Phục hồi thần kinh, giảm đau dây thần kinh.' WHERE id = UUID_TO_BIN('0aaaaaaa-0032-0032-0032-000000000032');
UPDATE medicines SET description = 'Tăng cường tuần hoàn não, cải thiện trí nhớ. Hỗ trợ điều trị sa sút trí tuệ.' WHERE id = UUID_TO_BIN('0aaaaaaa-0033-0033-0033-000000000033');
UPDATE medicines SET description = 'Chiết xuất bạch quả. Tăng tuần hoàn máu não, cải thiện trí nhớ người già.' WHERE id = UUID_TO_BIN('0aaaaaaa-0034-0034-0034-000000000034');
UPDATE medicines SET description = 'Giảm triệu chứng cảm cúm: nghẹt mũi, sổ mũi, đau đầu, sốt.' WHERE id = UUID_TO_BIN('0aaaaaaa-0035-0035-0035-000000000035');
UPDATE medicines SET description = 'Giảm nhanh triệu chứng cảm cúm, sổ mũi, hắt hơi. Dùng cho người lớn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0036-0036-0036-000000000036');
UPDATE medicines SET description = 'Giảm triệu chứng cảm cúm, nghẹt mũi, đau nhức cơ thể.' WHERE id = UUID_TO_BIN('0aaaaaaa-0037-0037-0037-000000000037');
UPDATE medicines SET description = 'Kháng histamin thế hệ 1. Điều trị dị ứng, viêm mũi dị ứng, mề đay.' WHERE id = UUID_TO_BIN('0aaaaaaa-0038-0038-0038-000000000038');
UPDATE medicines SET description = 'Kháng histamin thế hệ 2. Ít gây buồn ngủ. Trị viêm mũi dị ứng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0039-0039-0039-000000000039');
UPDATE medicines SET description = 'Kháng histamin không gây buồn ngủ. Điều trị viêm mũi dị ứng, mề đay mạn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0040-0040-0040-000000000040');
UPDATE medicines SET description = 'Kháng histamin thế hệ mới nhất. Hiệu quả cao, không buồn ngủ.' WHERE id = UUID_TO_BIN('0aaaaaaa-0041-0041-0041-000000000041');
UPDATE medicines SET description = 'Thuốc ho thảo dược chiết xuất lá thường xuân. Long đờm, giảm ho.' WHERE id = UUID_TO_BIN('0aaaaaaa-0042-0042-0042-000000000042');
UPDATE medicines SET description = 'Thuốc ho đông y bổ phế. Trị ho khan, ho có đờm, viêm họng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0043-0043-0043-000000000043');
UPDATE medicines SET description = 'Thuốc ho long đờm. Điều trị ho, viêm phế quản cấp và mạn tính.' WHERE id = UUID_TO_BIN('0aaaaaaa-0044-0044-0044-000000000044');
UPDATE medicines SET description = 'Siro ho thảo dược cho trẻ em. Giảm ho, long đờm an toàn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0045-0045-0045-000000000045');
UPDATE medicines SET description = 'Ức chế bơm proton. Điều trị trào ngược dạ dày, viêm loét dạ dày.' WHERE id = UUID_TO_BIN('0aaaaaaa-0046-0046-0046-000000000046');
UPDATE medicines SET description = 'Ức chế bơm proton thế hệ mới. Hiệu quả cao hơn Omeprazole.' WHERE id = UUID_TO_BIN('0aaaaaaa-0047-0047-0047-000000000047');
UPDATE medicines SET description = 'Giảm co thắt cơ trơn. Điều trị hội chứng ruột kích thích.' WHERE id = UUID_TO_BIN('0aaaaaaa-0048-0048-0048-000000000048');
UPDATE medicines SET description = 'Cầm tiêu chảy cấp. Giảm nhu động ruột, giảm số lần đi ngoài.' WHERE id = UUID_TO_BIN('0aaaaaaa-0049-0049-0049-000000000049');
UPDATE medicines SET description = 'Bảo vệ niêm mạc ruột. Điều trị tiêu chảy cấp và mạn tính.' WHERE id = UUID_TO_BIN('0aaaaaaa-0050-0050-0050-000000000050');
UPDATE medicines SET description = 'Chống nôn, giảm buồn nôn. Điều trị đầy bụng, khó tiêu.' WHERE id = UUID_TO_BIN('0aaaaaaa-0051-0051-0051-000000000051');
UPDATE medicines SET description = 'Kháng acid dạ dày. Giảm nhanh ợ nóng, khó tiêu, đầy bụng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0052-0052-0052-000000000052');
UPDATE medicines SET description = 'Nhuận tràng kích thích. Điều trị táo bón cấp và chuẩn bị nội soi.' WHERE id = UUID_TO_BIN('0aaaaaaa-0053-0053-0053-000000000053');
UPDATE medicines SET description = 'Chống đầy hơi, giảm khí trong đường tiêu hóa. Dùng sau ăn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0054-0054-0054-000000000054');
UPDATE medicines SET description = 'Chẹn kênh Canxi. Hạ huyết áp, điều trị tăng huyết áp vô căn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0055-0055-0055-000000000055');
UPDATE medicines SET description = 'Ức chế thụ thể Angiotensin II. Hạ áp, bảo vệ thận ở bệnh nhân đái tháo đường.' WHERE id = UUID_TO_BIN('0aaaaaaa-0056-0056-0056-000000000056');
UPDATE medicines SET description = 'Statin hạ mỡ máu. Giảm Cholesterol toàn phần và LDL-Cholesterol.' WHERE id = UUID_TO_BIN('0aaaaaaa-0057-0057-0057-000000000057');
UPDATE medicines SET description = 'Statin liều thấp. Dự phòng bệnh tim mạch, hạ Cholesterol.' WHERE id = UUID_TO_BIN('0aaaaaaa-0058-0058-0058-000000000058');
UPDATE medicines SET description = 'Ức chế men chuyển. Hạ huyết áp, điều trị suy tim.' WHERE id = UUID_TO_BIN('0aaaaaaa-0059-0059-0059-000000000059');
UPDATE medicines SET description = 'Chẹn Beta không chọn lọc. Điều trị tăng huyết áp, suy tim ổn định.' WHERE id = UUID_TO_BIN('0aaaaaaa-0060-0060-0060-000000000060');
UPDATE medicines SET description = 'Lợi tiểu quai. Điều trị phù do suy tim, suy thận, xơ gan.' WHERE id = UUID_TO_BIN('0aaaaaaa-0061-0061-0061-000000000061');
UPDATE medicines SET description = 'Chẹn kênh Canxi. Hạ huyết áp, điều trị đau thắt ngực.' WHERE id = UUID_TO_BIN('0aaaaaaa-0062-0062-0062-000000000062');
UPDATE medicines SET description = 'Ức chế men chuyển tác dụng kéo dài. Kiểm soát huyết áp 24 giờ.' WHERE id = UUID_TO_BIN('0aaaaaaa-0063-0063-0063-000000000063');
UPDATE medicines SET description = 'Ức chế thụ thể AT1. Hạ áp ổn định, dung nạp tốt.' WHERE id = UUID_TO_BIN('0aaaaaaa-0064-0064-0064-000000000064');
UPDATE medicines SET description = 'Statin thế hệ mới. Hạ mỡ máu mạnh, tăng HDL-Cholesterol.' WHERE id = UUID_TO_BIN('0aaaaaaa-0065-0065-0065-000000000065');
UPDATE medicines SET description = 'Chống ngưng tập tiểu cầu. Phòng ngừa đột quỵ và nhồi máu cơ tim.' WHERE id = UUID_TO_BIN('0aaaaaaa-0066-0066-0066-000000000066');
UPDATE medicines SET description = 'Chống kết tập tiểu cầu liều thấp. Phòng ngừa tim mạch cho người lớn tuổi.' WHERE id = UUID_TO_BIN('0aaaaaaa-0067-0067-0067-000000000067');
UPDATE medicines SET description = 'Hạ đường huyết nhóm Biguanid. Điều trị đái tháo đường type 2.' WHERE id = UUID_TO_BIN('0aaaaaaa-0068-0068-0068-000000000068');
UPDATE medicines SET description = 'Metformin liều cao. Kiểm soát đường huyết ở bệnh nhân đái tháo đường.' WHERE id = UUID_TO_BIN('0aaaaaaa-0069-0069-0069-000000000069');
UPDATE medicines SET description = 'Kích thích tiết Insulin. Kiểm soát đường huyết sau ăn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0070-0070-0070-000000000070');
UPDATE medicines SET description = 'Sulfonylurea kích thích tụy tiết Insulin. Điều trị đái tháo đường type 2.' WHERE id = UUID_TO_BIN('0aaaaaaa-0071-0071-0071-000000000071');
UPDATE medicines SET description = 'Insulin hỗn hợp. Điều trị đái tháo đường type 1 và type 2 nặng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0072-0072-0072-000000000072');
UPDATE medicines SET description = 'Kem kháng sinh + kháng viêm. Điều trị viêm da nhiễm khuẩn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0073-0073-0073-000000000073');
UPDATE medicines SET description = 'Kem kháng sinh + kháng viêm. Điều trị viêm da, eczema nhiễm khuẩn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0074-0074-0074-000000000074');
UPDATE medicines SET description = 'Kem kháng nấm. Điều trị nấm da, lang ben, viêm da tiết bã.' WHERE id = UUID_TO_BIN('0aaaaaaa-0075-0075-0075-000000000075');
UPDATE medicines SET description = 'Kem chống nắng phổ rộng SPF50+. Bảo vệ da khỏi tia UVA và UVB.' WHERE id = UUID_TO_BIN('0aaaaaaa-0076-0076-0076-000000000076');
UPDATE medicines SET description = 'Kem bôi da đa năng. Trị mụn nhọt, viêm da, ngứa do côn trùng cắn.' WHERE id = UUID_TO_BIN('0aaaaaaa-0077-0077-0077-000000000077');
UPDATE medicines SET description = 'Đo thân nhiệt nhanh chóng, chính xác. Màn hình LCD, cảnh báo sốt.' WHERE id = UUID_TO_BIN('0aaaaaaa-0078-0078-0078-000000000078');
UPDATE medicines SET description = 'Đo huyết áp tự động chính xác. Thương hiệu Omron Nhật Bản.' WHERE id = UUID_TO_BIN('0aaaaaaa-0079-0079-0079-000000000079');
UPDATE medicines SET description = 'Kiểm tra đường huyết tại nhà. Kết quả nhanh 5 giây.' WHERE id = UUID_TO_BIN('0aaaaaaa-0080-0080-0080-000000000080');
UPDATE medicines SET description = 'Bông y tế tiệt trùng. Dùng sát khuẩn vết thương, vệ sinh cá nhân.' WHERE id = UUID_TO_BIN('0aaaaaaa-0081-0081-0081-000000000081');
UPDATE medicines SET description = 'Gạc y tế vô trùng. Băng bó vết thương, cầm máu.' WHERE id = UUID_TO_BIN('0aaaaaaa-0082-0082-0082-000000000082');
UPDATE medicines SET description = 'Băng dính vết thương cá nhân Urgo. Thoáng khí, không thấm nước.' WHERE id = UUID_TO_BIN('0aaaaaaa-0083-0083-0083-000000000083');
UPDATE medicines SET description = 'Omega-3 từ dầu cá biển sâu. Tốt cho tim mạch, não bộ và thị lực.' WHERE id = UUID_TO_BIN('0aaaaaaa-0084-0084-0084-000000000084');
UPDATE medicines SET description = 'Men vi sinh hỗ trợ tiêu hóa. Cân bằng hệ vi sinh đường ruột.' WHERE id = UUID_TO_BIN('0aaaaaaa-0085-0085-0085-000000000085');
UPDATE medicines SET description = 'Bổ sung dịch khớp. Giảm đau, hỗ trợ điều trị thoái hóa khớp.' WHERE id = UUID_TO_BIN('0aaaaaaa-0086-0086-0086-000000000086');
UPDATE medicines SET description = 'Collagen Type II thủy phân. Tái tạo sụn khớp, giảm đau xương khớp.' WHERE id = UUID_TO_BIN('0aaaaaaa-0087-0087-0087-000000000087');
UPDATE medicines SET description = 'Viên uống hỗ trợ xương khớp JEX. Giảm đau, chống viêm tự nhiên.' WHERE id = UUID_TO_BIN('0aaaaaaa-0088-0088-0088-000000000088');
UPDATE medicines SET description = 'Vitamin tổng hợp sủi bọt. Tăng cường năng lượng, giảm mệt mỏi.' WHERE id = UUID_TO_BIN('0aaaaaaa-0089-0089-0089-000000000089');
UPDATE medicines SET description = 'Sữa dinh dưỡng Ensure cho người lớn. Bổ sung đầy đủ vitamin và khoáng chất.' WHERE id = UUID_TO_BIN('0aaaaaaa-0090-0090-0090-000000000090');
UPDATE medicines SET description = 'Trà xanh Olong giảm cân. Hỗ trợ đốt mỡ thừa, thanh lọc cơ thể.' WHERE id = UUID_TO_BIN('0aaaaaaa-0091-0091-0091-000000000091');
UPDATE medicines SET description = 'Kết hợp Glucosamine và Chondroitin. Bảo vệ và tái tạo sụn khớp toàn diện.' WHERE id = UUID_TO_BIN('0aaaaaaa-0092-0092-0092-000000000092');
UPDATE medicines SET description = 'Bổ sung Vitamin D3 và K2. Hỗ trợ hấp thu canxi, chắc khỏe xương.' WHERE id = UUID_TO_BIN('0aaaaaaa-0093-0093-0093-000000000093');
UPDATE medicines SET description = 'Bổ sung canxi từ cơm gạo. Dễ hấp thu, không gây táo bón.' WHERE id = UUID_TO_BIN('0aaaaaaa-0094-0094-0094-000000000094');
UPDATE medicines SET description = 'Trà gừng mật ong ấm bụng. Giảm ho, làm ấm cơ thể, tăng sức đề kháng.' WHERE id = UUID_TO_BIN('0aaaaaaa-0095-0095-0095-000000000095');
UPDATE medicines SET description = 'Kết hợp Kẽm và Vitamin C. Tăng cường miễn dịch, làm đẹp da.' WHERE id = UUID_TO_BIN('0aaaaaaa-0096-0096-0096-000000000096');
UPDATE medicines SET description = 'Giải độc gan từ thảo dược thiên nhiên. Bảo vệ và tái tạo tế bào gan.' WHERE id = UUID_TO_BIN('0aaaaaaa-0097-0097-0097-000000000097');

SELECT '✅ Catalog seeded with ' || COUNT(*) || ' medicines' AS status FROM medicines;