# UAT MASTER PLAN - PCMS (Pharmacy Chain Management System)

**Version:** 2.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10 (commit `63b9397`)
**Test type:** User Acceptance Testing - REST API (Backend Only)
**Total coverage:** 262 endpoints across **18 services** (12 B2B + 5 B2C + 1 AI)

---

## 1. Project Information

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  PCMS - Pharmacy Chain Management System                                      ║
║  Stack    : Spring Boot 4.0.7 + Spring Cloud 2025.1.2 + MySQL 8 + Eureka    ║
║             + Python 3.11 FastAPI (ai-engine-service)                        ║
║  Services : 15 Java microservices + 1 Python AI service behind Gateway       ║
║  Auth     : JWT (HS256) - currently permitAll in dev profile                 ║
║  Frontend : (out of scope for this UAT - API only)                           ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

### 1.1 Service Map (B2B + Infrastructure - via API Gateway port 8080)

| #  | Service               | Port | Tier | Gateway Path Prefix                                       |
|----|-----------------------|------|------|-----------------------------------------------------------|
| 1  | api-gateway           | 8080 | INFRA| (root)                                                    |
| 2  | discovery-server      | 8761 | INFRA| (Eureka)                                                  |
| 3  | config-server         | 8888 | INFRA| (Config)                                                  |
| 4  | user-service          | 8081 | B2B  | `/api/v1/auth/**` `/api/v1/users/**` `/api/v1/dashboard/**` `/api/v1/audit-logs/**` |
| 5  | branch-service        | 8082 | B2B  | `/api/v1/branches/**`                                     |
| 6  | catalog-service       | 8083 | B2B  | `/api/v1/medicines/**` `/api/v1/search/**`               |
| 7  | category-service      | 8084 | B2B  | `/api/v1/categories/**`                                   |
| 8  | supplier-service      | 8085 | B2B  | `/api/v1/suppliers/**`                                    |
| 9  | inventory-service     | 8086 | B2B  | `/api/v1/inventory/**`                                    |
| 10 | customer-service      | 8087 | B2B  | `/api/v1/customers/**`                                    |
| 11 | order-service         | 8088 | B2B  | `/api/v1/orders/**` `/api/v1/coupons/**` `/api/v1/admin/outbox/**` |
| 12 | payment-service       | 8089 | B2B  | `/api/v1/payments/**` `/api/v1/webhooks/**`               |
| 13 | prescription-service  | 8090 | B2B  | `/api/v1/prescriptions/**`                                |
| 14 | notification-service  | 8091 | B2B  | `/api/v1/notifications/**`                                |
| 15 | report-service        | 8092 | B2B  | `/api/v1/reports/**`                                      |

### 1.2 Service Map (B2C + AI - via API Gateway port 8080)

| #  | Service                      | Port | Tier | Gateway Path Prefix                                  |
|----|------------------------------|------|------|------------------------------------------------------|
| 16 | customer-portal-service      | 8093 | B2C  | `/api/v1/addresses/**` `/api/v1/cart/**` `/api/v1/family/**` `/api/v1/favorites/**` `/api/v1/installment/**` `/api/v1/notif-settings/**` `/api/v1/orders/**` `/api/v1/prescriptions/**` `/api/v1/shop/**` `/api/v1/store/**` `/api/v1/vouchers/**` `/api/v1/wallet/**` `/api/v1/health-articles/**` `/api/v1/diseases/**` `/api/v1/verify-origin/**` `/api/v1/vaccines/**` `/api/v1/vaccine-bookings/**` `/api/v1/vaccination-ledger/**` `/api/v1/admin/videos/**` |
| 17 | pharmacist-workbench-service | 8094 | B2C  | `/api/v1/consultations/**` `/api/v1/rx/**` `/api/v1/follow-ups/**` `/api/v1/vip-marks/**` |
| 18 | mobile-bff                   | 8096 | B2C  | `/api/v1/mobile/**` (incl. reminders)                |
| 19 | health-tools-service         | 8097 | B2C  | `/api/v1/health/**`                                   |
| 20 | ecom-ops-service             | 8098 | B2C  | `/api/v1/ecom-ops/flash-sales/**` `/api/v1/admin/flash-sales/**` |
| 21 | ai-engine-service (Python)   | 8095 | AI   | `/api/v1/ai/**` (direct, not via gateway)            |

