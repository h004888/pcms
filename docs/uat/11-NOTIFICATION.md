# UAT Test: Notification Service (UC13)

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10 (commit `63b9397`)
**Module:** `notification-service` (port 8091)
**Use Case:** UC13 - Notifications (compose, send, read, retry, templates) + Outbox consumers
**Total endpoints:** 15
**Controllers:**

- `NotificationController` (`/notifications`) - 11 endpoints
- `NotificationTemplateController` (`/notifications/templates`) - 2 endpoints
- `OutboxConsumerController` (`/notifications`) - 3 internal endpoints

---

## 1. Service Info

```
╔══════════════════════════════════════════════════════════════════════╗
║  SERVICE   : notification-service                                   ║
║  PORT      : 8091 (direct) / 8080 (via Gateway)                      ║
║  STACK     : Spring Boot 4.0.7 + Java 21 + MySQL 8                   ║
║  PREFIX    : /api/v1/notifications/**                                ║
║              /api/v1/notifications/templates/**                     ║
║  SECURITY  : JWT HS256 - permitAll in dev profile                    ║
║  CLIENTS   : order-service, inventory-service (via outbox events)    ║
║  ENUMS     : NotificationChannel={IN_APP,EMAIL,SMS}                  ║
║              NotificationStatus={PENDING,SENT,FAILED,READ,DELETED}   ║
║  INTERNAL  : Outbox consumers (orders/paid, inventory/low-stock,    ║
║              inventory/expiry) — idempotent via X-Outbox-Event-Id    ║
╚══════════════════════════════════════════════════════════════════════╝
```

### 1.1 Indicator Legend

| Symbol      | Meaning                                                   |
|-------------|-----------------------------------------------------------|
| ← INPUT     | Field value to send in request                            |
| ← VERIFY    | Assertion to check in response                            |
| ← CAPTURE   | Save this value as variable for next step                 |
| ← SEED      | Pre-conditions (data must exist)                          |
| ← INTERNAL  | Outbox consumer (called by other services, not end-users) |
| ← IDEMPOTENT| Same X-Outbox-Event-Id returns DUPLICATE without side-effects |

### 1.2 Authorization Matrix

| Endpoint                               | Admin/CEO | Manager | Staff  | Customer (B2C) | Outbox (internal) |
|----------------------------------------|-----------|---------|--------|----------------|-------------------|
| GET /notifications                     | ✓         | ✓       | ✓      | ✓ (own)        | ✗                 |
| GET /notifications/unread              | ✓         | ✓       | ✓      | ✓              | ✗                 |
| GET /notifications/{id}                | ✓         | ✓       | ✓      | ✓ (own)        | ✗                 |
| POST /notifications (send)             | ✓         | ✓       | ✗      | ✗              | ✗                 |
| POST /notifications/bulk               | ✓         | ✓       | ✗      | ✗              | ✗                 |
| POST /notifications/broadcast          | ✓         | ✓       | ✗      | ✗              | ✗                 |
| POST /notifications/compose            | ✓         | ✓       | ✗      | ✗              | ✗                 |
| POST /notifications/{id}/retry         | ✓         | ✓       | ✗      | ✗              | ✗                 |
| PUT /notifications/{id}/read           | ✓         | ✓       | ✓      | ✓ (own)        | ✗                 |
| PUT /notifications/read-all            | ✓         | ✓       | ✓      | ✓ (own)        | ✗                 |
| DELETE /notifications/{id}             | ✓         | ✓       | ✓      | ✓ (own + X-User-Id header) | ✗        |
| GET /notifications/templates           | ✓         | ✓       | ✓      | ✗              | ✗                 |
| POST /notifications/templates          | ✓         | ✗       | ✗      | ✗              | ✗                 |
| POST /notifications/orders/paid        | ✗         | ✗       | ✗      | ✗              | ✓ (order-svc)     |
| POST /notifications/inventory/low-stock| ✗         | ✗       | ✗      | ✗              | ✓ (inventory-svc) |
| POST /notifications/inventory/expiry   | ✗         | ✗       | ✗      | ✗              | ✓ (inventory-svc) |

---

## 2. Endpoint Summary

