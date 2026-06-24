# UAT Test: Report Service (UC09)

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10 (commit `63b9397`)
**Module:** `report-service` (port 8092)
**Use Case:** UC09 - View Reports (revenue, inventory, staff, realtime, export, schedule)
**Total endpoints:** 14 documented (11 main + 3 alias)
**Controller:** `ReportController` (`/reports`)

---

## 1. Service Info

```
╔══════════════════════════════════════════════════════════════════════╗
║  SERVICE   : report-service                                          ║
║  PORT      : 8092 (direct) / 8080 (via Gateway)                      ║
║  STACK     : Spring Boot 4.0.7 + Java 21 + MySQL 8                   ║
║              + Apache POI (Excel) + Apache PDFBox (PDF)              ║
║  PREFIX    : /api/v1/reports/**                                     ║
║  SECURITY  : JWT HS256 - permitAll in dev profile                    ║
║  CLIENTS   : order-service, inventory-service (read-only aggregations)║
║  ENUMS     : GroupBy={DAY,WEEK,MONTH} (RevenueReportRequest)         ║
║  AUTH      : Admin/CEO (full), Manager (branch-scoped via X-Branch-Id)║
╚══════════════════════════════════════════════════════════════════════╝
```

### 1.1 Indicator Legend

| Symbol      | Meaning                                                   |
|-------------|-----------------------------------------------------------|
| ← INPUT     | Field value to send in request                            |
| ← VERIFY    | Assertion to check in response                            |
| ← CAPTURE   | Save this value as variable for next step                 |
| ← SEED      | Pre-conditions (data must exist)                          |
| ← SCOPE     | Branch-scope via X-Branch-Id header (multi-tenant)        |
| ← ASYNC     | Export endpoints may return 202 Accepted (job-queue)      |

### 1.2 Authorization Matrix

| Endpoint                              | Admin/CEO | Manager (own branch) | Pharmacist |
|---------------------------------------|-----------|----------------------|------------|
| GET /reports/revenue                  | ✓ all     | ✓ own                | ✗          |
| POST /reports/revenue                 | ✓ all     | ✓ own                | ✗          |
| GET /reports/inventory                | ✓ all     | ✓ own                | ✓ own      |
| POST /reports/inventory               | ✓ all     | ✓ own                | ✗          |
| GET /reports/staff                    | ✓ all     | ✓ own                | ✗          |
| POST /reports/staff                   | ✓ all     | ✓ own                | ✗          |
| GET /reports/realtime/stats           | ✓ all     | ✓ own                | ✓ own      |
| GET /reports/realtime/recent-orders   | ✓ all     | ✓ own                | ✓ own      |
| GET /reports/export                   | ✓ all     | ✓ own                | ✗          |
| POST /reports/schedule                | ✓ all     | ✗                   | ✗          |
| GET /reports/schedules                | ✓ all     | ✓ own                | ✗          |
| POST /reports/export/excel            | ✓ all     | ✓ own                | ✗          |
| POST /reports/export/pdf              | ✓ all     | ✓ own                | ✗          |
| DELETE /reports/schedules/{id}        | ✓ all     | ✗                   | ✗          |

---

## 2. Endpoint Summary

| #  | Method | Path                                              | Auth         | Purpose                                |
|----|--------|---------------------------------------------------|--------------|----------------------------------------|
| 1  | GET    | `/api/v1/reports/revenue?from=...&to=...`         | permit+scope | Revenue aggregate (query params)       |
| 2  | POST   | `/api/v1/reports/revenue`                         | permit+scope | Revenue aggregate (JSON body)          |
| 3  | GET    | `/api/v1/reports/inventory?branchId=...`          | permit+scope | Inventory snapshot                     |
| 4  | POST   | `/api/v1/reports/inventory`                       | permit+scope | Inventory snapshot (JSON body)         |
| 5  | GET    | `/api/v1/reports/staff?fromDate=...&toDate=...`   | permit+scope | Staff performance                      |
| 6  | POST   | `/api/v1/reports/staff`                           | permit+scope | Staff performance (JSON body)          |
| 7  | GET    | `/api/v1/reports/realtime/stats`                  | permit+scope | Today KPIs (dashboard live tiles)      |
| 8  | GET    | `/api/v1/reports/realtime/recent-orders`          | permit+scope | N most recent orders                   |
| 9  | GET    | `/api/v1/reports/export?type=...&format=...`      | permit+scope | Sync export (Excel/PDF download)       |
| 10 | POST   | `/api/v1/reports/schedule`                        | permit       | Schedule periodic report               |
| 11 | GET    | `/api/v1/reports/schedules`                       | permit       | List all schedules                     |
| 12 | POST   | `/api/v1/reports/export/excel`                    | permit+scope | Async Excel export job (TICKET-305)    |
| 13 | POST   | `/api/v1/reports/export/pdf`                      | permit+scope | Async PDF export job (TICKET-306)      |
| 14 | DELETE | `/api/v1/reports/schedules/{id}`                  | permit       | Cancel schedule (TICKET-306)           |

