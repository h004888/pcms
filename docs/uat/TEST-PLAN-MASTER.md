# PCMS - Master Test Plan (Aggregated)

**Version:** 1.0
**Date:** 2026-06-19
**Source:** Aggregated from `docs/uat/01-..19-*.md`
**Execution Rule:** Mỗi Test Plan batch ≤ 20 cases (theo api-testing-agent skill)
**Total:** 262 endpoints, ~785 test cases → **chia thành ~40 Test Plan batches**

---

## 1. Test Plan Index

| TP ID   | UAT File                    | Service                  | Endpoints | Cases | Batches | Status |
|---------|-----------------------------|--------------------------|----------:|------:|--------:|:------:|
| TP-01   | `01-AUTH-USER.md`           | user-service             |        23 |    61 |    4    |   ☐    |
| TP-02   | `02-BRANCH.md`              | branch-service           |         8 |    23 |    2    |   ☐    |
| TP-03   | `03-CATALOG.md`             | catalog-service          |        16 |    45 |    3    |   ☐    |
| TP-04   | `04-CATEGORY.md`            | category-service         |         5 |    15 |    1    |   ☐    |
| TP-05   | `05-SUPPLIER.md`            | supplier-service         |         6 |    20 |    1    |   ☐    |
| TP-06   | `06-INVENTORY.md`           | inventory-service        |        21 |    60 |    3    |   ☐    |
| TP-07   | `07-CUSTOMER.md`            | customer-service         |        14 |    40 |    2    |   ☐    |
| TP-08   | `08-ORDER.md`               | order-service            |        14 |    50 |    3    |   ☐    |
| TP-09   | `09-PAYMENT.md`             | payment-service          |         9 |    35 |    2    |   ☐    |
| TP-10   | `10-PRESCRIPTION.md`        | prescription-service     |         9 |    30 |    2    |   ☐    |
| TP-11   | `11-NOTIFICATION.md`        | notification-service     |        15 |    45 |    3    |   ☐    |
| TP-12   | `12-REPORT.md`              | report-service           |        14 |    30 |    2    |   ☐    |
| TP-13   | `13-CUSTOMER-PORTAL.md`*    | customer-portal-service  |        60 |   150 |    8    |   ☐    |
| TP-14   | `14-PHARMACIST-WORKBENCH.md`| pharmacist-workbench     |        18 |    45 |    3    |   ☐    |
| TP-15   | `15-MOBILE-BFF.md`          | mobile-bff               |         9 |    21 |    2    |   ☐    |
| TP-16   | `16-HEALTH-TOOLS.md`        | health-tools-service     |         4 |    13 |    1    |   ☐    |
| TP-17   | `17-ECOM-OPS.md`            | ecom-ops-service         |         7 |    20 |    1    |   ☐    |
| TP-18   | `18-AI-ENGINE.md`           | ai-engine-service (Py)   |        13 |    34 |    2    |   ☐    |
| TP-19   | `19-E2E-FLOWS.md`           | Cross-service            |    6 flows|    30 |    2    |   ☐    |
| **TOTAL** |                           |                          |   **263** |**~789**|  **47** |       |

\* TP-13 (Customer Portal) chưa có file `13-CUSTOMER-PORTAL.md` trong repo - cần tạo nếu cần test 60 endpoints này.

---

## 2. Test Plan Batches (max 20 cases/batch)

### TP-01: user-service (Auth + User Mgmt) - 4 batches

**TP-01A: Auth Flow (login/register/refresh)** — 18 cases
| # | Method | Endpoint | Scenario | Expected |
|---|--------|----------|----------|----------|
| 1 | POST   | `/api/v1/auth/login` | Valid admin login | 200 |
| 2 | POST   | `/api/v1/auth/login` | Valid customer login | 200 |
| 3 | POST   | `/api/v1/auth/login` | Wrong password | 401 |
| 4 | POST   | `/api/v1/auth/login` | Non-existent user | 401 |
| 5 | POST   | `/api/v1/auth/register` | New customer registration | 201 |
| 6 | POST   | `/api/v1/auth/register` | Duplicate email | 409 |
| 7 | POST   | `/api/v1/auth/register` | Missing required field | 400 |
| 8 | POST   | `/api/v1/auth/refresh` | Valid refresh token | 200 |
| 9 | POST   | `/api/v1/auth/refresh` | Invalid/expired token | 401 |
| 10 | POST  | `/api/v1/auth/logout` | Valid logout | 204 |
| 11 | POST  | `/api/v1/auth/forgot-password` | Existing email | 200 |
| 12 | POST  | `/api/v1/auth/forgot-password` | Non-existent email | 200 (security) |
| 13 | POST  | `/api/v1/auth/reset-password` | Valid token + new password | 200 |
| 14 | POST  | `/api/v1/auth/reset-password` | Invalid token | 400 |
| 15 | GET   | `/api/v1/auth/me` | With valid JWT | 200 |
| 16 | GET   | `/api/v1/auth/me` | Without JWT | 401 |
| 17 | POST  | `/api/v1/auth/change-password` | Old + new password | 200 |
| 18 | POST  | `/api/v1/auth/change-password` | Wrong old password | 401 |

