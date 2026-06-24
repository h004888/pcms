# UAT Test Scenario: Mobile BFF Service

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `mobile-bff` (port 8096)
**UAT Doc Reference:** `15-MOBILE-BFF.md`
**Coverage:** UC17 (Mobile Backend-for-Frontend - MOBILE-HOME, MOBILE-MED-REMINDER, inline passthroughs)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  MOBILE-BFF                                                                  ║
║  Tier    : B2C (Aggregator pattern)                                           ║
║  Port    : 8096 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/mobile/**                            ║
║            http://localhost:8080/api/v1/branches/**   (passthrough)          ║
║            http://localhost:8080/api/v1/notifications/** (passthrough)       ║
║            http://localhost:8080/api/v1/orders/**     (passthrough)          ║
║  Auth    : JWT HS256 - permitAll in dev                                       ║
║  DB      : MySQL 8 (schema = mobile_bff_db) - reminders only                 ║
║  Tests   : 9 endpoints (2 controllers + 3 client passthroughs), est. 45 min   ║
║                                                                              ║
║  Note   : This is a BFF (Backend-for-Frontend). It AGGREGATES multiple        ║
║           downstream calls (notification + order + branch + reminder) into    ║
║           a single payload for the mobile app. Response latency may be       ║
║           higher (parallel calls to 4+ services, expect 200-600ms).          ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller             | Method | Path                                            | Description                | Auth (prod) | Test Cases |
|----|------------------------|--------|-------------------------------------------------|----------------------------|-------------|-----------:|
| 1  | MobileController       | GET    | `/mobile/home`                                  | Aggregated home page       | Customer    |          4 |
| 2  | MobileController       | GET    | `/mobile/nearby-pharmacies`                     | Nearby branches (stub)     | Customer    |          2 |
| 3  | ReminderController     | POST   | `/mobile/medication-reminders`                  | Create reminder            | Customer    |          4 |
| 4  | ReminderController     | GET    | `/mobile/medication-reminders`                  | List active reminders      | Customer    |          3 |
| 5  | ReminderController     | PUT    | `/mobile/medication-reminders/{id}/deactivate`  | Deactivate reminder        | Customer    |          3 |
| 6  | ReminderController     | DELETE | `/mobile/medication-reminders/{id}`             | Delete reminder            | Customer    |          2 |
| 7  | BranchClient (proxy)   | GET    | `/api/v1/branches`                              | Pass-through to branches   | -           |          1 |
| 8  | NotificationClient (proxy) | GET| `/api/v1/notifications`                         | Pass-through               | -           |          1 |
| 9  | OrderClient (proxy)    | GET    | `/api/v1/orders`                                | Pass-through               | -           |          1 |
| **TOTAL**                 |        |                                                 |                            |             |     **~21**|

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `mobile_bff_db` migrated
- [x] Eureka: `MOBILE-BFF` registered
- [x] All downstream services UP: branch, notification, order, customer-portal
- [x] At least 1 B2C customer with orders + notifications (for /home aggregation)
- [x] Gateway routes `/api/v1/mobile/**`, `/api/v1/branches/**`, `/api/v1/notifications/**`, `/api/v1/orders/**` all working

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/MOBILE-BFF           ║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8080/api/v1/mobile/home               ║
║       -H 'X-User-Id: {{customerId}}'                          ║
║  → 200 OK with all 5 sections                                 ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  No service-specific seed needed (BFF has no admin data).     ║
║                                                              ║
║  However, for /mobile/home to be meaningful, customer must   ║
║  have:                                                        ║
║    - ≥1 notification (notification-service)                   ║
║    - ≥1 order (order-service)                                 ║
║    - ≥1 reminder (this service)                              ║
║                                                              ║
║  Use the regular flows in 07-CUSTOMER, 08-ORDER,             ║
║  11-NOTIFICATION, 13-CUSTOMER-PORTAL to set those up.        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 4. Authorization Matrix (B2C only)

| Endpoint                                            | Customer (B2C) | Other role | Dev behavior |
|-----------------------------------------------------|----------------|------------|--------------|
| GET /mobile/home                                    | ✓              | ✗          | permitAll    |
| GET /mobile/nearby-pharmacies                       | ✓              | ✗          | permitAll    |
| POST /mobile/medication-reminders                   | ✓              | ✗          | permitAll    |
| GET /mobile/medication-reminders                    | ✓              | ✗          | permitAll    |
| PUT /mobile/medication-reminders/{id}/deactivate    | ✓ (own)        | ✗          | permitAll    |
| DELETE /mobile/medication-reminders/{id}            | ✓ (own)        | ✗          | permitAll    |
| GET /api/v1/branches                                | ✓ (own area)   | ✓          | permitAll    |
| GET /api/v1/notifications                           | ✓ (own)        | ✓          | permitAll    |
| GET /api/v1/orders                                  | ✓ (own)        | ✓          | permitAll    |

---

## 5. Variables to Capture

| Variable             | Example                                | Captured from            |
|----------------------|----------------------------------------|--------------------------|
| `{{gateway}}`        | `http://localhost:8080`                | (static)                 |
| `{{customerToken}}`  | `eyJhbGciOiJIUzI1...`                  | Master Plan §7 Step 2    |
| `{{customerId}}`     | `uuid`                                 | Master Plan §7 Step 2    |
| `{{reminderId}}`     | `uuid`                                 | TC-03 below              |
| `{{familyMemberId}}` | `uuid`                                 | from 13-CUSTOMER-PORTAL  |

---

## TC-01: GET /mobile/home (Aggregated home page)

### TC-01a: Get Home - Happy Path (all sections populated)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get aggregated mobile home page                      ║
║  METHOD: GET  PATH: /api/v1/mobile/home                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
║    X-User-Id     : {{customerId}}         ← required       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "userId": "{{customerId}}",                              ║
║    "role": "CUSTOMER",                                      ║
║    "recentNotifications": [                                 ║
║      { "id":"...","title":"Order confirmed",                ║
║        "channel":"PUSH","read":false, ... },                ║
║      ...                                                    ║
║    ],                                                       ║
║    "recentOrders": [                                        ║
║      { "id":"...","orderNumber":"ORD-001",                  ║
║        "status":"DELIVERED","total":150000, ... },          ║
║      ...                                                    ║
║    ],                                                       ║
║    "activeReminders": [                                     ║
║      { "id":"{{reminderId}}","medicineName":"Paracetamol",  ║
║        "frequency":"MORNING,EVENING", "active":true,        ║
║        "scheduleTime":"08:00,20:00", ... },                 ║
║      ...                                                    ║
║    ],                                                       ║
║    "nearbyPharmacies": [],        ← stub, always empty     ║
║    "aiChatContext": { "lastSessionId":null, "suggestions":[]}║
║  }                                                          ║
║  ← VERIFY: 5 top-level sections all present                 ║
║  ← VERIFY: recentNotifications has 0+ items                 ║
║  ← VERIFY: recentOrders has 0+ items                       ║
║  ← VERIFY: nearbyPharmacies === [] (PostGIS not wired)     ║
║  ← ASYNC: response may take 200-600ms (4 downstream calls)  ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01b: Get Home - Happy Path (no data yet, new customer)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get home for freshly registered customer             ║
║  ← SEED: register a new customer (no orders/notifs yet)     ║
║  METHOD: GET  PATH: /api/v1/mobile/home                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    X-User-Id : {{newCustomerId}}                            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "userId": "{{newCustomerId}}",                           ║
║    "role": "CUSTOMER",                                      ║
║    "recentNotifications": [],                               ║
║    "recentOrders":       [],                                ║
║    "activeReminders":    [],                                ║
║    "nearbyPharmacies":   [],                                ║
║    "aiChatContext":      {...}                              ║
║  }                                                          ║
║  ← VERIFY: empty arrays (NOT 404)                           ║
║  ← VERIFY: status 200 even when all sections empty          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01c: Get Home - Negative Path (missing X-User-Id header)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call without X-User-Id                               ║
║  METHOD: GET  PATH: /api/v1/mobile/home                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    (no X-User-Id)                                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    401 Unauthorized                                         ║
║  { "code":"AUTH_003",                                       ║
║    "message":"X-User-Id header is required" }               ║
║  ← VERIFY: clear error message about missing header         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01d: Get Home - Negative Path (invalid X-User-Id format)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pass non-UUID string                                 ║
║  METHOD: GET  PATH: /api/v1/mobile/home                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    X-User-Id : not-a-uuid                                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  { "code":"VAL_003", "message":"X-User-Id must be UUID" }  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /mobile/nearby-pharmacies (Stub)

### TC-02a: Nearby - Happy Path (always returns empty)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get nearby pharmacies                                ║
║  METHOD: GET  PATH: /api/v1/mobile/nearby-pharmacies        ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    lat=10.7626&lng=106.6602    ← Ho Chi Minh City coords   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
║  ← VERIFY: always empty list (PostGIS integration TBD)      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02b: Nearby - No params

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call without lat/lng                                 ║
║  METHOD: GET  PATH: /api/v1/mobile/nearby-pharmacies        ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    (none)                                                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
║  ← VERIFY: graceful handling of missing optional params     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /mobile/medication-reminders (Create reminder)

### TC-03a: Create Reminder - Happy Path (for self)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create medication reminder                           ║
║  METHOD: POST  PATH: /api/v1/mobile/medication-reminders    ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Content-Type  : application/json                         ║
║    Authorization : Bearer {{customerToken}}   ← optional    ║
║    X-User-Id     : {{customerId}}                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "familyMemberId": null,                                  ║
║    "medicineName": "Paracetamol 500mg",                     ║
║    "dosage": "1 tablet",                                    ║
║    "frequency": "MORNING,EVENING",                          ║
║    "scheduleTime": "08:00,20:00",                           ║
║    "startDate": "2026-06-20",                               ║
║    "endDate":   "2026-07-20",                               ║
║    "notes": "After meal"                                    ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{reminderId}}",       ← CAPTURE                  ║
║    "customerId": "{{customerId}}",                          ║
║    "familyMemberId": null,                                  ║
║    "medicineName": "Paracetamol 500mg",                     ║
║    "dosage": "1 tablet",                                    ║
║    "frequency": "MORNING,EVENING",                          ║
║    "scheduleTime": "08:00,20:00",                           ║
║    "startDate": "2026-06-20",                               ║
║    "endDate":   "2026-07-20",                               ║
║    "active": true,                                          ║
║    "notes": "After meal",                                   ║
║    "createdAt": "2026-06-19T..."                            ║
║  }                                                          ║
║  ← CAPTURE: id as {{reminderId}}                            ║
║  ← VERIFY: active === true (default for new reminder)       ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03b: Create Reminder - For Family Member

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create reminder for family member                    ║
║  ← SEED: a family member (see 13-CUSTOMER-PORTAL TC-12)     ║
║  METHOD: POST  PATH: /api/v1/mobile/medication-reminders    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "familyMemberId": "{{familyMemberId}}",                  ║
║    "medicineName": "Amlodipine 5mg",                        ║
║    "dosage": "1 tablet",                                    ║
║    "frequency": "MORNING",                                  ║
║    "scheduleTime": "07:00",                                 ║
║    "startDate": "2026-06-20",                               ║
║    "endDate":   null,                                       ║
║    "notes": "For mother - high blood pressure"              ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { ...familyMemberId === {{familyMemberId}}... }            ║
║  ← VERIFY: familyMemberId populated correctly               ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03c: Create Reminder - Negative Path (missing required fields)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit incomplete reminder                           ║
║  METHOD: POST  PATH: /api/v1/mobile/medication-reminders    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT (only customerId)        ║
║  {                                                          ║
║    "customerId": "{{customerId}}"                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  {                                                          ║
║    "code":"VAL_001","message":"Validation failed",          ║
║    "errors":[                                               ║
║      { "field":"medicineName","message":"medicineName is required" },║
║      { "field":"frequency","message":"frequency is required" },║
║      { "field":"startDate","message":"startDate is required" }║
║    ]                                                        ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03d: Create Reminder - Invalid Frequency

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pass invalid frequency value                         ║
║  METHOD: POST  PATH: /api/v1/mobile/medication-reminders    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "medicineName":"Test",                                   ║
║    "frequency":"WEEKDAYS_12345",                            ║
║    "startDate":"2026-06-20"                                 ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    422 Unprocessable Entity                                 ║
║  { "code":"BIZ_003",                                        ║
║    "message":"frequency must be one of: MORNING/NOON/EVENING/NIGHT/CUSTOM" }║
║                                                              ║
║  (Note: depends on whether enum is enforced)                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: GET /mobile/medication-reminders (List active)

### TC-04a: List - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List active reminders for customer                   ║
║  METHOD: GET  PATH: /api/v1/mobile/medication-reminders     ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    customerId={{customerId}}                                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{reminderId}}", "medicineName":"Paracetamol 500mg",║
║      "frequency":"MORNING,EVENING", "active":true, ... },   ║
║    { "id":"...","medicineName":"Amlodipine 5mg",            ║
║      "frequency":"MORNING", "active":true, ... }            ║
║  ]                                                          ║
║  ← VERIFY: includes reminder from TC-03a                    ║
║  ← VERIFY: only active reminders                            ║
║  ← VERIFY: for {{customerId}} only                          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04b: List - Empty (no reminders)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List for customer with no reminders                  ║
║  METHOD: GET  PATH: /api/v1/mobile/medication-reminders     ║
║  QUERY PARAMS:                                             ║
║    customerId=00000000-0000-0000-0000-000000000000          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
║  ← VERIFY: empty array (NOT 404)                            ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04c: List - Negative Path (missing customerId)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call without customerId query                        ║
║  METHOD: GET  PATH: /api/v1/mobile/medication-reminders     ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    (none)                                                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  { "code":"VAL_002",                                        ║
║    "message":"customerId query parameter is required" }     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: PUT /mobile/medication-reminders/{id}/deactivate

### TC-05a: Deactivate Reminder - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Deactivate a reminder (soft-disable)                ║
║  METHOD: PUT  PATH: /api/v1/mobile/medication-reminders/{{reminderId}}/deactivate║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{reminderId}}",                                  ║
║    "medicineName": "Paracetamol 500mg",                     ║
║    "active": false,                ← VERIFY                 ║
║    ...                                                      ║
║  }                                                          ║
║  ← VERIFY: subsequent GET /mobile/medication-reminders      ║
║    does NOT include {{reminderId}} anymore                  ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-05b: Deactivate Reminder - Negative Path (non-existent UUID)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Deactivate unknown reminder                          ║
║  METHOD: PUT  PATH: /api/v1/mobile/medication-reminders/00000000-0000-0000-0000-000000000000/deactivate║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Reminder not found" }        ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-05c: Deactivate Already-Deactivated Reminder (idempotent)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Deactivate already-deactivated reminder              ║
║  METHOD: PUT  PATH: /api/v1/mobile/medication-reminders/{{reminderId}}/deactivate║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{reminderId}}", "active":false, ... }             ║
║  ← VERIFY: idempotent - no error                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: DELETE /mobile/medication-reminders/{id}

### TC-06a: Delete Reminder - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Permanently delete reminder                          ║
║  METHOD: DELETE  PATH: /api/v1/mobile/medication-reminders/{{reminderId}}║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    204 No Content                                           ║
║  ← VERIFY: subsequent GET does not include this reminder    ║
║  ← VERIFY: hard delete (not soft - cannot reactivate)       ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-06b: Delete Reminder - Negative Path (non-existent UUID)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Delete unknown reminder                              ║
║  METHOD: DELETE  PATH: /api/v1/mobile/medication-reminders/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Reminder not found" }        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-07: GET /api/v1/branches (Inline pass-through)

### TC-07a: Branches Pass-through

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call pass-through for branches                       ║
║  METHOD: GET  PATH: /api/v1/branches                        ║
║  ← Note: This route is registered on BOTH api-gateway       ║
║    (→ branch-service) AND on mobile-bff BranchClient.       ║
║    Gateway routes win, so this hits branch-service directly. ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    page=0&size=20                                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "data":[ { "id":"{{branchHQ}}", "code":"HQ", ... } ],    ║
║    "page":0,"size":20,"total":3,... }                       ║
║  ← VERIFY: same shape as branch-service direct call          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-08: GET /api/v1/notifications (Inline pass-through)

### TC-08a: Notifications Pass-through

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call pass-through for notifications                  ║
║  METHOD: GET  PATH: /api/v1/notifications                   ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    X-User-Id     : {{customerId}}                           ║
║    Authorization : Bearer {{customerToken}}                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "data":[ ...notifications for {{customerId}}... ],      ║
║    ... }                                                    ║
║  ← VERIFY: same shape as notification-service direct call   ║
║  ← VERIFY: filtered to current customer only                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-09: GET /api/v1/orders (Inline pass-through)

### TC-09a: Orders Pass-through

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call pass-through for orders                         ║
║  METHOD: GET  PATH: /api/v1/orders                          ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    X-User-Id     : {{customerId}}                           ║
║    Authorization : Bearer {{customerToken}}                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "data":[ ...orders for {{customerId}}... ], ... }        ║
║  ← VERIFY: same shape as order-service direct call          ║
║  ← VERIFY: B2C scoped to own orders                         ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 10. Edge Cases & Notes

```
╔══════════════════════════════════════════════════════════════╗
║  EDGE-01: Downstream service down (circuit breaker)          ║
║    If notification-service is down, /mobile/home should      ║
║    still return 200 with empty recentNotifications[]         ║
║    (graceful degradation). Verify by killing the service.   ║
║                                                              ║
║  EDGE-02: Concurrent reminder creates                        ║
║    Multiple POSTs at the same instant for the same           ║
║    customer/medicine - all should succeed (no unique         ║
║    constraint on (customer, medicine) - allowed dupes).      ║
║                                                              ║
║  EDGE-03: Aggregator latency                                  ║
║    /mobile/home does 4+ parallel HTTP calls.                  ║
║    p95 latency should be < 600ms. Use LoadRunner.            ║
║                                                              ║
║  EDGE-04: scheduleTime format                                ║
║    Must match regex HH:MM,HH:MM (24h format).                ║
║    "8:00" or "8:00am" should fail validation.                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 11. Pass / Fail Summary

```
╔══════════════════════════════════════════════════════════════╗
║  Total test cases: 21                                        ║
║  Expected pass: ≥19 (≥90%)                                   ║
║  Known gaps   : PostGIS for nearby-pharmacies (returns [])   ║
║                                                              ║
║  Sign-off: ___________________  Date: ___________             ║
╚══════════════════════════════════════════════════════════════╝
```

**End of `15-MOBILE-BFF.md`**