---

## 3. Prerequisites

1. **Environment**: Gateway running at `http://localhost:8080`.
2. **Database**: `pcms_report` schema with `report_schedules`, `report_export_jobs` tables.
3. **Seeded data** (run Section 4 first):
   - At least 1 branch
   - Orders with payments in the past 30 days (so revenue has data)
   - Staff with role PHARMACIST for staff report
   - Inventory batches (for inventory report)
4. **Multi-tenant scope**: In dev, send `X-Branch-Id: {{branchHQ}}` header to scope to HQ. Omit for all branches.

---

## 4. Test Data Seeding

### 4.1 Date-range Test Data

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create orders across last 30 days for revenue testing ║
║  METHOD: POST  PATH: /api/v1/orders (x10)                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST: each with different `createdAt` via direct DB      ║
║  INSERT OR through 5 paid orders in past 7 days              ║
║  ← VERIFY: SELECT COUNT(*) FROM orders WHERE status='PAID'   ║
║           AND paidAt >= NOW() - INTERVAL 30 DAY; → >= 5      ║
║                                                               ║
║  STAFF PERFORMANCE:                                           ║
║  ← VERIFY: at least 2 staff with PHARMACIST role + sales      ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.2 Captured Variables

| Variable           | Example                                 | Source                  |
|--------------------|-----------------------------------------|-------------------------|
| `{{gateway}}`      | `http://localhost:8080`                 | static                  |
| `{{accessToken}}`  | `eyJhbGciOiJIUzI1...`                   | POST /auth/login        |
| `{{branchHQ}}`     | `uuid`                                  | POST /branches          |
| `{{branchQ1}}`     | `uuid`                                  | POST /branches          |
| `{{userId}}`       | `uuid` (admin/manager)                  | POST /auth/login        |
| `{{staffId}}`      | `uuid` (pharmacist for staff report)    | POST /users             |
| `{{FROM}}`         | `2026-05-01`                            | manual                  |
| `{{TO}}`           | `2026-06-19`                            | manual (today)          |
| `{{SCHEDULE_ID}}`  | `uuid`                                  | POST /reports/schedule  |

---

## 5. Test Cases

### 5.1 GET /api/v1/reports/revenue

