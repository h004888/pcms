# UAT - Customer Service

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10
**Service:** customer-service (Spring Boot 4.0.7, Java 21)
**Port:** 8087 (internal) — accessed via Gateway `:8080/api/v1/customers/**`
**Total endpoints covered:** 14 (11 CustomerController + 3 CustomerPortalController)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  customer-service                                                              ║
║  ──────────────────────────────────────────────────────────────────────────    ║
║  Module      : Customer Profile (B2B) + Customer Portal (B2C self-service)    ║
║  Database    : pcms_customer                                                  ║
║  Auth        : permitAll in dev profile (JWT HS256 in prod)                     ║
║  Loyalty     : BR07 — 1 point / 1000 VND order total                          ║
║  Tier        : BRONZE / SILVER / GOLD / PLATINUM                              ║
║  Code format : CUST-yyyy#### (auto-generated)                                  ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

### 1.1 CustomerController (`/customers`) — B2B admin endpoints (11)

| #  | Method | Path                              | Mô tả                          | Test Status |
|----|--------|-----------------------------------|--------------------------------|-------------|
| 1  | GET    | `/customers`                      | List + search + paginate       | ☐           |
| 2  | GET    | `/customers/{id}`                 | Chi tiết                       | ☐           |
| 3  | GET    | `/customers/phone/{phone}`        | Tra theo SĐT                   | ☐           |
| 4  | GET    | `/customers/code/{code}`          | Tra theo mã CUST-...           | ☐           |
| 5  | GET    | `/customers/{id}/tier`            | Hạng thành viên                | ☐           |
| 6  | GET    | `/customers/{id}/orders`          | Lịch sử đơn hàng              | ☐           |
| 7  | GET    | `/customers/{id}/points`          | Lịch sử điểm thưởng            | ☐           |
| 8  | GET    | `/customers/{id}/history`         | Activity history (combined)    | ☐           |
| 9  | POST   | `/customers`                      | Tạo mới (admin)                | ☐           |
| 10 | PUT    | `/customers/{id}`                 | Cập nhật                       | ☐           |
| 11 | DELETE | `/customers/{id}`                 | Soft delete                    | ☐           |
| 12 | PUT    | `/customers/{id}/points/add`      | Cộng điểm (internal/payment)   | ☐           |

> Note: GET /code/{code} có trong controller nhưng không nằm trong scope brief gốc — vẫn cover ở §4.4 vì nó tồn tại.

### 1.2 CustomerPortalController (`/customers`) — B2C self-service (3)

| #  | Method | Path                  | Mô tả                          | Test Status |
|----|--------|-----------------------|--------------------------------|-------------|
| 13 | POST   | `/customers/register` | Self-register (public)         | ☐           |
| 14 | GET    | `/customers/me`       | Lấy profile của tôi            | ☐           |
| 15 | PUT    | `/customers/me`       | Cập nhật profile của tôi       | ☐           |

---

## 2. Prerequisites

Xem **[`00-MASTER-PLAN.md`](./00-MASTER-PLAN.md)** §3.

### 2.1 Pre-seed yêu cầu

| Biến          | Mô tả                              | Nguồn                       |
|---------------|-------------------------------------|-----------------------------|
| `{{adminId}}` | Admin user UUID                     | 01-AUTH-USER.md             |
| `{{orderPaid}}`| Order đã thanh toán (optional)     | 08-ORDER.md                 |

> Customer-service **KHÔNG** có FK đến service khác, có thể test độc lập mà không cần seed users/branches/medicines.

### 2.2 Capture variables

| Biến            | Mô tả                              | Step              |
|-----------------|-------------------------------------|-------------------|
| `{{cust1}}`     | Customer UUID đầu tiên (B2B)        | 4.1.A             |
| `{{custPhone}}` | Phone number unique                 | 4.1.A             |
| `{{custCode}}`  | "CUST-2026####"                    | 4.1.A             |
| `{{portalCust}}`| B2C portal customer UUID            | 5.1.A             |
| `{{portalEmail}}`| "<customer@pcms.vn>"                 | 5.1.A             |

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

## 4. Test Cases — CustomerController (B2B)

### 4.1 POST /customers — Create customer (B2B)

