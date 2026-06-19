# UAT MASTER PLAN - PCMS (Pharmacy Chain Management System)

**Version:** 1.0
**Date:** 2026-06-18
**Backend code state:** Local `main` after merge of `origin/khanh` (commit `63b9397`)
**Test type:** User Acceptance Testing - REST API (Backend Only)

---

## 1. Project Information

╔══════════════════════════════════════════════════════════════════════════════╗
║  PCMS - Pharmacy Chain Management System                                      ║
║  Stack    : Spring Boot 4.0.7 + Spring Cloud 2025.1.2 + MySQL 8 + Eureka    ║
║  Services : 12 microservices behind Spring Cloud Gateway                      ║
║  Auth     : JWT (HS256) - currently permitAll in dev profile                 ║
║  Frontend : (out of scope for this UAT - API only)                           ║
╚══════════════════════════════════════════════════════════════════════════════╝

### 1.1 Service Map (via API Gateway - port 8080)

| #  | Service               | Port | Gateway Path Prefix                | Health           |
|----|-----------------------|------|------------------------------------|------------------|
| 1  | api-gateway           | 8080 | (root)                             | `/actuator/health` |
| 2  | discovery-server      | 8761 | (Eureka)                           | `/actuator/health` |
| 3  | config-server         | 8888 | (Config)                           | `/actuator/health` |
| 4  | user-service          | 8081 | `/api/v1/auth/**` `/api/v1/users/**` `/api/v1/dashboard/**` `/api/v1/audit-logs/**` | `/actuator/health` |
| 5  | branch-service        | 8082 | `/api/v1/branches/**`              | `/actuator/health` |
| 6  | catalog-service       | 8083 | `/api/v1/medicines/**` `/api/v1/search/**` | `/actuator/health` |
| 7  | category-service      | 8084 | `/api/v1/categories/**`            | `/actuator/health` |
| 8  | supplier-service      | 8085 | `/api/v1/suppliers/**`             | `/actuator/health` |
| 9  | inventory-service     | 8086 | `/api/v1/inventory/**`             | `/actuator/health` |
| 10 | customer-service      | 8087 | `/api/v1/customers/**`             | `/actuator/health` |
| 11 | order-service         | 8088 | `/api/v1/orders/**` `/api/v1/coupons/**` `/api/v1/admin/outbox/**` | `/actuator/health` |
| 12 | payment-service       | 8089 | `/api/v1/payments/**` `/api/v1/webhooks/**` | `/actuator/health` |
| 13 | prescription-service  | 8090 | `/api/v1/prescriptions/**`         | `/actuator/health` |
| 14 | notification-service  | 8091 | `/api/v1/notifications/**`         | `/actuator/health` |
| 15 | report-service        | 8092 | `/api/v1/reports/**`               | `/actuator/health` |

---

## 2. Test Scope (146 endpoints across 12 services)

| Use Case | Module          | Endpoints | UAT Doc                |
|----------|-----------------|-----------|------------------------|
| UC01     | Auth            | 5         | `01-AUTH-USER.md`      |
| UC02     | User Mgmt       | 13        | `01-AUTH-USER.md`      |
| UC03     | Branch          | 9         | `02-BRANCH.md`         |
| UC04     | Catalog/Medicine| 13        | `03-CATALOG.md`        |
| UC05     | Inventory       | 21        | `04-INVENTORY.md`      |
| UC06     | Order & Coupon  | 14        | `05-ORDER.md`          |
| UC07     | Payment         | 9         | `06-PAYMENT.md`        |
| UC08     | Customer        | 13        | `07-CUSTOMER.md`       |
| UC09     | Report          | 11        | `08-REPORT.md`         |
| UC10     | Search          | 4         | `03-CATALOG.md`        |
| UC11     | Supplier        | 6         | `03-CATALOG.md`        |
| UC12     | Prescription    | 9         | `09-PRESCRIPTION.md`   |
| UC13     | Notification    | 14        | `10-NOTIFICATION.md`   |
| E2E      | Cross-service   | 5 flows   | `11-E2E-FLOWS.md`      |

---

## 3. Prerequisites

