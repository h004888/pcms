# UAT - Inventory Service

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10
**Service:** inventory-service (Spring Boot 4.0.7, Java 21)
**Port:** 8086 (internal) — accessed via Gateway `:8080/api/v1/inventory/**`
**Total endpoints covered:** 21 (19 InventoryController + 2 OutboxConsumerController)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  inventory-service                                                              ║
║  ──────────────────────────────────────────────────────────────────────────    ║
║  Module      : Quản lý tồn kho (Import / Export / Transfer / Consume)          ║
║  Database    : pcms_inventory                                                  ║
║  Auth        : permitAll in dev profile (JWT HS256 in prod)                     ║
║  Idempotency : mutating endpoints hỗ trợ Idempotency-Key (CR-05)                ║
║  FIFO        : NSF-05 — xuất kho theo batch cũ nhất                            ║
║  Outbox      : 2 endpoint nhận event từ order-service (paid/cancelled)         ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

### 1.1 InventoryController (`/inventory`) — 19 endpoints

| #  | Method | Path                                                  | Mô tả                              | Test Status |
|----|--------|-------------------------------------------------------|------------------------------------|-------------|
| 1  | GET    | `/inventory`                                          | List batches (by branch)           | ☐           |
| 2  | GET    | `/inventory/{id}`                                     | Chi tiết 1 batch                   | ☐           |
| 3  | GET    | `/inventory/batches/scan/{code}`                      | Quét barcode → batch info          | ☐           |
| 4  | GET    | `/inventory/branch/{branchId}/medicine/{medicineId}`  | Tồn theo branch + medicine         | ☐           |
| 5  | POST   | `/inventory/import`                                   | Nhập kho                           | ☐           |
| 6  | POST   | `/inventory/export`                                   | Xuất kho (FIFO)                    | ☐           |
| 7  | POST   | `/inventory/consume`                                  | Tiêu hao (order paid flow)         | ☐           |
| 8  | POST   | `/inventory/transfer`                                 | Chuyển kho nội bộ                  | ☐           |
| 9  | POST   | `/inventory/bulk/import` (JSON)                       | Nhập kho hàng loạt (JSON array)    | ☐           |
| 10 | POST   | `/inventory/bulk/import` (multipart)                  | Nhập kho hàng loạt (CSV upload)    | ☐           |
| 11 | POST   | `/inventory/bulk/import-file`                         | Nhập kho hàng loạt (alias)        | ☐           |
| 12 | GET    | `/inventory/bulk/export`                              | Export CSV danh sách tồn           | ☐           |
| 13 | GET    | `/inventory/transactions`                             | Audit trail theo batch             | ☐           |
| 14 | GET    | `/inventory/low-stock`                                | Cảnh báo tồn thấp                  | ☐           |
| 15 | GET    | `/inventory/alerts/low-stock`                         | Alias của /low-stock               | ☐           |
| 16 | GET    | `/inventory/expiring`                                 | Cảnh báo sắp hết hạn (days param) | ☐           |
| 17 | GET    | `/inventory/alerts/expiry`                            | Alias của /expiring                | ☐           |
| 18 | GET    | `/inventory/report/stock-level`                       | Báo cáo mức tồn                   | ☐           |
| 19 | GET    | `/inventory/report/movement`                          | Báo cáo biến động kho             | ☐           |

### 1.2 OutboxConsumerController (`/inventory/orders`) — 2 endpoints (internal)

> ⚠️ **INTERNAL API** — Đây là webhook nội bộ, order-service sẽ gọi trực tiếp khi order PAID/CANCELLED. Trong dev, gọi thủ công qua gateway để test. Trong prod nên có X-Service-Token header.

| #  | Method | Path                                                  | Mô tả                              | Test Status |
|----|--------|-------------------------------------------------------|------------------------------------|-------------|
| 20 | POST   | `/inventory/orders/{orderId}/paid`                    | Outbox: đơn đã thanh toán         | ☐           |
| 21 | POST   | `/inventory/orders/{orderId}/cancelled`               | Outbox: đơn bị huỷ                | ☐           |

---

## 2. Prerequisites