**Test case 4.1.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo customer mới (B2B)                              ║
║  METHOD: POST  PATH: /api/v1/customers                    ║
║  HEADER: Content-Type: application/json                   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  {                                                         ║
║    "name":"Nguyen Van A",                                 ║
║    "phone":"0901234567",                                  ║
║    "email":"a@example.com",                               ║
║    "address":"123 Le Loi, District 1, HCMC",              ║
║    "dob":"1990-01-15",                                    ║
║    "gender":"MALE"                                        ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201 Created                                     ║
║  {                                                         ║
║    "id":"uuid-{{cust1}}",      ← CAPTURE                  ║
║    "code":"CUST-20260001",    ← CAPTURE (auto-gen)        ║
║    "name":"Nguyen Van A",                                  ║
║    "phone":"0901234567",         ← CAPTURE                 ║
║    "email":"a@example.com",                                ║
║    "tier":"BRONZE",                                        ║
║    "points":0,                                             ║
║    "createdAt":"2026-06-19T..."                            ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  ← CAPTURE: id, code, phone                                ║
║  ← VERIFY: code match pattern CUST-yyyy####                ║
║  ← VERIFY: tier = BRONZE (default)                        ║
║  ← VERIFY: points = 0                                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.B: Validation — thiếu name**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo customer thiếu name                            ║
║  REQUEST: { "name":"", "phone":"0901111111" }             ║
║  EXPECTED: 400, error "Tên khách hàng không được để trống" ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.C: Validation — thiếu phone**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo customer thiếu phone                           ║
║  REQUEST: { "name":"Test", "phone":"" }                   ║
║  EXPECTED: 400, error "Số điện thoại không được để trống"  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.D: Duplicate phone**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo customer với phone đã tồn tại                   ║
║  REQUEST: { "name":"Trung", "phone":"{{custPhone}}" }     ║
║  ← SEED: {{custPhone}} từ 4.1.A                           ║
║  EXPECTED: 409 Conflict                                    ║
║  ← VERIFY: message chứa "SĐT đã tồn tại" hoặc tương tự   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.E: Minimal payload (chỉ name + phone)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo customer tối thiểu                              ║
║  REQUEST: { "name":"Min Test", "phone":"0902222222" }     ║
║  EXPECTED: 201 Created                                     ║
║  ← VERIFY: email=null, address=null, dob=null, gender=null║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.2 GET /customers — List + search

**Test case 4.2.A: List happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List customers                                      ║
║  METHOD: GET  PATH: /api/v1/customers                     ║
║  QUERY: page=0&size=20                                    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  {                                                         ║
║    "content":[ {"id":"...","name":"...",...} ],            ║
║    "page":0, "size":20, "totalElements":3,                 ║
║    "totalPages":1                                          ║
║  }                                                         ║
║  ← VERIFY: content là array (không null)                   ║
║  ← VERIFY: totalElements >= 3 (từ master Step 8)          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.B: Search by name/phone**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search "Nguyen"                                     ║
║  METHOD: GET  PATH: /api/v1/customers?search=Nguyen        ║
║  EXPECTED: 200 OK, content chỉ chứa customer có tên "Nguyen"║
║  ← VERIFY: mỗi item.name chứa "Nguyen" (case-insensitive) ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.C: Search no match**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search "ZZZNoMatch"                                 ║
║  METHOD: GET  PATH: /api/v1/customers?search=ZZZNoMatch    ║
║  EXPECTED: 200 OK, content=[]                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.D: Pagination**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Page 0 size 2                                       ║
║  METHOD: GET  PATH: /api/v1/customers?page=0&size=2        ║
║  EXPECTED: 200 OK                                          ║
║  ← VERIFY: content.length = 2                              ║
║  ← VERIFY: totalElements > 2 (để verify totalPages > 1)    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.3 GET /customers/{id} — Get by id

**Test case 4.3.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get customer by id                                  ║
║  METHOD: GET  PATH: /api/v1/customers/{{cust1}}            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{cust1}}", "name":"Nguyen Van A",                ║
║    "phone":"{{custPhone}}", "tier":"BRONZE",               ║
║    "points":0, ... }                                       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.3.B: Not found**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get UUID giả                                        ║
║  METHOD: GET  PATH: /api/v1/customers/00000000-0000-0000-  ║
║                              0000-000000000000             ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.4 GET /customers/phone/{phone} — Get by phone

**Test case 4.4.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by phone                                        ║
║  METHOD: GET  PATH: /api/v1/customers/phone/{{custPhone}}  ║
║  EXPECTED: 200 OK, id = {{cust1}}                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.4.B: Phone không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by phone lạ                                     ║
║  METHOD: GET  PATH: /api/v1/customers/phone/0000000000     ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.5 GET /customers/code/{code} — Get by code (membership card)

