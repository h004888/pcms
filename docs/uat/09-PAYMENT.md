# UAT Test: Payment Service (UC07)

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10 (commit `63b9397`)
**Module:** `payment-service` (port 8089)
**Use Case:** UC07 - Process Payment (Cash, Card, QR) + Webhook Receiver
**Total endpoints:** 9 (8 PaymentController + 1 WebhookController)

---

## 1. Service Info

```
╔══════════════════════════════════════════════════════════════════════╗
║  SERVICE   : payment-service                                         ║
║  PORT      : 8089 (direct) / 8080 (via Gateway)                      ║
║  STACK     : Spring Boot 4.0.7 + Java 21 + MySQL 8                   ║
║  PREFIX    : /api/v1/payments/**  +  /api/v1/webhooks/**             ║
║  SECURITY  : JWT HS256 - permitAll in dev profile                    ║
║  CLIENTS   : order-service (markOrderPaid), customer-service          ║
║  DB        : pcms_payment  (tables: payments, webhook_events)         ║
║  ENUMS     : PaymentMethod={CASH,CARD,QR,LOYALTY_POINTS}             ║
║              PaymentStatus={PENDING,SUCCESS,FAILED,                  ║
║                              PARTIALLY_REFUNDED,REFUNDED}            ║
╚══════════════════════════════════════════════════════════════════════╝
```

### 1.1 Indicator Legend

| Symbol      | Meaning                                              |
|-------------|------------------------------------------------------|
| ← INPUT     | Field value to send in request                       |
| ← VERIFY    | Assertion to check in response                       |
| ← CAPTURE   | Save this value as variable for next step            |
| ← SEED      | Pre-conditions (data must exist)                     |
| ← HMAC      | Compute HMAC-SHA256 signature over raw body          |
| ← ASYNC     | Long-running webhook (gateway retry semantics)       |

### 1.2 Authorization Matrix

| Endpoint                  | Admin/CEO | Manager | Pharmacist | Customer (B2C) | Gateway |
|---------------------------|-----------|---------|------------|----------------|---------|
| GET /payments (read)      | ✓         | ✓       | ✓          | ✗              | ✗       |
| POST /payments (process)  | ✓         | ✓       | ✓          | ✓ (own order)  | ✗       |
| POST /payments/{id}/refund| ✓         | ✓       | ✗          | ✗              | ✗       |
| PUT /payments/{id}/refund (legacy) | ✓ | ✓     | ✗          | ✗              | ✗       |
| POST /webhooks/payment-gateway | (Gateway-only - HMAC auth)                              |

---

## 2. Endpoint Summary

| #  | Method | Path                                          | Controller              | Auth   | Purpose                                  |
|----|--------|-----------------------------------------------|-------------------------|--------|------------------------------------------|
| 1  | GET    | `/api/v1/payments`                            | PaymentController       | permit | List all payments (paginated)            |
| 2  | GET    | `/api/v1/payments/{id}`                       | PaymentController       | permit | Get payment by UUID                      |
| 3  | GET    | `/api/v1/payments/invoice/{invoiceNumber}`    | PaymentController       | permit | Lookup payment by invoice number         |
| 4  | GET    | `/api/v1/payments/order/{orderId}`            | PaymentController       | permit | Lookup payment for a specific order       |
| 5  | POST   | `/api/v1/payments`                            | PaymentController       | permit | Process a new payment                    |
| 6  | POST   | `/api/v1/payments/{id}/refund`                | PaymentController       | permit | Refund (full or partial)                 |
| 7  | PUT    | `/api/v1/payments/{id}/refund`                | PaymentController       | permit | Legacy soft-cancel (PUT alias)           |
| 8  | GET    | `/api/v1/payments/{id}/refund-history`        | PaymentController       | permit | List all refund events for payment       |
| 9  | POST   | `/api/v1/webhooks/payment-gateway`            | WebhookController       | HMAC   | Gateway webhook (HMAC-SHA256 verified)   |

---

## 3. Prerequisites

1. **Environment**: Gateway running at `http://localhost:8080`, payment-service registered on Eureka (port 8089).
2. **Database**: `pcms_payment` schema migrated; tables `payments`, `webhook_events` exist.
3. **Webhook secret**: `app.payment.webhook-secret` configured in `config-server/config/payment-service.yml`:
   - Dev: `pcms-payment-gateway-webhook-secret-2026-do-not-share`
