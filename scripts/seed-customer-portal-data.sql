-- =====================================================
-- PCMS - Seed data for customer-portal-service
-- Run: mysql -u pcms_user -ppcms_pass pcms_customer_portal < seed-customer-portal-data.sql
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE vaccine_slots;
TRUNCATE TABLE vaccines;
TRUNCATE TABLE vouchers;
TRUNCATE TABLE health_articles;
TRUNCATE TABLE disease_info;
TRUNCATE TABLE herbs;
TRUNCATE TABLE ingredients;
TRUNCATE TABLE videos;
TRUNCATE TABLE home_banners;
TRUNCATE TABLE review;
SET FOREIGN_KEY_CHECKS=1;

-- ============= VACCINES =============
INSERT INTO vaccines (id, name, manufacturer, description, doses_required, days_between_doses, price, status, created_at, updated_at) VALUES
(UUID_TO_BIN('a1111111-1111-1111-1111-111111111111'), 'Vaccine Cúm mùa (Influenza)', 'Sanofi Pasteur', 'Vaccine phòng cúm mùa cho người lớn và trẻ em trên 6 tháng tuổi', 1, 365, 350000, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), 'Vaccine COVID-19 (Pfizer-BioNTech)', 'Pfizer', 'Vaccine mRNA phòng COVID-19, liều nhắc lại mỗi 6-12 tháng', 1, 180, 0, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), 'Vaccine Viêm gan B', 'GSK', 'Vaccine phòng viêm gan B, lịch 3 liều (0-1-6 tháng)', 3, 30, 250000, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('a4444444-4444-4444-4444-444444444444'), 'Vaccine Uốn ván - Bạch hầu (Td)', 'Sanofi Pasteur', 'Vaccine nhắc lại phòng uốn ván và bạch hầu cho người lớn', 1, 3650, 120000, 'ACTIVE', NOW(), NOW());

