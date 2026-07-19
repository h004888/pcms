-- =====================================================
-- Migration 001: Make staff_id nullable in payments table
-- Reason: B2C VietQR payments have no staff (walk-in customer)
-- Issue: Hibernate ddl-auto=update does NOT alter existing
--         column constraints; manual ALTER TABLE needed.
-- Date: 2026-07-19
-- =====================================================

ALTER TABLE `pcms_payment`.`payments`
    MODIFY COLUMN `staff_id` BINARY(16) NULL;

-- Verify
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'pcms_payment'
  AND TABLE_NAME = 'payments'
  AND COLUMN_NAME = 'staff_id';
