# UAT - Cross-Service E2E Flows

**Version:** 1.0  **Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10 (commit `63b9397`)
**Test type:** End-to-End integration (multi-service, RabbitMQ outbox + REST chains)
**Coverage:** 6 E2E flows × 4 services (B2B/B2C/AI) = 24 service-touch points

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  PCMS - Cross-Service E2E Test Suite                                           ║
║  Goal    : Validate message/event flow giữa các service qua                  ║
║            RabbitMQ outbox + REST calls                                        ║
║  Strategy: Mỗi flow có 1 happy path + 1 rollback path                        ║
║  Stack   : 15 Java microservices + 1 Python AI + RabbitMQ + Redis            ║
║  Auth    : permitAll in dev (JWT HS256 in prod - bypass cho test)            ║
║  Outbox  : Idempotent nhờ X-Outbox-Event-Id header                          ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

## 1. Flow Summary

| # | Flow ID | Name                                            | Services Touched                                       | Est. | Status |
|---|---------|-------------------------------------------------|--------------------------------------------------------|------:|:------:|
| 1 | E2E-01  | Order → Payment → Inventory → Notification     | order, payment, inventory, notification              | 25m   | ☐      |
| 2 | E2E-02  | Prescription → Sign → Link Order               | prescription, order, ai-engine (OCR optional)        | 30m   | ☐      |
| 3 | E2E-03  | B2C Cart → Checkout → Order → Payment          | customer-portal, order, payment, notification         | 30m   | ☐      |
| 4 | E2E-04  | Outbox: low-stock alert                        | inventory, notification                               | 15m   | ☐      |
| 5 | E2E-05  | B2C Consult + AI drug-check                    | pharmacist-workbench, ai-engine                       | 25m   | ☐      |
| 6 | E2E-06  | Vaccine booking flow                           | customer-portal, notification                         | 20m   | ☐      |
|   |         | **TOTAL**                                       |                                                        | **~2.5h** |   |

## 2. Prerequisites

### 2.1 Environment

| Resource      | URL / Port                  | Status |
|---------------|-----------------------------|:------:|
| API Gateway   | `http://localhost:8080`    | ☐      |
| Eureka        | `http://localhost:8761`    | ☐      |
| AI Engine     | `http://localhost:8095`    | ☐      |
| RabbitMQ      | `localhost:5672`           | ☐      |
| MySQL 8       | `localhost:3306` (root/root)| ☐      |
| PostgreSQL    | `localhost:5432` (pgvector) | ☐      |
| Redis 7       | `localhost:6379`           | ☐      |
| OPENAI_API_KEY| env var present            | ☐      |

### 2.2 Pre-seeded data (từ các UAT files 01-18)

| Variable | Description | Source |
|----------|-------------|--------|
| `{{branchHQ}}` | Branch Headquarter | 02-BRANCH |
| `{{med1}}`-`{{med3}}` | Medicine UUIDs (Paracetamol, Amoxicillin, Vitamin C) | 03-CATALOG |
| `{{cust1}}` | B2B customer UUID | 07-CUSTOMER |
| `{{customerId}}` | B2C customer UUID | 07-CUSTOMER |
| `{{customerToken}}` | B2C customer JWT | 07-CUSTOMER |
| `{{adminId}}` | Admin/Manager user | 01-AUTH-USER |
| `{{pharmacistId}}` | Pharmacist user | 14-PHARMACIST-WORKBENCH |
| `{{pharmacistToken}}` | Pharmacist JWT | 14-PHARMACIST-WORKBENCH |
| `{{batch1}}` | Inventory batch HQ (qty≥100) | 06-INVENTORY |
| `{{batchLow}}` | Inventory batch HQ qty=5 (low-stock) | 06-INVENTORY |
| `{{managerId}}` | Branch manager (notification target) | 01-AUTH-USER |
| `{{vacc1}}`, `{{vacc2}}` | Vaccine UUIDs | (seed) |

### 2.3 Indicator Legend

| Symbol | Meaning |
|--------|---------|
| ← INPUT | Data gửi trong request |
| ← VERIFY | Assertion cần kiểm tra trong response |
| ← CAPTURE | Lưu giá trị thành biến cho step sau |
| ← SEED | Pre-condition (data phải tồn tại) |
| ← ROLLBACK | Cleanup step khi flow fails |
| ← ASYNC | Long-running - poll hoặc đợi listener |
| ← ENV | Requires environment variable |

## 3. ASCII Timeline Conventions

Mỗi flow có 1 timeline diagram:

```
T+Xs   [actor]    → action                          ← status
T+Xs   [service]  → side-effect                     ✓
```

Trong đó `✓` = pass, `✗` = fail, `⚠` = warning.

---

# ===========================================================================

# FLOW 1 / E2E-01: Order → Payment → Inventory → Notification

# ===========================================================================

### 1.1 Objective

Validate end-to-end B2B flow: tạo order → process payment → consume inventory → emit notification tới customer. Kiểm thử 4 service + RabbitMQ outbox.

### 1.2 Services Touched

| # | Service | Port | Role | DB |
|---|---------|------|------|----|
| 1 | order-service | 8088 | Tạo order, mark PAID | pcms_order |
| 2 | payment-service | 8089 | Xử lý payment, fire outbox | pcms_payment |
| 3 | notification-service | 8091 | Nhận outbox, gửi notif | pcms_notification |
| 4 | inventory-service | 8086 | Trừ stock FIFO | pcms_inventory |

### 1.3 Pre-flight

```
☐ RabbitMQ có exchange `pcms.outbox`
☐ notification-service subscribed queue `notification.orders.paid`
☐ inventory-service có {{batch1}} remainingQty ≥ 100
☐ Customer {{cust1}} có contact (phone/email)
```

### 1.4 ASCII Timeline

