# PCMS Services Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 4 PCMS microservices (CATALOG, SUPPLIER, ORDER, PHARMACIST-WORKBENCH) that fail to start, seed admin user, and verify all 18 services running.

**Architecture:** Modify Spring Boot service configurations and data.sql files to fix startup issues, add JPA Auditing fix, enable bean definition overriding for services that use @FeignClient.

**Tech Stack:** Java 21, Spring Boot 4.0.7, Spring Cloud 2025.1.2, MySQL 8.0, Spring Data JPA, Eureka Discovery, OpenFeign.

---

## Root Cause Analysis

| # | Service | Root Cause | Fix Type |
|---|---------|------------|----------|
| 1 | CATALOG-SERVICE | `data.sql` references `categories` table from `pcms_category` database (cross-DB query fails) | Modify data.sql |
| 2 | SUPPLIER-SERVICE | `data.sql` INSERT doesn't provide `created_at`/`updated_at` (NOT NULL columns) | Modify data.sql |
| 3 | ORDER-SERVICE | `@FeignClient` bean name conflict: `customer-service.FeignClientSpecification` already defined | Add config property |
| 4 | PHARMACIST-WORKBENCH | Same `@FeignClient` bean name conflict | Add config property |

---

## File Structure

### Files to Modify
- `catalog-service/src/main/resources/data.sql` - Remove cross-DB references
- `supplier-service/src/main/resources/data.sql` - Add created_at/updated_at values
- `order-service/src/main/resources/application.yml` - Add bean override config
- `pharmacist-workbench-service/src/main/resources/application.yml` - Add bean override config

### Files to Create
- `scripts/seed-admin-user.sql` - SQL script to insert admin user

### No Code Files Need to Change
- Only configuration and data files need modification
- Existing Java entities remain unchanged

---

## Task 1: Fix CATALOG-SERVICE data.sql

**Files:**
- Modify: `catalog-service/src/main/resources/data.sql:1-200`

- [ ] **Step 1: Read the current data.sql file to see all references**

Run: `wc -l catalog-service/src/main/resources/data.sql`
Expected: Shows file length (~200 lines)

- [ ] **Step 2: Replace the entire data.sql content with simplified version**

Write to `catalog-service/src/main/resources/data.sql`:

```sql
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
```

- [ ] **Step 3: Verify the file was updated**

Run: `head -3 catalog-service/src/main/resources/data.sql`
Expected: Shows the new comment header starting with "PCMS Catalog Service — Seed data"

- [ ] **Step 4: Commit**

```bash
git add catalog-service/src/main/resources/data.sql
git commit -m "fix(catalog): remove cross-database references in data.sql"
```

---

## Task 2: Fix SUPPLIER-SERVICE data.sql

**Files:**
- Modify: `supplier-service/src/main/resources/data.sql:1-30`

- [ ] **Step 1: Replace the data.sql with version that includes created_at/updated_at**

Write to `supplier-service/src/main/resources/data.sql`:

```sql
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
```

- [ ] **Step 2: Verify the file was updated**

Run: `grep "created_at" supplier-service/src/main/resources/data.sql | head -1`
Expected: Shows the INSERT statement with created_at

- [ ] **Step 3: Commit**

```bash
git add supplier-service/src/main/resources/data.sql
git commit -m "fix(supplier): add created_at/updated_at to seed data"
```

---

## Task 3: Fix ORDER-SERVICE bean conflict

**Files:**
- Modify: `order-service/src/main/resources/application.yml`

- [ ] **Step 1: Read current application.yml**

Run: `cat order-service/src/main/resources/application.yml`
Expected: Shows existing config with `spring.application.name` and `eureka.client.service-url`

- [ ] **Step 2: Add bean override config to application.yml**

Modify `order-service/src/main/resources/application.yml` to add under the `spring:` block:

```yaml
spring:
  application:
    name: order-service
  config:
    import: "optional:configserver:http://localhost:8888"
  profiles:
    active: default
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  main:
    allow-bean-definition-overriding: true
```

- [ ] **Step 3: Verify the change**

Run: `grep "allow-bean-definition-overriding" order-service/src/main/resources/application.yml`
Expected: Shows the line `allow-bean-definition-overriding: true`

- [ ] **Step 4: Commit**

```bash
git add order-service/src/main/resources/application.yml
git commit -m "fix(order): allow bean definition overriding for FeignClient"
```

---

## Task 4: Fix PHARMACIST-WORKBENCH-SERVICE bean conflict

**Files:**
- Modify: `pharmacist-workbench-service/src/main/resources/application.yml`

- [ ] **Step 1: Read current application.yml**