**TP-01B: User CRUD** — 18 cases
**TP-01C: Dashboard** — 15 cases
**TP-01D: Audit Logs** — 10 cases

### TP-02: branch-service - 2 batches

**TP-02A: Branch CRUD (8 cases)**
| # | Method | Endpoint | Scenario | Expected |
|---|--------|----------|----------|----------|
| 1 | GET    | `/api/v1/branches` | List all branches | 200 |
| 2 | GET    | `/api/v1/branches/{id}` | Get by UUID | 200 |
| 3 | POST   | `/api/v1/branches` | Create new branch | 201 |
| 4 | PUT    | `/api/v1/branches/{id}` | Update branch | 200 |
| 5 | DELETE | `/api/v1/branches/{id}` | Soft delete | 204 |
| 6 | GET    | `/api/v1/branches?code=HQ` | Filter by code | 200 |
| 7 | POST   | `/api/v1/branches` | Missing required field | 400 |
| 8 | GET    | `/api/v1/branches/00000000-...` | Non-existent UUID | 404 |

**TP-02B: Branch Stats + Negative** — 15 cases

### TP-03: catalog-service (Medicine + Search) - 3 batches

**TP-03A: Medicine CRUD** — 18 cases
**TP-03B: Medicine Search/Filter** — 15 cases
**TP-03C: Medicine Image + Barcode** — 12 cases

### TP-04: category-service - 1 batch (15 cases)

**TP-04A: All Category endpoints** — 15 cases

### TP-05: supplier-service - 1 batch (20 cases)

### TP-06: inventory-service - 3 batches

**TP-06A: Inventory CRUD** — 18 cases
**TP-06B: Batch + Stock Operations** — 20 cases (consume/import/adjust)
**TP-06C: Outbox + Low-stock Alerts** — 22 cases

### TP-07: customer-service - 2 batches

**TP-07A: Customer CRUD** — 18 cases
**TP-07B: Customer Search/Stats** — 22 cases

### TP-08: order-service - 3 batches

**TP-08A: Order CRUD** — 20 cases
**TP-08B: Coupon Operations** — 15 cases
**TP-08C: Outbox Admin** — 15 cases

### TP-09: payment-service - 2 batches

**TP-09A: Payment Operations** — 20 cases
**TP-09B: Webhooks + Refund** — 15 cases

### TP-10: prescription-service - 2 batches

**TP-10A: Prescription CRUD** — 15 cases
**TP-10B: Sign + Link** — 15 cases

### TP-11: notification-service - 3 batches

**TP-11A: Notification CRUD** — 18 cases
**TP-11B: Templates** — 15 cases
**TP-11C: Delivery Status** — 12 cases

### TP-12: report-service - 2 batches

**TP-12A: Revenue/Sales Reports** — 16 cases
**TP-12B: Staff/Inventory Reports** — 14 cases

### TP-13: customer-portal-service - 8 batches (60 endpoints)

**TP-13A: Address + Cart** — 20 cases
**TP-13B: Orders + Vouchers + Wallet** — 18 cases
**TP-13C: Prescriptions + Family + Favorites** — 18 cases
**TP-13D: Installment + Notif-Settings + Store** — 18 cases
**TP-13E: Shop + Vaccines + Vaccine-Bookings** — 20 cases
**TP-13F: Vaccination-Ledger + Verify-Origin** — 15 cases
**TP-13G: Health-Articles + Diseases** — 15 cases
**TP-13H: Admin Videos** — 10 cases

### TP-14: pharmacist-workbench - 3 batches

**TP-14A: Consultations** — 18 cases
**TP-14B: Rx + Follow-ups** — 15 cases
**TP-14C: VIP Marks** — 12 cases

### TP-15: mobile-bff - 2 batches

**TP-15A: Mobile Read APIs** — 12 cases
**TP-15B: Mobile Mutations + Reminders** — 9 cases

### TP-16: health-tools-service - 1 batch (13 cases)

### TP-17: ecom-ops-service - 1 batch (20 cases)

### TP-18: ai-engine-service - 2 batches

**TP-18A: Health + Chat + OCR** — 18 cases
**TP-18B: Drug-check + Forecast + Sessions** — 16 cases

### TP-19: E2E Cross-Service Flows - 2 batches

**TP-19A: E2E-01 + E2E-02 + E2E-04** — 15 cases (order+payment+inventory+prescription flows)
**TP-19B: E2E-03 + E2E-05 + E2E-06** — 15 cases (B2C + AI + vaccine flows)

---

## 3. Quick Smoke Test (TP-SMOKE)

**Mục đích:** Verify tất cả 21 services (incl. AI) đều online trước khi chạy test chi tiết.