```
T+0s   [user]    → POST /api/v1/orders              ← INPUT
T+0.3s [order]   → persist PENDING_PAYMENT          ✓
T+0.5s [order]   → outbox "order.created"           ✓
T+1s   [user]    → POST /api/v1/payments            ← INPUT
T+1.5s [payment] → persist payment SUCCESS          ✓
T+1.7s [order]   → consume → mark PAID              ✓
T+1.8s [order]   → HTTP /inventory/consume          ✓
T+2s   [inventory] → FIFO trừ stock                ✓
T+2.5s [notif]   → consume "orders.paid"            ✓
T+3s   [notif]   → send notification                ✓
```

### 1.5 Step-by-Step — Happy Path

**Step 1.5.1: Tạo order**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order B2B                       ← INPUT         ║
║  METHOD: POST  PATH: /api/v1/orders                          ║
║  SERVICE: order-service                                       ║
║  HEADER: Authorization: Bearer {{accessToken}} (manager)     ║
║          Idempotency-Key: <uuid>                              ║
║  REQUEST:                                                     ║
║  { "customerId":"{{cust1}}", "branchId":"{{branchHQ}}",      ║
║    "staffId":"{{adminId}}",                                   ║
║    "items":[{"medicineId":"{{med1}}","qty":2,"unitPrice":5000},║
║             {"medicineId":"{{med2}}","qty":1,"unitPrice":8000}],║
║    "couponCode":null }                                         ║
║  EXPECTED: 201 Created → status=PENDING_PAYMENT, total=18000  ║
║  ← CAPTURE: orderId, orderNumber                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 1.5.2: Tạo payment**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Initiate payment                     ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/payments                         ║
║  SERVICE: payment-service                                     ║
║  REQUEST:                                                     ║
║  { "orderId":"{{orderId}}","customerId":"{{cust1}}",         ║
║    "branchId":"{{branchHQ}}","method":"CASH",                ║
║    "amount":18000,"currency":"VND" }                           ║
║  EXPECTED: 201 Created → status=SUCCESS                      ║
║  ← CAPTURE: paymentId                                         ║
║  ← ASYNC: outbox "orders.paid" published                      ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 1.5.3: Verify order auto-PAID**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Poll cho order PAID                  ← ASYNC          ║
║  METHOD: GET  PATH: /api/v1/orders/{{orderId}}                ║
║  SERVICE: order-service                                       ║
║  ← ASYNC: poll tối đa 10s                                     ║
║  EXPECTED: 200 OK → status=PAID, paidAt set                   ║
║  ← VERIFY: status=PAID                                        ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 1.5.4: Verify inventory consumed (FIFO)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check stock bị trừ                  ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/inventory?branchId={{branchHQ}}   ║
║           &medicineId={{med1}}                                ║
║  SERVICE: inventory-service                                   ║
║  EXPECTED: 200 OK → remainingQty=98 (giảm đúng qty=2)        ║
║  ← VERIFY: FIFO trừ từ batch cũ nhất                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 1.5.5: Verify notification delivered**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check customer received "Order confirmed"              ║
║  METHOD: GET  PATH: /api/v1/notifications?customerId={{cust1}}║
║  SERVICE: notification-service                                ║
║  ← ASYNC: poll tối đa 15s                                     ║
║  EXPECTED: 200 OK → type=ORDER_PAID, status=SENT              ║
║  ← VERIFY: type=ORDER_PAID, orderId match                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 1.5.6: Idempotency test**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Replay outbox event (RabbitMQ redelivery simulation)  ║
║  METHOD: POST  PATH: /api/v1/notifications/orders/paid        ║
║  HEADER: X-Outbox-Event-Id: <same uuid as 1.5.2>              ║
║  SERVICE: notification-service                                ║
║  EXPECTED: 200 OK → status=duplicate, notificationId=null    ║
║  ← VERIFY: KHÔNG tạo notification mới (count vẫn = 1)        ║
╚══════════════════════════════════════════════════════════════╝
```

### 1.6 Rollback Path

```
╔══════════════════════════════════════════════════════════════╗
║  SCENARIO: Payment FAILED → order vẫn PENDING_PAYMENT         ║
╠══════════════════════════════════════════════════════════════╣
║  R1.1: Tạo order (giống 1.5.1) → CAPTURE orderId_RO2          ║
║  R1.2: Payment với method=FAKE_DECLINE                        ║
║    REQUEST: { "orderId":"{{orderId_RO2}}", "method":"CARD",   ║
║      "amount":18000, "cardToken":"tok_test_decline" }         ║
║    EXPECTED: 402 Payment Required                              ║
║  R1.3: GET /api/v1/orders/{{orderId_RO2}}                     ║
║    EXPECTED: status = PENDING_PAYMENT (không PAID)             ║
║  R1.4: GET /api/v1/inventory?medicineId={{med1}}              ║
║    EXPECTED: remainingQty KHÔNG giảm                           ║
║  R1.5: GET /api/v1/notifications?customerId={{cust1}}         ║
║    EXPECTED: KHÔNG có ORDER_PAID cho orderId_RO2              ║
║  ← ROLLBACK: DELETE /api/v1/orders/{{orderId_RO2}}            ║
╚══════════════════════════════════════════════════════════════╝
```

### 1.7 Verification Checkpoints

| Check | Expected | Pass? |
|-------|----------|:-----:|
| Order created PENDING_PAYMENT | 201 + correct JSON | ☐ |
| Outbox "order.created" published | row status=PUBLISHED | ☐ |
| Payment SUCCESS | 201 + paymentId | ☐ |
| Order auto-PAID within 10s | status=PAID | ☐ |
| Inventory FIFO consumed | remainingQty -2 | ☐ |
| Notification ORDER_PAID delivered | type + SENT | ☐ |
| Idempotency: replay → duplicate | no new notif | ☐ |
| Rollback: payment fail → order vẫn PENDING | stock intact | ☐ |

### 1.8 Cleanup

```bash
PUT /api/v1/inventory/{{batch1}}/adjust { "deltaQty":2, "reason":"E2E-01 cleanup" }
DELETE /api/v1/orders/{{orderId}}
```

---

# ===========================================================================

# FLOW 2 / E2E-02: Prescription → Sign → Link Order

# ===========================================================================

### 2.1 Objective

Validate luồng bán thuốc theo đơn: tạo prescription → ký số SHA-256 → link tới order đã tạo.

### 2.2 Services Touched

| # | Service | Port | Role | DB |
|---|---------|------|------|----|
| 1 | prescription-service | 8090 | Tạo, ký số, link order | pcms_prescription |
| 2 | order-service | 8088 | Tạo order với prescriptionId ref | pcms_order |
| 3 | ai-engine (optional) | 8095 | OCR scan đơn (offline) | pgvector |

### 2.3 ASCII Timeline

```
T+0s    [doctor]  → POST /api/v1/prescriptions/draft    ← INPUT
T+0.5s  [presc]   → persist DRAFT                        ✓
T+1s    [doctor]  → POST /prescriptions/{{id}}/sign      ← SIGN
T+1.2s  [presc]   → SHA-256 hash, status=SIGNED          ✓
T+1.5s  [doctor]  → POST /api/v1/orders (prescriptionId) ← INPUT
T+2s    [order]   → persist with Rx ref                  ✓
T+2.5s  [doctor]  → POST /prescriptions/{{id}}/link-order
T+2.7s  [presc]   → set prescription.orderId             ✓
T+3s    [user]    → GET /prescriptions/{{id}}            ← VERIFY
```

### 2.4 Step-by-Step — Happy Path

**Step 2.4.1: Tạo draft prescription**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bác sĩ tạo đơn thuốc (DRAFT)        ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/prescriptions/draft             ║
║  SERVICE: prescription-service                                ║
║  HEADER: Authorization: Bearer {{accessToken}}               ║
║          X-User-Id    : {{doctorId}}                          ║
║  REQUEST:                                                     ║
║  { "patientId":"{{patientId}}","doctorId":"{{doctorId}}",   ║
║    "diagnosis":"Viêm họng cấp","notes":"Uống sau ăn",        ║
║    "medicines":[{"medicineId":"{{med_presc}}",               ║
║      "dosage":"500mg","frequency":"3 lần/ngày",               ║
║      "durationDays":7,"quantity":21}]}                        ║
║  EXPECTED: 201 Created → status=DRAFT, code auto-generated   ║
║  ← CAPTURE: prescId                                           ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 2.4.2: Ký số prescription**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Ký số (DRAFT → SIGNED)               ← SIGN           ║
║  METHOD: PUT  PATH: /api/v1/prescriptions/{{prescId}}/sign   ║
║  SERVICE: prescription-service                                ║
║  HEADER: X-User-Id: {{doctorId}}                              ║
║  REQUEST: { "signedBy":"{{doctorId}}",                        ║
║    "signedAt":"2026-06-19T11:00:00", "algorithm":"SHA-256" } ║
║  EXPECTED: 200 OK → status=SIGNED, signatureHash (64 hex)    ║
║  ← VERIFY: hash length=64, status=SIGNED                      ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 2.4.3: Tạo order có prescriptionId ref**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo order có prescriptionId ref     ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/orders                          ║
║  SERVICE: order-service                                       ║
║  REQUEST:                                                     ║
║  { "customerId":"{{patientId}}","branchId":"{{branchHQ}}",  ║
║    "staffId":"{{adminId}}",                                   ║
║    "prescriptionId":"{{prescId}}", ← cross-service ref       ║
║    "items":[{"medicineId":"{{med_presc}}",                   ║
║      "qty":21,"unitPrice":12000}]}                            ║
║  EXPECTED: 201 Created → status=PENDING_PAYMENT, total=252000║
║  ← CAPTURE: orderId_RX                                        ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 2.4.4: Link prescription → order**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Set prescription.orderId           ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{prescId}}/      ║
║           link-order?orderId={{orderId_RX}}                   ║
║  SERVICE: prescription-service                                ║
║  EXPECTED: 200 OK → orderId field populated                  ║
║  ← VERIFY: prescription.orderId = orderId_RX                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 2.4.5: Verify bi-directional link**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify both sides thấy nhau        ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/prescriptions/{{prescId}}         ║
║           + GET /api/v1/orders/{{orderId_RX}}                  ║
║  EXPECTED: prescription.orderId=orderId_RX,                   ║
║            order.prescriptionId=prescId                       ║
║  ← VERIFY: bi-directional reference ✓                         ║
╚══════════════════════════════════════════════════════════════╝
```