4. **Seeded data** (run Section 4 first):
   - At least 1 branch (HQ), 5 medicines, 10 inventory batches
   - At least 1 customer + 1 order (status CREATED) ready to pay
5. **Auth**: Dev profile is `permitAll` - no JWT needed. For prod simulation, login first:
   - `POST /api/v1/auth/login` → `{{accessToken}}`
6. **Tools**: `openssl dgst -sha256 -hmac` (or Python `hmac` module) for webhook HMAC signature.

---

## 4. Test Data Seeding

### 4.1 Create Order to Pay For

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create test order                                     ║
║  METHOD: POST  PATH: /api/v1/orders                          ║
║  HEADER:                                                     ║
║    Content-Type : application/json                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                    ║
║  {                                                          ║
║    "customerId": "{{cust1}}",                                ║
║    "branchId": "{{branchHQ}}",                               ║
║    "items": [                                                ║
║      { "medicineId": "{{med1}}", "qty": 2, "unitPrice": 5000 },║
║      { "medicineId": "{{med3}}", "qty": 1, "unitPrice": 25000 }║
║    ],                                                        ║
║    "paymentMethod": "CASH"                                   ║
║  }                                                          ║
║  EXPECTED: 201 Created                                       ║
║  ← CAPTURE: ORDER_ID_1 (uuid)                                ║
║  ← CAPTURE: ORDER_TOTAL_1 (= 35000 VND)                      ║
╠══════════════════════════════════════════════════════════════╣
║  STEP: Create second test order (for refund tests)           ║
║  ← REPEAT same payload, capture ORDER_ID_2                   ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.2 Captured Variables

| Variable        | Example                                | Source                        |
|-----------------|----------------------------------------|-------------------------------|
| `{{gateway}}`   | `http://localhost:8080`                | static                        |
| `{{accessToken}}` | `eyJhbGciOiJIUzI1...`                | POST /api/v1/auth/login       |
| `{{branchHQ}}`  | `uuid`                                 | POST /api/v1/branches         |
| `{{cust1}}`     | `uuid` (Nguyen Van A)                  | POST /api/v1/customers        |
| `{{med1}}`      | `uuid` (Paracetamol 500mg)             | POST /api/v1/medicines        |
| `{{med3}}`      | `uuid` (Vitamin C 1000mg)              | POST /api/v1/medicines        |
| `{{staff1}}`    | `uuid` (pharmacist user)               | POST /api/v1/auth/login       |
| `{{ORDER_ID_1}}`| `uuid`                                 | POST /api/v1/orders           |
| `{{ORDER_ID_2}}`| `uuid`                                 | POST /api/v1/orders           |
| `{{TX_REF_1}}`  | `TX-20260619-001`                      | manual - unique per test run  |
| `{{WEBHOOK_SECRET}}` | `pcms-payment-gateway-webhook-secret-2026-do-not-share` | config-server |

---

## 5. Test Cases

### 5.1 GET /api/v1/payments (List with pagination)

#### Test 5.1.1 - Happy path: list payments page 0 size 20

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List payments                                         ║
║  METHOD: GET  PATH: /api/v1/payments?page=0&size=20          ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                     ║
║    Authorization : Bearer {{accessToken}}    ← optional dev   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: (none)                                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "content": [                                              ║
║      {                                                       ║
║        "id": "uuid",                                        ║
║        "orderId": "uuid",                                    ║
║        "invoiceNumber": "INV-20260619-0001",                 ║
║        "paymentMethod": "CASH",                              ║
║        "amount": 35000,                                      ║
║        "refundedAmount": 0,                                  ║
║        "status": "SUCCESS",                                  ║
║        "createdAt": "2026-06-19T10:00:00"                    ║
║      }                                                       ║
║    ],                                                        ║
║    "page": 0,                                                ║
║    "size": 20,                                               ║
║    "totalElements": 2,                                       ║
║    "totalPages": 1                                           ║
║  }                                                          ║
║  ← VERIFY: content.length >= 1, each has status, invoiceNumber║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.2 - Edge: empty page (no payments yet)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List empty page                                       ║
║  METHOD: GET  PATH: /api/v1/payments?page=99&size=20         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "content": [],                                           ║
║    "page": 99,                                               ║
║    "totalElements": 0                                       ║
║  }                                                          ║
║  ← VERIFY: content = [], totalElements = 0                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.3 - Negative: invalid page (negative)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Negative page param                                   ║
║  METHOD: GET  PATH: /api/v1/payments?page=-1&size=20         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    400 Bad Request                                           ║
║  ← VERIFY: error message contains "page" or "must be >= 0"   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 GET /api/v1/payments/{id} (Get by UUID)