**Test case 4.5.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by code                                         ║
║  METHOD: GET  PATH: /api/v1/customers/code/{{custCode}}    ║
║  EXPECTED: 200 OK, id = {{cust1}}                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.5.B: Code không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by code lạ                                      ║
║  METHOD: GET  PATH: /api/v1/customers/code/CUST-99999999   ║
║  EXPECTED: 404 Not Found (MSG31)                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.6 GET /customers/{id}/tier — Get tier

**Test case 4.6.A: Happy path — customer mới = BRONZE**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy tier của customer mới                           ║
║  METHOD: GET  PATH: /api/v1/customers/{{cust1}}/tier       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "customerId":"{{cust1}}", "tier":"BRONZE" }             ║
║  ← VERIFY: tier ∈ {BRONZE, SILVER, GOLD, PLATINUM}        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.B: Customer không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy tier UUID giả                                   ║
║  METHOD: GET  PATH: /api/v1/customers/00000000-.../tier    ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.7 GET /customers/{id}/orders — Order history

**Test case 4.7.A: Happy path — customer có orders**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy lịch sử đơn hàng                               ║
║  METHOD: GET  PATH: /api/v1/customers/{{cust1}}/orders     ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{cust1}} đã có ít nhất 1 order ở 08-ORDER       ║
║  EXPECTED: 200 OK                                          ║
║  { "content":[                                             ║
║      {"orderId":"...","orderNumber":"ORD-2026...","total":50000,"status":"PAID"},║
║    ...                                                     ║
║  ], "totalElements":>=1 }                                  ║
║  ← VERIFY: mỗi item có orderId, orderNumber, total        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.7.B: Customer chưa có order**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer mới tạo, chưa order                        ║
║  ← SEED: tạo customer mới ở 4.1.E (Min Test)             ║
║  METHOD: GET  PATH: /api/v1/customers/{{custMin}}/orders  ║
║  EXPECTED: 200 OK, content=[], totalElements=0             ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.8 GET /customers/{id}/points — Loyalty points

**Test case 4.8.A: Happy path — chưa có giao dịch**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy lịch sử điểm của customer mới                   ║
║  METHOD: GET  PATH: /api/v1/customers/{{cust1}}/points     ║
║  EXPECTED: 200 OK                                          ║
║  { "content":[], "totalElements":0, ... }                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.8.B: Sau khi cộng điểm (4.12)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy lịch sử điểm sau khi add points                 ║
║  ← SEED: đã chạy 4.12.A (add 5 points)                    ║
║  METHOD: GET  PATH: /api/v1/customers/{{cust1}}/points     ║
║  EXPECTED: 200 OK, content[].points = +5, reason chứa     ║
║           "ORDER_PAID" hoặc text đã truyền                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.9 GET /customers/{id}/history — Combined history

**Test case 4.9.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy activity history tổng hợp                      ║
║  METHOD: GET  PATH: /api/v1/customers/{{cust1}}/history    ║
║  QUERY: page=0&size=20                                    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  {                                                         ║
║    "customer":{"id":"{{cust1}}","name":"Nguyen Van A",...},║
║    "orders":{"content":[...],"totalElements":0},           ║
║    "points":{"content":[...],"totalElements":0}            ║
║  }                                                         ║
║  ← VERIFY: có 3 phần: customer, orders, points            ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.10 PUT /customers/{id} — Update

**Test case 4.10.A: Happy path — đổi địa chỉ**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cập nhật địa chỉ customer                          ║
║  METHOD: PUT  PATH: /api/v1/customers/{{cust1}}            ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "name":"Nguyen Van A (renamed)",                        ║
║    "phone":"{{custPhone}}",                                ║
║    "email":"a-new@example.com",                            ║
║    "address":"456 Nguyen Trai, District 5, HCMC",         ║
║    "gender":"FEMALE" }                                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{cust1}}", "name":"Nguyen Van A (renamed)",      ║
║    "email":"a-new@example.com", "address":"456..." }      ║
║  ← VERIFY: updatedAt > createdAt                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.10.B: Update không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update UUID giả                                     ║
║  METHOD: PUT  PATH: /api/v1/customers/00000000-0000-0000-  ║
║                              0000-000000000000             ║
║  REQUEST: { "name":"X", "phone":"0999999999" }             ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.10.C: Update phone trùng customer khác**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update phone thành phone của customer khác           ║
║  ← SEED: tạo thêm customer B ở 4.1 (phone 0909876543)     ║
║  METHOD: PUT  PATH: /api/v1/customers/{{cust1}}            ║
║  REQUEST: { "name":"A", "phone":"0909876543" }             ║
║  EXPECTED: 409 Conflict                                    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.11 DELETE /customers/{id} — Soft delete