### 2.5 Rollback Path

```
╔══════════════════════════════════════════════════════════════╗
║  SCENARIO: Link DRAFT prescription (bị reject)                ║
╠══════════════════════════════════════════════════════════════╣
║  R2.1: Tạo prescription DRAFT mới → CAPTURE prescId_DRAFT     ║
║  R2.2: POST /prescriptions/{{prescId_DRAFT}}/link-order       ║
║    EXPECTED: 422 Unprocessable Entity                          ║
║    ← VERIFY: "Cannot link DRAFT, must SIGN first"             ║
║  R2.3: Sign (giống 2.4.2) → status=SIGNED                    ║
║  R2.4: Re-link → EXPECTED: 200 OK                            ║
║  ← ROLLBACK: DELETE /api/v1/prescriptions/{{prescId_DRAFT}}  ║
║    DELETE /api/v1/orders/{{orderId_RX}}                       ║
╚══════════════════════════════════════════════════════════════╝
```

### 2.6 Verification Checkpoints

| Check | Expected | Pass? |
|-------|----------|:-----:|
| Draft created, code=RX-yyyy#### auto-gen | 201 + DRAFT | ☐ |
| Sign attaches signatureHash (64 hex) | 200 + SIGNED | ☐ |
| Order có prescriptionId ref | 201 + echoed | ☐ |
| Link sets prescription.orderId | 200 + populated | ☐ |
| Bi-directional link | both GETs thấy nhau | ☐ |
| Rollback: link DRAFT rejected | 422 | ☐ |

### 2.7 Cleanup

```bash
DELETE /api/v1/prescriptions/{{prescId}}
DELETE /api/v1/orders/{{orderId_RX}}
```

---

# ===========================================================================

# FLOW 3 / E2E-03: B2C Cart → Checkout → Order → Payment

# ===========================================================================

### 3.1 Objective

Validate B2C mua hàng: customer thêm cart → preview total → confirm → order tạo tự động → payment → notification.

