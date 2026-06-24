# UAT Test Scenario: Pharmacist Workbench Service

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `pharmacist-workbench-service` (port 8094)
**UAT Doc Reference:** `14-PHARMACIST-WORKBENCH.md`
**Coverage:** UC16 (Pharmacist Workbench - RX-CONSULT, RX-CUST-PROFILE-360, RX-FOLLOW-UP, RX-CROSS-SELL, RX-VIP-MARK)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  PHARMACIST-WORKBENCH-SERVICE                                                 ║
║  Tier    : B2C (pharmacist-facing - role-restricted in prod)                  ║
║  Port    : 8094 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/consultations/**                      ║
║            http://localhost:8080/api/v1/rx/**                                ║
║            http://localhost:8080/api/v1/follow-ups/**                        ║
║            http://localhost:8080/api/v1/vip-marks/**                         ║
║  Auth    : JWT HS256 - permitAll in dev                                       ║
║             • /consultations, /rx, /follow-ups → Admin/Mgr/Pharmacist (prod) ║
║             • /vip-marks → Admin/Mgr/Pharmacist (prod)                       ║
║  DB      : MySQL 8 (schema = pharmacist_workbench_db)                        ║
║  Tests   : 18 endpoints, 5 controllers, ~50 cases, est. 90 min                ║
║                                                                              ║
║  Note   : This service is for INTERNAL pharmacy staff, not for end-customers.║
║           X-User-Id header carries pharmacist/admin UUID in B2C namespace.   ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller             | Method | Path                                          | Description                 | Auth (prod) | Test Cases |
|----|------------------------|--------|-----------------------------------------------|-----------------------------|-------------|-----------:|
| 1  | ConsultationController | POST   | `/consultations`                              | Start new consultation       | Pharmacist  |          4 |
| 2  | ConsultationController | GET    | `/consultations/{id}`                         | Get consultation             | Pharmacist  |          2 |
| 3  | ConsultationController | POST   | `/consultations/{id}/end`                     | End consultation             | Pharmacist  |          2 |
| 4  | ConsultationController | POST   | `/consultations/{id}/messages`                | Append message               | Pharmacist  |          2 |
| 5  | ConsultationController | GET    | `/consultations/by-customer/{customerId}`     | List by customer             | Pharmacist  |          2 |
| 6  | ConsultationController | GET    | `/consultations/by-pharmacist/{pharmacistId}` | List by pharmacist           | Pharmacist  |          2 |
| 7  | Customer360Controller  | GET    | `/rx/customers/{id}/profile-360`              | Customer 360° profile        | Pharmacist  |          3 |
| 8  | FollowUpController     | POST   | `/follow-ups`                                 | Schedule follow-up           | Pharmacist  |          3 |
| 9  | FollowUpController     | GET    | `/follow-ups/by-customer/{customerId}`        | List follow-ups for customer | Pharmacist  |          2 |
| 10 | FollowUpController     | POST   | `/follow-ups/{id}/response`                   | Record customer response     | Pharmacist  |          3 |
| 11 | FollowUpController     | DELETE | `/follow-ups/{id}`                            | Cancel follow-up             | Pharmacist  |          2 |
| 12 | RxAiController         | POST   | `/rx/cross-sell`                              | AI cross-sell suggestion     | Pharmacist  |          3 |
| 13 | RxAiController         | POST   | `/rx/drug-check`                              | AI drug interaction check    | Pharmacist  |          3 |
| 14 | VipMarkController      | POST   | `/vip-marks`                                  | Mark customer VIP            | Pharmacist  |          3 |
| 15 | VipMarkController      | GET    | `/vip-marks/by-customer/{customerId}`         | Get VIP mark for customer    | Pharmacist  |          2 |
| 16 | VipMarkController      | GET    | `/vip-marks/by-tier/{tier}`                   | List VIPs by tier            | Pharmacist  |          3 |
| 17 | VipMarkController      | GET    | `/vip-marks`                                  | List all VIPs                | Pharmacist  |          2 |
| 18 | VipMarkController      | DELETE | `/vip-marks/{customerId}`                     | Remove VIP mark              | Pharmacist  |          2 |
| **TOTAL**                 |        |                                               |                             |             |     **~45**|

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `pharmacist_workbench_db` migrated
- [x] Eureka: `PHARMACIST-WORKBENCH-SERVICE` registered
- [x] ai-engine-service UP (port 8095) for /rx/cross-sell and /rx/drug-check
- [x] At least 1 pharmacist user exists (`{{pharmacistId}}`)
- [x] At least 1 customer exists (`{{customerId}}`)
- [x] At least 3 medicines exist (`{{med1}}`, `{{med2}}`, `{{med3}}`) for drug-check

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/PHARMACIST-WORKBENCH-SERVICE║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8080/api/v1/rx/customers/00000000-0000-0000-0000-000000000001/profile-360║
║  → 200 OK                                                     ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  No service-specific seed needed. Pre-requisite:              ║
║                                                              ║
║  - 1 pharmacist from user-service:                            ║
║      email : pharmacist@pcms.vn / pharmacist123              ║
║      role  : PHARMACIST                                       ║
║      id    : {{pharmacistId}}                                 ║
║                                                              ║
║  - 1 customer from customer-service (or B2C):                 ║
║      email : customer@pcms.vn / customer123                  ║
║      id    : {{customerId}}                                   ║
║                                                              ║
║  - Customer must have ≥1 order + ≥1 prescription for         ║
║    profile-360 to be meaningful.                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 4. Authorization Matrix (B2C role-restricted)

| Endpoint group                              | Admin | Manager | Pharmacist | Customer (B2C) | Dev |
|---------------------------------------------|-------|---------|------------|----------------|------|
| /consultations/**                           | ✓     | ✓       | ✓          | ✗              | permitAll |
| /rx/customers/{id}/profile-360              | ✓     | ✓       | ✓          | ✗              | permitAll |
| /rx/cross-sell, /rx/drug-check              | ✓     | ✓       | ✓          | ✗              | permitAll |
| /follow-ups/**                              | ✓     | ✓       | ✓          | ✗              | permitAll |
| /vip-marks/**                               | ✓     | ✓       | ✓          | ✗              | permitAll |

---

## 5. Variables to Capture

| Variable             | Example                                | Captured from            |
|----------------------|----------------------------------------|--------------------------|
| `{{gateway}}`        | `http://localhost:8080`                | (static)                 |
| `{{accessToken}}`    | `eyJhbGciOiJIUzI1...`                  | `01-AUTH-USER.md` TC-01  |
| `{{pharmacistToken}}`| `eyJhbGciOiJIUzI1...` (role=PHARMACIST) | `01-AUTH-USER.md`        |
| `{{pharmacistId}}`   | `uuid`                                 | `01-AUTH-USER.md` TC-09  |
| `{{customerId}}`     | `uuid`                                 | Master Plan §7 Step 2    |
| `{{consultId}}`      | `uuid`                                 | TC-01 below              |
| `{{followUpId}}`     | `uuid`                                 | TC-08 below              |
| `{{vipMarkId}}`      | `uuid` (from response)                 | TC-14 below              |
| `{{med1}}`-`{{med3}}`| `uuid`                                 | Master Plan §7 Step 6    |

---

## TC-01: POST /consultations (Start consultation)

### TC-01a: Start Consultation - Happy Path (TEXT)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Start new text consultation                           ║
║  METHOD: POST  PATH: /api/v1/consultations                  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{pharmacistToken}}                ║
║    X-User-Id     : {{pharmacistId}}    ← pharmacist acting ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "channel": "TEXT"                                        ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{consultId}}",           ← CAPTURE              ║
║    "customerId": "{{customerId}}",                          ║
║    "pharmacistId": "{{pharmacistId}}",                      ║
║    "channel": "TEXT",                                       ║
║    "status": "ACTIVE",                                      ║
║    "startedAt": "2026-06-19T10:00:00",                      ║
║    "endedAt": null                                          ║
║  }                                                          ║
║  ← CAPTURE: id as {{consultId}}                             ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01b: Start Consultation - VOICE Channel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Start voice consultation                             ║
║  METHOD: POST  PATH: /api/v1/consultations                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "customerId":"{{customerId}}", "channel":"VOICE" }       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "channel":"VOICE", "status":"ACTIVE", ... }              ║
║  ← VERIFY: channel === "VOICE"                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01c: Start Consultation - VIDEO Channel

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Start video consultation                             ║
║  METHOD: POST  PATH: /api/v1/consultations                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "customerId":"{{customerId}}", "channel":"VIDEO" }       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "channel":"VIDEO", ... }                                 ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01d: Start Consultation - Negative Path (missing fields)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit with missing fields                           ║
║  METHOD: POST  PATH: /api/v1/consultations                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT (empty)                 ║
║  {}                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  { "code":"VAL_001","message":"Validation failed",          ║
║    "errors":[                                               ║
║      { "field":"customerId","message":"customerId is required" },║
║      { "field":"channel","message":"channel is required" }   ║
║    ] }                                                      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /consultations/{id}

### TC-02a: Get Consultation - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get consultation by id                               ║
║  METHOD: GET  PATH: /api/v1/consultations/{{consultId}}     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{consultId}}", "customerId":"...",                ║
║    "status":"ACTIVE", "channel":"TEXT", ... }               ║
║  ← VERIFY: same as TC-01a response                         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02b: Get Consultation - Negative Path (non-existent)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get unknown consultation                             ║
║  METHOD: GET  PATH: /api/v1/consultations/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Consultation not found" }     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /consultations/{id}/end

### TC-03a: End Consultation - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: End the active consultation                          ║
║  METHOD: POST  PATH: /api/v1/consultations/{{consultId}}/end║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{consultId}}", "status":"ENDED",                  ║
║    "endedAt":"2026-06-19T10:30:00", ... }                   ║
║  ← VERIFY: status === "ENDED" (was "ACTIVE")                ║
║  ← VERIFY: endedAt populated                                ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03b: End Consultation - Negative (already ended)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Try to end an already-ENDED consultation            ║
║  METHOD: POST  PATH: /api/v1/consultations/{{consultId}}/end║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    409 Conflict                                             ║
║  { "code":"BIZ_004",                                        ║
║    "message":"Consultation already ended" }                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: POST /consultations/{id}/messages

### TC-04a: Append Message - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Append a chat message to consultation                ║
║  ← SEED: start a NEW consultation first (TC-01)              ║
║  METHOD: POST  PATH: /api/v1/consultations/{{newConsultId}}/messages║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  "Xin chào, tôi cần tư vấn về thuốc Paracetamol"          ║
║                                                              ║
║  (raw string body, not JSON object)                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{newConsultId}}", "status":"ACTIVE", ... }        ║
║  ← VERIFY: message appended (check via DB or transcript GET)║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04b: Append Message - Negative (non-existent consultation)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Append to unknown consultation                       ║
║  METHOD: POST  PATH: /api/v1/consultations/00000000-0000-0000-0000-000000000000/messages║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  "test message"                                              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Consultation not found" }     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: GET /consultations/by-customer/{customerId}

### TC-05a: List By Customer - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all consultations for a customer                ║
║  METHOD: GET  PATH: /api/v1/consultations/by-customer/{{customerId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{consultId}}", "status":"ENDED", ... },         ║
║    { "id":"{{newConsultId}}", "status":"ACTIVE", ... }      ║
║  ]                                                          ║
║  ← VERIFY: includes both consultations from TC-01 + TC-04  ║
║  ← VERIFY: sorted by startedAt DESC                         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-05b: List By Customer - Empty

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer with no consultations                       ║
║  METHOD: GET  PATH: /api/v1/consultations/by-customer/00000000-0000-0000-0000-000000000099║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: GET /consultations/by-pharmacist/{pharmacistId}

### TC-06a: List By Pharmacist - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List active consultations handled by this pharmacist ║
║  METHOD: GET  PATH: /api/v1/consultations/by-pharmacist/{{pharmacistId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [ { "id":"...", "customerId":"...", "channel":"TEXT",     ║
║      "status":"ACTIVE", ... } ]                             ║
║  ← VERIFY: only ACTIVE consultations                        ║
║  ← VERIFY: pharmacistId === {{pharmacistId}}                ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-06b: List By Pharmacist - Empty

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pharmacist with no active consultations              ║
║  METHOD: GET  PATH: /api/v1/consultations/by-pharmacist/00000000-0000-0000-0000-000000000099║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-07: GET /rx/customers/{id}/profile-360

### TC-07a: Get Customer 360 - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get full customer profile for pharmacist view        ║
║  METHOD: GET  PATH: /api/v1/rx/customers/{{customerId}}/profile-360║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{pharmacistToken}}                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "fullName": "Nguyen Van Customer",                       ║
║    "phone": "0901234567",                                   ║
║    "tier": "GOLD",                                          ║
║    "points": 1500,                                          ║
║    "allergies": [ "Penicillin" ],                           ║
║    "chronicConditions": [ "Hypertension" ],                 ║
║    "familyMembers": [                                       ║
║      { "memberName":"Nguyen Thi B", "relationship":"SPOUSE" }║
║    ],                                                       ║
║    "recentOrders": [ ...last 5 orders... ],                 ║
║    "activePrescriptions": [ ...current Rx... ],             ║
║    "consultationCount": 2,                                  ║
║    "lastVisitAt": "2026-06-15T..."                          ║
║  }                                                          ║
║  ← VERIFY: includes allergies + chronicConditions            ║
║  ← VERIFY: family members aggregated                         ║
║  ← VERIFY: recentOrders limited to last 5                    ║
║  ← ASYNC: may take 200-500ms (multi-service aggregation)    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-07b: Get Customer 360 - Negative Path (non-existent customer)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get 360 for unknown customer                         ║
║  METHOD: GET  PATH: /api/v1/rx/customers/00000000-0000-0000-0000-000000000000/profile-360║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31","message":"Customer not found" }         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-07c: Get Customer 360 - Negative Path (prod: customer token)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call with B2C customer JWT (prod only)               ║
║  METHOD: GET  PATH: /api/v1/rx/customers/{{customerId}}/profile-360║
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE (prod):                                 ║
║    403 Forbidden                                            ║
║  { "code":"AUTH_004",                                       ║
║    "message":"Pharmacist role required" }                   ║
║                                                              ║
║  NOTE: Dev profile returns 200 (permitAll).                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-08: POST /follow-ups (Schedule follow-up)

### TC-08a: Schedule Follow-up - Happy Path (3 days)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Schedule a 3-day follow-up after purchase             ║
║  METHOD: POST  PATH: /api/v1/follow-ups                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{pharmacistToken}}                ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "orderId": "{{orderId}}",                                ║
║    "prescriptionId": null,                                  ║
║    "daysFromNow": 3,                                        ║
║    "type": "FEEDBACK"                                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{followUpId}}",         ← CAPTURE                ║
║    "customerId": "{{customerId}}",                          ║
║    "orderId": "{{orderId}}",                                ║
║    "scheduledAt": "2026-06-22T10:00:00",   ← now + 3 days   ║
║    "type": "FEEDBACK",                                      ║
║    "status": "PENDING"                                      ║
║  }                                                          ║
║  ← CAPTURE: id as {{followUpId}}                            ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-08b: Schedule Follow-up - 7-day REMINDER

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Schedule 7-day reminder                              ║
║  METHOD: POST  PATH: /api/v1/follow-ups                     ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "customerId":"{{customerId}}",                           ║
║    "prescriptionId":"{{prescId}}",                          ║
║    "daysFromNow":7, "type":"REMINDER" }                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "type":"REMINDER", "scheduledAt":"2026-06-26T...",       ║
║    "status":"PENDING", ... }                                ║
║  ← VERIFY: scheduledAt = today + 7 days                     ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-08c: Schedule Follow-up - Negative (daysFromNow invalid)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pass negative or zero days                           ║
║  METHOD: POST  PATH: /api/v1/follow-ups                     ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "customerId":"{{customerId}}", "daysFromNow":0,          ║
║    "type":"FEEDBACK" }                                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  { "code":"VAL_001",                                        ║
║    "message":"daysFromNow must be >= 1" }                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-09: GET /follow-ups/by-customer/{customerId}

### TC-09a: List Follow-ups - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all follow-ups for a customer                   ║
║  METHOD: GET  PATH: /api/v1/follow-ups/by-customer/{{customerId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{followUpId}}", "type":"FEEDBACK",              ║
║      "status":"PENDING", "scheduledAt":"2026-06-22T...",   ║
║      "response":null, ... },                                ║
║    { "id":"...","type":"REMINDER", "status":"PENDING", ... }║
║  ]                                                          ║
║  ← VERIFY: includes follow-up from TC-08a + TC-08b          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-09b: List Follow-ups - Empty

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer with no follow-ups                          ║
║  METHOD: GET  PATH: /api/v1/follow-ups/by-customer/00000000-0000-0000-0000-000000000099║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-10: POST /follow-ups/{id}/response (Record response)

### TC-10a: Record Response - TAKEN_OK

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer reports taking medicine OK                  ║
║  METHOD: POST  PATH: /api/v1/follow-ups/{{followUpId}}/response║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "response": "TAKEN_OK",                                  ║
║    "note": "No side effects, symptoms improved"             ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{followUpId}}", "status":"COMPLETED",             ║
║    "response":"TAKEN_OK",                                   ║
║    "note":"No side effects, symptoms improved",             ║
║    "respondedAt":"2026-06-19T..." }                         ║
║  ← VERIFY: status === "COMPLETED"                           ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-10b: Record Response - SIDE_EFFECT

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer reports side effect                        ║
║  ← SEED: create a new follow-up (TC-08)                     ║
║  METHOD: POST  PATH: /api/v1/follow-ups/{{newFollowUpId}}/response║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "response":"SIDE_EFFECT",                                ║
║    "note":"Drowsiness after taking" }                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "status":"FLAGGED", "response":"SIDE_EFFECT", ... }      ║
║  ← VERIFY: status === "FLAGGED" (escalates for review)      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-10c: Record Response - Negative (non-existent follow-up)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Record response on unknown follow-up                 ║
║  METHOD: POST  PATH: /api/v1/follow-ups/00000000-0000-0000-0000-000000000000/response║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31","message":"Follow-up not found" }        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-11: DELETE /follow-ups/{id}

### TC-11a: Cancel Follow-up - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel a follow-up task                              ║
║  ← SEED: create new follow-up (TC-08)                       ║
║  METHOD: DELETE  PATH: /api/v1/follow-ups/{{newFollowUpId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    204 No Content                                           ║
║  ← VERIFY: subsequent GET does not include this id         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-11b: Cancel Follow-up - Negative (non-existent)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel unknown follow-up                             ║
║  METHOD: DELETE  PATH: /api/v1/follow-ups/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-12: POST /rx/cross-sell (AI suggestions)

### TC-12a: Cross-sell - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get AI cross-sell suggestions for an order           ║
║  ← REQUIRES: ai-engine-service UP at port 8095               ║
║  METHOD: POST  PATH: /api/v1/rx/cross-sell                  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Content-Type  : application/json                         ║
║    Authorization : Bearer {{pharmacistToken}}                ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "orderId": "{{orderId}}",                                ║
║    "customerId": "{{customerId}}",                          ║
║    "cartItems": [                                           ║
║      { "medicineId":"{{med1}}", "qty":2 },                  ║
║      { "medicineId":"{{med2}}", "qty":1 }                   ║
║    ],                                                       ║
║    "limit": 3                                               ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "suggestions": [                                         ║
║      { "medicineId":"{{med3}}",                             ║
║        "medicineName":"Cough Syrup",                        ║
║        "reason":"Often bought together with Paracetamol",   ║
║        "confidence":0.85 },                                 ║
║      ...                                                    ║
║    ],                                                       ║
║    "modelVersion":"v1.2"                                    ║
║  }                                                          ║
║  ← VERIFY: suggestions array length <= limit                ║
║  ← VERIFY: each has medicineId, confidence (0..1)           ║
║  ← ASYNC: may take 1-3 seconds (AI call)                    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-12b: Cross-sell - Negative (empty cart)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty cart                                           ║
║  METHOD: POST  PATH: /api/v1/rx/cross-sell                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "cartItems":[] }                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "suggestions":[], "modelVersion":"v1.2" }                ║
║  ← VERIFY: empty suggestions (NOT 400)                      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-12c: Cross-sell - Negative (AI service down)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Kill ai-engine-service, then call                    ║
║  METHOD: POST  PATH: /api/v1/rx/cross-sell                  ║
║  EXPECTED RESPONSE:                                        ║
║    503 Service Unavailable                                  ║
║  { "code":"AI_DOWN", "message":"AI engine unavailable" }    ║
║                                                              ║
║  NOTE: Resilience depends on circuit breaker config.        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-13: POST /rx/drug-check (Drug interactions)

### TC-13a: Drug Check - Happy Path (safe combination)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check drug interactions for a list of medicines     ║
║  METHOD: POST  PATH: /api/v1/rx/drug-check                  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Content-Type  : application/json                         ║
║    Authorization : Bearer {{pharmacistToken}}                ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "medicineIds": [ "{{med1}}", "{{med2}}" ],               ║
║    "customerId": "{{customerId}}"                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "interactions": [                                        ║
║      { "medicine1":"{{med1}}","medicine2":"{{med2}}",       ║
║        "severity":"NONE","description":"" }                 ║
║    ],                                                       ║
║    "allergyWarnings": [],                                   ║
║    "overallRisk":"LOW"                                      ║
║  }                                                          ║
║  ← VERIFY: severity === "NONE" or "LOW" for safe combo      ║
║  ← ASYNC: 1-3 seconds (AI call)                            ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-13b: Drug Check - Conflict Interaction

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check known conflicting pair                         ║
║  ← SEED: ensure medicines have interaction flag in DB      ║
║  METHOD: POST  PATH: /api/v1/rx/drug-check                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "medicineIds":["{{med1}}", "{{med3}}"],                  ║
║    "customerId":"{{customerId}}" }                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "interactions":[                                         ║
║      { "severity":"MODERATE",                               ║
║        "description":"May increase drowsiness", ... } ],     ║
║    "overallRisk":"MODERATE" }                               ║
║  ← VERIFY: severity !== "NONE"                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-13c: Drug Check - Allergy Warning

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Customer with Penicillin allergy, check Penicillin    ║
║  ← SEED: customer has allergy="Penicillin"                  ║
║  METHOD: POST  PATH: /api/v1/rx/drug-check                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "medicineIds":["{{penicillinMedicineId}}"],              ║
║    "customerId":"{{customerId}}" }                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "allergyWarnings":[                                      ║
║      { "allergen":"Penicillin",                             ║
║        "medicine":"{{penicillinMedicineName}}",             ║
║        "severity":"HIGH" } ],                               ║
║    "overallRisk":"HIGH" }                                   ║
║  ← VERIFY: allergyWarnings non-empty                        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-14: POST /vip-marks (Mark customer VIP)

### TC-14a: Mark VIP GOLD - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Mark customer as VIP GOLD tier                      ║
║  METHOD: POST  PATH: /api/v1/vip-marks                      ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{pharmacistToken}}                ║
║    X-User-Id     : {{pharmacistId}}       ← actor          ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "tier": "GOLD",                                          ║
║    "reason": "High-value repeat customer, chronic Rx"       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{vipMarkId}}",          ← CAPTURE                ║
║    "customerId": "{{customerId}}",                          ║
║    "tier": "GOLD",                                          ║
║    "reason": "High-value repeat customer, chronic Rx",      ║
║    "markedBy": "{{pharmacistId}}",                          ║
║    "loyaltyScore": 850,                                     ║
║    "markedAt": "2026-06-19T..."                             ║
║  }                                                          ║
║  ← CAPTURE: id as {{vipMarkId}}                             ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-14b: Mark VIP - Different Tiers

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Mark different customer as PLATINUM                 ║
║  METHOD: POST  PATH: /api/v1/vip-marks                      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "customerId":"{{otherCustomerId}}",                      ║
║    "tier":"PLATINUM",                                       ║
║    "reason":"Top 1% spender" }                              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "tier":"PLATINUM", ... }                                 ║
║  ← VERIFY: tier === "PLATINUM"                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-14c: Mark VIP - Negative (invalid tier)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pass unknown tier                                    ║
║  METHOD: POST  PATH: /api/v1/vip-marks                      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  { "customerId":"{{customerId}}", "tier":"DIAMOND" }        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  { "code":"VAL_004",                                        ║
║    "message":"tier must be BRONZE/SILVER/GOLD/PLATINUM" }   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-15: GET /vip-marks/by-customer/{customerId}

### TC-15a: Get VIP Mark - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get VIP mark for a customer                          ║
║  METHOD: GET  PATH: /api/v1/vip-marks/by-customer/{{customerId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  { "id":"{{vipMarkId}}", "customerId":"{{customerId}}",    ║
║    "tier":"GOLD", "loyaltyScore":850, ... }                 ║
║  ← VERIFY: tier === "GOLD" (from TC-14a)                    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-15b: Get VIP Mark - Negative (no mark)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get VIP mark for customer with no mark               ║
║  METHOD: GET  PATH: /api/v1/vip-marks/by-customer/{{regularCustomerId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31","message":"No VIP mark for customer" }   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-16: GET /vip-marks/by-tier/{tier}

### TC-16a: List VIPs by Tier - GOLD

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all GOLD tier VIPs                              ║
║  METHOD: GET  PATH: /api/v1/vip-marks/by-tier/GOLD          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [ { "id":"{{vipMarkId}}", "tier":"GOLD",                   ║
║      "loyaltyScore":850, ... } ]                            ║
║  ← VERIFY: all items have tier === "GOLD"                   ║
║  ← VERIFY: sorted by loyaltyScore DESC                       ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-16b: List VIPs by Tier - Empty (no PLATINUM yet)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List PLATINUM (no customers have this tier)          ║
║  ← SEED: delete PLATINUM mark from TC-14b if any            ║
║  METHOD: GET  PATH: /api/v1/vip-marks/by-tier/PLATINUM     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-16c: List VIPs by Tier - Invalid tier

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Pass unknown tier in path                            ║
║  METHOD: GET  PATH: /api/v1/vip-marks/by-tier/DIAMOND       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
║  ← VERIFY: returns empty (not 400) - filter is permissive   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-17: GET /vip-marks (List all VIPs)

### TC-17a: List All VIPs - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all VIP marks sorted by loyalty score           ║
║  METHOD: GET  PATH: /api/v1/vip-marks                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"...", "tier":"PLATINUM", "loyaltyScore":980, ... },║
║    { "id":"{{vipMarkId}}", "tier":"GOLD", "loyaltyScore":850, ... },║
║    ...                                                      ║
║  ]                                                          ║
║  ← VERIFY: sorted by loyaltyScore DESC                      ║
║  ← VERIFY: includes all tiers                               ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-17b: List All VIPs - Empty (no marks)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No VIPs in system                                    ║
║  ← SEED: delete all VIP marks via TC-18                     ║
║  METHOD: GET  PATH: /api/v1/vip-marks                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-18: DELETE /vip-marks/{customerId}

### TC-18a: Unmark VIP - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Remove VIP mark for a customer                       ║
║  METHOD: DELETE  PATH: /api/v1/vip-marks/{{customerId}}    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    204 No Content                                           ║
║  ← VERIFY: subsequent GET /vip-marks/by-customer/{{customerId}}║
║    returns 404                                             ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-18b: Unmark VIP - Negative (no existing mark)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Try to remove non-existent VIP mark                  ║
║  METHOD: DELETE  PATH: /api/v1/vip-marks/00000000-0000-0000-0000-000000000099║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31","message":"No VIP mark to remove" }      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Edge Cases & Notes

```
╔══════════════════════════════════════════════════════════════╗
║  EDGE-01: Concurrent VIP marks                              ║
║    Two pharmacists marking the same customer simultaneously. ║
║    Should be idempotent (replace tier) or 409 conflict.     ║
║                                                              ║
║  EDGE-02: Consultation message size                          ║
║    Append a 10MB string message - should fail 413 or limit. ║
║                                                              ║
║  EDGE-03: daysFromNow = 365                                  ║
║    Should accept but scheduledAt is far future. Test 400    ║
║    if business rule caps at e.g. 30 days.                   ║
║                                                              ║
║  EDGE-04: AI service timeout                                 ║
║    /rx/cross-sell and /rx/drug-check depend on ai-engine.  ║
║    If ai-engine times out (>30s), service returns 503.      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Pass / Fail Summary

```
╔══════════════════════════════════════════════════════════════╗
║  Total test cases: 45                                        ║
║  Expected pass: ≥40 (≥89%)                                   ║
║  Known gaps   : Cross-service aggregation latency            ║
║                                                              ║
║  Sign-off: ___________________  Date: ___________             ║
╚══════════════════════════════════════════════════════════════╝
```

**End of `14-PHARMACIST-WORKBENCH.md`**
