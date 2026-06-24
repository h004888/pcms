# UAT - Order Service (Coupon + Order + Outbox)

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10
**Service:** order-service (Spring Boot 4.0.7, Java 21)
**Port:** 8088 (internal) — accessed via Gateway `:8080/api/v1/{orders,coupons,admin/outbox}/**`
**Total endpoints covered:** 14 (4 Coupon + 9 Order + 1 OutboxAdmin)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  order-service                                                                   ║
║  ──────────────────────────────────────────────────────────────────────────    ║
║  Module      : Order lifecycle + Coupon management + Outbox admin               ║
║  Database    : pcms_order                                                      ║
║  Auth        : permitAll in dev profile (JWT HS256 in prod)                     ║
║  Lifecycle   : PENDING_PAYMENT → PAID → APPROVED / CANCELLED                    ║
║  Business    : BR04 (bulk discount 5% khi qty ≥ 10), BR05 (FIFO)                ║
║  Outbox      : 1 admin endpoint retry dead-letter events                        ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

### 1.1 CouponController (`/coupons`) — 4 endpoints

| # | Method | Path                | Mô tả                          | Test Status |
|---|--------|---------------------|--------------------------------|-------------|
| 1 | GET    | `/coupons`          | List coupons                   | ☐           |
| 2 | POST   | `/coupons`          | Tạo coupon                     | ☐           |
| 3 | PUT    | `/coupons/{id}`     | Cập nhật                       | ☐           |
| 4 | DELETE | `/coupons/{id}`     | Vô hiệu hoá (soft deactivate) | ☐           |

### 1.2 OrderController (`/orders`) — 9 endpoints

| #  | Method | Path                              | Mô tả                          | Test Status |
|----|--------|-----------------------------------|--------------------------------|-------------|
| 5  | GET    | `/orders`                         | List + filter                  | ☐           |
| 6  | GET    | `/orders/{id}`                    | Chi tiết                       | ☐           |
| 7  | GET    | `/orders/number/{orderNumber}`    | Tra theo orderNumber           | ☐           |
| 8  | POST   | `/orders`                         | Tạo đơn                       | ☐           |
| 9  | PUT    | `/orders/{id}`                    | Cập nhật (PENDING)             | ☐           |
| 10 | PUT    | `/orders/{id}/pay`                | Mark as PAID                   | ☐           |
| 11 | POST   | `/orders/{id}/approve`            | Duyệt đơn                     | ☐           |
| 12 | POST   | `/orders/{id}/reject`             | Từ chối                        | ☐           |
| 13 | DELETE | `/orders/{id}`                    | Huỷ đơn                       | ☐           |

### 1.3 OutboxAdminController (`/admin/outbox`) — 1 endpoint

> ⚠️ **INTERNAL API** — admin retry cho dead-letter outbox events. Trong prod nên có header `X-Service-Token` hoặc được bảo vệ bởi role=ADMIN.

| #  | Method | Path                              | Mô tả                          | Test Status |
|----|--------|-----------------------------------|--------------------------------|-------------|
| 14 | POST   | `/admin/outbox/retry/{id}`        | Retry dead-letter event        | ☐           |

---

## 2. Prerequisites

Xem **[`00-MASTER-PLAN.md`](./00-MASTER-PLAN.md)** §3.

### 2.1 Pre-seed bắt buộc

| Biến              | Mô tả                                   | Nguồn                       |
|-------------------|------------------------------------------|-----------------------------|
| `{{branchHQ}}`    | Branch Headquarter                       | 02-BRANCH.md                |
| `{{branchQ1}}`    | Branch Q1                                | 02-BRANCH.md                |
| `{{cust1}}`       | Customer UUID (B2B)                      | 07-CUSTOMER.md 4.1          |
| `{{med1}}`        | Medicine UUID (Paracetamol)              | 03-CATALOG.md               |
| `{{med2}}`        | Medicine UUID (Amoxicillin)              | 03-CATALOG.md               |
| `{{batch1}}`      | Batch UUID tại HQ (Paracetamol, qty≥100) | 06-INVENTORY.md             |
| `{{adminId}}`     | Admin user UUID                          | 01-AUTH-USER.md             |
| `{{prescId}}`     | Prescription UUID (optional)            | 10-PRESCRIPTION.md          |

### 2.2 Capture variables cho file này