#### Test 5.2.1 - Happy path: get payment by id

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get payment by id                                     ║
║  METHOD: GET  PATH: /api/v1/payments/{{PAYMENT_ID_1}}        ║
║  ← SEED: Create payment via 5.5 first; CAPTURE PAYMENT_ID_1  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{PAYMENT_ID_1}}",                                 ║
║    "orderId": "{{ORDER_ID_1}}",                              ║
║    "invoiceNumber": "INV-20260619-0001",                     ║
║    "paymentMethod": "CASH",                                  ║
║    "amount": 35000,                                          ║
║    "refundedAmount": 0,                                      ║
║    "tenderedAmount": 50000,                                  ║
║    "changeAmount": 15000,                                    ║
║    "transactionRef": null,                                   ║
║    "status": "SUCCESS",                                      ║
║    "createdAt": "2026-06-19T10:00:00"                        ║
║  }                                                          ║
║  ← VERIFY: id matches, status=SUCCESS                        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.2 - Negative: payment id not found

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Random UUID not found                                 ║
║  METHOD: GET  PATH: /api/v1/payments/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    404 Not Found                                             ║
║  {                                                          ║
║    "error": "RESOURCE_NOT_FOUND",                            ║
║    "message": "Payment with id 00000000-... not found"       ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.3 - Negative: malformed UUID

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Non-UUID id                                           ║
║  METHOD: GET  PATH: /api/v1/payments/not-a-uuid              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    400 Bad Request (type mismatch)                           ║
║  ← VERIFY: error mentions UUID parsing                       ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 GET /api/v1/payments/invoice/{invoiceNumber} (Lookup by invoice)

#### Test 5.3.1 - Happy path: lookup by invoice number

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Lookup by invoice number                              ║
║  METHOD: GET  PATH: /api/v1/payments/invoice/INV-20260619-0001║
║  ← SEED: Payment with this invoice exists                    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{PAYMENT_ID_1}}",                                 ║
║    "invoiceNumber": "INV-20260619-0001",                     ║
║    "status": "SUCCESS",                                      ║
║    ...                                                       ║
║  }                                                          ║
║  ← VERIFY: invoiceNumber matches request                    ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.3.2 - Negative: invoice not found

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown invoice                                       ║
║  METHOD: GET  PATH: /api/v1/payments/invoice/INV-DOES-NOT-EXIST║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    404 Not Found                                             ║
║  ← VERIFY: message "Payment with invoice INV-DOES-NOT-EXIST" ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.4 GET /api/v1/payments/order/{orderId} (Lookup by order)

#### Test 5.4.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get payment for order                                 ║
║  METHOD: GET  PATH: /api/v1/payments/order/{{ORDER_ID_1}}    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{PAYMENT_ID_1}}",                                 ║
║    "orderId": "{{ORDER_ID_1}}",                              ║
║    "status": "SUCCESS",                                      ║
║    "amount": 35000                                           ║
║  }                                                          ║
║  ← VERIFY: orderId matches request path                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.2 - Negative: order has no payment

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Order without payment                                 ║
║  METHOD: GET  PATH: /api/v1/payments/order/{{ORDER_NO_PAY}}  ║
║  ← SEED: Create an order but DO NOT pay for it              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    404 Not Found                                             ║
║  ← VERIFY: error message indicates no payment for order     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.5 POST /api/v1/payments (Process Payment)