### 3.1 Environment
```yaml
OS          : Windows 10/11, macOS, or Linux
Java        : JDK 21+
Maven       : 3.9+
Docker      : (optional) for MySQL/Redis containers
MySQL       : 8.x running (default localhost:3306, root/root)
Postman     : v10+ (or use curl/httpie)
Gateway URL : http://localhost:8080
Eureka URL  : http://localhost:8761
```

### 3.2 Startup Sequence
```bash
# 1. Start infrastructure
docker compose -f scripts/docker-compose-infra.yml up -d   # MySQL + Redis

# 2. Start discovery & config first
mvn -pl discovery-server spring-boot:run
mvn -pl config-server spring-boot:run

# 3. Start business services (wait 30s between batches)
mvn -pl user-service,branch-service,category-service,supplier-service spring-boot:run
mvn -pl catalog-service,inventory-service,customer-service spring-boot:run
mvn -pl order-service,payment-service,prescription-service spring-boot:run
mvn -pl notification-service,report-service spring-boot:run

# 4. Start gateway last
mvn -pl api-gateway spring-boot:run

# 5. Verify Eureka registration
curl http://localhost:8761/eureka/apps
```

### 3.3 Test Data Seeding
A SQL seed file must be loaded: `scripts/seed-test-data.sql` containing:
- 1 admin user (`admin@pcms.vn` / `admin123`)
- 1 branch manager + 2 pharmacists
- 3 branches (HQ, District1, District7)
- 5 categories
- 10 suppliers
- 50 medicines
- 20 customers
- 100 inventory batches