### 3.2 Services Touched

| # | Service | Port | Role | DB |
|---|---------|------|------|----|
| 1 | customer-portal-service | 8093 | Cart, checkout | pcms_portal |
| 2 | order-service | 8088 | Tạo order từ checkout | pcms_order |
| 3 | payment-service | 8089 | B2C payment | pcms_payment |
| 4 | notification-service | 8091 | Notif cho B2C customer | pcms_notification |

### 3.3 ASCII Timeline

```
T+0s   [customer] → POST /api/v1/cart/items               ← INPUT
T+0.3s [portal]   → persist cart_item                      ✓
T+1s   [customer] → POST /api/v1/cart/items (x2)          ← INPUT
T+2s   [customer] → POST /api/v1/cart/checkout/preview     ← INPUT
T+2.3s [portal]   → calculate total                        ✓
T+3s   [customer] → POST /api/v1/cart/checkout/confirm     ← INPUT
T+3.5s [portal]   → call order-service POST /orders        ✓
T+4s   [order]    → persist PENDING_PAYMENT                ✓
T+4.5s [customer] → POST /api/v1/payments                  ← INPUT
T+5s   [payment]  → process B2C payment                    ✓
T+5.5s [notif]    → "Order confirmed" push                  ✓
```

### 3.4 Step-by-Step — Happy Path

