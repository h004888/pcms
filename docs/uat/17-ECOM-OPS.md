# UAT Test Scenario: E-commerce Ops Service (Flash Sales)

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `ecom-ops-service` (port 8098)
**UAT Doc Reference:** `17-ECOM-OPS.md`
**Coverage:** UC19 (E-commerce Ops - SHOP-FLASH-SALE, Flash Sale Admin CRUD)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  ECOM-OPS-SERVICE                                                            ║
║  Tier    : B2C (Public + Admin namespaces)                                    ║
║  Port    : 8098 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/ecom-ops/flash-sales/**              ║
║            http://localhost:8080/api/v1/admin/flash-sales/**                  ║
║  Auth    : JWT HS256 - permitAll in dev                                       ║
║             • /ecom-ops/flash-sales/** → Customer or guest (public)           ║
║             • /admin/flash-sales/**  → ADMIN/MANAGER only (prod)              ║
║  DB      : MySQL 8 (schema = ecom_ops_db)                                     ║
║  Tests   : 7 endpoints (2 public + 5 admin), ~25 cases, est. 45 min           ║
║                                                                              ║
║  Note   : Two controllers share one FlashSaleService:                         ║
║           - EcomFlashSaleController (/ecom-ops/flash-sales) - public          ║
║           - FlashSaleController (/admin/flash-sales) - admin                  ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller             | Method | Path                                  | Description                       | Auth (prod)     | Test Cases |
|----|------------------------|--------|---------------------------------------|-----------------------------------|-----------------|-----------:|
| 1  | EcomFlashSaleController| GET    | `/ecom-ops/flash-sales/active`        | Public list active sales          | Customer/guest  |          3 |
| 2  | EcomFlashSaleController| GET    | `/ecom-ops/flash-sales/{id}`           | Public sale detail                | Customer/guest  |          3 |
| 3  | FlashSaleController    | POST   | `/admin/flash-sales`                  | Create new sale                   | Admin           |          4 |
| 4  | FlashSaleController    | GET    | `/admin/flash-sales/active`           | Admin list active (alias)         | Admin           |          2 |
| 5  | FlashSaleController    | GET    | `/admin/flash-sales`                  | Admin list ALL (incl cancelled)   | Admin           |          3 |
| 6  | FlashSaleController    | GET    | `/admin/flash-sales/{id}`             | Admin get by id                   | Admin           |          2 |
| 7  | FlashSaleController    | POST   | `/admin/flash-sales/{id}/cancel`      | Cancel a sale                     | Admin           |          3 |
| **TOTAL**                 |        |                                       |                                   |                 |     **~20**|

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `ecom_ops_db` migrated
- [x] Eureka: `ECOM-OPS-SERVICE` registered
- [x] Gateway route `/api/v1/ecom-ops/flash-sales/**` → ecom-ops-service
- [x] Gateway route `/api/v1/admin/flash-sales/**` → ecom-ops-service
- [x] At least 1 admin user exists (`{{accessToken}}` from `01-AUTH-USER.md`)
- [x] At least 3 medicines exist in catalog (`{{med1}}`, `{{med2}}`, `{{med3}}`)
- [x] At least 1 flash sale active (seeded by `seed-test-data.sql`)

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/ECOM-OPS-SERVICE      ║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8080/api/v1/ecom-ops/flash-sales/active║
║       | jq length                                              ║
║  → ≥1 (active sale seeded)                                    ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  Flash sales seeded by SQL (see scripts/seed-flash-sales.sql):║
║                                                              ║
║  1. SUMMER-COOL (active now)                                 ║
║     - 3 medicines, 20% off, max 2 per user                   ║
║                                                              ║
║  2. VITAMIN-WEEK (active)                                    ║
║     - 2 medicines, 15% off                                   ║
║                                                              ║
║  3. EXPIRED-SALE (in the past, status=ENDED)                 ║
║     - 1 medicine, 50% off                                    ║
╚══════════════════════════════════════════════════════════════╝
```

If not seeded, see TC-03 (create admin) and TC-04 (list active) to bootstrap.

---

## 4. Authorization Matrix (B2C split)

| Endpoint                                  | Customer (B2C) | Admin (B2B) | Dev behavior |
|-------------------------------------------|----------------|-------------|--------------|
| GET  /ecom-ops/flash-sales/active         | ✓              | ✓           | permitAll    |
| GET  /ecom-ops/flash-sales/{id}           | ✓              | ✓           | permitAll    |
| POST /admin/flash-sales                   | ✗              | ✓           | permitAll    |
| GET  /admin/flash-sales/active            | ✗              | ✓           | permitAll    |
| GET  /admin/flash-sales                   | ✗              | ✓           | permitAll    |
| GET  /admin/flash-sales/{id}              | ✗              | ✓           | permitAll    |
| POST /admin/flash-sales/{id}/cancel       | ✗              | ✓           | permitAll    |

---

## 5. Variables to Capture

| Variable             | Example                                | Captured from            |
|----------------------|----------------------------------------|--------------------------|
| `{{gateway}}`        | `http://localhost:8080`                | (static)                 |
| `{{accessToken}}`    | `eyJhbGciOiJIUzI1...`                  | `01-AUTH-USER.md` TC-01  |
| `{{customerToken}}`  | `eyJhbGciOiJIUzI1...`                  | Master Plan §7 Step 2    |
| `{{med1}}`           | `uuid` (Paracetamol 500mg)             | Master Plan §7 Step 6    |
| `{{med2}}`           | `uuid` (Vitamin C 1000mg)              | Master Plan §7 Step 6    |
| `{{med3}}`           | `uuid` (Cough Syrup)                   | Master Plan §7 Step 6    |
| `{{seedSaleId}}`     | `uuid` (SUMMER-COOL)                   | seeded by SQL            |
| `{{newSaleId}}`      | `uuid`                                 | TC-03 below              |

---

## TC-01: GET /ecom-ops/flash-sales/active (Public active list)

### TC-01a: List Active - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all active flash sales for customers            ║
║  METHOD: GET  PATH: /api/v1/ecom-ops/flash-sales/active     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    (none required - public endpoint)                        ║
║    Authorization : Bearer {{customerToken}}  ← optional    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{seedSaleId}}",                                 ║
║      "name":"SUMMER-COOL",                                  ║
║      "description":"Summer cooling products -20%",          ║
║      "startsAt":"2026-06-01T00:00:00",                      ║
║      "endsAt":"2026-06-30T23:59:59",                        ║
║      "discountPct":20.00,                                   ║
║      "maxQtyPerUser":2,                                     ║
║      "status":"ACTIVE",                                     ║
║      "items":[                                              ║
║        { "medicineId":"{{med1}}", "medicineName":"Paracetamol 500mg",║
║          "originalPrice":5000, "salePrice":4000,            ║
║          "remainingQty":150 },                              ║
║        { "medicineId":"{{med2}}", "medicineName":"Vitamin C 1000mg",║
║          "originalPrice":15000, "salePrice":12000,          ║
║          "remainingQty":80 },                               ║
║        { "medicineId":"{{med3}}", "medicineName":"Cough Syrup",║
║          "originalPrice":25000, "salePrice":20000,          ║
║          "remainingQty":40 }                                ║
║      ] },                                                   ║
║    { "id":"...", "name":"VITAMIN-WEEK", ... }               ║
║  ]                                                          ║
║  ← VERIFY: only ACTIVE status sales (no ENDED/CANCELLED)    ║
║  ← VERIFY: endsAt > now (real active sales)                 ║
║  ← VERIFY: each item has originalPrice, salePrice            ║
║  ← VERIFY: remainingQty > 0 for all items                   ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01b: List Active - Empty (all sales ended)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify empty list when no active sales                ║
║  METHOD: GET  PATH: /api/v1/ecom-ops/flash-sales/active     ║
║  ← SEED: cancel or expire all active sales via TC-07 first ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
║  ← VERIFY: empty array (NOT 404)                            ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01c: List Active - Filter by date logic

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify SUMMER-COOL sale shows when current date in range║
║  METHOD: GET  PATH: /api/v1/ecom-ops/flash-sales/active     ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: contains "SUMMER-COOL" if today's date is        ║
║    between 2026-06-01 and 2026-06-30                        ║
║  ← VERIFY: does NOT contain "EXPIRED-SALE" (status=ENDED)    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /ecom-ops/flash-sales/{id} (Public sale detail)

### TC-02a: Get Sale Detail - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get specific flash sale for display                  ║
║  METHOD: GET  PATH: /api/v1/ecom-ops/flash-sales/{{seedSaleId}}║
╠══════════════════════════════════════════════════════════════╣
║  PATH PARAM:                                                ║
║    id = {{seedSaleId}}                                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id":"{{seedSaleId}}",                                   ║
║    "name":"SUMMER-COOL",                                    ║
║    "description":"Summer cooling products -20%",             ║
║    "startsAt":"2026-06-01T00:00:00",                        ║
║    "endsAt":"2026-06-30T23:59:59",                          ║
║    "discountPct":20.00,                                     ║
║    "maxQtyPerUser":2,                                       ║
║    "status":"ACTIVE",                                       ║
║    "items":[                                                ║
║      { "medicineId":"{{med1}}", "medicineName":"Paracetamol 500mg",║
║        "originalPrice":5000, "salePrice":4000,              ║
║        "remainingQty":150, "soldQty":50 },                  ║
║      ...                                                    ║
║    ]                                                        ║
║  }                                                          ║
║  ← VERIFY: items array populated                            ║
║  ← VERIFY: salePrice = originalPrice * (100-discountPct)/100║
║  ← VERIFY: remainingQty + soldQty = initialQty              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02b: Get Sale Detail - Negative Path (non-existent UUID)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get flash sale with unknown UUID                     ║
║  METHOD: GET  PATH: /api/v1/ecom-ops/flash-sales/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Flash sale not found" }      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02c: Get Sale Detail - Invalid UUID format

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pass non-UUID string in path                         ║
║  METHOD: GET  PATH: /api/v1/ecom-ops/flash-sales/not-a-uuid ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  { "code":"VAL_003", "message":"Invalid UUID format" }      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /admin/flash-sales (Admin create sale)

### TC-03a: Create Flash Sale - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create a new flash sale (admin only)                  ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales              ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{accessToken}}   ← admin JWT    ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "name": "BACK-TO-SCHOOL-2026",                           ║
║    "description": "Back to school vitamin promo -25%",      ║
║    "startsAt": "2026-07-01T00:00:00",                       ║
║    "endsAt":   "2026-07-15T23:59:59",                       ║
║    "discountPct": 25.00,                                    ║
║    "maxQtyPerUser": 3,                                      ║
║    "items": [                                               ║
║      {                                                      ║
║        "medicineId": "{{med2}}",                            ║
║        "originalPrice": 15000,                              ║
║        "saleQty": 100                                       ║
║      },                                                     ║
║      {                                                      ║
║        "medicineId": "{{med3}}",                            ║
║        "originalPrice": 25000,                              ║
║        "saleQty": 50                                        ║
║      }                                                     ║
║    ]                                                        ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{newSaleId}}",       ← CAPTURE                  ║
║    "name": "BACK-TO-SCHOOL-2026",                           ║
║    "status": "SCHEDULED",                                   ║
║    "items":[ ... ]                                          ║
║  }                                                          ║
║  ← VERIFY: status === "SCHEDULED" (since startsAt > now)    ║
║  ← CAPTURE: id as {{newSaleId}}                             ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03b: Create Flash Sale - Immediate Active

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create a sale that is ACTIVE immediately             ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "name": "INSTANT-DEAL-2026",                             ║
║    "description": "Instant deal",                           ║
║    "startsAt": "2026-06-19T00:00:00",   ← in the past      ║
║    "endsAt":   "2026-06-25T23:59:59",                       ║
║    "discountPct": 10.00,                                    ║
║    "maxQtyPerUser": 5,                                      ║
║    "items": [                                               ║
║      { "medicineId":"{{med1}}", "originalPrice":5000, "saleQty":50 }║
║    ]                                                        ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id":"...", "status":"ACTIVE",       ← auto-transition   ║
║    ...                                                      ║
║  }                                                          ║
║  ← VERIFY: appears in GET /ecom-ops/flash-sales/active       ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03c: Create Flash Sale - Negative Path (missing required fields)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create sale without name/startsAt/endsAt             ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "description": "incomplete sale"                         ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  {                                                          ║
║    "code":"VAL_001", "message":"Validation failed",         ║
║    "errors":[                                               ║
║      { "field":"name","message":"name is required" },       ║
║      { "field":"startsAt","message":"startsAt is required" },║
║      { "field":"endsAt","message":"endsAt is required" }    ║
║    ]                                                        ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03d: Create Flash Sale - Negative Path (endsAt before startsAt)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create sale with inverted date range                 ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "name":"BAD-DATES",                                      ║
║    "startsAt":"2026-07-15T00:00:00",                        ║
║    "endsAt":"2026-07-01T00:00:00",     ← before startsAt   ║
║    "items":[ {"medicineId":"{{med1}}","originalPrice":5000,║
║               "saleQty":10} ]                               ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    422 Unprocessable Entity                                 ║
║  { "code":"BIZ_001",                                        ║
║    "message":"endsAt must be after startsAt" }              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: GET /admin/flash-sales/active (Admin active list)

### TC-04a: Admin List Active - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Admin gets active sales (alias of /ecom-ops/active)   ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales/active       ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{accessToken}}                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [ ... same shape as TC-01a ... ]                          ║
║  ← VERIFY: same content as /ecom-ops/flash-sales/active      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04b: Admin List Active - Negative (prod: customer JWT)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call admin endpoint with customer JWT (prod only)     ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales/active       ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
║                                                              ║
║  EXPECTED RESPONSE (prod):                                 ║
║    403 Forbidden                                            ║
║  { "code":"AUTH_002","message":"Admin role required" }      ║
║                                                              ║
║  NOTE: Dev profile is permitAll, returns 200.               ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: GET /admin/flash-sales (Admin list ALL)

### TC-05a: List All Sales - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Admin lists ALL sales (incl cancelled/ended)         ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales               ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{accessToken}}                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{newSaleId}}", "name":"BACK-TO-SCHOOL-2026",    ║
║      "status":"SCHEDULED", ... },                           ║
║    { "id":"{{seedSaleId}}", "name":"SUMMER-COOL",           ║
║      "status":"ACTIVE", ... },                              ║
║    { "id":"...", "name":"EXPIRED-SALE",                     ║
║      "status":"ENDED", ... },                               ║
║    { "id":"...", "name":"INSTANT-DEAL-2026",                ║
║      "status":"ACTIVE", ... }                               ║
║  ]                                                          ║
║  ← VERIFY: includes ALL statuses (ACTIVE, SCHEDULED, ENDED, CANCELLED)║
║  ← VERIFY: total count > active count (extra ENDED ones)    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-05b: List All - Sorted (newest first)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify default sort order                            ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales               ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: list ordered by startsAt DESC or createdAt DESC   ║
║    (newest sale appears first)                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-05c: List All - Negative (no admin auth, prod)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call without auth (prod only)                        ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales               ║
║  EXPECTED RESPONSE (prod):                                 ║
║    401 Unauthorized                                         ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: GET /admin/flash-sales/{id} (Admin get by id)

### TC-06a: Admin Get Sale - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Admin gets sale detail (works for any status)        ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales/{{newSaleId}} ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{accessToken}}                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{newSaleId}}", "name":"BACK-TO-SCHOOL-2026",      ║
║    "status":"SCHEDULED", "items":[...], ... }               ║
║  ← VERIFY: even SCHEDULED sales accessible via admin route  ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-06b: Admin Get Sale - Negative (non-existent UUID)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get unknown sale                                     ║
║  METHOD: GET  PATH: /api/v1/admin/flash-sales/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Flash sale not found" }      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-07: POST /admin/flash-sales/{id}/cancel (Admin cancel)

### TC-07a: Cancel Sale - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel an active flash sale                          ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales/{{newSaleId}}/cancel║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{accessToken}}                   ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "reason": "Incorrect pricing - admin override"           ║
║  }                                                          ║
║                                                              ║
║  (Note: if controller has no body, omit - some impls no body)║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id":"{{newSaleId}}",                                    ║
║    "status":"CANCELLED",        ← VERIFY                    ║
║    "cancelledAt":"2026-06-19T...",                          ║
║    ...                                                      ║
║  }                                                          ║
║  ← VERIFY: subsequent GET /ecom-ops/flash-sales/active      ║
║    does NOT include this id anymore                         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-07b: Cancel Sale - Negative (already cancelled)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Try to cancel an already-CANCELLED sale              ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales/{{newSaleId}}/cancel║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    409 Conflict                                             ║
║  { "code":"BIZ_002",                                        ║
║    "message":"Sale is already cancelled" }                  ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-07c: Cancel Sale - Negative (non-existent UUID)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel unknown sale                                  ║
║  METHOD: POST  PATH: /api/v1/admin/flash-sales/00000000-0000-0000-0000-000000000000/cancel║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Flash sale not found" }      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 8. Edge Cases & Notes

```
╔══════════════════════════════════════════════════════════════╗
║  EDGE-01: Concurrent purchase attempt                       ║
║    When remainingQty hits 0, cart/checkout should reject.   ║
║    Tested in customer-portal E2E flow.                      ║
║                                                              ║
║  EDGE-02: Sale with no items                                 ║
║    → Should fail 400 (items required)                       ║
║                                                              ║
║  EDGE-03: discountPct > 100                                 ║
║    → Should fail 400 (negative sale price)                  ║
║                                                              ║
║  EDGE-04: maxQtyPerUser = 0                                 ║
║    → Should fail 400 (must be >= 1)                         ║
║                                                              ║
║  EDGE-05: Sale time-zone                                     ║
║    All LocalDateTime stored as server TZ (Asia/Ho_Chi_Minh) ║
║    Verify startsAt/endsAt render correctly in response.     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 9. Pass / Fail Summary

```
╔══════════════════════════════════════════════════════════════╗
║  Total test cases: 20                                        ║
║  Expected pass: ≥18 (≥90%)                                   ║
║  Known gaps   : Concurrent purchase (covered in E2E-03)      ║
║                                                              ║
║  Sign-off: ___________________  Date: ___________             ║
╚══════════════════════════════════════════════════════════════╝
```

**End of `17-ECOM-OPS.md`**