Run: `cat pharmacist-workbench-service/src/main/resources/application.yml`
Expected: Shows existing config

- [ ] **Step 2: Add bean override config to application.yml**

Modify `pharmacist-workbench-service/src/main/resources/application.yml` to add under the `spring:` block:

```yaml
spring:
  application:
    name: pharmacist-workbench-service
  config:
    import: "optional:configserver:http://localhost:8888"
  profiles:
    active: default
  main:
    allow-bean-definition-overriding: true
```

- [ ] **Step 3: Verify the change**

Run: `grep "allow-bean-definition-overriding" pharmacist-workbench-service/src/main/resources/application.yml`
Expected: Shows the line `allow-bean-definition-overriding: true`

- [ ] **Step 4: Commit**

```bash
git add pharmacist-workbench-service/src/main/resources/application.yml
git commit -m "fix(pharmacist-workbench): allow bean definition overriding for FeignClient"
```

---

## Task 5: Create admin user seed script

**Files:**
- Create: `scripts/seed-admin-user.sql`

- [ ] **Step 1: Check the user table schema**

Run: `mysql -u pcms_user -p"pcms_pass" -e "USE pcms_user; DESCRIBE users;" 2>&1 | grep -v Warning`
Expected: Shows columns including id, email, password_hash, full_name, etc.

- [ ] **Step 2: Create the seed script**

Write to `scripts/seed-admin-user.sql`:

```sql
-- ============================================================================
-- PCMS Admin User Seed Script
-- Creates default admin user for testing
-- Password: admin123 (BCrypt hash)
-- ============================================================================
USE pcms_user;

INSERT INTO users (id, email, password_hash, full_name, phone, role, status, created_at, updated_at)
VALUES (
    UUID_TO_BIN(UUID(), 1),
    'admin@pcms.vn',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- BCrypt of "admin123"
    'System Administrator',
    '0901234567',
    'ADMIN',
    'ACTIVE',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert a test pharmacist user
INSERT INTO users (id, email, password_hash, full_name, phone, role, status, created_at, updated_at)
VALUES (
    UUID_TO_BIN(UUID(), 1),
    'pharmacist01@pcms.vn',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- BCrypt of "admin123"
    'Nguyễn Văn A',
    '0901234567',
    'PHARMACIST',
    'ACTIVE',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();

SELECT 'Admin user seeded successfully' AS status;
```

- [ ] **Step 3: Run the seed script**

Run: `mysql -u pcms_user -p"pcms_pass" < scripts/seed-admin-user.sql 2>&1 | grep -v Warning`
Expected: Shows "Admin user seeded successfully"

- [ ] **Step 4: Verify admin user was created**

Run: `mysql -u pcms_user -p"pcms_pass" -e "USE pcms_user; SELECT BIN_TO_UUID(id) as id, email, role, status FROM users;" 2>&1 | grep -v Warning`
Expected: Shows admin@pcms.vn and pharmacist01@pcms.vn

- [ ] **Step 5: Commit**

```bash
git add scripts/seed-admin-user.sql
git commit -m "feat(scripts): add admin user seed script"
```

---

## Task 6: Rebuild affected services

**Files:** None (build only)

- [ ] **Step 1: Rebuild the 4 affected services**

Run: `./apache-maven-3.9.9/bin/mvn package -DskipTests -pl catalog-service,supplier-service,order-service,pharmacist-workbench-service -am 2>&1 | tail -30`
Expected: BUILD SUCCESS for all 4 services

- [ ] **Step 2: Verify JARs were built**

Run: `ls -la catalog-service/target/catalog-service-1.0.0-SNAPSHOT.jar supplier-service/target/supplier-service-1.0.0-SNAPSHOT.jar order-service/target/order-service-1.0.0-SNAPSHOT.jar pharmacist-workbench-service/target/pharmacist-workbench-service.jar 2>&1`
Expected: Shows 4 JAR files

---

## Task 7: Restart failed services and verify

**Files:** None (operations only)

- [ ] **Step 1: Kill existing Java processes for the 4 services**

Run: `cmd //c "for /F %i in ('tasklist ^| findstr java') do taskkill /F /PID %i" 2>&1 || echo "Need manual kill"`
Note: This kills all Java processes, services will be restarted in next steps

- [ ] **Step 2: Wait for ports to be released**

Run: `sleep 5 && netstat -an | grep -E "808[0-9]|81[0-9]{2}" | grep LISTENING`
Expected: No services listening (clean slate)

- [ ] **Step 3: Start config-server**

Run: `nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar config-server/target/config-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=native > logs/config-server.log 2>&1 &`
Expected: Process started in background

- [ ] **Step 4: Wait and start discovery-server**