| Biến              | Mô tả                                   | Step              |
|-------------------|------------------------------------------|-------------------|
| `{{coupon1}}`     | Coupon UUID đầu tiên                     | 4.2.A             |
| `{{couponCode}}`  | Coupon code (e.g. "WELCOME10")           | 4.2.A             |
| `{{orderId}}`     | Order UUID vừa tạo                       | 5.1.A             |
| `{{orderNum}}`    | Order number (ORD-2026...)               | 5.1.A             |
| `{{orderPaid}}`   | Order UUID sau khi pay                   | 5.6.A             |
| `{{orderApproved}}`| Order UUID sau khi approve             | 5.7.A             |
| `{{orderRejected}}`| Order UUID sau khi reject              | 5.8.A             |
| `{{outboxId}}`    | Dead-letter outbox event UUID           | 6.1.A             |

---

## 3. Indicator Legend

| Symbol     | Ý nghĩa                                              |
|------------|-------------------------------------------------------|
| ← INPUT    | Dữ liệu gửi trong request                            |
| ← SELECT   | Lựa chọn giữa các options                             |
| ← VERIFY   | Assertion cần kiểm tra trong response                |
| ← SEED     | Điều kiện tiên quyết (data phải tồn tại)             |
| ← CAPTURE  | Lưu giá trị thành biến để dùng step sau              |

---

## 4. Test Cases — CouponController

### 4.1 GET /coupons — List coupons

**Test case 4.1.A: List happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List coupons                                        ║
║  METHOD: GET  PATH: /api/v1/coupons                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  [ { "id":"...", "code":"WELCOME10", "couponType":"PERCENT",║
║      "value":10, "active":true, ... } ]                  ║
║  ← VERIFY: response là array (không null)                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.B: List khi chưa có coupon**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List khi DB rỗng                                   ║
║  ← SEED: chưa tạo coupon nào                              ║
║  EXPECTED: 200 OK, []                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.2 POST /coupons — Create

**Test case 4.2.A: Happy path — PERCENT coupon**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo coupon PERCENT 10%                              ║
║  METHOD: POST  PATH: /api/v1/coupons                       ║
║  HEADER: Content-Type: application/json                   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                  ← INPUT                         ║
║  {                                                         ║
║    "code":"WELCOME10",                                     ║
║    "description":"Giảm 10% cho khách mới",                ║
║    "couponType":"PERCENT",                                 ║
║    "value": 10,                                            ║
║    "validFrom":"2026-06-01T00:00:00",                      ║
║    "validTo":"2026-12-31T23:59:59",                        ║
║    "maxUses": 1000                                         ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201 Created                                     ║
║  { "id":"uuid-{{coupon1}}",   ← CAPTURE                  ║
║    "code":"WELCOME10",       ← CAPTURE                    ║
║    "couponType":"PERCENT", "value":10, "active":true }    ║
║  ← CAPTURE: id → {{coupon1}}, code → {{couponCode}}        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.B: Happy path — FIXED coupon**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo coupon FIXED 50000 VND                          ║
║  REQUEST:                                                  ║
║  { "code":"FIXED50K",                                      ║
║    "description":"Giảm 50K cho đơn từ 200K",               ║
║    "couponType":"FIXED",                                   ║
║    "value":50000,                                          ║
║    "validFrom":"2026-06-01T00:00:00",                      ║
║    "validTo":"2026-12-31T23:59:59",                        ║
║    "maxUses":500 }                                         ║
║  EXPECTED: 201 Created                                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.C: Validation — thiếu code**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Coupon thiếu code                                   ║
║  REQUEST: { "code":"", "description":"X",                  ║
║             "couponType":"PERCENT", "value":10, ... }      ║
║  EXPECTED: 400, error "Mã coupon không được để trống"       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.D: Validation — value = 0**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Coupon value = 0                                    ║
║  REQUEST: { "code":"X", "description":"X", "couponType":   ║
║             "PERCENT", "value":0, "validFrom":"...",        ║
║             "validTo":"..." }                              ║
║  EXPECTED: 400, error "Giá trị coupon phải lớn hơn 0"     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.E: Duplicate code**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo coupon trùng code                              ║
║  ← SEED: {{couponCode}} đã tồn tại ở 4.2.A                ║
║  REQUEST: { "code":"{{couponCode}}", "description":"Dup",  ║
║             "couponType":"PERCENT","value":5, ... }       ║
║  EXPECTED: 409 Conflict                                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.F: Validation — validFrom > validTo**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: validFrom > validTo                                 ║
║  REQUEST: { ..., "validFrom":"2026-12-31T00:00:00",        ║
║             "validTo":"2026-06-01T00:00:00" }              ║
║  EXPECTED: 400 hoặc 422, error "validFrom must be <        ║
║           validTo"                                         ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.3 PUT /coupons/{id} — Update

**Test case 4.3.A: Happy path — tăng maxUses**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cập nhật coupon                                     ║
║  METHOD: PUT  PATH: /api/v1/coupons/{{coupon1}}           ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "description":"Giảm 10% (cập nhật)",                    ║
║    "value":15,                                             ║
║    "maxUses":2000,                                         ║
║    "validFrom":"2026-06-01T00:00:00",                      ║
║    "validTo":"2027-06-30T23:59:59" }                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{coupon1}}", "description":"Giảm 10% (cập nhật)",║
║    "value":15, "maxUses":2000 }                            ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.3.B: Update không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update UUID giả                                     ║
║  METHOD: PUT  PATH: /api/v1/coupons/00000000-0000-0000-    ║
║                              0000-000000000000             ║
║  REQUEST: { "description":"X" }                            ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.4 DELETE /coupons/{id} — Deactivate