| #  | Method | Path                                                  | Controller                  | Auth   | Purpose                                  |
|----|--------|-------------------------------------------------------|-----------------------------|--------|------------------------------------------|
| 1  | GET    | `/api/v1/notifications?recipientId=...`               | NotificationController      | permit | List user notifications                  |
| 2  | GET    | `/api/v1/notifications/unread?recipientId=...`        | NotificationController      | permit | List only SENT (unread)                  |
| 3  | GET    | `/api/v1/notifications/{id}`                          | NotificationController      | permit | Get by UUID                              |
| 4  | POST   | `/api/v1/notifications`                               | NotificationController      | permit | Send single notification                 |
| 5  | POST   | `/api/v1/notifications/bulk`                         | NotificationController      | permit | Send to multiple recipients              |
| 6  | POST   | `/api/v1/notifications/broadcast`                    | NotificationController      | permit | Send to role/branch audience             |
| 7  | POST   | `/api/v1/notifications/compose`                      | NotificationController      | permit | Template-based compose                   |
| 8  | POST   | `/api/v1/notifications/{id}/retry`                   | NotificationController      | permit | Re-attempt failed delivery               |
| 9  | PUT    | `/api/v1/notifications/{id}/read`                    | NotificationController      | permit | Mark single as read                      |
| 10 | PUT    | `/api/v1/notifications/read-all?recipientId=...`      | NotificationController      | permit | Mark all recipient's notifications read  |
| 11 | DELETE | `/api/v1/notifications/{id}`                         | NotificationController      | X-User-Id | Soft delete (TICKET-304)              |
| 12 | GET    | `/api/v1/notifications/templates`                    | NotificationTemplateController | permit | List templates                        |
| 13 | POST   | `/api/v1/notifications/templates`                    | NotificationTemplateController | permit | Create template                       |
| 14 | POST   | `/api/v1/notifications/orders/paid`                  | OutboxConsumerController    | internal | Order paid event                     |
| 15 | POST   | `/api/v1/notifications/inventory/low-stock`          | OutboxConsumerController    | internal | Low stock alert                      |
| 16 | POST   | `/api/v1/notifications/inventory/expiry`             | OutboxConsumerController    | internal | Expiry warning                        |

---

## 3. Prerequisites

1. **Environment**: Gateway running at `http://localhost:8080`.
2. **Database**: `pcms_notification` schema with `notifications`, `notification_templates`, `notification_outbox_log` tables.
3. **Seeded data**:
   - At least 1 recipient user `{{userId1}}`
   - For broadcast: multiple users with different roles
4. **Outbox events** (optional): When running tests 14-16, simulate by calling directly (outbox publisher in order/inventory services normally does this).

---

## 4. Test Data Seeding

### 4.1 Recipient Users

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create test recipients                                ║
║  METHOD: POST  PATH: /api/v1/users (x3)                      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST 1:                                                  ║
║  { "email":"recv1@pcms.vn", "fullName":"Recipient One",      ║
║    "phone":"0911111111", "role":"CUSTOMER" }                ║
║  ← CAPTURE: USER_ID_1                                        ║
║  REQUEST 2:                                                  ║
║  { "email":"recv2@pcms.vn", "fullName":"Recipient Two",      ║
║    "phone":"0922222222", "role":"CUSTOMER" }                ║
║  ← CAPTURE: USER_ID_2                                        ║
║  REQUEST 3:                                                  ║
║  { "email":"recv3@pcms.vn", "fullName":"Recipient Three",    ║
║    "phone":"0933333333", "role":"STAFF" }                   ║
║  ← CAPTURE: USER_ID_3                                        ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.2 Captured Variables

| Variable         | Example                              | Source                  |
|------------------|--------------------------------------|-------------------------|
| `{{gateway}}`    | `http://localhost:8080`              | static                  |
| `{{accessToken}}`| `eyJhbGciOiJIUzI1...`                | POST /auth/login        |
| `{{USER_ID_1}}`  | `uuid` (Recipient One)               | POST /users             |
| `{{USER_ID_2}}`  | `uuid` (Recipient Two)               | POST /users             |
| `{{USER_ID_3}}`  | `uuid` (Recipient Three - STAFF)     | POST /users             |
| `{{NOTIF_ID_1}}` | `uuid`                               | POST /notifications     |
| `{{TEMPLATE_ID}}`| `uuid`                               | POST /templates         |
| `{{OUTBOX_EVT}}` | `uuid`                               | generated for idempotency |

---

## 5. Test Cases - NotificationController

### 5.1 GET /api/v1/notifications (List)