**Step 3.4.1: Login B2C**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Login B2C customer                  ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/auth/login                      ║
║  SERVICE: user-service                                        ║
║  REQUEST: { "email":"customer@pcms.vn",                      ║
║    "password":"customer123" }                                  ║
║  EXPECTED: 200 OK → accessToken, userId, role=CUSTOMER        ║
║  ← CAPTURE: customerToken, customerId                        ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 3.4.2: Add items to cart**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Add items to cart                   ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/cart/items                      ║
║  SERVICE: customer-portal-service                            ║
║  HEADER: Authorization: Bearer {{customerToken}}              ║
║          X-Customer-Id: {{customerId}}                        ║
║  REQUEST: { "medicineId":"{{med1}}", "qty":2,                ║
║    "branchId":"{{branchHQ}}" }                                ║
║  EXPECTED: 201 Created → cartId, itemId, lineTotal=10000     ║
║  ← CAPTURE: cartId, cartItemId1                              ║
║  ← REPEAT cho {{med3}} qty=1 → CAPTURE cartItemId2           ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 3.4.3: Update cart item qty**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Sửa qty Vitamin C → 3               ← INPUT          ║
║  METHOD: PUT  PATH: /api/v1/cart/items/{{cartItemId2}}        ║
║  SERVICE: customer-portal-service                            ║
║  REQUEST: { "qty":3 }                                         ║
║  EXPECTED: 200 OK, qty updated                                ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 3.4.4: Preview checkout**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tính tổng trước khi confirm        ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/cart/checkout/preview            ║
║  SERVICE: customer-portal-service                            ║
║  REQUEST: { "cartId":"{{cartId}}",                           ║
║    "addressId":"{{addressId}}",                               ║
║    "shippingMethod":"STANDARD" }                              ║
║  EXPECTED: 200 OK → subtotal=19000, discount=10000,          ║
║    shippingFee=15000, total=24000                            ║
║  ← VERIFY: total = subtotal-discount+shippingFee              ║
║  ← VERIFY: KHÔNG tạo order (chỉ preview)                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 3.4.5: Confirm checkout → tạo order**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Confirm (tạo order tại order-service)                 ║
║  METHOD: POST  PATH: /api/v1/cart/checkout/confirm            ║
║  SERVICE: customer-portal-service                            ║
║  HEADER: Authorization: Bearer {{customerToken}}              ║
║          Idempotency-Key: <uuid>                              ║
║  REQUEST: { "cartId":"{{cartId}}","addressId":"{{addressId}}",║
║    "paymentMethod":"CASH_ON_DELIVERY" }                       ║
║  EXPECTED: 200 OK → orderId, status=PENDING_PAYMENT,          ║
║    nextAction=REDIRECT_TO_PAYMENT                              ║
║  ← CAPTURE: orderId_C, orderNum_C                             ║
║  ← VERIFY: order tạo tại order-service (cross-service)        ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 3.4.6: B2C payment**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Thanh toán B2C                       ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/payments                         ║
║  SERVICE: payment-service                                     ║
║  HEADER: Authorization: Bearer {{customerToken}} (B2C allowed)║
║          X-Customer-Id: {{customerId}}                        ║
║  REQUEST: { "orderId":"{{orderId_C}}",                        ║
║    "customerId":"{{customerId}}",                             ║
║    "branchId":"{{branchHQ}}","method":"CASH_ON_DELIVERY",     ║
║    "amount":24000,"currency":"VND" }                          ║
║  EXPECTED: 201 Created → status=PENDING (COD)                 ║
║  ← CAPTURE: paymentId_C                                       ║
║  ← ASYNC: COD flips to SUCCESS khi giao                       ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 3.4.7: Verify B2C customer thấy order**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: B2C customer xem order             ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/orders/me?status=PENDING_PAYMENT ║
║  SERVICE: customer-portal-service                            ║
║  EXPECTED: 200 OK → orders[] chứa orderId_C                  ║
║  ← VERIFY: orderId_C appears in customer's list               ║
╚══════════════════════════════════════════════════════════════╝
```

### 3.5 Rollback Path

```
╔══════════════════════════════════════════════════════════════╗
║  SCENARIO: Out-of-stock → checkout fail                       ║
╠══════════════════════════════════════════════════════════════╣
║  R3.1: Add item out-of-stock                                  ║
║    POST /api/v1/cart/items { "medicineId":"{{medOut}}","qty":1 }║
║    EXPECTED: 201 Created (cart add OK, validate ở preview)    ║
║  R3.2: Preview                                                 ║
║    POST /api/v1/cart/checkout/preview                          ║
║    EXPECTED: 422 "Medicine {{medOut}} out of stock"           ║
║  R3.3: Try confirm → EXPECTED: 422, no orderId               ║
║  R3.4: Remove OOS item                                        ║
║    DELETE /api/v1/cart/items/{{cartItemIdOut}}                 ║
║  ← ROLLBACK: DELETE /api/v1/orders/{{orderId_C}} (nếu đã tạo)║
╚══════════════════════════════════════════════════════════════╝
```

### 3.6 Verification Checkpoints

| Check | Expected | Pass? |
|-------|----------|:-----:|
| Login B2C returns JWT | 200 + accessToken | ☐ |
| Add 2 items to cart (201 each) | cartId + itemIds | ☐ |
| Update qty reflects in cart | new qty visible | ☐ |
| Preview math correct | total = subtotal-disc+ship | ☐ |
| Confirm tạo order tại order-service | orderId + PENDING | ☐ |
| B2C payment accepted | 201 + paymentId | ☐ |
| /orders/me shows customer's order | appears in list | ☐ |
| Rollback: OOS blocked at preview | 422 + no order | ☐ |

### 3.7 Cleanup

```bash
DELETE /api/v1/cart/items/{{cartItemId1}}
DELETE /api/v1/cart/items/{{cartItemId2}}
DELETE /api/v1/orders/{{orderId_C}}
DELETE /api/v1/payments/{{paymentId_C}}
```

---

# ===========================================================================

# FLOW 4 / E2E-04: Outbox Low-Stock Alert

# ===========================================================================

### 4.1 Objective

Validate outbox chain: inventory detect low-stock → fire event → notification-service consume → alert manager.

### 4.2 Services Touched

| # | Service | Port | Role | DB |
|---|---------|------|------|----|
| 1 | inventory-service | 8086 | Detect low-stock, fire outbox | pcms_inventory |
| 2 | notification-service | 8091 | Consume event, send alert | pcms_notification |

### 4.3 ASCII Timeline

```
T+0s   [user]    → POST /api/v1/inventory/{{batchLow}}/consume?qty=2
T+0.3s [inventory] → trừ stock, qty=3
T+0.5s [inventory] → check 3 < minStock 20 → outbox event
T+0.6s [inventory] → publish RabbitMQ
T+1s   [notif]   → consume event from queue
T+1.3s [notif]   → send via EMAIL/SMS
```

### 4.4 Step-by-Step — Happy Path

**Step 4.4.1: Verify pre-state**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify batch ở dưới min            ← SEED            ║
║  METHOD: GET  PATH: /api/v1/inventory/{{batchLow}}            ║
║  SERVICE: inventory-service                                   ║
║  EXPECTED: 200 OK → remainingQty=5, minStockLevel=20         ║
║  ← VERIFY: remainingQty < minStockLevel                       ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 4.4.2: Consume thêm stock**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Consume 2 đơn vị (qty=5-2=3, vẫn dưới min)           ║
║  METHOD: POST  PATH: /api/v1/inventory/{{batchLow}}/consume   ║
║  SERVICE: inventory-service                                   ║
║  REQUEST: { "qty":2, "reason":"E2E-04 test consume" }         ║
║  EXPECTED: 200 OK → remainingQty=3, lowStockTriggered=true   ║
║  ← ASYNC: outbox event published                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 4.4.3: Verify outbox event**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check outbox_event table            ← VERIFY          ║
║  SERVICE: inventory-service (internal)                        ║
║  Query:                                                       ║
║    SELECT id, status, payload FROM outbox_event               ║
║    WHERE aggregate_id='{{batchLow}}'                          ║
║    AND event_type='inventory.low-stock'                       ║
║    ORDER BY created_at DESC LIMIT 1;                          ║
║  EXPECTED: status=PUBLISHED within 5s                         ║
║    payload: { "branchId":"{{branchHQ}}",                      ║
║      "medicineId":"{{med1}}", "qtyOnHand":3, "minQty":20,    ║
║      "recipientId":"{{managerId}}" }                          ║
║  ← CAPTURE: outboxEventId                                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 4.4.4: Verify notification delivered**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Manager check notification inbox   ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/notifications?userId={{managerId}}║
║  SERVICE: notification-service                                ║
║  ← ASYNC: poll tối đa 10s                                     ║
║  EXPECTED: 200 OK → type=LOW_STOCK_ALERT, status=SENT         ║
║    title: "Cảnh báo tồn kho thấp - Paracetamol 500mg"         ║
║    body : "Còn 3/20 tại chi nhánh HQ. Vui lòng nhập thêm."   ║
║  ← VERIFY: type=LOW_STOCK_ALERT, recipient=managerId          ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 4.4.5: Verify low-stock list endpoint**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Query low-stock batches            ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/inventory/low-stock                ║
║           ?branchId={{branchHQ}}                              ║
║  SERVICE: inventory-service                                   ║
║  EXPECTED: 200 OK → array chứa {{batchLow}}                   ║
║  ← VERIFY: {{batchLow}} in list                               ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.5 Rollback Path

```
╔══════════════════════════════════════════════════════════════╗
║  SCENARIO: notification-service down → outbox backlog         ║
╠══════════════════════════════════════════════════════════════╣
║  R4.1: Stop notification-service (shell stop container)       ║
║  R4.2: Consume thêm (qty=2, xuống 1)                          ║
║    POST /api/v1/inventory/{{batchLow}}/consume                 ║
║    REQUEST: { "qty":2, "reason":"E2E-04 rollback" }            ║
║    EXPECTED: 200 OK, lowStockTriggered=true                    ║
║  R4.3: Verify outbox event stuck PENDING                      ║
║    SELECT count(*) FROM outbox_event                           ║
║    WHERE status='PENDING' AND event_type='inventory.low-stock'║
║    EXPECTED: count ≥ 1                                         ║
║  R4.4: Restart notification-service (start container)         ║
║  R4.5: Wait for backlog drain (max 30s)                        ║
║    EXPECTED: PENDING count = 0                                ║
║  R4.6: Verify manager nhận 2 alerts                            ║
║    GET /api/v1/notifications?userId={{managerId}}              ║
║    EXPECTED: 2 LOW_STOCK_ALERT entries                         ║
║  ← ROLLBACK: PUT /api/v1/inventory/{{batchLow}}/adjust         ║
║    { "deltaQty":4, "reason":"E2E-04 restore" }                 ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.6 Verification Checkpoints