| # | Service                    | Endpoint                                 | Expected |
|---|----------------------------|------------------------------------------|----------|
| 1 | api-gateway                | `GET /actuator/health`                   | 200 UP   |
| 2 | discovery-server           | `GET /eureka/apps`                       | 200 XML  |
| 3 | user-service               | `GET /api/v1/users`                      | 200 []   |
| 4 | branch-service             | `GET /api/v1/branches`                   | 200 []   |
| 5 | catalog-service            | `GET /api/v1/medicines`                  | 200 []   |
| 6 | category-service           | `GET /api/v1/categories`                 | 200 []   |
| 7 | supplier-service           | `GET /api/v1/suppliers`                  | 200 []   |
| 8 | inventory-service          | `GET /api/v1/inventory`                  | 200 []   |
| 9 | customer-service           | `GET /api/v1/customers`                  | 200 []   |
| 10 | order-service             | `GET /api/v1/orders`                     | 200 []   |
| 11 | payment-service           | `GET /api/v1/payments`                   | 200 []   |
| 12 | prescription-service      | `GET /api/v1/prescriptions`              | 200 []   |
| 13 | notification-service      | `GET /api/v1/notifications`              | 200 []   |
| 14 | report-service            | `GET /api/v1/reports`                    | 200 []   |
| 15 | customer-portal-service   | `GET /api/v1/store`                      | 200 []   |
| 16 | pharmacist-workbench      | `GET /api/v1/consultations`              | 200 []   |
| 17 | mobile-bff                | `GET /api/v1/mobile/profile`             | 200      |
| 18 | health-tools-service      | `GET /api/v1/health/quizzes`             | 200 8    |
| 19 | ecom-ops-service          | `GET /api/v1/ecom-ops/flash-sales/active`| 200 []   |
| 20 | ai-engine-service (direct)| `GET /healthz`                           | 200 UP   |
| 21 | ai-engine-service (direct)| `GET /readyz`                            | 200 READY|

**Pass criteria:** 21/21 services UP. Nếu bất kỳ service nào DOWN, khởi động lại theo `scripts/run-local.bat`.

---

## 4. Authenticated Test Plan (TP-AUTH)

Sau smoke test, cần authenticate để lấy JWT cho các test cases phía sau:

| # | Step | Endpoint | Capture |
|---|------|----------|---------|
| 1 | Admin login | `POST /api/v1/auth/login` | `{{accessToken}}` |
| 2 | Manager login | `POST /api/v1/auth/login` | `{{managerToken}}` |
| 3 | Pharmacist login | `POST /api/v1/auth/login` | `{{pharmacistToken}}` |
| 4 | Customer register | `POST /api/v1/customers/register` | `{{customerToken}}`, `{{customerId}}` |

---

## 5. Test Execution Order (Dependencies)

```
TP-SMOKE  (verify all 21 services UP)
   ↓
TP-AUTH   (capture JWTs)
   ↓
TP-04     (categories) ← needed by TP-03 medicines
TP-05     (suppliers)  ← needed by TP-03 medicines
   ↓
TP-03     (medicines)   ← needed by all below
   ↓
TP-02     (branches)    ← needed by TP-06 inventory, TP-08 orders
   ↓
TP-06     (inventory)
TP-07     (customers)
   ↓
TP-08     (orders)
TP-09     (payments)
TP-10     (prescriptions)
TP-11     (notifications)
TP-12     (reports)
TP-13     (customer-portal)
TP-14     (pharmacist-workbench)
TP-15     (mobile-bff)
TP-16     (health-tools)
TP-17     (ecom-ops)
TP-18     (ai-engine)
   ↓
TP-19     (E2E cross-service flows)
```

---

## 6. Pass / Fail Criteria

**PASS** khi:
- 21/21 services UP trong smoke test
- ≥ 90% test cases pass cho mỗi TP
- Không có 5xx không mong đợi
- Auth flow đầy đủ (login, register, refresh, logout)
- E2E flows (TP-19) đều pass happy + rollback paths

**FAIL** khi:
- Bất kỳ service nào DOWN trong smoke test
- Critical endpoint (auth, payment, order create) trả 5xx
- Data integrity violated (e.g., stock âm, duplicate order)
- Outbox event stuck PENDING > 30s

---

## 7. Execution Scripts

| Script | Purpose |
|--------|---------|
| `scripts/uat/lib.sh` | Shared functions (curl wrapper, assert, variable capture) |
| `scripts/uat/run-all.sh` | Master runner: run all 19 service scripts sequentially |
| `scripts/uat/run-smoke.sh` | TP-SMOKE only (verify 21 services) |
| `scripts/uat/test-NN-*.sh` | Per-service test script (one per UAT file) |
| `scripts/uat/run-batch.sh` | Run specific TP batch (e.g., TP-01A) |

**Cách dùng:**
```bash
# 1. Smoke test trước
bash scripts/uat/run-smoke.sh

# 2. Nếu smoke pass, chạy all
bash scripts/uat/run-all.sh

# 3. Hoặc chạy riêng từng service
bash scripts/uat/test-01-auth-user.sh

# 4. Hoặc chạy 1 batch cụ thể
bash scripts/uat/run-batch.sh TP-01A
```

---

**End of TEST-PLAN-MASTER.md**