**Test case 4.4.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Vô hiệu hoá coupon                                  ║
║  METHOD: DELETE  PATH: /api/v1/coupons/{{coupon1}}         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 204 No Content                                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.4.B: Verify deactivated**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify coupon đã deactivate                        ║
║  METHOD: GET  PATH: /api/v1/coupons                        ║
║  ← VERIFY: response không còn {{coupon1}}                  ║
║    (hoặc có nhưng active=false)                            ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.4.C: Delete UUID giả**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Delete UUID giả                                     ║
║  METHOD: DELETE  PATH: /api/v1/coupons/00000000-...        ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Test Cases — OrderController

### 5.1 POST /orders — Create order

**Test case 5.1.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order mới                                      ║
║  METHOD: POST  PATH: /api/v1/orders                        ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                  ← INPUT                         ║
║  {                                                         ║
║    "customerId":"{{cust1}}",                               ║
║    "branchId":"{{branchHQ}}",                              ║
║    "staffId":"{{adminId}}",                                ║
║    "items":[                                               ║
║      { "medicineId":"{{med1}}", "qty":2, "unitPrice":5000 },║
║      { "medicineId":"{{med2}}", "qty":1, "unitPrice":8000 }║
║    ],                                                      ║
║    "couponCode": null                                      ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201 Created                                     ║
║  { "id":"uuid-{{orderId}}",   ← CAPTURE                   ║
║    "orderNumber":"ORD-20260619-0001",  ← CAPTURE          ║
║    "status":"PENDING_PAYMENT",                             ║
║    "subtotal":18000, "discount":0, "total":18000,          ║
║    "createdAt":"2026-06-19T..." }                          ║
╠══════════════════════════════════════════════════════════════╣
║  ← CAPTURE: id → {{orderId}}, orderNumber → {{orderNum}}   ║
║  ← VERIFY: status = PENDING_PAYMENT                        ║
║  ← VERIFY: total = subtotal - discount                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.B: Order có coupon**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order với coupon                                ║
║  ← SEED: tạo coupon mới PERCENT 10 ở 4.2.A (re-run)     ║
║  REQUEST:                                                  ║
║  { "customerId":"{{cust1}}", "branchId":"{{branchHQ}}",    ║
║    "items":[{"medicineId":"{{med1}}","qty":2,              ║
║              "unitPrice":5000}],                           ║
║    "couponCode":"{{couponCode}}" }                         ║
║  EXPECTED: 201 Created                                     ║
║  { "subtotal":10000, "discount":1000, "total":9000 }      ║
║  ← VERIFY: discount = subtotal * 10%                       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.C: Validation — items rỗng**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order không có items                            ║
║  REQUEST: { "customerId":"{{cust1}}", "branchId":"...",    ║
║             "items":[] }                                   ║
║  EXPECTED: 400, error "items must not be empty"            ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.D: Validation — qty âm**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order với qty = -1                              ║
║  REQUEST: { ..., "items":[{"medicineId":"{{med1}}",        ║
║                             "qty":-1,"unitPrice":5000}] }  ║
║  EXPECTED: 400, error "qty must be > 0"                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.E: BR04 — bulk discount qty >= 10**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Order 10 hộp Paracetamol (BR04 discount 5%)        ║
║  REQUEST:                                                  ║
║  { "customerId":"{{cust1}}", "branchId":"{{branchHQ}}",    ║
║    "items":[{"medicineId":"{{med1}}","qty":10,             ║
║              "unitPrice":5000}] }                          ║
║  EXPECTED: 201 Created                                     ║
║  { "subtotal":50000, "discount":2500, "total":47500 }      ║
║  ← VERIFY: discount = 50000 * 0.05 (5% theo BR04)         ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.F: Customer không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order với customerId giả                       ║
║  REQUEST: { "customerId":"00000000-...", "branchId":"...",  ║
║             "items":[...] }                                ║
║  EXPECTED: 422, error "customer không tồn tại"             ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.G: Insufficient stock (qty > tồn)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Order qty > tồn kho                                ║
║  ← SEED: {{batch1}} có remainingQty = 95                  ║
║  REQUEST:                                                  ║
║  { "customerId":"{{cust1}}", "branchId":"{{branchHQ}}",    ║
║    "items":[{"medicineId":"{{med1}}","qty":999,            ║
║              "unitPrice":5000}] }                          ║
║  EXPECTED: 422 Unprocessable Entity                        ║
║  ← VERIFY: message "insufficient stock"                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.H: Coupon không hợp lệ / hết hạn**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Order với couponCode không tồn tại                  ║
║  REQUEST: { ..., "couponCode":"FAKE-XYZ-123" }             ║
║  EXPECTED: 422, error "coupon not found or expired"        ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 GET /orders — List + filter

**Test case 5.2.A: List all**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List orders                                         ║
║  METHOD: GET  PATH: /api/v1/orders                         ║
║  QUERY: page=0&size=20                                    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "content":[ { "id":"{{orderId}}", "status":"PENDING_   ║
║                  PAYMENT", ... } ], ... }                  ║
║  ← VERIFY: content chứa {{orderId}}                        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.B: Filter by customerId**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Filter theo customer                                ║
║  METHOD: GET  PATH: /api/v1/orders?customerId={{cust1}}    ║
║  EXPECTED: 200 OK, tất cả items.customerId = {{cust1}}     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.C: Filter by status**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Filter status=PENDING_PAYMENT                       ║
║  METHOD: GET  PATH: /api/v1/orders?status=PENDING_PAYMENT  ║
║  EXPECTED: 200 OK, chỉ chứa items status=PENDING_PAYMENT   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.D: Filter by date range**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Filter date 2026-06-01 → 2026-06-30                 ║
║  METHOD: GET  PATH: /api/v1/orders?dateFrom=2026-06-01     ║
║                              &dateTo=2026-06-30            ║
║  EXPECTED: 200 OK, tất cả createdAt trong khoảng           ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 GET /orders/{id} — Get by id

**Test case 5.3.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get order by id                                     ║
║  METHOD: GET  PATH: /api/v1/orders/{{orderId}}             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{orderId}}", "orderNumber":"{{orderNum}}",        ║
║    "status":"PENDING_PAYMENT", "items":[...], "total":... } ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.3.B: Not found**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get UUID giả                                        ║
║  METHOD: GET  PATH: /api/v1/orders/00000000-0000-0000-      ║
║                              0000-000000000000             ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.4 GET /orders/number/{orderNumber} — Get by order number

**Test case 5.4.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by order number                                 ║
║  METHOD: GET  PATH: /api/v1/orders/number/{{orderNum}}     ║
║  EXPECTED: 200 OK, id = {{orderId}}                        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.4.B: Order number không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get order number lạ                                 ║
║  METHOD: GET  PATH: /api/v1/orders/number/ORD-2099-9999    ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.5 PUT /orders/{id} — Update (PENDING only)

**Test case 5.5.A: Happy path — đổi qty**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update qty của order PENDING                        ║
║  METHOD: PUT  PATH: /api/v1/orders/{{orderId}}             ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "items":[{"medicineId":"{{med1}}","qty":3,              ║
║              "unitPrice":5000}] }                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{orderId}}", "subtotal":15000, "total":15000,    ║
║    "status":"PENDING_PAYMENT" }                            ║
║  ← VERIFY: items[0].qty = 3                                ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.5.B: Update order đã PAID (422)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update order đã thanh toán                          ║
║  ← SEED: đã chạy 5.6 ({{orderPaid}})                      ║
║  METHOD: PUT  PATH: /api/v1/orders/{{orderPaid}}           ║
║  REQUEST: { "items":[...] }                                ║
║  EXPECTED: 422 Unprocessable Entity                        ║
║  ← VERIFY: message "chỉ update được order PENDING_PAYMENT" ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.5.C: Update không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update UUID giả                                     ║
║  METHOD: PUT  PATH: /api/v1/orders/00000000-...            ║
║  REQUEST: { "items":[...] }                                ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.6 PUT /orders/{id}/pay — Mark as paid