| Check | Expected | Pass? |
|-------|----------|:-----:|
| Batch low-state detected pre-trigger | qty < minStock | ☐ |
| Consume response có lowStockTriggered=true | flag set | ☐ |
| Outbox event PUBLISHED within 5s | row in outbox_event | ☐ |
| Manager nhận LOW_STOCK_ALERT | type + recipient match | ☐ |
| /inventory/low-stock lists {{batchLow}} | alert endpoint thấy | ☐ |
| Rollback: notif down → event backlog | PENDING count > 0 | ☐ |
| Rollback: notif restart → backlog drain | PENDING count = 0 in 30s | ☐ |

### 4.7 Cleanup

```bash
PUT /api/v1/inventory/{{batchLow}}/adjust { "deltaQty":4, "reason":"E2E-04 restore" }
```

---

# ===========================================================================

# FLOW 5 / E2E-05: B2C Consult + AI Drug-Check

# ===========================================================================

### 5.1 Objective

Validate luồng tư vấn dược: customer chat AI → escalate → pharmacist consult → AI drug-check → mark VIP.

### 5.2 Services Touched

| # | Service | Port | Role | DB |
|---|---------|------|------|----|
| 1 | ai-engine-service | 8095 | Chat RAG, drug-check AI | pgvector |
| 2 | pharmacist-workbench-service | 8094 | Consultation mgmt, vip-marks | pcms_workbench |

### 5.3 ASCII Timeline

```
T+0s    [customer]  → POST /api/v1/ai/chat (RAG)         ← INPUT
T+2s    [ai-engine] → RAG + OpenAI return                 ✓
T+2.5s  [customer]  → "Tôi đang dùng 5 thuốc khác"       ← INPUT
T+4s    [ai-engine] → escalate=true                       ✓
T+4.5s  [customer]  → POST /api/v1/consultations (TEXT)  ← INPUT
T+5s    [workbench] → ACTIVE consultation                  ✓
T+6s    [pharmacist]→ POST /api/v1/rx/drug-check          ← INPUT
T+8s    [workbench] → call ai-engine drug-check            ✓
T+9s    [pharmacist]→ POST /api/v1/vip-marks              ← INPUT
T+9.5s  [workbench] → persist VIP mark GOLD                ✓
```

### 5.4 Step-by-Step — Happy Path