#### Test 5.5.1 - Happy path: CASH payment

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Process CASH payment                                  ║
║  METHOD: POST  PATH: /api/v1/payments                        ║
║  HEADER:                                                     ║
║    Content-Type  : application/json                          ║
║    Idempotency-Key: {{IDEMPOTENCY_KEY_1}}    ← uuid v4       ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "orderId": "{{ORDER_ID_1}}",                              ║
║    "paymentMethod": "CASH",                                  ║
║    "amount": 35000,                                          ║
║    "tenderedAmount": 50000,                                  ║
║    "staffId": "{{staff1}}"                                   ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    201 Created                                               ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "orderId": "{{ORDER_ID_1}}",                              ║
║    "invoiceNumber": "INV-20260619-0001",                     ║
║    "paymentMethod": "CASH",                                  ║
║    "amount": 35000,                                          ║
║    "tenderedAmount": 50000,                                  ║
║    "changeAmount": 15000,                                    ║
║    "status": "SUCCESS",                                      ║
║    "createdAt": "2026-06-19T10:00:00"                        ║
║  }                                                          ║
║  ← CAPTURE: PAYMENT_ID_1, INVOICE_NUMBER_1                   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.2 - Happy path: CARD payment (async - waits for webhook)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Process CARD payment                                  ║
║  METHOD: POST  PATH: /api/v1/payments                        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "orderId": "{{ORDER_ID_2}}",                              ║
║    "paymentMethod": "CARD",                                  ║
║    "amount": 35000,                                          ║
║    "transactionRef": "TX-20260619-002",                      ║
║    "staffId": "{{staff1}}"                                   ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    201 Created                                               ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "paymentMethod": "CARD",                                  ║
║    "transactionRef": "TX-20260619-002",                      ║
║    "status": "PENDING"     ← webhook will change to SUCCESS  ║
║    ...                                                       ║
║  }                                                          ║
║  ← CAPTURE: PAYMENT_ID_2                                     ║
║  ← THEN: send webhook (see Test 5.9) to flip status → SUCCESS║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.3 - Happy path: QR (Sepay/VNPay-style)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Process QR payment                                    ║
║  METHOD: POST  PATH: /api/v1/payments                        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "orderId": "{{ORDER_ID_3}}",                              ║
║    "paymentMethod": "QR",                                    ║
║    "amount": 50000,                                          ║
║    "transactionRef": "TX-20260619-003",                      ║
║    "staffId": "{{staff1}}"                                   ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201, status=PENDING                               ║
║  ← CAPTURE: PAYMENT_ID_3                                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.4 - Happy path: LOYALTY_POINTS

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pay with loyalty points                               ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "orderId": "{{ORDER_ID_4}}",                              ║
║    "paymentMethod": "LOYALTY_POINTS",                        ║
║    "amount": 10000,                                          ║
║    "customerId": "{{cust1}}",                                ║
║    "staffId": "{{staff1}}"                                   ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201, status=SUCCESS                               ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.5 - Negative: missing orderId (validation)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required orderId                              ║
║  METHOD: POST  PATH: /api/v1/payments                        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "paymentMethod": "CASH",                                  ║
║    "amount": 35000                                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    400 Bad Request                                           ║
║  {                                                          ║
║    "error": "VALIDATION_ERROR",                              ║
║    "fieldErrors": [                                          ║
║      { "field": "orderId", "message": "must not be null" }   ║
║    ]                                                         ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.6 - Negative: amount <= 0

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Negative amount                                       ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "orderId": "{{ORDER_ID_1}}",                              ║
║    "paymentMethod": "CASH",                                  ║
║    "amount": -1000                                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400, message "amount must be greater than 0"      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.7 - Negative: invalid paymentMethod enum

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bogus payment method                                  ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "orderId": "{{ORDER_ID_1}}",                              ║
║    "paymentMethod": "BITCOIN",                               ║
║    "amount": 35000                                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: error mentions PaymentMethod enum values          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.8 - Edge: duplicate payment for same order (idempotency)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Replay with same Idempotency-Key                      ║
║  METHOD: POST  PATH: /api/v1/payments                        ║
║  HEADER:                                                     ║
║    Idempotency-Key: {{IDEMPOTENCY_KEY_1}}   ← REUSED         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: same as Test 5.5.1                            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (idempotent replay returns original body)  ║
║  ← VERIFY: response.id == PAYMENT_ID_1 (same UUID)           ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.6 POST /api/v1/payments/{id}/refund (Full / partial refund)

#### Test 5.6.1 - Happy path: full refund

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Full refund                                           ║
║  METHOD: POST  PATH: /api/v1/payments/{{PAYMENT_ID_1}}/refund║
║  ← SEED: Payment status=SUCCESS (Test 5.5.1)                ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "amount": 35000,                                          ║
║    "reason": "Customer returned items"                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{PAYMENT_ID_1}}",                                 ║
║    "amount": 35000,                                          ║
║    "refundedAmount": 35000,                                  ║
║    "status": "REFUNDED",                                     ║
║    ...                                                       ║
║  }                                                          ║
║  ← VERIFY: refundedAmount == amount, status == REFUNDED     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.2 - Happy path: partial refund (leave order partially paid)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Partial refund                                        ║
║  METHOD: POST  PATH: /api/v1/payments/{{PAYMENT_ID_2}}/refund║
║  ← SEED: PAYMENT_ID_2 amount=35000, status=SUCCESS (from 5.5.2 + webhook)║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "amount": 15000,                                          ║
║    "reason": "1 item returned out of 2"                      ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: refundedAmount == 15000, status == PARTIALLY_REFUNDED║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.3 - Happy path: refund with empty body (defaults to full)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Refund with no body                                   ║
║  METHOD: POST  PATH: /api/v1/payments/{{PAYMENT_ID_3}}/refund║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: (empty)                                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=REFUNDED (default = full amount)  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.4 - Negative: refund more than paid

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Refund amount > original                              ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "amount": 99999999,                                       ║
║    "reason": "Invalid test"                                  ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 or 422 - business rule violation             ║
║  ← VERIFY: message "refund exceeds remaining amount"         ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.5 - Negative: refund with amount <= 0

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Refund amount = 0                                     ║
║  REQUEST:                                                    ║
║  { "amount": 0, "reason": "test" }                          ║
║  EXPECTED: 400 Bad Request (validation)                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.6 - Negative: refund already-fully-refunded payment

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Double refund                                         ║
║  ← SEED: PAYMENT_ID_1 already refunded in 5.6.1             ║
║  METHOD: POST /api/v1/payments/{{PAYMENT_ID_1}}/refund       ║
║  REQUEST: { "amount": 1000, "reason": "second refund" }      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 422 Unprocessable Entity                          ║
║  ← VERIFY: message "Payment already fully refunded"         ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.7 - Negative: reason too long (> 255 chars)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Over-length reason                                    ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "amount": 1000,                                           ║
║    "reason": "A".repeat(300)                                 ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Lý do hoàn tiền không được vượt quá 255 ký tự"  ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.7 PUT /api/v1/payments/{id}/refund (Legacy soft-cancel)