**Test case 5.6.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Mark order as PAID (gọi bởi payment-service)       ║
║  METHOD: PUT  PATH: /api/v1/orders/{{orderId}}/pay        ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"uuid-{{orderPaid}}", ← CAPTURE                    ║
║    "status":"PAID",                                        ║
║    "paidAt":"2026-06-19T..." }                              ║
║  ← CAPTURE: id → {{orderPaid}}                             ║
║  ← VERIFY: status = PAID (không còn PENDING_PAYMENT)       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.6.B: Pay order đã PAID (idempotent hoặc 422)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pay lại order đã PAID                               ║
║  METHOD: PUT  PATH: /api/v1/orders/{{orderPaid}}/pay      ║
║  EXPECTED: 200 OK (idempotent) HOẶC 422                     ║
║  ← VERIFY: không có side effect lần 2                      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.6.C: Pay order không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pay UUID giả                                        ║
║  METHOD: PUT  PATH: /api/v1/orders/00000000-.../pay       ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.7 POST /orders/{id}/approve — Approve (cho đơn có toa)

**Test case 5.7.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Duyệt order (có prescriptionId)                    ║
║  ← SEED: tạo order mới có prescriptionId ở 5.1            ║
║  ← SEED: gọi 5.6 /pay để có orderPaid với toa             ║
║  METHOD: POST  PATH: /api/v1/orders/{{orderPaid}}/approve  ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"uuid-{{orderApproved}}", ← CAPTURE                ║
║    "status":"APPROVED",                                    ║
║    "approvedAt":"2026-06-19T...",                          ║
║    "approvedBy":"{{adminId}}" }                            ║
║  ← CAPTURE: id → {{orderApproved}}                         ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.7.B: Approve order PENDING (chưa thanh toán)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Approve order chưa PAID                             ║
║  ← SEED: tạo order mới (chưa chạy 5.6) ở 5.1             ║
║  METHOD: POST  PATH: /api/v1/orders/{{orderId}}/approve   ║
║  EXPECTED: 422, error "order must be PAID before approve"   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.7.C: Approve order không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Approve UUID giả                                    ║
║  METHOD: POST  PATH: /api/v1/orders/00000000-.../approve  ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.8 POST /orders/{id}/reject — Reject