**Step 5.4.1: AI Engine health check**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify AI engine ready            ← SEED            ║
║  METHOD: GET  PATH: /healthz                                  ║
║  SERVICE: ai-engine-service (Python)                          ║
║  URL: http://localhost:8095/healthz                            ║
║  EXPECTED: 200 OK → status=UP, openai=UP, pgvector=UP       ║
║  ← VERIFY: openai & pgvector both UP                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 5.4.2: Customer chat với AI**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer hỏi về thuốc              ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/ai/chat                          ║
║  SERVICE: ai-engine-service (DIRECT, port 8095)              ║
║  HEADER: X-Customer-Id: {{customerId}}                        ║
║  REQUEST:                                                     ║
║  { "customerId":"{{customerId}}",                            ║
║    "message":"Tôi có thể dùng đồng thời Paracetamol và Aspirin không?",║
║    "sessionId":null }  ← null = new session                  ║
║  EXPECTED: 200 OK → sessionId, answer, sources, escalate=false║
║  ← CAPTURE: sessionId                                         ║
║  ← ASYNC: 1-3s (OpenAI call)                                 ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 5.4.3: Multi-drug escalation trigger**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Multi-drug message → escalate=true                    ║
║  METHOD: POST  PATH: /api/v1/ai/chat                          ║
║  SERVICE: ai-engine-service                                   ║
║  REQUEST:                                                     ║
║  { "sessionId":"{{sessionId}}", "customerId":"{{customerId}}",║
║    "message":"Tôi đang dùng 5 loại thuốc cho tim mạch, huyết áp, tiểu đường. Tôi cần tư vấn gấp" }║
║  EXPECTED: 200 OK → escalate=true, reason=multi-drug          ║
║  ← VERIFY: escalate=true                                      ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 5.4.4: Bắt đầu consultation với pharmacist**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Start text consultation            ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/consultations                   ║
║  SERVICE: pharmacist-workbench-service                       ║
║  HEADER: Authorization: Bearer {{customerToken}}              ║
║          X-Customer-Id: {{customerId}}                        ║
║  REQUEST: { "customerId":"{{customerId}}","channel":"TEXT" }  ║
║  EXPECTED: 200 OK → status=ACTIVE, aiSessionRef=sessionId    ║
║  ← CAPTURE: consultId                                         ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 5.4.5: Pharmacist assign + customer message**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pharmacist claim + customer msg  ← INPUT            ║
║  METHOD: PUT  PATH: /api/v1/consultations/{{consultId}}/      ║
║           assign?pharmacistId={{pharmacistId}}                ║
║  HEADER: X-User-Id: {{pharmacistId}}                          ║
║  EXPECTED: 200 OK, pharmacistId assigned                      ║
║  ────────────────────────────────────────────────────────     ║
║  METHOD: POST PATH: /api/v1/consultations/{{consultId}}/messages║
║  REQUEST BODY: "Danh sách: Aspirin 81mg, Metformin 500mg..." ║
║  EXPECTED: 200 OK, message persisted                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 5.4.6: AI drug-check**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: AI drug interaction check          ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/rx/drug-check                    ║
║  SERVICE: pharmacist-workbench-service (gọi ai-engine)       ║
║  HEADER: Authorization: Bearer {{pharmacistToken}}            ║
║          X-User-Id    : {{pharmacistId}}                      ║
║  REQUEST: { "medicineIds":["{{med1}}","{{med2}}","{{med3}}"], ║
║    "customerId":"{{customerId}}","consultId":"{{consultId}}" }║
║  EXPECTED: 200 OK → interactions[], overallRisk=MODERATE      ║
║  ← ASYNC: 1-3s                                                ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 5.4.7: Mark VIP + verify**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: End consultation + mark VIP GOLD   ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/consultations/{{consultId}}/end  ║
║  REQUEST: { "summary":"Đã tư vấn 5 thuốc..." }                ║
║  EXPECTED: 200 OK, status=ENDED                               ║
║  ────────────────────────────────────────────────────────     ║
║  METHOD: POST  PATH: /api/v1/vip-marks                        ║
║  REQUEST: { "customerId":"{{customerId}}","tier":"GOLD",     ║
║    "reason":"Khách phức tạp","consultId":"{{consultId}}" }    ║
║  EXPECTED: 201 Created → vipMarkId, tier=GOLD                  ║
║  ────────────────────────────────────────────────────────     ║
║  VERIFY: GET /api/v1/vip-marks/by-customer/{{customerId}}     ║
║  EXPECTED: tier=GOLD, consultId match                          ║
╚══════════════════════════════════════════════════════════════╝
```

### 5.5 Rollback Path

```
╔══════════════════════════════════════════════════════════════╗
║  SCENARIO: AI engine down → drug-check trả 503 graceful     ║
╠══════════════════════════════════════════════════════════════╣
║  R5.1: Stop ai-engine (shell stop container)                 ║
║  R5.2: Try drug-check                                         ║
║    POST /api/v1/rx/drug-check                                 ║
║    EXPECTED: 503 Service Unavailable (không 500)              ║
║  R5.3: Try AI chat                                             ║
║    POST http://localhost:8095/api/v1/ai/chat                   ║
║    EXPECTED: 503 Service Unavailable                          ║
║  R5.4: Restart ai-engine (start container)                   ║
║  R5.5: Verify recovery                                        ║
║    GET http://localhost:8095/healthz                           ║
║    EXPECTED: 200 OK, status=UP                                ║
║  R5.6: Re-run drug-check → EXPECTED: 200 OK với overallRisk ║
║  ← ROLLBACK: DELETE /api/v1/vip-marks/{{customerId}}         ║
║    DELETE /api/v1/consultations/{{consultId}} (nếu cho phép) ║
╚══════════════════════════════════════════════════════════════╝
```

### 5.6 Verification Checkpoints

| Check | Expected | Pass? |
|-------|----------|:-----:|
| AI health check: openai + pgvector UP | both UP | ☐ |
| Chat RAG returns answer + sources | non-empty answer | ☐ |
| Multi-drug message triggers escalate=true | escalate flag set | ☐ |
| Consultation ACTIVE với aiSessionRef | linked to AI session | ☐ |
| Pharmacist assigned, customer appends message | message persisted | ☐ |
| Drug-check returns interactions + risk level | 200 + overallRisk | ☐ |
| VIP mark GOLD linked tới consult | vipMarkId + consultId | ☐ |
| Rollback: AI down → graceful 503, no 500 | 503 not 500 | ☐ |

### 5.7 Cleanup

```bash
DELETE /api/v1/vip-marks/{{customerId}}
# AI sessions: TTL 30 ngày, không cần xoá thủ công
```

---

# ===========================================================================

# FLOW 6 / E2E-06: Vaccine Booking Flow

# ===========================================================================

### 6.1 Objective

Validate luồng đặt lịch tiêm vaccine: browse → check slots → book → confirm notification → ledger update.

### 6.2 Services Touched

| # | Service | Port | Role | DB |
|---|---------|------|------|----|
| 1 | customer-portal-service | 8093 | Browse, slots, book, ledger | pcms_portal |
| 2 | notification-service | 8091 | Confirm + reminder | pcms_notification |

### 6.3 ASCII Timeline

```
T+0s    [customer] → GET /api/v1/vaccines                 ← INPUT
T+0.3s  [portal]   → list available vaccines              ✓
T+1s    [customer] → GET /api/v1/vaccines/{{vacc1}}/slots ← INPUT
T+1.3s  [portal]   → return time slots                    ✓
T+2s    [customer] → POST /api/v1/vaccine-bookings         ← INPUT
T+2.5s  [portal]   → persist CONFIRMED, outbox event      ✓
T+3s    [notif]    → consume, send confirmation           ✓
T+3.5s  [customer] → GET /api/v1/vaccine-bookings/me       ← VERIFY
T+4s    [customer] → GET /api/v1/vaccination-ledger/me     ← VERIFY
```

### 6.4 Step-by-Step — Happy Path

**Step 6.4.1: Browse vaccines**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List vaccines available           ← INPUT          ║
║  METHOD: GET  PATH: /api/v1/vaccines                          ║
║  SERVICE: customer-portal-service                            ║
║  HEADER: Authorization: Bearer {{customerToken}}               ║
║  EXPECTED: 200 OK → non-empty array of vaccines              ║
║  ← CAPTURE: vacc1                                             ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 6.4.2: Check available slots**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check slots for vaccine 1          ← INPUT          ║
║  METHOD: GET  PATH: /api/v1/vaccines/{{vacc1}}/slots           ║
║           ?branchId={{branchHQ}}&days=7                       ║
║  SERVICE: customer-portal-service                            ║
║  EXPECTED: 200 OK → slots[7 days] with available=true        ║
║  ← CAPTURE: slotDate, slotTime                                ║
║  ← VERIFY: có ít nhất 1 slot available=true                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 6.4.3: Book vaccination slot**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Đặt lịch tiêm vaccine             ← INPUT          ║
║  METHOD: POST  PATH: /api/v1/vaccine-bookings                 ║
║  SERVICE: customer-portal-service                            ║
║  HEADER: Authorization: Bearer {{customerToken}}              ║
║          X-Customer-Id: {{customerId}}                        ║
║          Idempotency-Key: <uuid>                              ║
║  REQUEST:                                                     ║
║  { "vaccineId":"{{vacc1}}","branchId":"{{branchHQ}}",        ║
║    "slotDate":"{{slotDate}}","slotTime":"{{slotTime}}",      ║
║    "addressId":"{{addressId}}","doseNumber":1,               ║
║    "notes":"Tiêm mũi 1 COVID-19" }                            ║
║  EXPECTED: 201 Created → status=CONFIRMED, totalPrice        ║
║  ← CAPTURE: bookingId                                         ║
║  ← ASYNC: outbox "vaccine.booked" published                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 6.4.4: Verify slot booked count incremented**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Re-check slots (booked count +1)   ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/vaccines/{{vacc1}}/slots           ║
║           ?branchId={{branchHQ}}&days=7                       ║
║  SERVICE: customer-portal-service                            ║
║  EXPECTED: 200 OK → booked +1 cho slot đã book                ║
║  ← VERIFY: booked +1                                          ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 6.4.5: Verify notification delivered**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify "Vaccine booking confirmed"  ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/notifications?customerId={{customerId}} ║
║  SERVICE: notification-service                                ║
║  ← ASYNC: poll tối đa 10s                                     ║
║  EXPECTED: 200 OK → type=VACCINE_BOOKED, status=SENT          ║
║  ← VERIFY: type=VACCINE_BOOKED, bookingId match                ║
╚══════════════════════════════════════════════════════════════╝
```