---

## 2. Test Scope - 262 Endpoints

### 2.1 B2B Services (146 endpoints, 12 services)

| Use Case | Module                  | Endpoints | UAT Doc                |
|----------|-------------------------|----------:|------------------------|
| UC01     | Auth (login, JWT)       |        10 | `01-AUTH-USER.md`      |
| UC02     | User Mgmt + Dashboard   |        18 | `01-AUTH-USER.md`      |
| UC03     | Branch                  |         8 | `02-BRANCH.md`         |
| UC04     | Catalog (medicine+image)|        12 | `03-CATALOG.md`        |
| UC10     | Search                  |         4 | `03-CATALOG.md`        |
| UC04b    | Category                |         5 | `04-CATEGORY.md`       |
| UC11     | Supplier                |         6 | `05-SUPPLIER.md`       |
| UC05     | Inventory + Outbox      |        21 | `06-INVENTORY.md`      |
| UC08     | Customer + Portal       |        14 | `07-CUSTOMER.md`       |
| UC06     | Order + Coupon + Outbox |        14 | `08-ORDER.md`          |
| UC07     | Payment + Webhook       |         9 | `09-PAYMENT.md`        |
| UC12     | Prescription            |         9 | `10-PRESCRIPTION.md`   |
| UC13     | Notification + Template |        15 | `11-NOTIFICATION.md`   |
| UC09     | Report                  |        11 | `12-REPORT.md`         |
|          | **B2B SUBTOTAL**        |   **146** |                        |

### 2.2 B2C Services (98 endpoints, 5 services)

| Use Case | Module                   | Endpoints | UAT Doc                   |
|----------|--------------------------|----------:|---------------------------|
| UC15     | Customer Portal (C-end)  |        60 | `13-CUSTOMER-PORTAL.md`   |
| UC16     | Pharmacist Workbench     |        18 | `14-PHARMACIST-WORKBENCH.md` |
| UC17     | Mobile BFF               |         9 | `15-MOBILE-BFF.md`        |
| UC18     | Health Tools (quizzes)   |         4 | `16-HEALTH-TOOLS.md`      |
| UC19     | E-com Ops (flash sales)  |         7 | `17-ECOM-OPS.md`          |
|          | **B2C SUBTOTAL**         |    **98** |                           |

### 2.3 AI Engine (18 endpoints, 1 service - Python)

| Use Case | Module            | Endpoints | UAT Doc                |
|----------|-------------------|----------:|------------------------|
| AI-01    | Chat (RAG)        |         1 | `18-AI-ENGINE.md`      |
| AI-02    | OCR Prescription  |         2 | `18-AI-ENGINE.md`      |
| AI-03    | Drug Check        |         1 | `18-AI-ENGINE.md`      |
| AI-04    | Semantic Search   |         1 | `18-AI-ENGINE.md`      |
| AI-05    | Demand Forecast   |         1 | `18-AI-ENGINE.md`      |
| AI-06    | Anomaly Detection |         1 | `18-AI-ENGINE.md`      |
| AI-07    | Medical Summary   |         1 | `18-AI-ENGINE.md`      |
| AI-08    | Content Moderation|         1 | `18-AI-ENGINE.md`      |
| AI-09    | Chat Sessions     |         3 | `18-AI-ENGINE.md`      |
| AI-10    | Dosage Check      |         1 | `18-AI-ENGINE.md`      |
| ⚠️       | Cross-sell (unmounted) | 2   | `18-AI-ENGINE.md` (note) |
|          | **AI SUBTOTAL**   |    **18** |                        |

### 2.4 E2E Cross-Service Flows (5+ flows)