#### Test 5.1.1 - Happy path: list by recipient

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List notifications for user                           ║
║  METHOD: GET  PATH: /api/v1/notifications?recipientId={{USER_ID_1}}&status=all&page=0&size=20║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "content": [                                              ║
║      {                                                       ║
║        "id": "uuid",                                        ║
║        "recipientId": "{{USER_ID_1}}",                       ║
║        "channel": "IN_APP",                                  ║
║        "template": null,                                     ║
║        "title": "Welcome",                                   ║
║        "body": "Hello World",                                ║
║        "status": "SENT",                                    ║
║        "sentAt": "2026-06-19T10:00:00",                      ║
║        "readAt": null,                                       ║
║        "createdAt": "2026-06-19T10:00:00"                    ║
║      }                                                       ║
║    ],                                                        ║
║    "page": 0, "size": 20, "totalElements": 1                 ║
║  }                                                          ║
║  ← VERIFY: all entries have recipientId == USER_ID_1        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.2 - Filter by status=read

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List READ notifications only                          ║
║  METHOD: GET  PATH: /api/v1/notifications?recipientId={{USER_ID_1}}&status=read║
║  ← SEED: At least 1 read notif for USER_ID_1                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: every item has status=READ                        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.3 - Filter by status=unread

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List UNREAD (status=unread maps to SENT)              ║
║  METHOD: GET  PATH: /api/v1/notifications?recipientId={{USER_ID_1}}&status=unread║
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: every item has status=SENT (unread)              ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.4 - Negative: missing recipientId (required param)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required param                                ║
║  METHOD: GET  PATH: /api/v1/notifications                   ║
║  EXPECTED: 400 Bad Request (MissingServletRequestParameterException)║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.5 - Edge: user has no notifications

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Recipient with zero notifs                            ║
║  METHOD: GET  PATH: /api/v1/notifications?recipientId={{USER_ID_NEW}}║
║  ← SEED: USER_ID_NEW = user that has never received a notif  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, content=[], totalElements=0               ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 GET /api/v1/notifications/unread

#### Test 5.2.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get unread                                            ║
║  METHOD: GET  PATH: /api/v1/notifications/unread?recipientId={{USER_ID_1}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "data": {                                                 ║
║      "content": [ ... ],                                    ║
║      "totalElements": 3                                      ║
║    }                                                         ║
║  }                                                          ║
║  ← VERIFY: all entries status=SENT (not yet READ)            ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.2 - Pagination

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pagination test                                       ║
║  METHOD: GET  PATH: /api/v1/notifications/unread?recipientId={{USER_ID_1}}&page=0&size=2║
║  ← SEED: USER_ID_1 has 5 unread notifs                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, content.length=2, totalElements=5         ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 GET /api/v1/notifications/{id}

#### Test 5.3.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by id                                             ║
║  METHOD: GET  PATH: /api/v1/notifications/{{NOTIF_ID_1}}    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: id, recipientId, status match expected values     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.3.2 - Negative: not found

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown UUID                                          ║
║  METHOD: GET  PATH: /api/v1/notifications/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.4 POST /api/v1/notifications (Send)