**Test case 4.11.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Xoá mềm customer                                   ║
║  METHOD: DELETE  PATH: /api/v1/customers/{{custMin}}       ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{custMin}} = "Min Test" từ 4.1.E                 ║
║  EXPECTED: 204 No Content                                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.11.B: Verify soft delete**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: GET customer đã xoá                                 ║
║  METHOD: GET  PATH: /api/v1/customers/{{custMin}}          ║
║  EXPECTED: 200 OK (vẫn trả về) nhưng status = INACTIVE    ║
║  ← VERIFY: status = "INACTIVE" hoặc tương đương            ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.11.C: Delete UUID giả**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Delete UUID giả                                     ║
║  METHOD: DELETE  PATH: /api/v1/customers/00000000-...      ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.12 PUT /customers/{id}/points/add — Add loyalty points

> Endpoint này được payment-service gọi sau khi order PAID (BR07). Trong dev/UAT gọi trực tiếp để verify.

**Test case 4.12.A: Add 5 points happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cộng 5 điểm cho customer                           ║
║  METHOD: PUT  PATH: /api/v1/customers/{{cust1}}/points/add║
║  HEADER: Content-Type: application/json                   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "points":5,                                             ║
║    "refOrderId":"{{orderPaid}}",                           ║
║    "reason":"ORDER_PAID:ORD-20260619-0001" }              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "customerId":"{{cust1}}", "addedPoints":5,              ║
║    "totalPoints":5 }                                       ║
║  ← VERIFY: totalPoints = previous + 5                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.12.B: Idempotency — gọi lại cùng refOrderId**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Gọi lại add points cùng refOrderId                  ║
║  METHOD: PUT  PATH: /api/v1/customers/{{cust1}}/points/add║
║  REQUEST: { "points":5, "refOrderId":"{{orderPaid}}" }    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "addedPoints":0, "totalPoints":5 }                       ║
║  ← VERIFY: totalPoints KHÔNG tăng (vẫn = 5)               ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.12.C: Add điểm âm (redeem)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Trừ 2 điểm (redeem voucher)                         ║
║  METHOD: PUT  PATH: /api/v1/customers/{{cust1}}/points/add║
║  REQUEST: { "points":-2, "refOrderId":"{{redeemId}}",      ║
║            "reason":"REDEEM:VOUCHER" }                    ║
║  EXPECTED: 200 OK, totalPoints = 3                        ║
║  ← VERIFY: totalPoints >= 0 (không âm)                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.12.D: Trừ điểm vượt quá tổng**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Trừ 999999 điểm                                     ║
║  METHOD: PUT  PATH: /api/v1/customers/{{cust1}}/points/add║
║  REQUEST: { "points":-999999, "reason":"REDEEM" }          ║
║  EXPECTED: 422 Unprocessable Entity                        ║
║  ← VERIFY: message "insufficient points"                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.12.E: Customer không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Add points cho UUID giả                             ║
║  METHOD: PUT  PATH: /api/v1/customers/00000000-.../       ║
║                              points/add                    ║
║  REQUEST: { "points":5 }                                   ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Test Cases — CustomerPortalController (B2C)

> ⚠️ Đây là endpoint PUBLIC (cho phép khách hàng tự đăng ký mà không cần JWT). Trong prod cần rate-limit.

### 5.1 POST /customers/register — Self-register

**Test case 5.1.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Khách tự đăng ký                                    ║
║  METHOD: POST  PATH: /api/v1/customers/register           ║
║  HEADER: Content-Type: application/json                   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                  ← INPUT                         ║
║  {                                                         ║
║    "email":"customer@pcms.vn",                             ║
║    "password":"customer123",                               ║
║    "name":"Nguyen Van Customer",                           ║
║    "phone":"0901234567",                                   ║
║    "dob":"1995-05-20"                                      ║
║  }                                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201 Created                                     ║
║  { "id":"uuid-{{portalCust}}",     ← CAPTURE              ║
║    "code":"CUST-20260002",                                 ║
║    "email":"customer@pcms.vn",     ← CAPTURE               ║
║    "name":"Nguyen Van Customer",                           ║
║    "tier":"BRONZE", "points":0 }                          ║
╠══════════════════════════════════════════════════════════════╣
║  ← CAPTURE: id → {{portalCust}}, email → {{portalEmail}}   ║
║  ← VERIFY: response KHÔNG chứa password (security)        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.B: Duplicate email**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Register với email đã tồn tại                       ║
║  REQUEST: { "email":"customer@pcms.vn", "password":"x",    ║
║             "name":"Dup", "phone":"0901111111" }           ║
║  EXPECTED: 409 Conflict                                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.C: Validation — password ngắn**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Register password 3 ký tự                            ║
║  REQUEST: { "email":"new@x.com", "password":"abc",         ║
║             "name":"Test", "phone":"0911111111" }          ║
║  EXPECTED: 400, error "password must be at least 8 chars"  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.D: Validation — thiếu email/name/phone**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Register thiếu email                                ║
║  REQUEST: { "password":"x12345678", "name":"X",            ║
║             "phone":"0911111111" }                         ║
║  EXPECTED: 400, error "email is required"                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 GET /customers/me — Lấy profile của tôi