**Step 6.4.6: Verify ledger entry + my bookings**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Vaccination ledger + bookings/me  ← VERIFY          ║
║  METHOD: GET  PATH: /api/v1/vaccination-ledger/me             ║
║           + GET /api/v1/vaccine-bookings/me                   ║
║  EXPECTED:                                                    ║
║  - Ledger: status=SCHEDULED, doseNumber=1, nextDoseDate=+21d  ║
║  - /me: list chứa bookingId, status=CONFIRMED                ║
║  ← VERIFY: ledger có entry, bookingId in /me list             ║
╚══════════════════════════════════════════════════════════════╝
```

### 6.5 Rollback Path

```
╔══════════════════════════════════════════════════════════════╗
║  SCENARIO: Double-booking conflict + cancel rollback          ║
╠══════════════════════════════════════════════════════════════╣
║  R6.1: Book cùng slot lần 2                                  ║
║    POST /api/v1/vaccine-bookings                              ║
║    REQUEST: { "vaccineId":"{{vacc1}}", "slotDate":"2026-06-21",║
║      "slotTime":"09:00", "addressId":"{{addressId}}",         ║
║      "doseNumber":2, "notes":"test duplicate" }               ║
║    EXPECTED: 409 Conflict                                     ║
║    ← VERIFY: "Slot already booked"                            ║
║  R6.2: Cancel booking                                         ║
║    DELETE /api/v1/vaccine-bookings/{{bookingId}}              ║
║    EXPECTED: 204 No Content                                   ║
║  R6.3: Verify slot freed                                      ║
║    GET /api/v1/vaccines/{{vacc1}}/slots                        ║
║    EXPECTED: booked count về 0                                ║
║  R6.4: Verify ledger entry cancelled                          ║
║    GET /api/v1/vaccination-ledger/me                          ║
║    EXPECTED: entry status=CANCELLED                           ║
╚══════════════════════════════════════════════════════════════╝
```

### 6.6 Verification Checkpoints

| Check | Expected | Pass? |
|-------|----------|:-----:|
| Browse vaccines returns list | 200 + non-empty | ☐ |
| Slots endpoint returns 7-day window | slots for N days | ☐ |
| Book tạo CONFIRMED booking | 201 + bookingId | ☐ |
| Slot booked count +1 | capacity unchanged, booked +1 | ☐ |
| Outbox "vaccine.booked" → notification | type=VACCINE_BOOKED | ☐ |
| Vaccination ledger có entry mới | SCHEDULED + nextDoseDate | ☐ |
| /vaccine-bookings/me shows booking | list contains bookingId | ☐ |
| Rollback: duplicate slot rejected (409) | 409 + "Slot already booked" | ☐ |
| Rollback: cancel frees slot + updates ledger | booked -1, status=CANCELLED | ☐ |

### 6.7 Cleanup

```bash
DELETE /api/v1/vaccine-bookings/{{bookingId}}
# Ledger tự update CANCELLED, notification cancel tự gửi (optional)
```

---

# ===========================================================================

# END OF FILE

# ===========================================================================

## Summary: Cross-Service Flow Matrix

| Service | Flows participated |
|---------|---------------------|
| order-service | E2E-01, E2E-02 |
| payment-service | E2E-01, E2E-03 |
| inventory-service | E2E-01, E2E-04 |
| notification-service | E2E-01, E2E-03, E2E-04, E2E-06 |
| prescription-service | E2E-02 |
| customer-portal-service | E2E-03, E2E-06 |
| pharmacist-workbench-service | E2E-05 |
| ai-engine-service | E2E-02 (OCR), E2E-05 |
| api-gateway | ALL 6 flows |

## Test Execution Tracker

| Flow | Tester | Date | Pass | Fail | Bugs | Status |
|------|--------|------|------|------|------|:------:|
| E2E-01 (B2B order+pay+inv+notif) |    |      |      |      |      | ☐      |
| E2E-02 (Prescription+link)       |    |      |      |      |      | ☐      |
| E2E-03 (B2C cart+checkout)       |    |      |      |      |      | ☐      |
| E2E-04 (Low-stock outbox)        |    |      |      |      |      | ☐      |
| E2E-05 (Consult+AI drug-check)   |    |      |      |      |      | ☐      |
| E2E-06 (Vaccine booking)         |    |      |      |      |      | ☐      |

## Pass/Fail Criteria

**PASS** khi:

- Tất cả 6 flow chạy happy path đến step cuối không cần manual intervention
- Rollback paths chạy đúng expected error codes
- Không có unhandled exception (5xx không mong đợi)
- Outbox events publish + consume đúng thứ tự
- Idempotency giữ vững khi replay event

**FAIL** khi:

- Bất kỳ service nào trong chain trả 5xx không mong đợi
- Outbox event stuck PENDING > 30s
- Notification không delivered khi flow yêu cầu
- Inventory stock bị âm hoặc không FIFO
- Customer/payment data bị inconsistent giữa 2 service

**End of `19-E2E-FLOWS.md`** ← Previous: [`18-AI-ENGINE.md`](./18-AI-ENGINE.md)