-- ============= VACCINE SLOTS =============
INSERT INTO vaccine_slots (id, vaccine_id, branch_id, slot_date, slot_time, total_qty, available_qty, created_at, updated_at) VALUES
(UUID_TO_BIN('b1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('a1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('11f16f7a-69ab-7347-bf04-bceca04d7b24'), CURDATE() + INTERVAL 1 DAY, '09:00:00', 20, 18, NOW(), NOW()),
(UUID_TO_BIN('b1111111-2222-2222-2222-222222222222'), UUID_TO_BIN('a1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('11f16f7a-69ab-2858-bf04-bceca04d7b24'), CURDATE() + INTERVAL 1 DAY, '14:00:00', 15, 12, NOW(), NOW()),
(UUID_TO_BIN('b2222222-1111-1111-1111-111111111111'), UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('11f16f7a-69ab-7347-bf04-bceca04d7b24'), CURDATE() + INTERVAL 2 DAY, '08:30:00', 30, 25, NOW(), NOW()),
(UUID_TO_BIN('b3333333-1111-1111-1111-111111111111'), UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('11f16f7a-69ab-7077-bf04-bceca04d7b24'), CURDATE() + INTERVAL 3 DAY, '10:00:00', 10, 9, NOW(), NOW());

-- ============= VOUCHERS =============
INSERT INTO vouchers (id, code, description, value, min_order_amount, max_discount, usage_limit, per_user_limit, used_count, valid_from, valid_to, status, created_at, updated_at) VALUES
(UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), 'WELCOME10', 'Giảm 10% đơn hàng đầu tiên', 10, 200000, 100000, 1000, 1, 0, NOW(), NOW() + INTERVAL 6 MONTH, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('c2222222-2222-2222-2222-222222222222'), 'FREESHIP50K', 'Miễn phí vận chuyển đơn từ 300K', 50000, 300000, 50000, 5000, 3, 142, NOW(), NOW() + INTERVAL 3 MONTH, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('c3333333-3333-3333-3333-333333333333'), 'SUMMER25', 'Giảm 25% thuốc không kê đơn', 25, 0, 150000, 2000, 1, 87, NOW(), NOW() + INTERVAL 2 MONTH, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('c4444444-4444-4444-4444-444444444444'), 'VIP100K', 'Voucher VIP trị giá 100K', 100000, 500000, 100000, 500, 1, 12, NOW(), NOW() + INTERVAL 12 MONTH, 'ACTIVE', NOW(), NOW());

-- ============= HEALTH ARTICLES =============
INSERT INTO health_articles (id, slug, title, body_markdown, category, author, view_count, status, published_at, created_at, updated_at) VALUES
(UUID_TO_BIN('d1111111-1111-1111-1111-111111111111'), 'cach-su-dung-paracetamol-dung-lieu',
 'Cách sử dụng Paracetamol đúng liều cho người lớn và trẻ em',
 '## Liều dùng Paracetamol\n\nNgười lớn: 500mg-1000mg mỗi 4-6 giờ, tối đa 4g/ngày.\n\nTrẻ em: 10-15mg/kg mỗi 4-6 giờ.',
 'meo-vat', 'DS. Nguyễn Văn A', 1240, 'PUBLISHED', NOW(), NOW(), NOW()),
(UUID_TO_BIN('d2222222-2222-2222-2222-222222222222'), 'phan-biet-cam-cum-va-cam-lanh',
 'Phân biệt cảm cúm và cảm lạnh — dấu hiệu nhận biết',
 '## Cảm lạnh thường gặp\n- Hắt hơi, sổ mũi nhẹ\n## Cảm cúm (Influenza)\n- Sốt cao 38-40°C\n- Đau cơ, mệt mỏi',
 'suc-khoe-tong-quat', 'BS. Trần Thị B', 892, 'PUBLISHED', NOW(), NOW(), NOW()),
(UUID_TO_BIN('d3333333-3333-3333-3333-333333333333'), 'dinh-duong-cho-ba-bau',
 'Dinh dưỡng cho bà bầu 3 tháng đầu',
 '## Những thực phẩm cần bổ sung\n- Axit folic, Sắt, Canxi, Omega-3',
 'thai-ky-tre-em', 'BS. Lê Thị C', 1567, 'PUBLISHED', NOW(), NOW(), NOW()),
(UUID_TO_BIN('d4444444-4444-4444-4444-444444444444'), 'kiem-soat-benh-tieu-duong-tuyp-2',
 'Kiểm soát bệnh tiểu đường tuýp 2 — 5 nguyên tắc vàng',
 '## 5 nguyên tắc: Chế độ ăn, Tập thể dục, Uống thuốc, Đo đường huyết, Khám bác sĩ',
 'benh-man-tinh', 'BS. Phạm Văn D', 2103, 'PUBLISHED', NOW(), NOW(), NOW()),
(UUID_TO_BIN('d5555555-5555-5555-5555-555555555555'), 'phong-cum-mua-dong',
 'Phòng cúm mùa đông cho trẻ nhỏ',
 '## 7 cách: Tiêm vaccine, Rửa tay, Tránh đông người, Vitamin C, Giữ ấm',
 'phong-benh', 'DS. Hoàng Thị E', 1789, 'PUBLISHED', NOW(), NOW(), NOW()),
(UUID_TO_BIN('d6666666-6666-6666-6666-666666666666'), 'tac-dung-cua-vitamin-c',
 'Tác dụng thần kỳ của Vitamin C đối với sức khỏe',
 '## Vitamin C: Tăng miễn dịch, Chống oxy hóa, Hỗ trợ hấp thu sắt',
 'dinh-duong', 'DS. Nguyễn Văn A', 3421, 'PUBLISHED', NOW(), NOW(), NOW());

-- ============= DISEASES =============
INSERT INTO disease_info (id, slug, name, body, severity, season, target_audience, view_count, created_at, updated_at) VALUES
(UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), 'tang-huyet-ap', 'Tăng huyết áp',
 '## Tăng huyết áp\n\n**Triệu chứng:** Đau đầu, chóng mặt, ù tai', 'HIGH', 'ALL_YEAR', 'ADULT', 3500, NOW(), NOW()),
(UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), 'da-thai-duong', 'Đái tháo đường tuýp 2',
 '## Đái tháo đường type 2\n\n**Chỉ số:** HbA1c > 6.5%', 'CRITICAL', 'ALL_YEAR', 'ADULT', 2800, NOW(), NOW()),
