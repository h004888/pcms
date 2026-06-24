# UAT Test Scenario: Supplier Management

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `supplier-service` (port 8085)
**UAT Doc Reference:** `05-SUPPLIER.md`
**Coverage:** UC11 (Manage Suppliers + Transaction History)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  SUPPLIER-SERVICE                                                           ║
║  Tier    : B2B                                                                ║
║  Port    : 8085 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/suppliers/**                        ║
║  Auth    : JWT HS256 - permitAll in dev, role-based in prod                   ║
║  DB      : MySQL 8 (schema = supplier_db)                                     ║
║  Tests   : 6 endpoints, ~20 cases, est. 45 min                               ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller          | Method | Path                          | Description                | Test Cases |
|----|---------------------|--------|-------------------------------|----------------------------|-----------:|
| 1  | SupplierController  | GET    | `/suppliers`                  | Danh sách NCC              |          4 |
| 2  | SupplierController  | GET    | `/suppliers/{id}`             | Chi tiết NCC               |          2 |
| 3  | SupplierController  | POST   | `/suppliers`                  | Tạo NCC                    |          4 |
| 4  | SupplierController  | PUT    | `/suppliers/{id}`             | Cập nhật NCC               |          4 |
| 5  | SupplierController  | GET    | `/suppliers/{id}/history`     | Lịch sử giao dịch NCC      |          3 |
| 6  | SupplierController  | DELETE | `/suppliers/{id}`             | Xoá mềm NCC                |          3 |
| **TOTAL**                |        |                               |                            |     **~20**|

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `supplier_db` migrated
- [x] Eureka: `SUPPLIER-SERVICE` registered
- [x] Gateway route `/api/v1/suppliers/**` → supplier-service
- [x] Admin JWT available (reuse `{{accessToken}}` from `01-AUTH-USER.md`)
- [x] Inventory-service running (for cross-service history check)

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/SUPPLIER-SERVICE     ║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8085/actuator/health | jq .status     ║
║  → "UP"                                                        ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  Suppliers seeded by SQL (see Master Plan §7.5):             ║
║                                                              ║
║  1. DHG Pharma                                                ║
║     taxCode     : 0301234567                                 ║
║     contact     : Tran Van A                                 ║
║     phone       : 0283333333                                 ║
║     email       : contact@dhg.vn                             ║
║                                                              ║
║  2. Traphaco                                                  ║
║     taxCode     : 0301234568                                 ║
║                                                              ║
║  3. Imexpharm                                                 ║
║     taxCode     : 0301234569                                 ║
║                                                              ║
║  All suppliers have: status = ACTIVE                         ║
╚══════════════════════════════════════════════════════════════╝
```

If not seeded, see Master Plan §7.5 Step 5 for bootstrap.

---

## 4. Variables to Capture

| Variable             | Example                                | Captured from              |
|----------------------|----------------------------------------|----------------------------|
| `{{sup1}}`           | `uuid` (DHG Pharma)                    | Master Plan §7.5           |
| `{{sup2}}`           | `uuid` (Traphaco)                      | Master Plan §7.5           |
| `{{sup3}}`           | `uuid` (Imexpharm)                     | Master Plan §7.5           |
| `{{newSupId}}`       | `uuid`                                 | TC-03 below                |
| `{{newSupTax}}`      | `e.g. "0312345678"`                    | TC-03 below                |

---

## TC-01: GET /suppliers (List with pagination)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all suppliers                                   ║
║  METHOD: GET  PATH: /api/v1/suppliers                       ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                              ║
║    page=0&size=20                                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "data": [                                                ║
║      { "id":"{{sup1}}","name":"DHG Pharma",                 ║
║        "taxCode":"0301234567","contactPerson":"Tran Van A", ║
║        "phone":"0283333333","email":"contact@dhg.vn",       ║
║        "address":"...","bankName":"...", "bankAccount":"...",║
║        "status":"ACTIVE",                                   ║
║        "createdAt":"...","updatedAt":"..." },               ║
║      { "id":"{{sup2}}","name":"Traphaco", ... },            ║
║      { "id":"{{sup3}}","name":"Imexpharm", ... }            ║
║    ],                                                       ║
║    "page":0,"size":20,"total":3,"totalPages":1             ║
║  }                                                          ║
║  ← VERIFY: sorted by name ASC (alphabetical)                 ║
║  ← VERIFY: total = 3                                        ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.1: GET /suppliers (Search by name)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?search=DHG                                         ║
║  EXPECTED: 200, data[] contains only "DHG Pharma"           ║
║  ← VERIFY: search is case-insensitive                       ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.2: GET /suppliers (Pagination — size cap)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?size=99999                                          ║
║  EXPECTED: 200, response.size capped at 100                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.3: GET /suppliers (Pagination — out of range)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?page=99                                             ║
║  EXPECTED: 200, data=[] empty array                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /suppliers/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get supplier detail by UUID                          ║
║  METHOD: GET  PATH: /api/v1/suppliers/{{sup1}}              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{sup1}}",                                        ║
║    "name": "DHG Pharma",                                    ║
║    "taxCode": "0301234567",                                 ║
║    "contactPerson": "Tran Van A",                           ║
║    "phone": "0283333333",                                   ║
║    "email": "contact@dhg.vn",                               ║
║    "address": "...",                                        ║
║    "bankName": "...",                                       ║
║    "bankAccount": "...",                                    ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "...", "updatedAt": "..."                   ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-02.1: GET /suppliers/{id} (Not found)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/suppliers/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                    ║
║  { "error":"SUPPLIER_NOT_FOUND",                            ║
║    "message":"Không tìm thấy nhà cung cấp" }                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /suppliers (Create new supplier)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create "Pharma Co. Ltd" supplier                     ║
║  METHOD: POST  PATH: /api/v1/suppliers                      ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    Authorization : Bearer {{accessToken}}                   ║
║    Content-Type  : application/json                          ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "name": "Pharma Co. Ltd",                                ║
║    "taxCode": "0312345678",                                 ║
║    "contactPerson": "Le Thi B",                             ║
║    "phone": "0284444444",                                   ║
║    "email": "sales@pharma-co.vn",                           ║
║    "address": "99 Le Loi, District 1, HCMC",                ║
║    "bankName": "Vietcombank",                               ║
║    "bankAccount": "1234567890"                              ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "name": "Pharma Co. Ltd",                                ║
║    "taxCode": "0312345678",                                 ║
║    "contactPerson": "Le Thi B",                             ║
║    "phone": "0284444444",                                   ║
║    "email": "sales@pharma-co.vn",                           ║
║    "address": "99 Le Loi, District 1, HCMC",                ║
║    "bankName": "Vietcombank",                               ║
║    "bankAccount": "1234567890",                             ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "...", "updatedAt": "..."                   ║
║  }                                                          ║
║  ← CAPTURE: {{newSupId}}, {{newSupTax}}="0312345678"        ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.1: POST /suppliers (Duplicate tax code)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: same taxCode as DHG ("0301234567")                 ║
║  EXPECTED: 409 Conflict                                     ║
║  { "error":"TAX_CODE_EXISTS",                               ║
║    "message":"Mã số thuế đã được đăng ký" }                ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.2: POST /suppliers (Invalid email format)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "name":"X", "taxCode":"0311111111",             ║
║             "phone":"0281111111", "email":"not-an-email" }  ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"email","msg":"Email không đúng định dạng" }     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.3: POST /suppliers (Missing name)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "taxCode":"0311111112","phone":"0281111112" }  ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"name","msg":"Tên nhà cung cấp không được để trống"}║
║  { "field":"phone","msg":"Số điện thoại không được để trống"}║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: PUT /suppliers/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update Pharma Co. — change phone + bank account      ║
║  METHOD: PUT  PATH: /api/v1/suppliers/{{newSupId}}          ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "name": "Pharma Co. Ltd",                                ║
║    "taxCode": "0312345678",                                 ║
║    "contactPerson": "Le Thi B Updated",                     ║
║    "phone": "0285555555",                                   ║
║    "email": "sales@pharma-co.vn",                           ║
║    "address": "99 Le Loi, District 1, HCMC",                ║
║    "bankName": "Techcombank",                               ║
║    "bankAccount": "9876543210",                             ║
║    "status": "ACTIVE"                                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{newSupId}}",                                     ║
║    "phone":"0285555555",                                    ║
║    "bankName":"Techcombank",                                ║
║    "bankAccount":"9876543210", ... }                        ║
║  ← VERIFY: updatedAt > createdAt                            ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.1: PUT /suppliers/{id} (404)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/suppliers/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.2: PUT /suppliers/{id} (status=INACTIVE)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { ... "status":"INACTIVE" }                       ║
║  EXPECTED: 200, status=INACTIVE                              ║
║  ← VERIFY: supplier disappears from active list             ║
║  ← RESET  : PUT status=ACTIVE before next test              ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.3: PUT /suppliers/{id} (Duplicate taxCode with other supplier)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: change taxCode to DHG's "0301234567"               ║
║  EXPECTED: 409 Conflict                                     ║
║  { "error":"TAX_CODE_EXISTS" }                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: GET /suppliers/{id}/history

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get supplier transaction history                     ║
║  METHOD: GET  PATH: /api/v1/suppliers/{{sup1}}/history      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "supplierId":"{{sup1}}",                               ║
║      "action":"INVENTORY_IMPORT",                           ║
║      "description":"Nhập 100 boxes MD001 (batch BTH-001)",  ║
║      "occurredAt":"2026-06-15T09:00:00Z" },                 ║
║    { "supplierId":"{{sup1}}",                               ║
║      "action":"INVENTORY_IMPORT",                           ║
║      "description":"Nhập 50 boxes MD002 (batch BTH-002)",   ║
║      "occurredAt":"2026-06-15T10:00:00Z" }                  ║
║  ]                                                          ║
║  ← VERIFY: response is JSON array (not paginated)            ║
║  ← VERIFY: all supplierId match {{sup1}}                    ║
║  ← VERIFY: sorted by occurredAt DESC (newest first)         ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.1: GET /suppliers/{id}/history (No transactions)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: GET /api/v1/suppliers/{{newSupId}}/history         ║
║  EXPECTED: 200, []  (empty array)                           ║
║  ← VERIFY: never null                                       ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.2: GET /suppliers/{id}/history (404 — supplier not found)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/suppliers/00000000-0000-0000-0000-000000000000/history║
║  EXPECTED: 404 Not Found (or 200 with [] depending on impl)  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: DELETE /suppliers/{id} (Soft delete)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Soft-delete the Pharma Co. supplier we created       ║
║  METHOD: DELETE  PATH: /api/v1/suppliers/{{newSupId}}       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    204 No Content (empty body)                              ║
║  ← VERIFY: GET /suppliers/{{newSupId}} → 404                ║
║  ← VERIFY: GET /suppliers → total drops to 3                ║
║  ← VERIFY: DB row still exists (soft delete)                ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-06.1: DELETE /suppliers/{id} (Already deleted — idempotency)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: DELETE /api/v1/suppliers/{{newSupId}} (twice)    ║
║  EXPECTED:                                                  ║
║    1st : 204 No Content                                     ║
║    2nd : 404 Not Found                                      ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-06.2: DELETE /suppliers/{id} (Supplier referenced by medicine)

```
╔══════════════════════════════════════════════════════════════╗
║  SETUP   : Create medicine MD001 with supplierId={{sup1}}   ║
║            (see 03-CATALOG.md)                              ║
║  REQUEST : DELETE /api/v1/suppliers/{{sup1}}                ║
║  EXPECTED: 422 Unprocessable Entity                         ║
║  { "error":"SUPPLIER_HAS_MEDICINES",                       ║
║    "message":"Nhà cung cấp đang có thuốc liên kết,         ║
║               vui lòng chuyển trước khi xoá" }              ║
║  NOTE    : Behavior depends on FK constraint strategy.      ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART E — Cross-Endpoint Integration

## TC-07: Full supplier lifecycle (E2E within this service)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Exercise complete supplier lifecycle                 ║
╠══════════════════════════════════════════════════════════════╣
║  1. POST   /suppliers                  → create (TC-03)     ║
║  2. GET    /suppliers/{id}             → confirm exists     ║
║  3. GET    /suppliers/{id}/history     → empty array         ║
║  4. PUT    /suppliers/{id}             → update (TC-04)     ║
║  5. GET    /suppliers?search=...       → appears in search  ║
║  6. PUT    /suppliers/{id}             → set INACTIVE        ║
║  7. DELETE /suppliers/{id}             → soft delete         ║
║  8. GET    /suppliers/{id}             → 404                 ║
║  9. GET    /suppliers/{id}/history     → 404                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-08: Cross-service — supplier ↔ inventory

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify supplier appears in inventory transaction list ║
╠══════════════════════════════════════════════════════════════╣
║  1. Use existing supplier {{sup1}}                            ║
║  2. POST /api/v1/inventory/import {supplierId: {{sup1}}, ...}║
║  3. GET  /api/v1/suppliers/{{sup1}}/history                   ║
║  EXPECTED: history contains INVENTORY_IMPORT entry from step 2║
║  ← VERIFY: cross-service event flow end-to-end               ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Summary of Test Variables

| Variable             | Final value                            | Source                |
|----------------------|----------------------------------------|------------------------|
| `{{sup1}}`           | uuid (DHG Pharma)                      | Master Plan §7.5       |
| `{{sup2}}`           | uuid (Traphaco)                        | Master Plan §7.5       |
| `{{sup3}}`           | uuid (Imexpharm)                       | Master Plan §7.5       |
| `{{newSupId}}`       | uuid (then soft-deleted)               | TC-03                  |
| `{{newSupTax}}`      | "0312345678"                           | TC-03                  |

---

## 6. Known Issues / Quirks

```
╔══════════════════════════════════════════════════════════════╗
║  OBS-SUPPLIER-001: List endpoint sorts by name ASC (default).║
║                     Master API doc shows no sort param.       ║
║  OBS-SUPPLIER-002: Tax code is the unique business key        ║
║                     (not supplier name). Two suppliers cannot ║
║                     share a tax code; returns 409.            ║
║  OBS-SUPPLIER-003: DELETE is soft delete (status=INACTIVE).  ║
║                     Compare with Category (hard delete).      ║
║  OBS-SUPPLIER-004: History endpoint is NOT paginated — may    ║
║                     return thousands of records for active    ║
║                     suppliers. Plan for pagination in v2.     ║
║  OBS-SUPPLIER-005: bankAccount has no IBAN/MSG validation.    ║
║                     Free-text only.                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Sign-off

| Role          | Name | Date | Pass / Fail | Notes |
|---------------|------|------|--------------|-------|
| Tester        |      |      | ☐            |       |
| Dev Lead      |      |      | ☐            |       |
| QA Manager    |      |      | ☐            |       |

**End of `05-SUPPLIER.md`** → Next: [`03-CATALOG.md`](./03-CATALOG.md)