#### Test 5.4.1 - Happy path: IN_APP channel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Send in-app notification                              ║
║  METHOD: POST  PATH: /api/v1/notifications                  ║
║  HEADER: Content-Type: application/json                      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "recipientId": "{{USER_ID_1}}",                           ║
║    "channel": "IN_APP",                                      ║
║    "title": "Order confirmed",                               ║
║    "body": "Your order #ORD-001 has been confirmed"          ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "recipientId": "{{USER_ID_1}}",                           ║
║    "channel": "IN_APP",                                      ║
║    "status": "SENT",       ← synchronous in dev             ║
║    "sentAt": "2026-06-19T10:00:00",                          ║
║    ...                                                       ║
║  }                                                          ║
║  ← CAPTURE: NOTIF_ID_1                                       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.2 - Happy path: EMAIL channel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Send email (mocked SMTP in dev)                       ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "recipientId": "{{USER_ID_1}}",                           ║
║    "channel": "EMAIL",                                       ║
║    "title": "Receipt",                                       ║
║    "body": "Thank you for your purchase"                     ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=SENT (or PENDING if SMTP queued)   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.3 - Happy path: SMS channel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Send SMS                                              ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "recipientId": "{{USER_ID_1}}",                           ║
║    "channel": "SMS",                                         ║
║    "title": "OTP",                                           ║
║    "body": "Your OTP code is 123456"                         ║
║  }                                                          ║
║  EXPECTED: 200 OK                                            ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.4 - Happy path: scheduled sendAt (future)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Schedule notification                                 ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "recipientId": "{{USER_ID_1}}",                           ║
║    "channel": "IN_APP",                                      ║
║    "title": "Reminder",                                      ║
║    "body": "Take your medicine in 30 minutes",               ║
║    "sendAt": "2026-06-19T15:00:00"                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=PENDING                            ║
║  ← VERIFY: createdAt set, sentAt=null until scheduled time  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.5 - Negative: missing recipientId

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required field                                ║
║  REQUEST: { "channel":"IN_APP", "title":"X", "body":"Y" }   ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Mã người nhận là bắt buộc"                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.6 - Negative: empty title

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Blank title                                           ║
║  REQUEST: { "recipientId":"{{USER_ID_1}}", "channel":"IN_APP", "title":"", "body":"x" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Tiêu đề không được để trống"                    ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.7 - Negative: invalid channel enum

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bad channel                                           ║
║  REQUEST: { "recipientId":"{{USER_ID_1}}", "channel":"PUSH", "title":"X", "body":"Y" }║
║  EXPECTED: 400 Bad Request (enum mismatch)                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.5 POST /api/v1/notifications/bulk

#### Test 5.5.1 - Happy path: send to 3 users

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bulk send                                             ║
║  METHOD: POST  PATH: /api/v1/notifications/bulk             ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "recipientIds": [                                         ║
║      "{{USER_ID_1}}",                                       ║
║      "{{USER_ID_2}}",                                       ║
║      "{{USER_ID_3}}"                                        ║
║    ],                                                        ║
║    "channel": "IN_APP",                                      ║
║    "title": "System maintenance",                            ║
║    "body": "PCMS will be down 22:00-23:00 tonight"           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "message": "Notifications queued",                        ║
║    "count": 3                                                ║
║  }                                                          ║
║  ← VERIFY: GET /notifications?recipientId=USER_ID_1 → +1 entry║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.2 - Negative: empty recipientIds

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty recipients                                      ║
║  REQUEST: { "recipientIds":[], "channel":"IN_APP", "title":"x", "body":"y" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Danh sách người nhận không được để trống"       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.5.3 - Edge: single recipient via bulk

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bulk of 1                                             ║
║  REQUEST: recipientIds=[USER_ID_1]                           ║
║  EXPECTED: 200 OK, count=1                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.6 POST /api/v1/notifications/broadcast

#### Test 5.6.1 - Happy path: broadcast by user IDs

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Broadcast to specific users                           ║
║  METHOD: POST  PATH: /api/v1/notifications/broadcast        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "audience": {                                             ║
║      "users": ["{{USER_ID_1}}", "{{USER_ID_2}}"]            ║
║    },                                                        ║
║    "channels": ["IN_APP", "EMAIL"],                          ║
║    "title": "Quarterly health tips",                         ║
║    "body": "Take care of yourselves this season"             ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { "message":"Broadcast queued", "count": 2 }                ║
║  ← VERIFY: each user receives 1 IN_APP + 1 EMAIL = 2 each   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.2 - Happy path: broadcast by role

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Broadcast to role                                     ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "audience": { "roles": ["PHARMACIST"] },                 ║
║    "channels": ["IN_APP"],                                   ║
║    "title": "Shift reminder",                                ║
║    "body": "Tomorrow's roster is ready"                      ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { "message":"Broadcast queued", "count": N }                ║
║  ← VERIFY: count > 0 (depends on seeded PHARMACIST count)   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.3 - Negative: empty audience + missing title

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty audience, blank title                           ║
║  REQUEST: { "audience":{}, "channels":["IN_APP"], "title":"", "body":"" }║
║  EXPECTED: 400 Bad Request (multiple validation errors)      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.4 - Negative: empty channels list

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No channels                                           ║
║  REQUEST: { "audience":{"users":["{{USER_ID_1}}"]}, "channels":[], "title":"X", "body":"Y" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Phải chọn ít nhất một kênh gửi"                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.7 POST /api/v1/notifications/compose

#### Test 5.7.1 - Happy path: template-based

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Compose with template                                 ║
║  ← SEED: Template created (Test 6.2 below) with code=WELCOME║
║  METHOD: POST  PATH: /api/v1/notifications/compose           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "recipientIds": ["{{USER_ID_1}}"],                        ║
║    "channels": ["IN_APP"],                                   ║
║    "template": "WELCOME",                                    ║
║    "variables": {                                            ║
║      "name": "Recipient One",                                ║
║      "coupon": "WELCOME10"                                   ║
║    },                                                        ║
║    "title": "Welcome to PCMS",                               ║
║    "body": "Hi {{name}}, here is your coupon {{coupon}}"      ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { "message":"Notifications queued", "count": 1 }            ║
║  ← VERIFY: body has variables substituted                    ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.2 - Negative: unknown template

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Template code not in DB                               ║
║  REQUEST: { "recipientIds":["{{USER_ID_1}}"], "channels":["IN_APP"], "template":"UNKNOWN", "title":"x", "body":"y" }║
║  EXPECTED: 200 OK queued, but rendered title/body used as-is (no error)║
║  ← OR: 404 if template validation strict                    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.8 POST /api/v1/notifications/{id}/retry

#### Test 5.8.1 - Happy path: retry FAILED notification

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Retry failed notification                             ║
║  METHOD: POST  PATH: /api/v1/notifications/{{FAILED_NOTIF_ID}}/retry║
║  ← SEED: FAILED_NOTIF_ID with status=FAILED (e.g. SMTP down)║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "id": "{{FAILED_NOTIF_ID}}",                             ║
║    "status": "SENT",      ← re-attempted successfully        ║
║    "sentAt": "2026-06-19T11:00:00"                           ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.2 - Negative: retry already-sent

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Retry SENT notification                               ║
║  ← SEED: NOTIF_ID_1 with status=SENT                        ║
║  METHOD: POST  PATH: /api/v1/notifications/{{NOTIF_ID_1}}/retry║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK or 422                                     ║
║  ← VERIFY: status remains SENT (no-op) or message "only failed"║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.3 - Negative: retry unknown id

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown UUID                                          ║
║  METHOD: POST  PATH: /api/v1/notifications/00000000-.../retry║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.9 PUT /api/v1/notifications/{id}/read

#### Test 5.9.1 - Happy path: mark single read

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Mark as read                                           ║
║  METHOD: PUT  PATH: /api/v1/notifications/{{NOTIF_ID_1}}/read║
║  ← SEED: NOTIF_ID_1 status=SENT                             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "id": "{{NOTIF_ID_1}}",                                  ║
║    "status": "READ",                                        ║
║    "readAt": "2026-06-19T11:00:00"                           ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.2 - Negative: mark already-read (idempotent?)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Re-mark read                                          ║
║  METHOD: PUT  PATH: /api/v1/notifications/{{NOTIF_ID_1}}/read║
║  ← SEED: NOTIF_ID_1 now READ                                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (idempotent - readAt not re-updated) OR 409║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.10 PUT /api/v1/notifications/read-all

#### Test 5.10.1 - Happy path: mark all user's notifications read

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Mark all read                                         ║
║  METHOD: PUT  PATH: /api/v1/notifications/read-all?recipientId={{USER_ID_1}}║
║  ← SEED: USER_ID_1 has 5 SENT (unread) notifications         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { "updated": 5 }                                            ║
║  ← VERIFY: GET /notifications?status=unread → empty for user ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.2 - Edge: user has no unread

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Already all read                                      ║
║  ← SEED: USER_ID_NEW has 0 SENT notifications               ║
║  METHOD: PUT  PATH: /api/v1/notifications/read-all?recipientId={{USER_ID_NEW}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, { "updated": 0 }                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.10.3 - Negative: missing recipientId

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No param                                              ║
║  METHOD: PUT  PATH: /api/v1/notifications/read-all          ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.11 DELETE /api/v1/notifications/{id} (TICKET-304)

#### Test 5.11.1 - Happy path: soft delete

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Soft delete (recipient deletes own)                   ║
║  METHOD: DELETE  PATH: /api/v1/notifications/{{NOTIF_ID_2}} ║
║  HEADER:                                                     ║
║    X-User-Id: {{USER_ID_1}}       ← must be recipient       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { ... }                                                     ║
║  ← VERIFY: GET /notifications → NOTIF_ID_2 excluded (DELETED filter)║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.11.2 - Negative: missing X-User-Id

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Anonymous delete                                      ║
║  METHOD: DELETE  PATH: /api/v1/notifications/{{NOTIF_ID_3}} ║
║  HEADER: (no X-User-Id)                                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 403 Forbidden                                     ║
║  ← VERIFY: message "X-User-Id header is required"           ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.11.3 - Negative: wrong user deletes someone else's

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cross-user delete attempt                             ║
║  ← SEED: NOTIF_ID_X belongs to USER_ID_1                    ║
║  METHOD: DELETE  PATH: /api/v1/notifications/{{NOTIF_ID_X}}║
║  HEADER: X-User-Id: {{USER_ID_2}}                            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 403 Forbidden or 404 Not Found (privacy)         ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Test Cases - NotificationTemplateController

### 6.1 GET /api/v1/notifications/templates

#### Test 6.1.1 - Happy path: list active

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List active templates                                 ║
║  METHOD: GET  PATH: /api/v1/notifications/templates?active=true║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  [                                                           ║
║    {                                                         ║
║      "id": "uuid",                                          ║
║      "code": "WELCOME",                                      ║
║      "channel": "IN_APP",                                    ║
║      "titleTemplate": "Welcome {{name}}",                    ║
║      "bodyTemplate": "Hi {{name}}, your coupon is {{coupon}}",║
║      "variables": "name,coupon",                             ║
║      "active": true,                                         ║
║      "createdAt": "...",                                     ║
║      "updatedAt": "..."                                      ║
║    }                                                         ║
║  ]                                                           ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 6.1.2 - Filter by channel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List EMAIL templates                                  ║
║  METHOD: GET  PATH: /api/v1/notifications/templates?channel=EMAIL║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: every item has channel=EMAIL                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 6.1.3 - Filter by active=false

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List inactive templates                               ║
║  METHOD: GET  PATH: /api/v1/notifications/templates?active=false║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, every item has active=false                ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 6.2 POST /api/v1/notifications/templates (Create)

#### Test 6.2.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create new template                                   ║
║  METHOD: POST  PATH: /api/v1/notifications/templates        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "code": "ORDER_CONFIRM",                                  ║
║    "channel": "IN_APP",                                      ║
║    "titleTemplate": "Order {{orderNumber}} confirmed",       ║
║    "bodyTemplate": "Your order {{orderNumber}} of {{total}} VND is confirmed",║
║    "variables": "orderNumber,total",                         ║
║    "active": true                                            ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 201 Created                                       ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "code": "ORDER_CONFIRM",                                  ║
║    "active": true,                                           ║
║    ...                                                       ║
║  }                                                          ║
║  ← CAPTURE: TEMPLATE_ID                                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 6.2.2 - Negative: duplicate code

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Duplicate template code                               ║
║  REQUEST: same code as 6.2.1 (ORDER_CONFIRM)                ║
║  EXPECTED: 409 Conflict (unique constraint)                  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 6.2.3 - Negative: missing required fields

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty code                                            ║
║  REQUEST: { "code":"", "channel":"IN_APP", "titleTemplate":"x", "bodyTemplate":"y" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Mã template không được để trống"                 ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 6.2.4 - Edge: create inactive template

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Inactive template                                     ║
║  REQUEST: ... "active": false                                ║
║  EXPECTED: 201 Created, active=false                          ║
║  ← VERIFY: not returned by default /templates?active=true   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Test Cases - OutboxConsumerController (Internal)

> **NOTE**: These endpoints are NOT exposed to end-users. They are called by order-service and inventory-service when their transactional outbox table is flushed. They use `X-Outbox-Event-Id` header for idempotency.

### 7.1 POST /api/v1/notifications/orders/paid

#### Test 7.1.1 - Happy path: order paid event

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Order paid event from order-service                   ║
║  METHOD: POST  PATH: /api/v1/notifications/orders/paid       ║
║  HEADER:                                                     ║
║    X-Outbox-Event-Id: {{OUTBOX_EVT_1}}    ← uuid v4         ║
║    Content-Type    : application/json                       ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "orderId": "{{orderId}}",                                 ║
║    "orderNumber": "ORD-20260619-0001",                       ║
║    "customerId": "{{customerId}}",                           ║
║    "branchId": "{{branchHQ}}",                               ║
║    "staffId": "{{staff1}}",                                  ║
║    "total": 350000,                                          ║
║    "paidAt": "2026-06-19T10:00:00"                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "status": "processed",                                    ║
║    "notificationId": "uuid"                                  ║
║  }                                                          ║
║  ← VERIFY: customer receives "Order confirmed" notification  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 7.1.2 - Idempotency: replay same event

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Replay OUTBOX_EVT_1                                   ║
║  HEADER: X-Outbox-Event-Id: {{OUTBOX_EVT_1}}  ← SAME        ║
║  REQUEST BODY: same as 7.1.1                                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "status": "duplicate",   ← not processed twice           ║
║    "notificationId": null                                    ║
║  }                                                          ║
║  ← VERIFY: customer has only 1 notification for this order  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 7.1.3 - Negative: missing required fields

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No orderId                                            ║
║  REQUEST: { "orderNumber":"x", "customerId":"{{customerId}}", "total":100 }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Mã đơn hàng là bắt buộc"                       ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 7.2 POST /api/v1/notifications/inventory/low-stock

#### Test 7.2.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Low stock alert from inventory-service                ║
║  METHOD: POST  PATH: /api/v1/notifications/inventory/low-stock║
║  HEADER:                                                     ║
║    X-Outbox-Event-Id: {{OUTBOX_EVT_2}}                       ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "branchId": "{{branchHQ}}",                               ║
║    "medicineId": "{{med1}}",                                 ║
║    "medicineName": "Paracetamol 500mg",                      ║
║    "qtyOnHand": 5,                                           ║
║    "minQty": 20,                                             ║
║    "recipientId": "{{managerId}}"                            ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=processed                          ║
║  ← VERIFY: manager receives low-stock notification           ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 7.2.2 - Negative: missing qty fields

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No qtyOnHand                                          ║
║  REQUEST: { "branchId":"{{branchHQ}}", "medicineId":"{{med1}}", "minQty":10 }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Số lượng tồn là bắt buộc"                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 7.2.3 - Idempotency

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Replay OUTBOX_EVT_2                                   ║
║  EXPECTED: 200 OK, status=duplicate                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 7.3 POST /api/v1/notifications/inventory/expiry

#### Test 7.3.1 - Happy path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Expiry alert from inventory-service                   ║
║  METHOD: POST  PATH: /api/v1/notifications/inventory/expiry  ║
║  HEADER: X-Outbox-Event-Id: {{OUTBOX_EVT_3}}                 ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "branchId": "{{branchHQ}}",                               ║
║    "medicineId": "{{med1}}",                                 ║
║    "medicineName": "Paracetamol 500mg",                      ║
║    "batchNo": "BTH-001",                                     ║
║    "expiryDate": "2026-07-15",                               ║
║    "recipientId": "{{managerId}}"                            ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=processed                          ║
║  ← VERIFY: manager receives "Expiry alert" notification     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 7.3.2 - Negative: missing batchNo

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No batchNo                                            ║
║  REQUEST: { "branchId":"{{branchHQ}}", "medicineId":"{{med1}}", "expiryDate":"2026-07-15" }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "Mã lô là bắt buộc"                              ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 7.3.3 - Idempotency

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Replay OUTBOX_EVT_3                                   ║
║  EXPECTED: 200 OK, status=duplicate                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 8. Business Rules Summary

| Rule                                                | Enforcement                                      |
|-----------------------------------------------------|--------------------------------------------------|
| Recipient can only delete own notifications         | X-User-Id header required (TICKET-304)           |
| Anonymous deletes rejected                          | 403 Forbidden                                     |
| Soft-deleted notifications excluded from list       | WHERE status != DELETED                          |
| Outbox events are idempotent                        | Same X-Outbox-Event-Id returns DUPLICATE          |
| Templates cached server-side                        | DB lookup once per (code, channel) tuple         |
| Broadcast expands to N recipients                   | One IN_APP / EMAIL / SMS per recipient × channel |
| Read-all is bulk SQL UPDATE                         | 1 round-trip, not N                              |

---

## 9. Post-Test Verification

```
╔══════════════════════════════════════════════════════════════╗
║  CHECK after all tests:                                      ║
║    GET /notifications?recipientId=USER_ID_1 → notifs present ║
║    SELECT status, COUNT(*) FROM notifications                ║
║      GROUP BY status;                                        ║
║      → SENT, READ, PENDING, FAILED, DELETED counts as expected║
║    SELECT * FROM notification_templates WHERE active=true;   ║
║      → list of templates                                     ║
║    SELECT * FROM notification_outbox_log;                    ║
║      → PROCESSED + DUPLICATE entries from idempotency tests  ║
╚══════════════════════════════════════════════════════════════╝
```

---

**End of 11-NOTIFICATION.md** → Next: [`12-REPORT.md`](./12-REPORT.md)