(UUID_TO_BIN('e3333333-3333-3333-3333-333333333333'), 'viem-hong-cap', 'Viêm họng cấp',
 '## Viêm họng cấp\n\n**Triệu chứng:** Đau rát họng, khó nuốt, sốt nhẹ', 'LOW', 'WINTER', 'ALL', 1500, NOW(), NOW()),
(UUID_TO_BIN('e4444444-4444-4444-4444-444444444444'), 'cum-mua', 'Cúm mùa',
 '## Cúm mùa\n\n**Triệu chứng:** Sốt cao đột ngột, đau cơ', 'MEDIUM', 'WINTER', 'ALL', 4200, NOW(), NOW()),
(UUID_TO_BIN('e5555555-5555-5555-5555-555555555555'), 'sot-xuat-huyet', 'Sốt xuất huyết',
 '## Sốt xuất huyết Dengue\n\n**Triệu chứng:** Sốt cao, phát ban', 'CRITICAL', 'SUMMER', 'ALL', 5100, NOW(), NOW());

-- ============= HERBS =============
INSERT INTO herbs (id, name_vi, name_en, traditional_use, image_url, created_at, updated_at) VALUES
(UUID_TO_BIN('1a111111-1111-1111-1111-111111111111'), 'Gừng', 'Ginger (Zingiber officinale)',
 'Chống nôn, giảm đau, kháng viêm.', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/Ginger_Whole.jpg/640px-Ginger_Whole.jpg', NOW(), NOW()),
(UUID_TO_BIN('2a222222-2222-2222-2222-222222222222'), 'Nghệ', 'Turmeric (Curcuma longa)',
 'Chống viêm, lành vết thương, bảo vệ gan.', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Curcuma_longa_002.jpg/640px-Curcuma_longa_002.jpg', NOW(), NOW()),
(UUID_TO_BIN('3a333333-3333-3333-3333-333333333333'), 'Atisô', 'Artichoke (Cynara scolymus)',
 'Thanh nhiệt, mát gan, lợi mật.', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Artichoke.jpg/640px-Artichoke.jpg', NOW(), NOW()),
(UUID_TO_BIN('4a444444-4444-4444-4444-444444444444'), 'Xạ đen', 'Celastrus hindsii',
 'Hỗ trợ điều trị ung thư, kháng viêm.', 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Celastrus_hindsii.jpg/640px-Celastrus_hindsii.jpg', NOW(), NOW());

-- ============= INGREDIENTS =============
INSERT INTO ingredients (id, name_vi, name_en, synonyms, created_at, updated_at) VALUES
(UUID_TO_BIN('5b111111-1111-1111-1111-111111111111'), 'Paracetamol', 'Paracetamol (Acetaminophen)', 'Acetaminophen, Tylenol, Panadol', NOW(), NOW()),
(UUID_TO_BIN('6b222222-2222-2222-2222-222222222222'), 'Ibuprofen', 'Ibuprofen', 'Brufen, Advil, Motrin', NOW(), NOW()),
(UUID_TO_BIN('7b333333-3333-3333-3333-333333333333'), 'Amoxicillin', 'Amoxicillin', 'Amoxil, Augmentin', NOW(), NOW()),
(UUID_TO_BIN('8b444444-4444-4444-4444-444444444444'), 'Metformin', 'Metformin', 'Glucophage, Glumetza', NOW(), NOW()),
(UUID_TO_BIN('9b555555-5555-5555-5555-555555555555'), 'Atorvastatin', 'Atorvastatin', 'Lipitor', NOW(), NOW());

-- ============= VIDEOS =============
INSERT INTO videos (id, title, youtube_id, thumbnail_url, duration_sec, category, source, status, view_count, created_at, updated_at) VALUES
(UUID_TO_BIN('caaaaaaa-1111-1111-1111-111111111111'), 'Cách sử dụng Paracetamol đúng liều', 'dQw4w9WgXcQ', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg', 135, 'huong-dan', 'Bộ Y tế', 'PUBLISHED', 1500, NOW(), NOW()),
(UUID_TO_BIN('cbbbbbbb-2222-2222-2222-222222222222'), 'Phân biệt cảm cúm và cảm lạnh', 'abc123XYZ', 'https://i.ytimg.com/vi/abc123XYZ/maxresdefault.jpg', 220, 'phong-benh', 'WHO', 'PUBLISHED', 980, NOW(), NOW()),
(UUID_TO_BIN('cccccccc-3333-3333-3333-333333333333'), '5 dấu hiệu sốt xuất huyết cần nhập viện', 'sxh2024demo', 'https://i.ytimg.com/vi/sxh2024demo/maxresdefault.jpg', 260, 'canh-bao', 'Bộ Y tế', 'PUBLISHED', 2100, NOW(), NOW()),
(UUID_TO_BIN('cddddddd-4444-4444-4444-444444444444'), 'Bảo quản thuốc trong mùa nóng', 'thuoc-mua-nong', 'https://i.ytimg.com/vi/thuoc-mua-nong/maxresdefault.jpg', 115, 'huong-dan', 'Dược sĩ tư vấn', 'PUBLISHED', 750, NOW(), NOW());

-- ============= HOME BANNERS =============
INSERT INTO home_banners (id, title, image_url, link_url, position, sort_order, status, start_at, end_at, created_at, updated_at) VALUES
(UUID_TO_BIN('ceeeeeee-1111-1111-1111-111111111111'), 'FPT Long Châu - Đồng hành cùng sức khỏe Việt', 'https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=1200', '/mobile', 'HERO', 1, 'ACTIVE', NOW(), NOW() + INTERVAL 6 MONTH, NOW(), NOW()),
(UUID_TO_BIN('cfffffff-2222-2222-2222-222222222222'), 'Chương trình Tích điểm đổi quà', 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=1200', '/wallet', 'HERO', 2, 'ACTIVE', NOW(), NOW() + INTERVAL 6 MONTH, NOW(), NOW()),
(UUID_TO_BIN('caaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1'), 'TRỢ LÝ DINH DƯỠNG TẬN TÌNH', '/promos/dinh-duong-1.png', '/dinh-duong', 'SUB_PROMO', 1, 'ACTIVE', NOW(), NOW() + INTERVAL 6 MONTH, NOW(), NOW()),
(UUID_TO_BIN('caaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee2'), 'DINH DƯỠNG ĐÚNG CÁCH', '/promos/dinh-duong-2.png', '/dinh-duong', 'SUB_PROMO', 2, 'ACTIVE', NOW(), NOW() + INTERVAL 6 MONTH, NOW(), NOW());

-- ============= REVIEWS (SPRINT 3 - T11) =============
INSERT INTO review (id, customer_id, medicine_id, rating, comment, created_at, updated_at) VALUES
-- Top 1: Clorpheniramin 4mg (0aaaaaaa-0038)
(UUID_TO_BIN('a0000001-aaaa-bbbb-cccc-000000000001'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0038-0038-0038-000000000038'), 5, 'Giảm dị ứng nhanh, không gây buồn ngủ nhiều.', NOW(), NOW()),
(UUID_TO_BIN('a0000002-aaaa-bbbb-cccc-000000000002'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('0aaaaaaa-0038-0038-0038-000000000038'), 4, 'Dùng tốt, hết ngứa sau 30 phút.', NOW(), NOW()),
(UUID_TO_BIN('a0000003-aaaa-bbbb-cccc-000000000003'), UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), UUID_TO_BIN('0aaaaaaa-0038-0038-0038-000000000038'), 5, 'Thuốc tốt, giá rẻ, nên mua.', NOW(), NOW()),
-- Top 2: Kem chống nắng SPF50 (0aaaaaaa-0076)
(UUID_TO_BIN('a0000004-aaaa-bbbb-cccc-000000000004'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0076-0076-0076-000000000076'), 5, 'Kem mịn, không bết dính, nâng tông nhẹ.', NOW(), NOW()),
(UUID_TO_BIN('a0000005-aaaa-bbbb-cccc-000000000005'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('0aaaaaaa-0076-0076-0076-000000000076'), 4, 'Chống nắng tốt, dùng hè này rất ưng.', NOW(), NOW()),
-- Top 3: Cetirizin 10mg (0aaaaaaa-0039)
(UUID_TO_BIN('a0000006-aaaa-bbbb-cccc-000000000006'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0039-0039-0039-000000000039'), 5, 'Viêm mũi dị ứng đỡ hẳn sau 1 liều.', NOW(), NOW()),
(UUID_TO_BIN('a0000007-aaaa-bbbb-cccc-000000000007'), UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), UUID_TO_BIN('0aaaaaaa-0039-0039-0039-000000000039'), 4, 'Ít buồn ngủ hơn thuốc cũ, dùng ổn.', NOW(), NOW()),
(UUID_TO_BIN('a0000008-aaaa-bbbb-cccc-000000000008'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('0aaaaaaa-0039-0039-0039-000000000039'), 3, 'Tạm được, cần thời gian mới thấy hiệu quả.', NOW(), NOW()),
-- Top 4: Canxi Cơm (0aaaaaaa-0094)
(UUID_TO_BIN('a0000009-aaaa-bbbb-cccc-000000000009'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0094-0094-0094-000000000094'), 5, 'Canxi dễ uống, không táo bón như loại khác.', NOW(), NOW()),
(UUID_TO_BIN('a0000010-aaaa-bbbb-cccc-000000000010'), UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), UUID_TO_BIN('0aaaaaaa-0094-0094-0094-000000000094'), 5, 'Bà bầu dùng rất tốt, canxi từ cơm dễ hấp thu.', NOW(), NOW()),
-- Top 5: Coldacmin Flu (0aaaaaaa-0035)
(UUID_TO_BIN('a0000011-aaaa-bbbb-cccc-000000000011'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('0aaaaaaa-0035-0035-0035-000000000035'), 4, 'Uống vào ngủ một giấc dậy khỏe ngay.', NOW(), NOW()),
(UUID_TO_BIN('a0000012-aaaa-bbbb-cccc-000000000012'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0035-0035-0035-000000000035'), 4, 'Cảm cúm uống 2 viên là đỡ hẳn.', NOW(), NOW()),
-- Top 6: Enalapril 5mg (0aaaaaaa-0059)
(UUID_TO_BIN('a0000013-aaaa-bbbb-cccc-000000000013'), UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), UUID_TO_BIN('0aaaaaaa-0059-0059-0059-000000000059'), 5, 'Huyết áp ổn định sau 1 tuần sử dụng.', NOW(), NOW()),
-- Top 7: Siro ho trẻ em (0aaaaaaa-0045)
(UUID_TO_BIN('a0000014-aaaa-bbbb-cccc-000000000014'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0045-0045-0045-000000000045'), 5, 'Bé nhà mình uống 3 ngày hết ho, vị ngọt dễ uống.', NOW(), NOW()),
(UUID_TO_BIN('a0000015-aaaa-bbbb-cccc-000000000015'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('0aaaaaaa-0045-0045-0045-000000000045'), 4, 'Trẻ em dùng an toàn, không tác dụng phụ.', NOW(), NOW()),
-- Top 8: Telmisartan 40mg (0aaaaaaa-0064)
(UUID_TO_BIN('a0000016-aaaa-bbbb-cccc-000000000016'), UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), UUID_TO_BIN('0aaaaaaa-0064-0064-0064-000000000064'), 5, 'Huyết áp ổn, ít tác dụng phụ hơn Losartan.', NOW(), NOW()),
-- Top 9: Bổ Phế Nam Hà (0aaaaaaa-0043)
(UUID_TO_BIN('a0000017-aaaa-bbbb-cccc-000000000017'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0043-0043-0043-000000000043'), 5, 'Thuốc đông y mà hiệu quả bất ngờ, đỡ ho rõ rệt.', NOW(), NOW()),
(UUID_TO_BIN('a0000018-aaaa-bbbb-cccc-000000000018'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('0aaaaaaa-0043-0043-0043-000000000043'), 3, 'Vị hơi khó uống nhưng công nhận hiệu quả.', NOW(), NOW()),
-- Top 10: Vitamin D3 K2 (0aaaaaaa-0093)
(UUID_TO_BIN('a0000019-aaaa-bbbb-cccc-000000000019'), UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), UUID_TO_BIN('0aaaaaaa-0093-0093-0093-000000000093'), 5, 'Bổ sung D3 K2 cho bé, hàng chính hãng.', NOW(), NOW()),
(UUID_TO_BIN('a0000020-aaaa-bbbb-cccc-000000000020'), UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('0aaaaaaa-0093-0093-0093-000000000093'), 4, 'Uống đều đặn thấy xương chắc khỏe hơn.', NOW(), NOW());

-- ============= QUICK LINKS (Phase 5 - SHOP-HOME hot categories strip) =============
INSERT INTO quick_links (id, label, icon, href, sort_order, status, created_at, updated_at) VALUES
(UUID_TO_BIN('faaaaaaa-1111-1111-1111-111111111111'), 'Cẩm nang Thuốc', 'Stethoscope', '/cam-nang', 1, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('faaaaaaa-2222-2222-2222-222222222222'), 'Tư vấn dược sĩ', 'MessageCircle', '/tu-van', 2, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('faaaaaaa-3333-3333-3333-333333333333'), 'Đơn của tôi', 'FileText', '/don-hang', 3, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('faaaaaaa-4444-4444-4444-444444444444'), 'Tìm nhà thuốc', 'MapPin', '/he-thong-cua-hang', 4, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('faaaaaaa-5555-5555-5555-555555555555'), 'Tiêm Vắc xin', 'Syringe', '/tiem-chung', 5, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('faaaaaaa-6666-6666-6666-666666666666'), 'Tra thuốc chính hãng', 'Shield', '/tra-cuu-thuoc', 6, 'ACTIVE', NOW(), NOW());

-- ============= BRANDS (Phase 1 - SHOP-HOME) =============
INSERT INTO brands (id, name, logo_url, sort_order, status, created_at, updated_at) VALUES
(UUID_TO_BIN('f1111111-1111-1111-1111-111111111111'), 'JOHNSON''S', '/brands/johnson.png', 1, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('f2222222-2222-2222-2222-222222222222'), 'Abbott', '/brands/abbott.png', 2, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('f3333333-3333-3333-3333-333333333333'), 'DUREX', '/brands/durex.png', 3, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('f4444444-4444-4444-4444-444444444444'), 'OMRON', '/brands/omron.png', 4, 'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN('f5555555-5555-5555-5555-555555555555'), 'Blackmores', '/brands/blackmores.png', 5, 'ACTIVE', NOW(), NOW());

-- ============= SUMMARY =============
SELECT '✅ Seed customer-portal completed!' AS status;
SELECT 'vaccines' AS tbl, COUNT(*) AS cnt FROM vaccines
UNION SELECT 'vaccine_slots', COUNT(*) FROM vaccine_slots
UNION SELECT 'vouchers', COUNT(*) FROM vouchers
UNION SELECT 'health_articles', COUNT(*) FROM health_articles
UNION SELECT 'diseases', COUNT(*) FROM disease_info
UNION SELECT 'herbs', COUNT(*) FROM herbs
UNION SELECT 'ingredients', COUNT(*) FROM ingredients
UNION SELECT 'videos', COUNT(*) FROM videos
UNION SELECT 'home_banners', COUNT(*) FROM home_banners
UNION SELECT 'reviews', COUNT(*) FROM review;