| Flow | Name                               | Touched Services                          | UAT Doc          |
|------|------------------------------------|-------------------------------------------|------------------|
| E2E-01 | Order → Payment → Inventory → Notif | order, payment, inventory, notification   | `19-E2E-FLOWS.md` |
| E2E-02 | Prescription → Sign → Link Order    | prescription, order, ai-engine (OCR)      | `19-E2E-FLOWS.md` |
| E2E-03 | B2C Cart → Checkout → Order        | customer-portal, order, payment           | `19-E2E-FLOWS.md` |
| E2E-04 | Outbox: low-stock alert           | inventory, notification                   | `19-E2E-FLOWS.md` |
| E2E-05 | B2C Consult + AI drug-check       | pharmacist-workbench, ai-engine           | `19-E2E-FLOWS.md` |
| E2E-06 | Vaccine booking flow              | customer-portal, notification             | `19-E2E-FLOWS.md` |

### 2.5 Grand Total

```
╔════════════════════════════════════════════════════════════╗
║  Total API endpoints: 262 (B2B: 146 + B2C: 98 + AI: 18)   ║
║  Total E2E flows: 6                                         ║
║  Total UAT documents: 19 files (00..19)                     ║
║  Estimated execution time: ~30 hours                       ║
╚════════════════════════════════════════════════════════════╝
```

---

## 3. Prerequisites

### 3.1 Environment

```yaml
OS          : Windows 10/11, macOS, or Linux
Java        : JDK 21+
Maven       : 3.9+
Python      : 3.11+ (for ai-engine-service)
Docker      : (optional) for MySQL/Redis containers
MySQL       : 8.x running (default localhost:3306, root/root)
PostgreSQL  : 16+ (for ai-engine pgvector)
Redis       : 7+ (for caching/queues)
Postman     : v10+ (or use curl/httpie)
Gateway URL : http://localhost:8080
AI URL      : http://localhost:8095
Eureka URL  : http://localhost:8761
```

### 3.2 Startup Sequence

```bash
# 1. Start infrastructure
docker compose -f scripts/docker-compose-infra.yml up -d   # MySQL + Redis + PostgreSQL

# 2. Start infra services (config + discovery first)
mvn -pl config-server,discovery-server spring-boot:run

# 3. Start B2B services (wait 30s between batches)
mvn -pl user-service,branch-service,category-service,supplier-service spring-boot:run
mvn -pl catalog-service,inventory-service,customer-service spring-boot:run
mvn -pl order-service,payment-service,prescription-service spring-boot:run
mvn -pl notification-service,report-service spring-boot:run

# 4. Start B2C services
mvn -pl customer-portal-service,pharmacist-workbench-service spring-boot:run
mvn -pl mobile-bff,health-tools-service,ecom-ops-service spring-boot:run

# 5. Start AI service (Python venv)
cd ai-engine-service
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8095

# 6. Start gateway last
mvn -pl api-gateway spring-boot:run

# 7. Verify all up
curl http://localhost:8080/actuator/health
curl http://localhost:8095/healthz
curl http://localhost:8761/eureka/apps
```

### 3.3 Test Data Seeding

A SQL seed file must be loaded: `scripts/seed-test-data.sql` containing:

- 1 admin user (`admin@pcms.vn` / `admin123`)
- 1 branch manager + 2 pharmacists
- 1 customer-portal user (`customer@pcms.vn` / `customer123`)
- 3 branches (HQ, District1, District7)
- 5 categories
- 10 suppliers
- 50 medicines
- 20 customers
- 100 inventory batches
- 8 health quizzes
- 5 flash sales
- 1 OpenAI API key in `OPENAI_API_KEY` env var

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
| 429  | Too Many Requests        | Rate limited                         |
| 500  | Server Error             | Unexpected exception                 |
| 503  | Service Unavailable      | AI engine not ready (pgvector down)  |

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
║    X-Customer-Id : <UUID>           ← B2C scope            ║
║    Idempotency-Key: <uuid>         ← mutating (CR-05)     ║
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
| ← ASYNC | Long-running (poll for completion)           |
| ← ENV | Requires environment variable (e.g. OPENAI_API_KEY) |

### 4.5 Authorization Matrix (target production behavior - dev is permitAll)