**Test case 5.8.A: Happy path — từ chối order PAID**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Từ chối order đã thanh toán                         ║
║  ← SEED: tạo + pay 1 order mới ở 5.1 + 5.6                ║
║  METHOD: POST  PATH: /api/v1/orders/{{orderPaid2}}/reject  ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"uuid-{{orderRejected}}", ← CAPTURE                ║
║    "status":"REJECTED",                                    ║
║    "rejectedAt":"2026-06-19T...",                          ║
║    "rejectedBy":"{{adminId}}",                             ║
║    "rejectionReason":"..." }                               ║
║  ← CAPTURE: id → {{orderRejected}}                         ║
║  ← VERIFY: stock đã được restore về batch (BR06)           ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.8.B: Reject với lý do trong body (optional param)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Reject có lý do                                     ║
║  REQUEST BODY (optional):                                  ║
║  { "reason":"Prescription expired" }                       ║
║  EXPECTED: 200 OK, rejectionReason="Prescription expired"  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.8.C: Reject order PENDING**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Reject order chưa thanh toán                        ║
║  ← SEED: tạo order mới (chưa pay)                         ║
║  METHOD: POST  PATH: /api/v1/orders/{{orderPending}}/reject║
║  EXPECTED: 422, error "order must be PAID before reject"    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.9 DELETE /orders/{id} — Cancel