#### Test 5.7.1 - Happy path: legacy cancel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Legacy soft-cancel via PUT                            ║
║  METHOD: PUT  PATH: /api/v1/payments/{{PAYMENT_PENDING}}/refund║
║  ← SEED: Create a PENDING payment (Test 5.5.2 style)        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: (none)                                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{PAYMENT_PENDING}}",                              ║
║    "status": "FAILED",      ← legacy maps PENDING → FAILED   ║
║    ...                                                       ║
║  }                                                          ║
║  ← VERIFY: status == FAILED (soft cancel)                    ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.2 - Negative: legacy cancel SUCCESS payment

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel already-paid                                   ║
║  ← SEED: Payment with status=SUCCESS                         ║
║  METHOD: PUT  PATH: /api/v1/payments/{{PAYMENT_ID_2}}/refund ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 422 or 409 Conflict                               ║
║  ← VERIFY: message "cannot cancel a successful payment"      ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.8 GET /api/v1/payments/{id}/refund-history

#### Test 5.8.1 - Happy path: view refund history

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: View refund history                                   ║
║  METHOD: GET  PATH: /api/v1/payments/{{PAYMENT_ID_1}}/refund-history║
║  ← SEED: PAYMENT_ID_1 has 1 refund event (from 5.6.1)        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "paymentId": "{{PAYMENT_ID_1}}",                          ║
║    "originalAmount": 35000,                                  ║
║    "refundedAmount": 35000,                                  ║
║    "remainingAmount": 0,                                     ║
║    "entries": [                                              ║
║      {                                                       ║
║        "amount": 35000,                                      ║
║        "reason": "Customer returned items",                  ║
║        "refundedAt": "2026-06-19T11:00:00"                   ║
║      }                                                       ║
║    ]                                                         ║
║  }                                                          ║
║  ← VERIFY: entries.length >= 1, sum(entries.amount) = refundedAmount║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.2 - Happy path: history with multiple partial refunds

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: History with multiple entries                        ║
║  ← SEED: Perform 2 partial refunds on PAYMENT_ID_X           ║
║  METHOD: GET  PATH: /api/v1/payments/{{PAYMENT_ID_X}}/refund-history║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "entries": [                                              ║
║      { "amount": 5000,  "reason": "1st", "refundedAt": "..." },║
║      { "amount": 10000, "reason": "2nd", "refundedAt": "..." }║
║    ],                                                        ║
║    "refundedAmount": 15000                                   ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.3 - Happy path: empty history (no refunds yet)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No refund events                                      ║
║  ← SEED: Fresh payment that has never been refunded         ║
║  METHOD: GET  PATH: /api/v1/payments/{{PAYMENT_ID_NEW}}/refund-history║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "refundedAmount": 0,                                      ║
║    "remainingAmount": 35000,                                 ║
║    "entries": []                                             ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.4 - Negative: history for unknown payment

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown payment id                                    ║
║  METHOD: GET  PATH: /api/v1/payments/00000000-0000-0000-0000-000000000000/refund-history║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.9 POST /api/v1/webhooks/payment-gateway (Webhook with HMAC)