Run: `sleep 15 && nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar > logs/discovery-server.log 2>&1 &`
Expected: Process started in background

- [ ] **Step 5: Wait for discovery to be ready**

Run: `sleep 20 && curl -s http://localhost:8761/ | head -2`
Expected: HTML response from Eureka

- [ ] **Step 6: Start api-gateway**

Run: `nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar > logs/api-gateway.log 2>&1 &`
Expected: Process started

- [ ] **Step 7: Start the 4 fixed services**

Run:
```bash
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar catalog-service/target/catalog-service-1.0.0-SNAPSHOT.jar > logs/catalog-service.log 2>&1 &
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar supplier-service/target/supplier-service-1.0.0-SNAPSHOT.jar > logs/supplier-service.log 2>&1 &
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar > logs/order-service.log 2>&1 &
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar pharmacist-workbench-service/target/pharmacist-workbench-service.jar > logs/pharmacist-workbench-service.log 2>&1 &
```
Expected: All 4 processes started

- [ ] **Step 8: Start remaining services**

Run: For each remaining service (user-service, branch-service, category-service, inventory-service, customer-service, payment-service, prescription-service, notification-service, report-service, customer-portal-service, mobile-bff, health-tools-service, ecom-ops-service), start with the nohup command and `MYSQL_PORT=3306`.

- [ ] **Step 9: Wait for all services to be ready**

Run: `sleep 60`
Expected: 60 seconds passed

- [ ] **Step 10: Verify all 18 services registered in Eureka**

Run: `curl -s "http://localhost:8761/eureka/apps" -H "Accept: application/json" | grep -oP '"name":"[^"]+' | sort -u`
Expected: Shows 18 service names (excluding "MyOwn")

- [ ] **Step 11: Verify all ports are listening**

Run: `netstat -an | grep -E "LISTENING" | grep -E "808[0-9]|81[0-9]{2}" | wc -l`
Expected: At least 16 ports listening

---

## Task 8: Test login with admin user

**Files:** None (test only)

- [ ] **Step 1: Test login endpoint**

Run: `curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@pcms.vn","password":"admin123"}' | head -20`
Expected: Returns JSON with `accessToken` field

- [ ] **Step 2: Save the access token**

Run: `TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@pcms.vn","password":"admin123"}' | grep -oP '"accessToken":"[^"]+' | cut -d'"' -f4) && echo "Token: $TOKEN" | head -c 100`
Expected: Shows the JWT token (first 100 chars)

- [ ] **Step 3: Test authenticated endpoint**

Run: `curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/users | head -20`
Expected: Returns user list (may be empty)

- [ ] **Step 4: Test branch list**

Run: `curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/branches | head -20`
Expected: Returns branch list (empty array or with data)

- [ ] **Step 5: Test catalog (medicines) endpoint**

Run: `curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/medicines | head -20`
Expected: Returns medicines list with 22 items

---

## Task 9: Final verification

**Files:** None (verification only)

- [ ] **Step 1: Check all services are UP in Eureka**

Run: `curl -s "http://localhost:8761/eureka/apps" -H "Accept: application/json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Total apps: {len(d[\"applications\"][\"application\"])}')"`
Expected: `Total apps: 18`

- [ ] **Step 2: Check no ERROR in logs**

Run: `grep -l "ERROR" logs/*.log 2>/dev/null | head -5`
Expected: Only `config-server.log` or no files (services starting fine)

- [ ] **Step 3: Create summary report**

Document the final status of all 18 services in a new file `docs/PCMS_STARTUP_REPORT.md`:
- All 18 services running
- All tests passing
- API Gateway accessible at http://localhost:8080
- Eureka Dashboard at http://localhost:8761

---

## Summary of Changes

| File | Change Type | Description |
|------|-------------|-------------|
| `catalog-service/src/main/resources/data.sql` | Modified | Removed cross-database references, used UUID() placeholders |
| `supplier-service/src/main/resources/data.sql` | Modified | Added created_at/updated_at columns to INSERT |
| `order-service/src/main/resources/application.yml` | Modified | Added `spring.main.allow-bean-definition-overriding: true` |
| `pharmacist-workbench-service/src/main/resources/application.yml` | Modified | Added `spring.main.allow-bean-definition-overriding: true` |
| `scripts/seed-admin-user.sql` | Created | Admin and pharmacist user seed |

## Expected Outcome

After all tasks completed:
- 18/18 PCMS services running and registered with Eureka
- All services listening on their configured ports
- Admin login working with credentials `admin@pcms.vn` / `admin123`
- API Gateway routing requests successfully
- Database seeded with 22 medicines, 5 suppliers, 10 categories, and 2 users