> Portal controller nhận customerId qua header `X-Customer-Id` (set bởi gateway sau khi xác thực JWT portal) HOẶC `X-User-Email` HOẶC query param `?customerId=`.

**Test case 5.2.A: Happy path — dùng X-Customer-Id**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy profile của tôi                                 ║
║  METHOD: GET  PATH: /api/v1/customers/me                   ║
║  HEADER: X-Customer-Id: {{portalCust}}                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{portalCust}}", "email":"customer@pcms.vn",       ║
║    "name":"Nguyen Van Customer", "tier":"BRONZE", ... }    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.B: Happy path — dùng X-User-Email**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy profile theo email                              ║
║  METHOD: GET  PATH: /api/v1/customers/me                   ║
║  HEADER: X-User-Email: customer@pcms.vn                    ║
║  EXPECTED: 200 OK, id = {{portalCust}}                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.C: Fallback — query param customerId**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lấy profile qua query param (dev only)               ║
║  METHOD: GET  PATH: /api/v1/customers/me?customerId=       ║
║                              {{portalCust}}                ║
║  EXPECTED: 200 OK, id = {{portalCust}}                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.D: Thiếu cả 3 identifier**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: GET /me không truyền identifier                     ║
║  METHOD: GET  PATH: /api/v1/customers/me                   ║
║  EXPECTED: 400 hoặc 422, error "Customer self-context      ║
║           is missing"                                      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.E: Customer không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: /me với customerId giả                              ║
║  METHOD: GET  PATH: /api/v1/customers/me                   ║
║  HEADER: X-Customer-Id: 00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 PUT /customers/me — Update profile của tôi

**Test case 5.3.A: Happy path — đổi địa chỉ**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cập nhật profile của tôi                            ║
║  METHOD: PUT  PATH: /api/v1/customers/me                   ║
║  HEADER: X-Customer-Id: {{portalCust}}                     ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "email":"customer@pcms.vn", "password":"customer123",   ║
║    "name":"Nguyen Van Customer",                           ║
║    "phone":"0901234567",                                   ║
║    "address":"789 Pham Ngu Lao, District 1, HCMC" }        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{portalCust}}", "address":"789 Pham Ngu Lao...",  ║
║    "updatedAt":"2026-06-19T..." }                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.3.B: Update qua email**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update profile dùng X-User-Email                    ║
║  METHOD: PUT  PATH: /api/v1/customers/me                   ║
║  HEADER: X-User-Email: customer@pcms.vn                    ║
║  REQUEST: { "name":"New Name", ... }                       ║
║  EXPECTED: 200 OK, id = {{portalCust}}                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.3.C: Thiếu identifier (giống 5.2.D)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: PUT /me không truyền identifier                     ║
║  METHOD: PUT  PATH: /api/v1/customers/me                   ║
║  REQUEST: { "name":"X" }                                   ║
║  EXPECTED: 400 hoặc 422                                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.3.D: Validation — password quá yếu**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update với password mới quá ngắn                    ║
║  REQUEST: { ..., "password":"123" }                        ║
║  EXPECTED: 400, error validation                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Capture Variables

| Biến              | Mô tả                                       | Step    |
|-------------------|----------------------------------------------|---------|
| `{{cust1}}`       | Customer B2B đầu tiên (Nguyen Van A)         | 4.1.A   |
| `{{custPhone}}`   | "0901234567"                                 | 4.1.A   |
| `{{custCode}}`    | "CUST-20260001"                              | 4.1.A   |
| `{{custMin}}`     | Customer tối thiểu (Min Test)                | 4.1.E   |
| `{{portalCust}}`  | B2C portal customer UUID                     | 5.1.A   |
| `{{portalEmail}}` | "<customer@pcms.vn>"                           | 5.1.A   |

---

## 7. Sign-off

| Role          | Name | Date | Signature |
|---------------|------|------|-----------|
| Tester        |      |      |           |
| Dev Lead      |      |      |           |
| Product Owner |      |      |           |

**End of 07-CUSTOMER.md** → Next: [`08-ORDER.md`](./08-ORDER.md)