> **CRITICAL**: This endpoint is the bridge between async payment gateways (CARD/QR) and PCMS. Security depends entirely on HMAC-SHA256 signature verification.

#### Setup: Compute HMAC signature

```bash
# Bash one-liner for HMAC computation
SECRET="pcms-payment-gateway-webhook-secret-2026-do-not-share"
BODY='{"eventType":"payment.success","transactionRef":"TX-20260619-002"}'
SIG=$(printf '%s' "$BODY" | openssl dgst -sha256 -hmac "$SECRET" -hex | awk '{print $2}')
echo "sha256=$SIG"
# → "sha256=abc123..."
```

```python
# Python equivalent
import hmac, hashlib
SECRET = b"pcms-payment-gateway-webhook-secret-2026-do-not-share"
BODY = b'{"eventType":"payment.success","transactionRef":"TX-20260619-002"}'
sig = hmac.new(SECRET, BODY, hashlib.sha256).hexdigest()
print(f"sha256={sig}")
```

#### Test 5.9.1 - Happy path: payment.success (CARD captured)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Webhook payment.success                               ║
║  METHOD: POST  PATH: /api/v1/webhooks/payment-gateway        ║
║  HEADER:                                                     ║
║    X-Signature  : sha256=<computed-hmac>      ← HMAC         ║
║    X-Event-Id   : evt-unique-001              ← idempotency  ║
║    Content-Type : application/json                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT  +  ← HMAC              ║
║  {                                                          ║
║    "eventType": "payment.success",                          ║
║    "id": "evt-unique-001",                                   ║
║    "transactionRef": "TX-20260619-002",                      ║
║    "amount": 35000,                                          ║
║    "gatewayResponseCode": "00"                               ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "status": "processed",                                    ║
║    "eventId": "evt-unique-001"                               ║
║  }                                                          ║
║  ← VERIFY: payment.status flipped PENDING → SUCCESS          ║
║  ← VERIFY: order-service marked order as PAID                ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.2 - Happy path: payment.failed

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Webhook payment.failed                                ║
║  REQUEST BODY:                                               ║
║  {                                                          ║
║    "eventType": "payment.failed",                            ║
║    "id": "evt-unique-002",                                   ║
║    "transactionRef": "TX-20260619-099",                      ║
║    "failureReason": "Insufficient funds"                     ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=processed                          ║
║  ← VERIFY: payment.status = FAILED                           ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.3 - Idempotency: replay same event

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Replay evt-unique-001                                 ║
║  REQUEST BODY: same as 5.9.1 (same X-Event-Id header)       ║
║  ← HMAC: compute again (same body → same sig)                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "status": "duplicate",      ← NOT processed twice         ║
║    "eventId": "evt-unique-001"                               ║
║  }                                                          ║
║  ← VERIFY: payment status not re-changed, no duplicate side-effects║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.4 - Negative: missing X-Signature (no HMAC)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No signature header                                   ║
║  HEADER: (X-Signature absent)                                ║
║  REQUEST BODY: same as 5.9.1                                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 401 Unauthorized                                  ║
║  { "status": "error", "message": "Invalid signature" }       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.5 - Negative: invalid HMAC (wrong secret)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bad signature                                         ║
║  HEADER:                                                     ║
║    X-Signature: sha256=deadbeefcafe0000000000000000000000... ║
║  REQUEST BODY: same as 5.9.1                                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 401 Unauthorized                                  ║
║  ← VERIFY: message "Invalid signature"                       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.6 - Negative: malformed JSON

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Garbage body                                          ║
║  HEADER: X-Signature: sha256=<hmac-of-this-body>             ║
║  REQUEST BODY: "this is not json {{{"                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: message "Invalid JSON body"                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.7 - Negative: missing transactionRef

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required field                                ║
║  HEADER: X-Signature: sha256=<hmac-of-this-body>             ║
║  REQUEST BODY:                                               ║
║  {                                                          ║
║    "eventType": "payment.success",                           ║
║    "id": "evt-no-tx"                                         ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: message "Missing transactionRef"                 ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.8 - Negative: transactionRef not in our DB

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown transaction                                   ║
║  REQUEST BODY:                                               ║
║  {                                                          ║
║    "eventType": "payment.success",                           ║
║    "id": "evt-unknown-tx",                                   ║
║    "transactionRef": "TX-DOES-NOT-EXIST-9999"                ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                     ║
║  ← VERIFY: message "Payment not found"                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.9 - Negative: unknown eventType

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unsupported event type                                ║
║  REQUEST BODY:                                               ║
║  {                                                          ║
║    "eventType": "payment.disputed",                          ║
║    "id": "evt-weird",                                        ║
║    "transactionRef": "TX-20260619-002"                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: message "Unknown event type"                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.10 - Verify order-service was notified (cross-service)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: After webhook, verify order paid                      ║
║  METHOD: GET  PATH: /api/v1/orders/{{ORDER_ID_2}}           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: order.status == "PAID" (or "COMPLETED")          ║
║  ← VERIFY: order.paidAt is set                               ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.11 - TICKET-206: alias endpoint POST /payments/webhook

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify legacy Stripe-style alias works               ║
║  METHOD: POST  PATH: /api/v1/payments/webhook                ║
║  HEADER: X-Signature, X-Event-Id (same flow as 5.9.1)       ║
║  REQUEST BODY: same as 5.9.1 with new X-Event-Id            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (same behavior as /webhooks/payment-gateway)║
║  ← VERIFY: gatewayEventId stored, payment status flipped    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Edge Cases & Business Rules Summary

| Scenario                                  | Expected status | Notes                          |
|-------------------------------------------|-----------------|--------------------------------|
| CASH overpay (tendered > amount)          | 201 + change    | changeAmount = tendered-amount |
| CASH exact (tendered == amount)           | 201 + change=0  |                                |
| CASH underpay (tendered < amount)         | 422             | short payment not allowed      |
| Refund after partial refund                | 200             | refundAmount tracked cumulatively |
| Refund after full refund                  | 422             | already refunded               |
| Webhook replay (same X-Event-Id)          | 200 + duplicate | idempotent                     |
| Webhook signature mismatch                | 401             | HMAC failed                    |
| Order-service down during webhook          | 200 + log warn  | webhook still marked processed  |
| LOYALTY_POINTS over balance               | 422             | check customer loyalty wallet  |

---

## 7. Post-Test Verification

```
╔══════════════════════════════════════════════════════════════╗
║  CHECK after running all tests:                              ║
║    GET /api/v1/payments            → totalElements matches   ║
║    GET /api/v1/payments?status=REFUNDED → all refunds listed ║
║    SELECT * FROM webhook_events;    → PROCESSED + DUPLICATE  ║
║    SELECT * FROM payments WHERE status='REFUNDED';           ║
║    Logs: grep "Webhook signature verification failed" logs   ║
║          → should appear in negative tests 5.9.4/5.9.5       ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 8. Bug Severity Quick Reference

| Severity | Example                                              |
|----------|------------------------------------------------------|
| Critical | Webhook processes payment without HMAC verification  |
| Major    | Refund of fully-refunded payment returns 200 (not 422) |
| Minor    | Missing change-amount field in JSON for exact CASH   |
| Trivial  | Log message typo                                      |

---

**End of 09-PAYMENT.md** → Next: [`10-PRESCRIPTION.md`](./10-PRESCRIPTION.md)