**Test case 5.9.A: Happy path — huỷ order PENDING**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Huỷ order PENDING_PAYMENT                           ║
║  ← SEED: tạo order mới ở 5.1                              ║
║  METHOD: DELETE  PATH: /api/v1/orders/{{orderId}}          ║
║  HEADER: X-User-Id: {{adminId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{orderId}}", "status":"CANCELLED" }              ║
║  ← VERIFY: status = CANCELLED                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.9.B: Cancel order đã APPROVED (422)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel order APPROVED                               ║
║  ← SEED: {{orderApproved}} từ 5.7.A                        ║
║  METHOD: DELETE  PATH: /api/v1/orders/{{orderApproved}}    ║
║  EXPECTED: 422, error "cannot cancel APPROVED order"        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.9.C: Cancel không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel UUID giả                                     ║
║  METHOD: DELETE  PATH: /api/v1/orders/00000000-...         ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Test Cases — OutboxAdminController (Internal)

> ⚠️ Endpoint admin — trong dev có thể gọi qua gateway. Trong prod nên có `X-Service-Token` header hoặc yêu cầu role=ADMIN.

### 6.1 POST /admin/outbox/retry/{id} — Retry dead-letter

**Test case 6.1.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Retry dead-letter outbox event                      ║
║  ← SEED: có 1 outbox event ở trạng thái DEAD_LETTER        ║
║           (mô phỏng bằng cách fail payment → outbox retry) ║
║  METHOD: POST  PATH: /api/v1/admin/outbox/retry/{{outboxId}}║
║  HEADER (prod): X-Service-Token: <secret>                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  {                                                         ║
║    "message": "Outbox event requeued successfully",        ║
║    "outboxEventId": "uuid-{{newOutboxId}}"                 ║
║  }                                                         ║
║  ← VERIFY: newOutboxId khác {{outboxId}}                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 6.1.B: Retry outbox không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Retry UUID giả                                      ║
║  METHOD: POST  PATH: /api/v1/admin/outbox/retry/00000000-  ║
║                              0000-0000-0000-000000000000   ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 6.1.C: Retry event không phải DEAD_LETTER**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Retry event PENDING (chưa fail)                     ║
║  ← SEED: tạo outbox event mới (status=PENDING)             ║
║  METHOD: POST  PATH: /api/v1/admin/outbox/retry/{{pendingOutboxId}}║
║  EXPECTED: 422 Unprocessable Entity                        ║
║  ← VERIFY: message "chỉ retry event ở trạng thái          ║
║            DEAD_LETTER"                                    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Capture Variables (chốt)

| Biến              | Mô tả                                       | Step      |
|-------------------|----------------------------------------------|-----------|
| `{{coupon1}}`     | Coupon UUID đầu tiên                         | 4.2.A     |
| `{{couponCode}}`  | "WELCOME10"                                  | 4.2.A     |
| `{{orderId}}`     | Order UUID mới tạo                           | 5.1.A     |
| `{{orderNum}}`    | "ORD-20260619-0001"                          | 5.1.A     |
| `{{orderPaid}}`   | Order UUID sau khi pay                       | 5.6.A     |
| `{{orderApproved}}`| Order UUID sau approve                      | 5.7.A     |
| `{{orderRejected}}`| Order UUID sau reject                      | 5.8.A     |
| `{{outboxId}}`    | Outbox event DEAD_LETTER                     | 6.1.A     |

---

## 8. End-to-End mini flow

```
╔════════════════════════════════════════════════════════════════════╗
║  HAPPY PATH: Create → Pay → Approve                                ║
╠════════════════════════════════════════════════════════════════════╣
║  1. POST /orders                              → orderId, PENDING   ║
║  2. PUT /orders/{orderId} (optional update)  → same id            ║
║  3. PUT /orders/{orderId}/pay                → PAID               ║
║  4. (Outbox auto) POST /inventory/orders/{orderId}/paid           ║
║     → stock consumed FIFO                                          ║
║  5. POST /orders/{orderId}/approve           → APPROVED           ║
║  6. (Outbox auto) POST /customers/{cust}/points/add                ║
║     → BR07 loyalty points awarded                                  ║
╚════════════════════════════════════════════════════════════════════╝

╔════════════════════════════════════════════════════════════════════╗
║  CANCEL PATH: Create → Pay → Reject (stock restored)              ║
╠════════════════════════════════════════════════════════════════════╣
║  1. POST /orders                              → PENDING            ║
║  2. PUT /orders/{orderId}/pay                → PAID               ║
║  3. POST /orders/{orderId}/reject            → REJECTED           ║
║  4. (Outbox auto) POST /inventory/orders/{orderId}/cancelled      ║
║     → stock restored (BR06)                                        ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 9. Sign-off

| Role          | Name | Date | Signature |
|---------------|------|------|-----------|
| Tester        |      |      |           |
| Dev Lead      |      |      |           |
| Product Owner |      |      |           |

**End of 08-ORDER.md** → Next: [`09-PAYMENT.md`](./09-PAYMENT.md)
