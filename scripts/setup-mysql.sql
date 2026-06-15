-- =====================================================
-- PCMS - MySQL Setup Script
-- Run as MySQL root to create user and 12 databases
-- Usage: mysql -u root -p < scripts/setup-mysql.sql
-- =====================================================

-- Create dedicated user for PCMS
CREATE USER IF NOT EXISTS 'pcms_user'@'localhost' IDENTIFIED BY 'pcms_pass';
CREATE USER IF NOT EXISTS 'pcms_user'@'%'         IDENTIFIED BY 'pcms_pass';

-- Grant privileges on all pcms_* databases
GRANT ALL PRIVILEGES ON `pcms\_%`.* TO 'pcms_user'@'localhost';
GRANT ALL PRIVILEGES ON `pcms\_%`.* TO 'pcms_user'@'%';
GRANT CREATE, DROP ON *.* TO 'pcms_user'@'localhost';
GRANT CREATE, DROP ON *.* TO 'pcms_user'@'%';

FLUSH PRIVILEGES;

-- Create 12 databases (one per business service)
CREATE DATABASE IF NOT EXISTS `pcms_user`         DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_branch`       DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_catalog`      DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_category`     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_supplier`     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_inventory`    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_customer`     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_order`        DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_payment`      DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_prescription` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_notification` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `pcms_report`       DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify
SELECT User, Host FROM mysql.user WHERE User LIKE 'pcms%';
SHOW DATABASES LIKE 'pcms_%';