#### Test 5.1.1 - Happy path: revenue last 30 days, grouped by day

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get daily revenue for last 30 days                    ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=2026-05-20&to=2026-06-19&groupBy=day
║  HEADER: X-Branch-Id: {{branchHQ}}        ← scope            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "data": [                                                ║
║      {                                                       ║
║        "date": "2026-05-20",                                 ║
║        "revenue": 1500000,                                   ║
║        "orderCount": 12,                                     ║
║        "avgTicket": 125000                                   ║
║      },                                                      ║
║      { "date": "2026-05-21", "revenue": 1800000, ... },    ║
║      ...                                                     ║
║    ],                                                        ║
║    "total": {                                               ║
║      "revenue": 45000000,                                    ║
║      "orderCount": 350,                                      ║
║      "avgTicket": 128571                                     ║
║    }                                                         ║
║  }                                                          ║
║  ← VERIFY: data.length > 0, total.revenue = sum(data.revenue)║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.2 - Group by week

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Weekly aggregation                                    ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=2026-04-01&to=2026-06-19&groupBy=week║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: data has ~12 entries (12 weeks)                  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.3 - Group by month

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Monthly aggregation                                   ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=2026-01-01&to=2026-06-19&groupBy=month║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: data has ~6 entries                              ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.4 - Filter by branchId query param

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Specific branch only                                  ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=2026-05-01&to=2026-06-19&branchId={{branchHQ}}&groupBy=day║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: data reflects HQ branch only                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.5 - Negative: missing from date

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required `from` param                         ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?to=2026-06-19   ║
║  EXPECTED: 400 Bad Request (MissingServletRequestParameter)  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.6 - Negative: malformed date

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bad date format                                       ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=20-06-2026&to=2026-06-19║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.7 - Negative: from > to (invalid range)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Reversed range                                        ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=2026-06-19&to=2026-05-01&groupBy=day║
║  EXPECTED: 200 OK with empty data, OR 422 if business rule   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.8 - Negative: invalid groupBy

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown groupBy value                                 ║
║  METHOD: GET  PATH: /api/v1/reports/revenue?from=2026-05-01&to=2026-06-19&groupBy=hour║
║  EXPECTED: 200 OK with default 'day', OR 400 if strict       ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 POST /api/v1/reports/revenue

#### Test 5.2.1 - Happy path: JSON body variant

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Revenue report via POST                               ║
║  METHOD: POST  PATH: /api/v1/reports/revenue                 ║
║  HEADER:                                                     ║
║    Content-Type: application/json                            ║
║    X-Branch-Id: {{branchHQ}}                                ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "fromDate": "2026-05-01",                                 ║
║    "toDate": "2026-06-19",                                   ║
║    "branchId": null,          ← null = use X-Branch-Id       ║
║    "groupBy": "WEEK"                                        ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (same shape as GET 5.1.2)                  ║
║  ← VERIFY: data aggregated by week                           ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.2 - Happy path: with explicit branchId in body

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Body branchId overrides header                        ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "fromDate": "2026-05-01",                                 ║
║    "toDate": "2026-06-19",                                   ║
║    "branchId": "{{branchQ1}}",                              ║
║    "groupBy": "MONTH"                                       ║
║  }                                                          ║
║  HEADER: X-Branch-Id: {{branchHQ}}   ← should be overridden ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: data is for branchQ1, NOT branchHQ              ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.3 - Negative: missing fromDate

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required field                                ║
║  REQUEST: { "toDate":"2026-06-19", "groupBy":"DAY" }         ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: field error on fromDate                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 GET /api/v1/reports/inventory