Xem **[`00-MASTER-PLAN.md`](./00-MASTER-PLAN.md)** §3 cho môi trường.

### 2.1 Pre-seed bắt buộc

| Biến          | Mô tả                              | Nguồn                      |
|---------------|-------------------------------------|----------------------------|
| `{{branchHQ}}`| Branch Headquarter                  | POST /branches (master 7.3) |
| `{{branchQ1}}`| Branch Q1                           | POST /branches             |
| `{{branchQ7}}`| Branch Q7                           | POST /branches             |
| `{{med1}}`    | Medicine UUID (Paracetamol)         | 03-CATALOG.md 4.6          |
| `{{med2}}`    | Medicine UUID (Amoxicillin)         | 03-CATALOG.md 4.6          |
| `{{sup1}}`    | Supplier UUID (DHG)                 | 05-SUPPLIER.md (master 7.5)|
| `{{adminId}}` | Admin user UUID                     | 01-AUTH-USER.md            |

### 2.2 Seed bắt buộc chạy trước khi test inventory

```
╔══════════════════════════════════════════════════════════════╗
║  Chạy từ MASTER PLAN Scenario 0.1:                          ║
║   Step 1-3: users + branches                                ║
║   Step 4-5: categories + suppliers                          ║
║   Step 6:   5 medicines                                    ║
║   Step 7:   10 batches đã import vào HQ                    ║
║                                                            ║
║  Ngoài ra cần tạo thêm:                                    ║
║   - 5 batches nữa ở Q1 và Q7 (để test transfer)           ║
║   - 2-3 batches với expiry gần (để test /expiring)        ║
║   - 1 batch với qty < minStockLevel (để test /low-stock)   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 3. Indicator Legend

| Symbol     | Ý nghĩa                                              |
|------------|-------------------------------------------------------|
| ← INPUT    | Dữ liệu gửi trong request                            |
| ← SELECT   | Lựa chọn giữa các options                             |
| ← VERIFY   | Assertion cần kiểm tra trong response                |
| ← SEED     | Điều kiện tiên quyết (data phải tồn tại)             |
| ← CAPTURE  | Lưu giá trị thành biến để dùng step sau              |
| ← ITERATE  | Lặp lại N lần                                         |
| ← UPLOAD   | Upload file (multipart)                               |

---

## 4. Test Cases — InventoryController

### 4.1 GET /inventory — List batches

**Test case 4.1.A: List batches by branch**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List batches của branch HQ                          ║
║  METHOD: GET  PATH: /api/v1/inventory?branchId={{branchHQ}} ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: HQ phải có >= 10 batches (master Step 7)        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  [                                                         ║
║    {                                                       ║
║      "id": "uuid-{{batch1}}",   ← CAPTURE                 ║
║      "medicineId":"{{med1}}",                              ║
║      "branchId":"{{branchHQ}}",                            ║
║      "batchNo":"BTH-001",                                  ║
║      "qty": 100, "remainingQty":100,                       ║
║      "expiryDate":"2027-12-31"                             ║
║    }                                                       ║
║  ]                                                         ║
║  ← CAPTURE: id batch đầu tiên → {{batch1}}                ║
║  ← VERIFY: length >= 10                                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.B: List với X-Branch-Id header**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List dùng header X-Branch-Id                        ║
║  METHOD: GET  PATH: /api/v1/inventory                     ║
║  HEADER: X-Branch-Id: {{branchQ1}}                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, chỉ trả về batches của Q1               ║
║  ← VERIFY: mọi item.branchId = {{branchQ1}}                ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.C: Empty list (branch chưa có batch)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List branch không có batch                          ║
║  METHOD: GET  PATH: /api/v1/inventory?branchId=            ║
║                              00000000-0000-0000-0000-      ║
║                              000000000000                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, []                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.2 GET /inventory/{id} — Get batch by id

**Test case 4.2.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get batch by id                                     ║
║  METHOD: GET  PATH: /api/v1/inventory/{{batch1}}          ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{batch1}} từ 4.1.A                              ║
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{batch1}}", "qty":100, "remainingQty":100,      ║
║    "status":"ACTIVE", "minStockLevel":10 }                ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.B: Not found**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get batch UUID giả                                  ║
║  METHOD: GET  PATH: /api/v1/inventory/00000000-0000-0000-  ║
║                              0000-000000000000             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.3 GET /inventory/batches/scan/{code} — Scan barcode

**Test case 4.3.A: Scan barcode hợp lệ**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Quét barcode                                        ║
║  METHOD: GET  PATH: /api/v1/inventory/batches/scan/        ║
║                              {{batch1_barcode}}             ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: batch có barcode="{{batch1_barcode}}"            ║
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{batch1}}", "medicineName":"Paracetamol 500mg",  ║
║    "remainingQty":100, ... }                               ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.3.B: Scan barcode không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Quét barcode lạ                                     ║
║  METHOD: GET  PATH: /api/v1/inventory/batches/scan/        ║
║                              UNKNOWN-BARCODE-999           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.4 GET /inventory/branch/{branchId}/medicine/{medicineId}

**Test case 4.4.A: Tồn kho của 1 thuốc tại 1 chi nhánh**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tra tồn kho Paracetamol tại HQ                      ║
║  METHOD: GET  PATH: /api/v1/inventory/branch/              ║
║                              {{branchHQ}}/medicine/         ║
║                              {{med1}}                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, [≥ 1 batch có medicineId={{med1}}]     ║
║  ← VERIFY: sum(remainingQty) >= 100                       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.4.B: Không có batch**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Thuốc chưa nhập kho HQ                              ║
║  METHOD: GET  PATH: /api/v1/inventory/branch/              ║
║                              {{branchHQ}}/medicine/         ║
║                              {{med5}}                       ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: med5 chưa import vào HQ                          ║
║  EXPECTED: 200 OK, []                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.5 POST /inventory/import — Import

**Test case 4.5.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Nhập kho 100 hộp Paracetamol                        ║
║  METHOD: POST  PATH: /api/v1/inventory/import             ║
║  HEADER:                                                   ║
║    Content-Type  : application/json                        ║
║    X-User-Id     : {{adminId}}                             ║
║    Idempotency-Key: <uuid>          ← CR-05                ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  {                                                         ║
║    "medicineId":"{{med2}}",                                ║
║    "branchId":"{{branchHQ}}",                              ║
║    "batchNo":"BTH-IMP-001",                                ║
║    "barcode":"BC-IMP-001",                                 ║
║    "qty": 100,                                             ║
║    "expiryDate":"2027-12-31",                              ║
║    "supplierId":"{{sup1}}",                                ║
║    "minStockLevel": 20                                     ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"uuid-{{batchImp}}", ← CAPTURE                     ║
║    "qty":100, "remainingQty":100,                           ║
║    "status":"ACTIVE", ... }                                ║
║  ← CAPTURE: id → {{batchImp}}                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.5.B: Idempotency — gọi lại cùng key**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Gọi lại import với cùng Idempotency-Key            ║
║  METHOD: POST  PATH: /api/v1/inventory/import             ║
║  HEADER: Idempotency-Key: <cùng uuid ở 4.5.A>             ║
║  REQUEST: body giống 4.5.A                                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, trả về cùng {{batchImp}} (không tạo mới)║
║  ← VERIFY: id == {{batchImp}}                              ║
║  ← VERIFY: total batches KHÔNG tăng (so với DB)            ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.5.C: Validation — qty = 0**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Import với qty = 0                                  ║
║  METHOD: POST  PATH: /api/v1/inventory/import             ║
║  REQUEST: { ... "qty":0, ... }                             ║
║  EXPECTED: 400 Bad Request, error "qty must be >= 1"       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.5.D: Validation — expiry trong quá khứ**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Import với expiry = 2020-01-01 (đã qua)             ║
║  METHOD: POST  PATH: /api/v1/inventory/import             ║
║  REQUEST: { ... "expiryDate":"2020-01-01", ... }           ║
║  EXPECTED: 400 hoặc 422, error "expiry must be in future"  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.5.E: Medicine không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Import với medicineId giả                          ║
║  METHOD: POST  PATH: /api/v1/inventory/import             ║
║  REQUEST: { "medicineId":"00000000-...", "branchId":"...", ║
║             "batchNo":"X", "qty":1, "expiryDate":"2030-01-01" }║
║  EXPECTED: 422 Unprocessable Entity                        ║
║  ← VERIFY: message chứa "medicine không tồn tại"           ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.6 POST /inventory/export — Export (FIFO)

**Test case 4.6.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Xuất kho 5 hộp Paracetamol (FIFO)                  ║
║  METHOD: POST  PATH: /api/v1/inventory/export             ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  {                                                         ║
║    "medicineId":"{{med1}}",                                ║
║    "branchId":"{{branchHQ}}",                              ║
║    "qty": 5,                                               ║
║    "reason":"SALE"                                         ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "success": true, "remainingQty":95,                    ║
║    "batchId":"{{batch1}}", "operation":"EXPORT_OUT" }     ║
║  ← VERIFY: remainingQty của {{batch1}} giảm 5             ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.B: Validation — qty > remainingQty**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Xuất quá tồn                                        ║
║  METHOD: POST  PATH: /api/v1/inventory/export             ║
║  REQUEST: { "medicineId":"{{med1}}",                       ║
║             "branchId":"{{branchHQ}}", "qty":999999,       ║
║             "reason":"SALE" }                              ║
║  EXPECTED: 422 Unprocessable Entity                        ║
║  ← VERIFY: message "insufficient stock" hoặc "tồn kho không đủ"║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.C: Validation — qty <= 0**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Export qty = 0                                      ║
║  REQUEST: { "medicineId":"{{med1}}", "branchId":"...",     ║
║             "qty":0, "reason":"SALE" }                     ║
║  EXPECTED: 400 Bad Request                                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.7 POST /inventory/consume — Consume (order paid flow)

**Test case 4.7.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tiêu hao 3 hộp cho order đã thanh toán             ║
║  METHOD: POST  PATH: /api/v1/inventory/consume            ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "medicineId":"{{med1}}", "branchId":"{{branchHQ}}",    ║
║    "qty":3, "orderId":"{{orderPaid}}" }                    ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{orderPaid}} là order đã PAID (08-ORDER.md)    ║
║  EXPECTED: 200 OK                                          ║
║  { "success":true, "remainingQty":92, "operation":"CONSUME" }║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.7.B: Insufficient stock**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Consume qty > tồn                                   ║
║  REQUEST: { "medicineId":"{{med1}}", "branchId":"...",     ║
║             "qty":99999, "orderId":"..." }                 ║
║  EXPECTED: 422                                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.8 POST /inventory/transfer — Transfer between branches

**Test case 4.8.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Chuyển 10 hộp từ HQ → Q1                           ║
║  METHOD: POST  PATH: /api/v1/inventory/transfer           ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "medicineId":"{{med1}}",                                ║
║    "fromBranchId":"{{branchHQ}}",                          ║
║    "toBranchId":"{{branchQ1}}",                           ║
║    "qty":10, "reason":"REBALANCE" }                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "success":true,                                        ║
║    "sourceRemainingQty": 85,   // 95 - 10                 ║
║    "destinationAddedQty": 10 }                            ║
║  ← VERIFY: GET /inventory?branchId={{branchQ1}} có thêm   ║
║    1 batch hoặc tăng qty batch Paracetamol Q1             ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.8.B: Transfer về cùng branch**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Transfer HQ → HQ                                    ║
║  REQUEST: { "fromBranchId":"{{branchHQ}}",                 ║
║             "toBranchId":"{{branchHQ}}", ... }            ║
║  EXPECTED: 422, error "from == to" hoặc "khác chi nhánh"   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.8.C: Transfer không đủ tồn**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Transfer 99999 hộp                                 ║
║  REQUEST: { "medicineId":"{{med1}}",                       ║
║             "fromBranchId":"{{branchHQ}}",                 ║
║             "toBranchId":"{{branchQ1}}",                   ║
║             "qty":99999, "reason":"..." }                  ║
║  EXPECTED: 422, error insufficient stock                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.9 POST /inventory/bulk/import (JSON) — Bulk import

**Test case 4.9.A: Happy path — import 3 batches**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bulk import 3 batches bằng JSON array               ║
║  METHOD: POST  PATH: /api/v1/inventory/bulk/import        ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  [                                                         ║
║    { "medicineId":"{{med1}}","branchId":"{{branchQ1}}",   ║
║      "batchNo":"BULK-001","qty":50,                       ║
║      "expiryDate":"2027-06-30","supplierId":"{{sup1}}" }, ║
║    { "medicineId":"{{med2}}","branchId":"{{branchQ1}}",   ║
║      "batchNo":"BULK-002","qty":30,                       ║
║      "expiryDate":"2027-06-30" },                          ║
║    { "medicineId":"{{med1}}","branchId":"{{branchQ7}}",   ║
║      "batchNo":"BULK-003","qty":20,                       ║
║      "expiryDate":"2027-06-30" }                           ║
║  ]                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  {                                                         ║
║    "successCount": 3,                                      ║
║    "failureCount": 0,                                      ║
║    "results": [                                            ║
║      {"batchNo":"BULK-001","status":"SUCCESS",             ║
║       "id":"uuid-...", ... },                              ║
║      ...                                                   ║
║    ]                                                       ║
║  }                                                         ║
║  ← VERIFY: successCount == 3                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.9.B: Partial failure**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bulk import có 1 item sai                          ║
║  REQUEST:                                                  ║
║  [                                                         ║
║    { "medicineId":"{{med1}}","branchId":"{{branchQ1}}",   ║
║      "batchNo":"BULK-OK","qty":10,"expiryDate":"2030-01-01" },║
║    { "medicineId":"00000000-0000-0000-0000-000000000000",  ║
║      "branchId":"{{branchQ1}}","batchNo":"BULK-BAD",      ║
║      "qty":10,"expiryDate":"2030-01-01" }                 ║
║  ]                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (multi-status)                           ║
║  { "successCount":1, "failureCount":1,                    ║
║    "results": [                                            ║
║      {"batchNo":"BULK-OK","status":"SUCCESS"},             ║
║      {"batchNo":"BULK-BAD","status":"FAILED",              ║
║       "error":"Medicine not found"}                        ║
║    ] }                                                     ║
║  ← VERIFY: failureCount > 0, có error message             ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.9.C: Empty array**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bulk import mảng rỗng                               ║
║  REQUEST: []                                               ║
║  EXPECTED: 400, error "items must not be empty"            ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.10 POST /inventory/bulk/import (multipart) — CSV upload

**Test case 4.10.A: Happy path — upload CSV**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload CSV bulk import                              ║
║  METHOD: POST  PATH: /api/v1/inventory/bulk/import         ║
║  HEADER: Content-Type: multipart/form-data                 ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: file=inventory-import.csv       ← UPLOAD       ║
║   (CSV content - 5 dòng hợp lệ)                          ║
║   Format: medicineId,branchId,batchNo,qty,expiryDate       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "successCount":5, "failureCount":0, "results":[...] }   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.10.B: CSV sai format**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload CSV không đúng header                         ║
║  FORM DATA: file=wrong-format.csv (header khác)            ║
║  EXPECTED: 400 hoặc 422, error parse                       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.10.C: Empty file**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload file rỗng                                    ║
║  FORM DATA: file=empty.csv (0 bytes)                       ║
║  EXPECTED: 400, error "file is empty"                      ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.11 POST /inventory/bulk/import-file — Alias endpoint

**Test case 4.11.A: Verify alias works**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload CSV qua alias /bulk/import-file              ║
║  METHOD: POST  PATH: /api/v1/inventory/bulk/import-file    ║
║  HEADER: Content-Type: multipart/form-data                 ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: file=inventory-import.csv                      ║
║  EXPECTED: 200 OK, kết quả giống 4.10.A                   ║
║  ← VERIFY: successCount > 0                               ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.12 GET /inventory/bulk/export — Export CSV

**Test case 4.12.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Export CSV inventory của branch HQ                  ║
║  METHOD: GET  PATH: /api/v1/inventory/bulk/export?        ║
║                              branchId={{branchHQ}}         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║    Content-Type: text/csv                                  ║
║    Content-Disposition: attachment; filename=              ║
║                         inventory-batches.csv              ║
║    (text body, header + ≥ 10 dòng)                        ║
║  ← VERIFY: file mở được bằng Excel                        ║
║  ← VERIFY: header có các cột medicineId,batchNo,qty,...   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.13 GET /inventory/transactions — Audit trail

**Test case 4.13.A: Lấy lịch sử 1 batch**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy transactions của {{batch1}}                    ║
║  METHOD: GET  PATH: /api/v1/inventory/transactions?       ║
║                              batchId={{batch1}}            ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{batch1}} đã có ít nhất 1 transaction từ 4.6    ║
║  EXPECTED: 200 OK                                          ║
║  [ { "type":"EXPORT_OUT", "qty":5,                         ║
║      "occurredAt":"2026-06-19T...", "actorId":"..." } ]   ║
║  ← VERIFY: type thuộc IMPORT/EXPORT/CONSUME/TRANSFER      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.13.B: Batch chưa có transaction**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Transactions của batch mới import                   ║
║  METHOD: GET  PATH: /api/v1/inventory/transactions?       ║
║                              batchId={{batchImp}}          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, []                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.14 GET /inventory/low-stock — Low stock alert

**Test case 4.14.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy danh sách batch tồn thấp                       ║
║  METHOD: GET  PATH: /api/v1/inventory/low-stock            ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: có ít nhất 1 batch với remainingQty <             ║
║           minStockLevel (đã setup ở pre-seed)              ║
║  EXPECTED: 200 OK, [≥ 1 item]                              ║
║  ← VERIFY: remainingQty < minStockLevel cho mỗi item      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.14.B: Empty list (no alert)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Khi không có batch nào dưới ngưỡng                 ║
║  EXPECTED: 200 OK, []                                     ║
║  ← NOTE: chỉ pass khi đã đảm bảo mọi batch                ║
║          remainingQty >= minStockLevel                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.15 GET /inventory/alerts/low-stock — Alias

**Test case 4.15.A: Verify alias**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy low-stock qua alias                              ║
║  METHOD: GET  PATH: /api/v1/inventory/alerts/low-stock     ║
║  EXPECTED: 200 OK, response giống 4.14.A                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.16 GET /inventory/expiring — Expiry alerts

**Test case 4.16.A: Default 30 ngày**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cảnh báo sắp hết hạn (mặc định 30 ngày)           ║
║  METHOD: GET  PATH: /api/v1/inventory/expiring             ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: có batch với expiry 2026-07-15 (< 30 ngày từ   ║
║          2026-06-19)                                       ║
║  EXPECTED: 200 OK, có batch đó trong danh sách             ║
║  ← VERIFY: mỗi item.expiryDate trong vòng 30 ngày tới     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.16.B: Custom days=7**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cảnh báo trong vòng 7 ngày                          ║
║  METHOD: GET  PATH: /api/v1/inventory/expiring?days=7      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, chỉ chứa batch sắp hết hạn trong 7 ngày║
║  ← VERIFY: subset của 4.16.A (nếu 4.16.A có 5 items thì   ║
║           4.16.B có ≤ 5 items)                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.17 GET /inventory/alerts/expiry — Alias

**Test case 4.17.A: Verify alias**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy expiry alerts qua alias                         ║
║  METHOD: GET  PATH: /api/v1/inventory/alerts/expiry        ║
║  EXPECTED: 200 OK, response giống 4.16.A                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.18 GET /inventory/report/stock-level — Stock report

**Test case 4.18.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Báo cáo mức tồn kho branch HQ                      ║
║  METHOD: GET  PATH: /api/v1/inventory/report/stock-level?  ║
║                              branchId={{branchHQ}}         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  [                                                         ║
║    { "medicineId":"{{med1}}",                              ║
║      "medicineName":"Paracetamol 500mg",                   ║
║      "totalRemainingQty": 95,                             ║
║      "batchCount": 1 }, ...                               ║
║  ]                                                         ║
║  ← VERIFY: ≥ 1 medicineId, totalRemainingQty > 0          ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.19 GET /inventory/report/movement — Movement report

**Test case 4.19.A: Happy path — all movements**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Báo cáo biến động kho trong tháng                   ║
║  METHOD: GET  PATH: /api/v1/inventory/report/movement      ║
║                              ?branchId={{branchHQ}}         ║
║                              &fromDate=2026-06-01           ║
║                              &toDate=2026-06-30             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  [ { "type":"IMPORT", "qty":100, "occurredAt":"..." },    ║
║    { "type":"EXPORT_OUT","qty":5,"occurredAt":"..." },   ║
║    { "type":"CONSUME","qty":3,"occurredAt":"..." } ]     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.19.B: Filter by type**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Báo cáo chỉ lấy EXPORT                              ║
║  METHOD: GET  PATH: /api/v1/inventory/report/movement      ║
║                              ?type=EXPORT_OUT&              ║
║                              branchId={{branchHQ}}         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, chỉ chứa items type=EXPORT_OUT         ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Test Cases — OutboxConsumerController (Internal)

> ⚠️ Các endpoint này là INTERNAL API, được order-service gọi qua event bus. Trong dev/UAT có thể gọi thủ công qua gateway để verify. Trong production nên có header `X-Service-Token`.

### 5.1 POST /inventory/orders/{orderId}/paid — Order paid event

**Test case 5.1.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Gửi event order paid                                ║
║  METHOD: POST  PATH: /api/v1/inventory/orders/             ║
║                              {{orderPaid}}/paid             ║
║  HEADER (nếu prod): X-Service-Token: <secret>              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: rỗng hoặc {}                                ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{orderPaid}} là order đã thanh toán ở 08-ORDER  ║
║  EXPECTED: 200 OK                                          ║
║  { "success":true,                                        ║
║    "message":"Inventory consumed for order {{orderPaid}}" }║
║  ← VERIFY: stock tương ứng đã giảm (kiểm tra 4.7)        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.B: Idempotent — gọi 2 lần**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Gọi /paid 2 lần liên tiếp                          ║
║  EXPECTED: cả 2 lần đều 200 OK                             ║
║  ← VERIFY: stock KHÔNG bị trừ 2 lần (idempotent)          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.C: Order không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Gửi event cho order giả                             ║
║  METHOD: POST  PATH: /api/v1/inventory/orders/             ║
║                              00000000-0000-0000-0000-      ║
║                              000000000000/paid              ║
║  EXPECTED: 404 hoặc 422 (tuỳ implementation)               ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 POST /inventory/orders/{orderId}/cancelled — Order cancelled event

**Test case 5.2.A: Happy path — restore stock**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Gửi event order cancelled                          ║
║  METHOD: POST  PATH: /api/v1/inventory/orders/             ║
║                              {{orderPaid}}/cancelled        ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: order đã được consume stock ở 5.1                ║
║  EXPECTED: 200 OK                                          ║
║  { "success":true,                                        ║
║    "message":"Stock restored for order" }                  ║
║  ← VERIFY: stock đã được cộng lại (BR06)                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.B: Cancel order chưa paid (no-op)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel order PENDING_PAYMENT (chưa consume)         ║
║  ← SEED: {{orderPending}} từ 08-ORDER.md                   ║
║  METHOD: POST  PATH: /api/v1/inventory/orders/             ║
║                              {{orderPending}}/cancelled     ║
║  EXPECTED: 200 OK, không thay đổi stock                    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Capture Variables

| Biến             | Mô tả                                       | Step |
|------------------|----------------------------------------------|------|
| `{{batch1}}`     | Batch đầu tiên ở HQ                         | 4.1.A|
| `{{batchImp}}`   | Batch mới import (test)                     | 4.5.A|
| `{{batch1_barcode}}`| Barcode của {{batch1}}                    | 4.3.A|
| `{{orderPaid}}`  | Order UUID đã thanh toán                    | 5.1.A|
| `{{orderPending}}`| Order UUID PENDING_PAYMENT                 | 5.2.B|

---

## 7. Sign-off

| Role          | Name | Date | Signature |
|---------------|------|------|-----------|
| Tester        |      |      |           |
| Dev Lead      |      |      |           |
| Product Owner |      |      |           |

**End of 06-INVENTORY.md** → Next: [`07-CUSTOMER.md`](./07-CUSTOMER.md)