If missing, run [Scenario 0.1](#scenario-01-bootstrap-test-data) to bootstrap manually.

---

## 4. Test Conventions

### 4.1 HTTP Method Legend

| Symbol | Method  | Use Case                        |
|--------|---------|---------------------------------|
| GET    | GET     | Read/list resources             |
| POST   | POST    | Create new resource             |
| PUT    | PUT     | Full update / state change      |
| PATCH  | PATCH   | (not used in current code)      |
| DELETE | DELETE  | Soft delete / cancel / refund   |

### 4.2 Status Code Legend

| Code | Meaning                  | When to expect                       |
|------|--------------------------|--------------------------------------|
| 200  | OK                       | Successful read/update               |
| 201  | Created                  | Resource created                     |
| 204  | No Content               | Soft delete / logout success         |
| 400  | Bad Request              | Validation error, missing field      |
| 401  | Unauthorized             | Missing/invalid JWT (prod profile)   |
| 403  | Forbidden                | Wrong role (prod profile)            |
| 404  | Not Found                | UUID/ID does not exist               |
| 409  | Conflict                 | Duplicate (unique constraint)        |
| 422  | Unprocessable Entity     | Business rule violation              |
| 500  | Server Error             | Unexpected exception                 |

### 4.3 ASCII Box Conventions (API-adapted from `uat-testing-ascii` skill)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: [step name]                                          ║
║  METHOD: POST  PATH: /api/v1/xxx                            ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer <JWT>    ← if auth required      ║
║    Content-Type  : application/json                        ║
║    X-User-Id     : <UUID>           ← service-to-service   ║
║    X-Branch-Id   : <UUID>           ← multi-tenant scope   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                         ║
║    "field1": "value",                                      ║
║    "field2": 123                                           ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    201 Created                                             ║
║  {                                                         ║
║    "id": "uuid",                                           ║
║    "field1": "value",                                      ║
║    "createdAt": "2026-06-18T10:00:00Z"                     ║
║  }                                                         ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.4 Indicator Legend

| Symbol | Meaning                                       |
|--------|-----------------------------------------------|
| ← INPUT | Field value to send in request                |
| ← SELECT | Choose between options                       |
| ← VERIFY | Assertion to check in response               |
| ← SEED | Pre-conditions (data must exist)             |
| ← CAPTURE | Save this value as variable for next step   |
| ← ITERATE | Loop scenario N times                        |

### 4.5 Authorization Matrix (target production behavior - dev is permitAll)

| Endpoint Group          | Admin/CEO | Manager | Pharmacist | Customer |
|-------------------------|-----------|---------|------------|----------|
| /auth/*                 | ✓         | ✓       | ✓          | ✓        |
| /users (read)           | ✓         | ✓(branch) | ✓        | ✗        |
| /users (write)          | ✓         | ✗       | ✗          | ✗        |
| /branches (read)        | ✓         | ✓       | ✓          | ✗        |
| /branches (write)       | ✓         | ✗       | ✗          | ✗        |
| /medicines, /categories, /suppliers | ✓ | ✓ | ✓ | ✗ |
| /inventory (write)      | ✓         | ✓       | ✓          | ✗        |
| /orders (create)        | ✓         | ✓       | ✓          | ✗        |
| /orders (approve)       | ✓         | ✓       | ✗          | ✗        |
| /payments               | ✓         | ✓       | ✓          | ✗        |
| /customers              | ✓         | ✓       | ✓          | ✓(own)   |
| /prescriptions          | ✓         | ✓       | ✓          | ✓(own)   |
| /reports (revenue, staff) | ✓       | ✓(branch)| ✗         | ✗        |
| /notifications          | ✓         | ✓       | ✓          | ✓(own)   |
| /admin/outbox/*         | ✓         | ✗       | ✗          | ✗        |
| /webhooks/*             | (Gateway) | (Gateway)| (Gateway) | (Gateway)|

---

## 5. Test Strategy

### 5.1 Test Levels

```
┌─────────────────────────────────────────────────────────────────┐
│  Level 1 - SMOKE  (1 hour)                                      │
│    Goal: All services respond 200 on /actuator/health            │
│    Pass: All 12 services up, Eureka shows 12+ instances         │
├─────────────────────────────────────────────────────────────────┤
│  Level 2 - HAPPY PATH  (3-4 hours)                              │
│    Goal: Each endpoint returns expected success response        │
│    Method: Follow each UAT-XX doc step-by-step with sample data │
├─────────────────────────────────────────────────────────────────┤
│  Level 3 - NEGATIVE PATH  (3-4 hours)                           │
│    Goal: Validation, business rules, error messages            │
│    Method: Send invalid payloads, expect 4xx with proper errors │
├─────────────────────────────────────────────────────────────────┤
│  Level 4 - INTEGRATION  (2-3 hours)                             │
│    Goal: Cross-service flows work (E2E scenarios)               │
│    Method: Run `11-E2E-FLOWS.md` scripts                       │
├─────────────────────────────────────────────────────────────────┤
│  Level 5 - NON-FUNCTIONAL  (1-2 hours)                          │
│    Goal: Performance baselines, error rate < 1%, latency p95   │
│    Method: 100 concurrent users, measure /actuator/metrics     │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Pass / Fail Criteria

**PASS** when:
- All 146 endpoints return correct response for at least 1 happy + 1 negative case
- All 5 E2E flows complete without manual intervention
- No 5xx in any test step (except where explicitly expected)
- Response time p95 < 500ms for reads, < 1000ms for writes

**FAIL** when:
- Any endpoint returns 500 for valid input
- Any business rule documented in code is not enforced
- E2E flow breaks (e.g., order not consuming stock)
- Duplicate / data integrity violation not caught

### 5.3 Bug Severity Matrix

| Severity | Definition                              | SLA    |
|----------|-----------------------------------------|--------|
| Critical | Data loss, security breach, 5xx on main | 4h     |
| Major    | Core feature broken, no workaround     | 1 day  |
| Minor    | Cosmetic, workaround exists             | 1 week |
| Trivial  | Doc typo, log noise                     | next sprint |

---

## 6. Test Execution Tracker

| # | Service / Use Case      | Tester | Date       | Pass | Fail | Bugs | Status |
|---|-------------------------|--------|------------|------|------|------|--------|
| 0 | Environment Bootstrap   |        |            |      |      |      | ☐      |
| 1 | Auth & User (UC01-02)   |        |            |      |      |      | ☐      |
| 2 | Branch (UC03)           |        |            |      |      |      | ☐      |
| 3 | Catalog/Category/Sup(UC04/10/11) |   |        |      |      |      | ☐      |
| 4 | Inventory (UC05)        |        |            |      |      |      | ☐      |
| 5 | Order & Coupon (UC06)   |        |            |      |      |      | ☐      |
| 6 | Payment (UC07)          |        |            |      |      |      | ☐      |
| 7 | Customer (UC08)         |        |            |      |      |      | ☐      |
| 8 | Report (UC09)           |        |            |      |      |      | ☐      |
| 9 | Prescription (UC12)     |        |            |      |      |      | ☐      |
| 10| Notification (UC13)     |        |            |      |      |      | ☐      |
| 11| E2E flows               |        |            |      |      |      | ☐      |

---

## 7. Scenario 0.1 - Bootstrap Test Data

If `scripts/seed-test-data.sql` is not available, run this sequence to bootstrap minimum data.

### Step 1: Create Admin User
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/users                          ║
║  REQUEST:                                                   ║
║  {                                                          ║
║    "email": "admin@pcms.vn",                                ║
║    "fullName": "System Admin",                              ║
║    "phone": "0900000001",                                   ║
║    "role": "ADMIN"                                          ║
║  }                                                          ║
║  EXPECTED: 200, server returns temp password in logs        ║
║  NOTE    : Get temp password from app log:                  ║
║            `grep "Generated password" user-service.log`     ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 2: Create Branches
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/branches  (x3)                 ║
║  REQUEST 1 (HQ):                                            ║
║  { "code":"HQ", "name":"Headquarter",                       ║
║    "address":"12 Le Loi, District 1, HCMC",                 ║
║    "phone":"0281234567" }                                   ║
║  REQUEST 2 (Q1):                                            ║
║  { "code":"Q1", "name":"District 1 Branch",                 ║
║    "address":"100 Nguyen Hue, District 1, HCMC",            ║
║    "phone":"0281234568" }                                   ║
║  REQUEST 3 (Q7):                                            ║
║  { "code":"Q7", "name":"District 7 Branch",                 ║
║    "address":"200 Phu My Hung, District 7, HCMC",           ║
║    "phone":"0281234569" }                                   ║
║  ← CAPTURE: HQ_ID, Q1_ID, Q7_ID                            ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 3: Create Categories
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/categories  (x5)               ║
║  { "name":"Pain Relief", "code":"PR", "description":"..." }  ║
║  { "name":"Antibiotics",  "code":"AB", "description":"..." } ║
║  { "name":"Vitamins",     "code":"VT", "description":"..." } ║
║  { "name":"Cold & Flu",   "code":"CF", "description":"..." } ║
║  { "name":"Digestive",    "code":"DG", "description":"..." } ║
║  ← CAPTURE: 5 category UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 4: Create Suppliers
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/suppliers  (x3)                ║
║  { "name":"DHG Pharma", "taxCode":"0301234567",             ║
║    "phone":"0283333333", "email":"contact@dhg.vn" }          ║
║  { "name":"Traphaco",   "taxCode":"0302345678", ... }        ║
║  { "name":"Imexpharm",  "taxCode":"0303456789", ... }        ║
║  ← CAPTURE: 3 supplier UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 5: Create Medicines
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/medicines  (x5 minimum)        ║
║  REQUEST:                                                   ║
║  { "sku":"MD001", "name":"Paracetamol 500mg",               ║
║    "categoryId":"<PR_ID>", "supplierId":"<SUP1_ID>",        ║
║    "price":5000, "unit":"box",                              ║
║    "prescriptionRequired":false }                           ║
║  ← REPEAT for Amoxicillin 500mg, Vitamin C 1000mg,          ║
║    Cough Syrup, Omeprazole 20mg                             ║
║  ← CAPTURE: 5 medicine UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 6: Import Inventory
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/inventory/import  (x10)        ║
║  REQUEST:                                                   ║
║  {                                                          ║
║    "medicineId":"<MD001_UUID>",                             ║
║    "branchId":"<HQ_ID>",                                    ║
║    "batchNo":"BTH-001", "qty":100,                          ║
║    "expiryDate":"2027-12-31",                               ║
║    "supplierId":"<SUP1_ID>", "minStockLevel":10             ║
║  }                                                          ║
║  ← VERIFY: 200, returns batch with id, remainingQty=100     ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 7: Create Customers
```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/customers  (x3)                ║
║  { "name":"Nguyen Van A", "phone":"0901234567",             ║
║    "email":"a@example.com" }                                ║
║  { "name":"Tran Thi B",   "phone":"0901234568", ... }        ║
║  { "name":"Le Van C",     "phone":"0901234569", ... }        ║
║  ← CAPTURE: 3 customer UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 8: Verify Bootstrap
```
╔══════════════════════════════════════════════════════════════╗
║  CHECK each list endpoint returns > 0 items:                 ║
║    GET /api/v1/users           → 1+ users                    ║
║    GET /api/v1/branches        → 3 branches                  ║
║    GET /api/v1/categories      → 5 categories                ║
║    GET /api/v1/suppliers       → 3 suppliers                 ║
║    GET /api/v1/medicines       → 5 medicines                 ║
║    GET /api/v1/inventory?branchId=<HQ_ID> → 10+ batches     ║
║    GET /api/v1/customers       → 3 customers                 ║
║                                                              ║
║  If any returns 0, repeat the corresponding step above.      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 8. Reference: Common Variables (capture in Postman / env file)

| Variable             | Example                                | Captured from                |
|----------------------|----------------------------------------|------------------------------|
| `{{gateway}}`        | `http://localhost:8080`                | (static)                     |
| `{{accessToken}}`    | `eyJhbGciOiJIUzI1...`                  | POST /auth/login             |
| `{{refreshToken}}`   | `eyJhbGciOiJIUzI1...`                  | POST /auth/login             |
| `{{adminId}}`        | `uuid`                                 | POST /users (admin)          |
| `{{userId}}`         | `uuid`                                 | POST /auth/login → sub claim |
| `{{branchHQ}}`       | `uuid`                                 | POST /branches (HQ)          |
| `{{branchQ1}}`       | `uuid`                                 | POST /branches (Q1)          |
| `{{branchQ7}}`       | `uuid`                                 | POST /branches (Q7)          |
| `{{med1}}`-`{{med5}}`| `uuid`                                 | POST /medicines              |
| `{{catPR}}`          | `uuid` (Pain Relief)                   | POST /categories             |
| `{{sup1}}`           | `uuid` (DHG)                           | POST /suppliers              |
| `{{cust1}}`          | `uuid` (Nguyen Van A)                  | POST /customers              |
| `{{orderId}}`        | `uuid`                                 | POST /orders                 |
| `{{paymentId}}`      | `uuid`                                 | POST /payments               |
| `{{prescId}}`        | `uuid`                                 | POST /prescriptions          |

---

## 9. Document Index

| File                                  | Endpoints | Test Cases | Est. Time |
|---------------------------------------|-----------|------------|-----------|
| `00-MASTER-PLAN.md` (this file)       | -         | 1          | 30 min    |
| `01-AUTH-USER.md`                     | 18        | ~50        | 90 min    |
| `02-BRANCH.md`                        | 9         | ~25        | 45 min    |
| `03-CATALOG.md`                       | 28        | ~75        | 120 min   |
| `04-INVENTORY.md`                     | 21        | ~60        | 120 min   |
| `05-ORDER.md`                         | 14        | ~50        | 90 min    |
| `06-PAYMENT.md`                       | 9         | ~35        | 60 min    |
| `07-CUSTOMER.md`                      | 13        | ~40        | 75 min    |
| `08-REPORT.md`                        | 11        | ~30        | 60 min    |
| `09-PRESCRIPTION.md`                  | 9         | ~30        | 60 min    |
| `10-NOTIFICATION.md`                  | 14        | ~40        | 75 min    |
| `11-E2E-FLOWS.md`                     | 5 flows   | ~20        | 90 min    |
| `TOTAL`                               | 146       | ~456       | ~16 hours |

---

## 10. Sign-off

| Role          | Name | Date | Signature |
|---------------|------|------|-----------|
| Test Lead     |      |      |           |
| Dev Lead      |      |      |           |
| Product Owner |      |      |           |
| QA Manager    |      |      |           |

**End of Master Plan** → Next: [`01-AUTH-USER.md`](./01-AUTH-USER.md)