#### Test 5.3.1 - Happy path: HQ inventory snapshot

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Inventory snapshot for HQ                             ║
║  METHOD: GET  PATH: /api/v1/reports/inventory                ║
║  HEADER: X-Branch-Id: {{branchHQ}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "data": [                                                ║
║      {                                                       ║
║        "medicineId": "{{med1}}",                             ║
║        "medicineName": "Paracetamol 500mg",                  ║
║        "batchNo": "BTH-001",                                 ║
║        "qtyOnHand": 95,                                      ║
║        "minStockLevel": 20,                                  ║
║        "expiryDate": "2027-12-31",                           ║
║        "valueVnd": 475000                                    ║
║      }                                                       ║
║    ],                                                        ║
║    "total": {                                               ║
║      "skuCount": 50,                                         ║
║      "totalQty": 5000,                                       ║
║      "totalValueVnd": 25000000                               ║
║    }                                                         ║
║  }                                                          ║
║  ← VERIFY: data.length > 0, total.skuCount = COUNT(distinct medicineId)║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.3.2 - Filter by branchId query param

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Specific branch                                      ║
║  METHOD: GET  PATH: /api/v1/reports/inventory?branchId={{branchQ1}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: data is for branchQ1 only                        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.3.3 - Edge: branch with no inventory

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty inventory                                       ║
║  ← SEED: branchEmpty with no inventory                       ║
║  METHOD: GET  PATH: /api/v1/reports/inventory?branchId={{branchEmpty}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, data=[], total.skuCount=0                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.4 POST /api/v1/reports/inventory

#### Test 5.4.1 - Happy path: JSON body with date filter

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Inventory via POST with date filter                   ║
║  METHOD: POST  PATH: /api/v1/reports/inventory               ║
║  HEADER: X-Branch-Id: {{branchHQ}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "branchId": null,                                         ║
║    "fromDate": "2026-05-01",                                 ║
║    "toDate": "2026-06-19"                                    ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: only batches with expiryDate in range             ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.2 - POST without dates (snapshot only)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No date filter                                        ║
║  REQUEST: { "branchId": "{{branchHQ}}" }                     ║
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: data has all batches for branch                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.5 GET /api/v1/reports/staff

#### Test 5.5.1 - Happy path: staff performance last 30 days

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Staff report                                          ║
║  METHOD: GET  PATH: /api/v1/reports/staff?fromDate=2026-05-20&toDate=2026-06-19
║  HEADER: X-Branch-Id: {{branchHQ}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "data": [                                                ║
║      {                                                       ║
║        "staffId": "{{staffId}}",                             ║
║        "staffName": "Dr. Tran Van B",                        ║
║        "ordersProcessed": 85,                                ║
║        "totalSales": 12500000,                               ║
║        "avgTicket": 147059,                                  ║
║        "refundsIssued": 2                                    ║
║      }                                                       ║
║    ],                                                        ║
║    "total": {                                               ║
║      "staffCount": 4,                                        ║
║      "totalSales": 45000000                                  ║
║    }                                                         ║
║  }                                                          ║
║  ← VERIFY: data.length > 0, each row has staffId, totalSales ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.2 - Branch filter

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: HQ staff only                                         ║
║  METHOD: GET  PATH: /api/v1/reports/staff?fromDate=2026-05-01&toDate=2026-06-19&branchId={{branchHQ}}
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: only HQ staff in response                        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.3 - Negative: missing fromDate

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing fromDate                                      ║
║  METHOD: GET  PATH: /api/v1/reports/staff?toDate=2026-06-19  ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.4 - Edge: date range with no orders

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No orders in window                                   ║
║  METHOD: GET  PATH: /api/v1/reports/staff?fromDate=2030-01-01&toDate=2030-01-31
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { "data":[], "total":{"staffCount":0, "totalSales":0} }     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.6 POST /api/v1/reports/staff

#### Test 5.6.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Staff report via POST                                 ║
║  METHOD: POST  PATH: /api/v1/reports/staff                   ║
║  HEADER: X-Branch-Id: {{branchHQ}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "fromDate": "2026-05-01",                                 ║
║    "toDate": "2026-06-19",                                   ║
║    "branchId": null                                          ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (same as GET 5.5.1)                        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.2 - Negative: missing required fields

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No dates                                             ║
║  REQUEST: { "branchId":"{{branchHQ}}" }                      ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.7 GET /api/v1/reports/realtime/stats

#### Test 5.7.1 - Happy path: today KPIs

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Realtime dashboard stats                              ║
║  METHOD: GET  PATH: /api/v1/reports/realtime/stats           ║
║  HEADER: X-Branch-Id: {{branchHQ}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "todayRevenue": 2500000,                                  ║
║    "todayOrders": 18,                                        ║
║    "todayCustomers": 15,                                     ║
║    "lowStockCount": 2,                                       ║
║    "expiringBatchesCount": 1,                                ║
║    "activeStaff": 5,                                         ║
║    "lastUpdated": "2026-06-19T11:00:00"                      ║
║  }                                                          ║
║  ← VERIFY: all 7 fields present, types correct (number/string)║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.2 - Filter by branchId

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Branch-specific realtime                              ║
║  METHOD: GET  PATH: /api/v1/reports/realtime/stats?branchId={{branchQ1}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: numbers reflect branchQ1 only                    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.8 GET /api/v1/reports/realtime/recent-orders

#### Test 5.8.1 - Happy path: default limit

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Recent 5 orders (default)                             ║
║  METHOD: GET  PATH: /api/v1/reports/realtime/recent-orders   ║
║  HEADER: X-Branch-Id: {{branchHQ}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  [                                                           ║
║    {                                                         ║
║      "orderId": "uuid",                                      ║
║      "orderNumber": "ORD-20260619-0012",                     ║
║      "customerName": "Nguyen Van A",                        ║
║      "total": 350000,                                        ║
║      "status": "PAID",                                       ║
║      "createdAt": "2026-06-19T10:55:00"                      ║
║    },                                                        ║
║    ... up to 5 entries                                       ║
║  ]                                                           ║
║  ← VERIFY: array.length <= 5, sorted by createdAt DESC       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.2 - Custom limit

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: 10 most recent orders                                 ║
║  METHOD: GET  PATH: /api/v1/reports/realtime/recent-orders?limit=10║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: array.length <= 10                                ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.3 - Negative: limit too large

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Excessive limit                                       ║
║  METHOD: GET  PATH: /api/v1/reports/realtime/recent-orders?limit=99999║
║  EXPECTED: 200 OK (server may cap at 100) or 400 if strict   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.4 - Negative: negative limit

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: limit = -1                                            ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.9 GET /api/v1/reports/export (Sync download)

#### Test 5.9.1 - Happy path: revenue Excel export

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Download revenue Excel                                ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=revenue&format=excel&from=2026-05-01&to=2026-06-19
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║    Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet║
║    Content-Disposition: attachment; filename="revenue-report.xlsx"║
║  <binary .xlsx content>                                      ║
║  ← VERIFY: response is binary, file opens in Excel/LibreOffice║
║  ← VERIFY: filename matches pattern {type}-report.{ext}    ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.2 - Happy path: revenue PDF export

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Download revenue PDF                                  ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=revenue&format=pdf&from=2026-05-01&to=2026-06-19
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║    Content-Type: application/pdf                             ║
║    Content-Disposition: attachment; filename="revenue-report.pdf"║
║  ← VERIFY: PDF opens correctly                               ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.3 - Happy path: inventory Excel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Inventory export                                      ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=inventory&format=excel&from=2026-05-01&to=2026-06-19
║  EXPECTED: 200 OK, .xlsx with inventory rows                ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.4 - Happy path: staff PDF

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Staff export                                          ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=staff&format=pdf&from=2026-05-01&to=2026-06-19
║  EXPECTED: 200 OK, .pdf with staff table                    ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.5 - Negative: invalid type

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bogus report type                                     ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=invalid&format=excel&from=2026-05-01&to=2026-06-19
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: error mentions valid types                       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.6 - Negative: invalid format

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bogus format                                          ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=revenue&format=csv&from=2026-05-01&to=2026-06-19
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.7 - Negative: missing from/to

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing date params                                   ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=revenue&format=excel
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.8 - Edge: xlsx alias for format=excel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: format=xlsx accepted as alias                         ║
║  METHOD: GET  PATH: /api/v1/reports/export?type=revenue&format=xlsx&from=2026-05-01&to=2026-06-19
║  EXPECTED: 200 OK, filename="revenue-report.xlsx"            ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.10 POST /api/v1/reports/schedule (Create schedule)

#### Test 5.10.1 - Happy path: weekly revenue email

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Schedule weekly revenue report                        ║
║  METHOD: POST  PATH: /api/v1/reports/schedule                ║
║  HEADER:                                                     ║
║    Content-Type: application/json                            ║
║    X-User-Id  : {{userId}}       ← creator                  ║
║    X-Branch-Id: {{branchHQ}}                                 ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "type": "revenue",                                        ║
║    "format": "pdf",                                          ║
║    "branchId": null,                                         ║
║    "cronExpression": "0 8 * * MON",                          ║
║    "recipientEmail": "ceo@pcms.vn",                          ║
║    "createdBy": "{{userId}}"                                 ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "type": "revenue",                                        ║
║    "format": "pdf",                                          ║
║    "cronExpression": "0 8 * * MON",                          ║
║    "recipientEmail": "ceo@pcms.vn",                          ║
║    "createdBy": "{{userId}}",                                ║
║    "active": true,                                           ║
║    "nextRunAt": "2026-06-23T08:00:00",  ← next Monday 8am  ║
║    "lastRunAt": null,                                        ║
║    "createdAt": "2026-06-19T11:00:00"                        ║
║  }                                                          ║
║  ← CAPTURE: SCHEDULE_ID                                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.2 - Happy path: daily low-stock

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Daily inventory schedule                              ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "type": "inventory",                                      ║
║    "format": "excel",                                        ║
║    "cronExpression": "0 7 * * *",                            ║
║    "recipientEmail": "manager@pcms.vn",                      ║
║    "createdBy": "{{userId}}"                                 ║
║  }                                                          ║
║  EXPECTED: 200 OK, nextRunAt = next 07:00                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.3 - Negative: missing createdBy and X-User-Id

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No creator identity                                   ║
║  HEADER: (no X-User-Id)                                      ║
║  REQUEST: { "type":"revenue", "format":"pdf", "cronExpression":"0 8 * * *", "recipientEmail":"x@y.z" }║
║  EXPECTED: 422 Unprocessable Entity                          ║
║  ← VERIFY: message "Thiếu thông tin người tạo lịch báo cáo" ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.4 - Negative: empty recipientEmail

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No email                                              ║
║  REQUEST: { "type":"revenue", "format":"pdf", "cronExpression":"0 8 * * *", "recipientEmail":"", "createdBy":"{{userId}}" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Email nhận không được để trống"                  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.5 - Negative: invalid cron

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Malformed cron                                        ║
║  REQUEST: { "type":"revenue", "format":"pdf", "cronExpression":"not-a-cron", "recipientEmail":"x@y.z", "createdBy":"{{userId}}" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: cron validation error                            ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.6 - Negative: type too long

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: type > 30 chars                                       ║
║  REQUEST: "type": "very-very-very-long-report-type-name"    ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Loại báo cáo không được vượt quá 30 ký tự"      ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.11 GET /api/v1/reports/schedules

#### Test 5.11.1 - Happy path: list all

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List schedules                                        ║
║  METHOD: GET  PATH: /api/v1/reports/schedules                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  [                                                           ║
║    { "id":"{{SCHEDULE_ID}}", "type":"revenue", "active":true, ... },║
║    { "id":"...", "type":"inventory", "active":true, ... }   ║
║  ]                                                           ║
║  ← VERIFY: array.length >= 1, each has id/type/active       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.11.2 - Empty list

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No schedules                                          ║
║  ← SEED: TRUNCATE report_schedules                          ║
║  METHOD: GET  PATH: /api/v1/reports/schedules                ║
║  EXPECTED: 200 OK, []                                        ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.12 POST /api/v1/reports/export/excel (Async, TICKET-305)

#### Test 5.12.1 - Happy path: queue Excel job

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Queue async Excel export                              ║
║  METHOD: POST  PATH: /api/v1/reports/export/excel            ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "reportType": "revenue",                                  ║
║    "format": "excel",                                        ║
║    "branchId": "{{branchHQ}}",                              ║
║    "fromDate": "2026-05-01",                                 ║
║    "toDate": "2026-06-19",                                   ║
║    "filters": { "groupBy": "WEEK" }                          ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 202 Accepted                                      ║
║  {                                                          ║
║    "status": "queued",                                       ║
║    "jobId": "uuid",                                          ║
║    "downloadUrl": "/api/v1/reports/export/download/{jobId}"  ║
║  }                                                          ║
║  ← CAPTURE: EXCEL_JOB_ID                                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.12.2 - Negative: invalid reportType

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bad reportType                                        ║
║  REQUEST: { "reportType":"unknown", ... }                    ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "reportType must be one of: revenue, inventory, staff"║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.12.3 - Negative: missing reportType

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No reportType                                         ║
║  REQUEST: { "format":"excel", "branchId":"{{branchHQ}}" }   ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.12.4 - Edge: no dates (default 7-day window)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Default 7-day window                                  ║
║  REQUEST: { "reportType":"revenue", "branchId":"{{branchHQ}}" }║
║  EXPECTED: 202 Accepted, jobId returned                      ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.13 POST /api/v1/reports/export/pdf (Async, TICKET-306)

#### Test 5.13.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Queue async PDF export                                ║
║  METHOD: POST  PATH: /api/v1/reports/export/pdf              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "reportType": "inventory",                                ║
║    "branchId": "{{branchHQ}}",                              ║
║    "fromDate": "2026-05-01",                                 ║
║    "toDate": "2026-06-19"                                    ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 202 Accepted                                      ║
║  { "status":"queued", "jobId":"uuid", "downloadUrl":"..." } ║
║  ← CAPTURE: PDF_JOB_ID                                       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.13.2 - Negative: missing reportType

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Same as Excel                                         ║
║  REQUEST: { "branchId":"{{branchHQ}}" }                     ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.14 DELETE /api/v1/reports/schedules/{id} (TICKET-306)

#### Test 5.14.1 - Happy path: cancel schedule

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel a schedule                                     ║
║  METHOD: DELETE  PATH: /api/v1/reports/schedules/{{SCHEDULE_ID}}║
║  ← SEED: SCHEDULE_ID with active=true                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "id": "{{SCHEDULE_ID}}",                                  ║
║    "active": false,        ← flipped to inactive            ║
║    "updatedAt": "..."                                       ║
║  }                                                          ║
║  ← VERIFY: GET /schedules → entry still present, active=false║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.14.2 - Idempotency: cancel already-cancelled

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel again                                          ║
║  METHOD: DELETE  PATH: /api/v1/reports/schedules/{{SCHEDULE_ID}}║
║  ← SEED: SCHEDULE_ID now active=false                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: no error, idempotent                             ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.14.3 - Negative: cancel unknown schedule

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown UUID                                          ║
║  METHOD: DELETE  PATH: /api/v1/reports/schedules/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Business Rules Summary

| Rule                                            | Enforcement                                      |
|-------------------------------------------------|--------------------------------------------------|
| X-Branch-Id header scopes Manager to own branch | Falls back to query param if header absent       |
| Date range required for all aggregations        | 400 if missing                                   |
| Cron expressions validated server-side          | 400 on invalid cron                              |
| Excel/PDF jobs are async                        | Returns 202 + jobId, not binary                  |
| Schedule cancel = soft (active=false)            | Row preserved for audit                          |
| Realtime stats are cached 30s                   | Reduces DB load                                  |
| Multi-branch users see aggregate across branches| Omit branchId OR header                          |

---

## 7. Performance Expectations

| Endpoint                          | p95 target | Notes                              |
|-----------------------------------|-----------:|------------------------------------|
| GET /reports/revenue              | < 500ms    | Indexed by paidAt + branchId       |
| POST /reports/revenue             | < 500ms    | Same as GET                        |
| GET /reports/inventory            | < 300ms    | Indexed by branchId                |
| GET /reports/realtime/stats       | < 100ms    | Redis cache 30s                    |
| GET /reports/realtime/recent-orders| < 100ms   | Redis cache 30s                    |
| GET /reports/export               | < 3000ms   | Excel gen is slow                  |
| POST /reports/export/excel        | < 50ms     | Just queues job                    |

---

## 8. Post-Test Verification

```
╔══════════════════════════════════════════════════════════════╗
║  CHECK after all tests:                                      ║
║    GET /reports/revenue?from=...&to=... → consistent totals  ║
║    GET /reports/realtime/stats → updated within last 30s    ║
║    GET /reports/schedules → contains SCHEDULE_ID with active=false║
║    Files downloaded from /export → valid xlsx/pdf (no 0 bytes)║
║    SELECT * FROM report_schedules;                          ║
║      → all test-created schedules present                   ║
║    SELECT * FROM report_export_jobs WHERE status='queued';   ║
║      → jobs from 5.12 + 5.13 present                        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 9. Bug Severity Quick Reference

| Severity | Example                                                  |
|----------|----------------------------------------------------------|
| Critical | /export returns 0-byte file (corrupted data)            |
| Major    | Revenue total != sum of rows (aggregation bug)           |
| Minor    | Schedule cancel does not flip active flag                |
| Trivial  | Filename has spaces when type contains special chars     |

---

**End of 12-REPORT.md** → Next: [`13-CUSTOMER-PORTAL.md`](./13-CUSTOMER-PORTAL.md)
