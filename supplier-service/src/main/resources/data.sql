-- ============================================================================
-- PCMS Supplier Service — Seed data
-- 5 suppliers lon cua VN
-- ============================================================================
INSERT INTO suppliers (id, name, tax_code, contact_person, phone, email, address, bank_name, bank_account, status, created_at, updated_at) VALUES
(UUID_TO_BIN(UUID(), 1), N'Công ty CP Dược phẩm Imexpharm',     '0300123456', N'Nguyễn Văn A', '0281234500', 'sales@imexpharm.com',      N'12 Đường 3 tháng 2, Q10, HCM',         N'Vietcombank',       '0071001234567',  'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), N'Công ty CP Traphaco',                '0300234567', N'Trần Thị B',    '0243456700', 'info@traphaco.com.vn',     N'75 Yên Nghĩa, Hà Đông, HN',            N'BIDV',              '1201000234567',  'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), N'Công ty CP Dược Hậu Giang',          '0300345678', N'Lê Văn C',      '0292384500', 'contact@dhgpharma.com.vn', N'288 Bis Nguyễn Văn Cừ, Q1, Cần Thơ',   N'Techcombank',       '1902012345678',  'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), N'Công ty CP Sanofi Việt Nam',         '0300456789', N'Phạm Văn D',    '0285678900', 'vn@sanofi.com',            N'10 Hàm Nghi, Q1, HCM',                 N'HSBC',              '001234567890',   'ACTIVE', NOW(), NOW()),
(UUID_TO_BIN(UUID(), 1), N'Công ty CP AstraZeneca Việt Nam',    '0300567890', N'Hoàng Thị E',   '0286789000', 'vn@astrazeneca.com',       N'18 Lý Thường Kiệt, Hoàn Kiếm, HN',     N'Standard Chartered','8881234567',     'ACTIVE', NOW(), NOW());