| Endpoint Group                  | Admin/CEO | Manager | Pharmacist | Customer (B2C) | Customer-Svc |
|---------------------------------|-----------|---------|------------|----------------|--------------|
| /auth/*                         | ✓         | ✓       | ✓          | ✓              | ✓            |
| /users (read)                   | ✓         | ✓(branch) | ✓        | ✗              | ✗            |
| /users (write)                  | ✓         | ✗       | ✗          | ✗              | ✗            |
| /branches (read)                | ✓         | ✓       | ✓          | ✓(own area)    | ✗            |
| /branches (write)               | ✓         | ✗       | ✗          | ✗              | ✗            |
| /medicines, /categories, /suppliers | ✓     | ✓       | ✓          | ✗              | ✗            |
| /inventory (write)              | ✓         | ✓       | ✓          | ✗              | ✗            |
| /orders (create)                | ✓         | ✓       | ✓          | ✓(B2C)         | ✗            |
| /orders (approve)               | ✓         | ✓       | ✗          | ✗              | ✗            |
| /payments                       | ✓         | ✓       | ✓          | ✓(own)         | ✗            |
| /customers (B2B)                | ✓         | ✓       | ✓          | ✗              | ✗            |
| /customers/me (B2C)             | ✗         | ✗       | ✗          | ✓(own)         | ✗            |
| /prescriptions (B2B)            | ✓         | ✓       | ✓          | ✗              | ✗            |
| /prescriptions/me (B2C)         | ✗         | ✗       | ✗          | ✓(own)         | ✗            |
| /reports (revenue, staff)       | ✓         | ✓(branch)| ✗         | ✗              | ✗            |
| /notifications                  | ✓         | ✓       | ✓          | ✓(own)         | ✗            |
| /admin/* (outbox, flashsales)   | ✓         | ✗       | ✗          | ✗              | ✗            |
| /webhooks/*                     | (Gateway) | (Gateway)| (Gateway) | (Gateway)      | (Gateway)    |
| /ai/** (ai-engine)              | ✓         | ✓       | ✓          | ✓              | ✓            |
| /consultations, /rx, /follow-ups| ✓         | ✓       | ✓          | ✗              | ✗            |
| /vip-marks                      | ✓         | ✓       | ✓          | ✗              | ✗            |
| /mobile/** (BFF)                | -         | -       | -          | ✓              | -            |
| /health/quizzes                 | -         | -       | -          | ✓              | -            |
| /ecom-ops/flash-sales           | -         | -       | -          | ✓(read)        | -            |

---

## 5. Test Strategy

### 5.1 Test Levels

```
┌─────────────────────────────────────────────────────────────────┐
│  Level 1 - SMOKE  (1 hour)                                      │
│    Goal: All 18 services respond 200 on /actuator/health        │
│    Pass: All services up, Eureka shows 18+ instances           │
├─────────────────────────────────────────────────────────────────┤
│  Level 2 - HAPPY PATH  (8-10 hours)                             │
│    Goal: Each of 262 endpoints returns expected success         │
│    Method: Follow each UAT-XX doc step-by-step with sample data │
├─────────────────────────────────────────────────────────────────┤
│  Level 3 - NEGATIVE PATH  (6-8 hours)                           │
│    Goal: Validation, business rules, error messages             │
│    Method: Send invalid payloads, expect 4xx with proper errors │
├─────────────────────────────────────────────────────────────────┤
│  Level 4 - INTEGRATION  (3-4 hours)                             │
│    Goal: 6 cross-service E2E flows complete                    │
│    Method: Run `19-E2E-FLOWS.md` scripts                       │
├─────────────────────────────────────────────────────────────────┤
│  Level 5 - NON-FUNCTIONAL  (2 hours)                            │
│    Goal: Performance baselines, error rate < 1%, latency p95   │
│    Method: 100 concurrent users, measure /actuator/metrics     │
├─────────────────────────────────────────────────────────────────┤
│  Level 6 - AI-SPECIFIC  (1 hour)                                │
│    Goal: ai-engine gracefully handles OpenAI failures           │
│    Method: Test with invalid key, network timeout, mock         │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Pass / Fail Criteria

**PASS** when:

- All 262 endpoints return correct response for at least 1 happy + 1 negative case
- All 6 E2E flows complete without manual intervention
- No 5xx in any test step (except where explicitly expected)
- Response time p95 < 500ms for reads, < 1000ms for writes (AI: < 5000ms)

**FAIL** when:

- Any endpoint returns 500 for valid input
- Any business rule documented in code is not enforced
- E2E flow breaks (e.g., order not consuming stock)
- Duplicate / data integrity violation not caught
- AI service returns 500 when OpenAI is down (should return 503 or fallback)

### 5.3 Bug Severity Matrix

| Severity | Definition                              | SLA    |
|----------|-----------------------------------------|--------|
| Critical | Data loss, security breach, 5xx on main | 4h     |
| Major    | Core feature broken, no workaround     | 1 day  |
| Minor    | Cosmetic, workaround exists             | 1 week |
| Trivial  | Doc typo, log noise                     | next sprint |

---

## 6. Test Execution Tracker

| #  | Service / Use Case            | Tester | Date       | Pass | Fail | Bugs | Status |
|----|-------------------------------|--------|------------|------|------|------|--------|
| 0  | Environment Bootstrap         |        |            |      |      |      | ☐      |
| 1  | Auth & User (UC01-02)         |        |            |      |      |      | ☐      |
| 2  | Branch (UC03)                 |        |            |      |      |      | ☐      |
| 3  | Catalog/Search (UC04/10)      |        |            |      |      |      | ☐      |
| 4  | Category (UC04b)              |        |            |      |      |      | ☐      |
| 5  | Supplier (UC11)               |        |            |      |      |      | ☐      |
| 6  | Inventory (UC05)              |        |            |      |      |      | ☐      |
| 7  | Customer (UC08)               |        |            |      |      |      | ☐      |
| 8  | Order & Coupon (UC06)         |        |            |      |      |      | ☐      |
| 9  | Payment (UC07)                |        |            |      |      |      | ☐      |
| 10 | Prescription (UC12)           |        |            |      |      |      | ☐      |
| 11 | Notification (UC13)           |        |            |      |      |      | ☐      |
| 12 | Report (UC09)                 |        |            |      |      |      | ☐      |
| 13 | Customer Portal (UC15, B2C)   |        |            |      |      |      | ☐      |
| 14 | Pharmacist Workbench (UC16)   |        |            |      |      |      | ☐      |
| 15 | Mobile BFF (UC17)             |        |            |      |      |      | ☐      |
| 16 | Health Tools (UC18)           |        |            |      |      |      | ☐      |
| 17 | E-com Ops (UC19)              |        |            |      |      |      | ☐      |
| 18 | AI Engine (AI-01..10)         |        |            |      |      |      | ☐      |
| 19 | E2E flows (6 flows)           |        |            |      |      |      | ☐      |

---

## 7. Scenario 0.1 - Bootstrap Test Data

If `scripts/seed-test-data.sql` is not available, run this sequence to bootstrap minimum data.

### Step 1: Create Admin User (B2B)

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

### Step 2: Create B2C Customer

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/customers/register             ║
║  REQUEST:                                                   ║
║  {                                                          ║
║    "email": "customer@pcms.vn",                             ║
║    "password": "customer123",                               ║
║    "fullName": "Nguyen Van Customer",                       ║
║    "phone": "0901234567"                                    ║
║  }                                                          ║
║  EXPECTED: 201, returns customer object                     ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 3: Create Branches

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

### Step 4: Create Categories (x5)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/categories                     ║
║  { "name":"Pain Relief", "code":"PR" }                      ║
║  { "name":"Antibiotics",  "code":"AB" }                     ║
║  { "name":"Vitamins",     "code":"VT" }                     ║
║  { "name":"Cold & Flu",   "code":"CF" }                     ║
║  { "name":"Digestive",    "code":"DG" }                     ║
║  ← CAPTURE: 5 category UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 5: Create Suppliers (x3)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/suppliers                      ║
║  { "name":"DHG Pharma", "taxCode":"0301234567",             ║
║    "phone":"0283333333", "email":"contact@dhg.vn" }        ║
║  ← REPEAT for Traphaco, Imexpharm                           ║
║  ← CAPTURE: 3 supplier UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 6: Create Medicines (x5)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/medicines                      ║
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

### Step 7: Import Inventory

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

### Step 8: Create Customers

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/customers  (x3)                ║
║  { "name":"Nguyen Van A", "phone":"0901234567",             ║
║    "email":"a@example.com" }                                ║
║  ← CAPTURE: 3 customer UUIDs                                ║
╚══════════════════════════════════════════════════════════════╝
```

### Step 9: Verify Bootstrap

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
║    GET /api/v1/health/quizzes  → 8 quizzes                   ║
║    GET /api/v1/ecom-ops/flash-sales/active → 1+ sales        ║
║    GET http://localhost:8095/healthz → {"status":"UP"}       ║
║                                                              ║
║  If any returns 0, repeat the corresponding step above.      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 8. Reference: Common Variables (capture in Postman / env file)

| Variable             | Example                                | Captured from                |
|----------------------|----------------------------------------|------------------------------|
| `{{gateway}}`        | `http://localhost:8080`                | (static)                     |
| `{{aiEngine}}`       | `http://localhost:8095`                | (static, Python service)     |
| `{{accessToken}}`    | `eyJhbGciOiJIUzI1...`                  | POST /auth/login             |
| `{{refreshToken}}`   | `eyJhbGciOiJIUzI1...`                  | POST /auth/login             |
| `{{customerToken}}`  | `eyJhbGciOiJIUzI1...`                  | POST /customers/register     |
| `{{adminId}}`        | `uuid`                                 | POST /users (admin)          |
| `{{userId}}`         | `uuid`                                 | POST /auth/login → sub claim |
| `{{customerId}}`     | `uuid`                                 | POST /customers/register     |
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
| `{{cartId}}`         | `uuid`                                 | POST /cart/items             |
| `{{addressId}}`      | `uuid`                                 | POST /addresses              |
| `{{voucherId}}`      | `uuid`                                 | POST /vouchers (admin)       |
| `{{consultId}}`      | `uuid`                                 | POST /consultations          |
| `{{vipMarkId}}`      | `uuid`                                 | POST /vip-marks              |
| `{{sessionId}}`      | `uuid`                                 | POST /ai/chat (AI)           |
| `{{OPENAI_API_KEY}}` | `sk-...`                               | env var                      |

---

## 9. Document Index (19 files)

| File                                  | Endpoints | Test Cases | Est. Time |
|---------------------------------------|----------:|-----------:|----------:|
| `00-MASTER-PLAN.md` (this file)       |         - |          1 |  30 min   |
| `01-AUTH-USER.md`                     |        18 |        ~50 |  90 min   |
| `02-BRANCH.md`                        |         8 |        ~25 |  45 min   |
| `03-CATALOG.md`                       |        16 |        ~45 |  90 min   |
| `04-CATEGORY.md`                      |         5 |        ~15 |  30 min   |
| `05-SUPPLIER.md`                      |         6 |        ~20 |  45 min   |
| `06-INVENTORY.md`                     |        21 |        ~60 | 120 min   |
| `07-CUSTOMER.md`                      |        14 |        ~40 |  75 min   |
| `08-ORDER.md`                         |        14 |        ~50 |  90 min   |
| `09-PAYMENT.md`                       |         9 |        ~35 |  60 min   |
| `10-PRESCRIPTION.md`                  |         9 |        ~30 |  60 min   |
| `11-NOTIFICATION.md`                  |        15 |        ~45 |  75 min   |
| `12-REPORT.md`                        |        11 |        ~30 |  60 min   |
| `13-CUSTOMER-PORTAL.md`               |        60 |       ~150 | 240 min   |
| `14-PHARMACIST-WORKBENCH.md`          |        18 |        ~50 |  90 min   |
| `15-MOBILE-BFF.md`                    |         9 |        ~25 |  45 min   |
| `16-HEALTH-TOOLS.md`                  |         4 |        ~15 |  30 min   |
| `17-ECOM-OPS.md`                      |         7 |        ~25 |  45 min   |
| `18-AI-ENGINE.md`                     |        18 |        ~45 |  90 min   |
| `19-E2E-FLOWS.md`                     |  6 flows  |        ~30 | 180 min   |
| **TOTAL**                             |   **262** |     **~785** | **~30 h** |

---

## 10. Sign-off

| Role          | Name | Date | Signature |
|---------------|------|------|-----------|
| Test Lead     |      |      |           |
| Dev Lead      |      |      |           |
| Product Owner |      |      |           |
| QA Manager    |      |      |           |

**End of Master Plan v2.0** → Next: [`01-AUTH-USER.md`](./01-AUTH-USER.md)
