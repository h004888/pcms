# UAT Test: Prescription Service (UC12)

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10 (commit `63b9397`)
**Module:** `prescription-service` (port 8090)
**Use Case:** UC12 - Issue Prescription (Doctor workflow: create → sign → link to order)
**Total endpoints:** 9 (PrescriptionController)
**Aliases covered:** TICKET-301 POST /sign, TICKET-302 POST /print, TICKET-303 DELETE

---

## 1. Service Info

```
╔══════════════════════════════════════════════════════════════════════╗
║  SERVICE   : prescription-service                                    ║
║  PORT      : 8090 (direct) / 8080 (via Gateway)                      ║
║  STACK     : Spring Boot 4.0.7 + Java 21 + MySQL 8                   ║
║  PREFIX    : /api/v1/prescriptions/**                                ║
║  SECURITY  : JWT HS256 - permitAll in dev profile                    ║
║  ENTITY    : Prescription (UUID id, code=RX-yyyy####,                ║
║              patientId, doctorId, orderId, signatureHash, status)   ║
║  ENUMS     : PrescriptionStatus={DRAFT, SIGNED, CANCELLED}           ║
║  CODE RULE : RX-{year}{4-digit-seq} - auto-generated on create       ║
║  SIGNATURE : server-side SHA-256 hash of canonical payload           ║
╚══════════════════════════════════════════════════════════════════════╝
```

### 1.1 Indicator Legend

| Symbol     | Meaning                                                |
|------------|--------------------------------------------------------|
| ← INPUT    | Field value to send in request                         |
| ← VERIFY   | Assertion to check in response                         |
| ← CAPTURE  | Save this value as variable for next step              |
| ← SEED     | Pre-conditions (data must exist)                       |
| ← SIGN     | Server-side digital signature (SHA-256 hash)           |
| ← ALIAS    | POST alias of a PUT endpoint (SDD §6.14)               |

### 1.2 Authorization Matrix

| Endpoint                             | Admin/CEO | Pharmacist (Doctor) | Customer (B2C) |
|--------------------------------------|-----------|---------------------|----------------|
| GET /prescriptions (list)            | ✓         | ✓ (own)             | ✗              |
| GET /prescriptions/{id}              | ✓         | ✓                   | ✗              |
| GET /prescriptions/code/{code}       | ✓         | ✓                   | ✗              |
| POST /prescriptions (create)         | ✓         | ✓                   | ✗              |
| POST /prescriptions/draft            | ✓         | ✓                   | ✗              |
| PUT /prescriptions/{id}              | ✓         | ✓                   | ✗              |
| PUT /prescriptions/{id}/sign         | ✓         | ✓                   | ✗              |
| POST /prescriptions/{id}/link-order  | ✓         | ✓                   | ✗              |
| GET /prescriptions/{id}/print        | ✓         | ✓                   | ✗              |
| DELETE /prescriptions/{id}           | ✓         | ✓                   | ✗              |

---

## 2. Endpoint Summary

| #  | Method | Path                                  | Auth   | Purpose                                    |
|----|--------|---------------------------------------|--------|--------------------------------------------|
| 1  | GET    | `/api/v1/prescriptions`               | permit | List (paginated)                           |
| 2  | GET    | `/api/v1/prescriptions/{id}`          | permit | Get by UUID                                |
| 3  | GET    | `/api/v1/prescriptions/code/{code}`   | permit | Get by code (e.g. RX-20260001)             |
| 4  | POST   | `/api/v1/prescriptions`               | permit | Create (auto-generate code)                |
| 5  | POST   | `/api/v1/prescriptions/draft`         | permit | Save as draft (status=DRAFT)               |
| 6  | PUT    | `/api/v1/prescriptions/{id}`          | permit | Update (only when DRAFT)                   |
| 7  | PUT    | `/api/v1/prescriptions/{id}/sign`     | permit | Sign (DRAFT → SIGNED, attach signatureHash)|
| 8  | POST   | `/api/v1/prescriptions/{id}/link-order` | permit | Link to order (?orderId=...)             |
| 9  | GET    | `/api/v1/prescriptions/{id}/print`    | permit | Print preview                              |

> **Aliases** (documented but not counted): POST /{id}/sign, POST /{id}/print, DELETE /{id}, POST /{id}/link-order (all return same shape as their PUT/GET counterparts).

---

## 3. Prerequisites

1. **Environment**: Gateway running at `http://localhost:8080`, prescription-service registered on Eureka.
2. **Database**: `pcms_prescription` schema with `prescriptions` table.
3. **Seeded data** (run Section 4 first):
   - At least 1 customer `{{patientId}}` (B2B customer) representing the patient
   - At least 1 pharmacist/doctor user `{{doctorId}}`
   - For link-order test: 1 paid or pending order `{{orderId}}`
4. **Auth**: Dev profile is `permitAll`. Optionally login for `{{accessToken}}`.

---

## 4. Test Data Seeding

### 4.1 Patient (B2B Customer)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create patient (B2B customer)                         ║
║  METHOD: POST  PATH: /api/v1/customers                       ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                    ║
║  {                                                          ║
║    "name": "Nguyen Van Patient",                             ║
║    "phone": "0912345678",                                    ║
║    "email": "patient@example.com",                           ║
║    "dateOfBirth": "1985-05-15"                               ║
║  }                                                          ║
║  EXPECTED: 201 Created                                       ║
║  ← CAPTURE: PATIENT_ID (uuid)                                ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.2 Doctor (Pharmacist User)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create doctor user                                    ║
║  METHOD: POST  PATH: /api/v1/users                           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                    ║
║  {                                                          ║
║    "email": "doctor@pcms.vn",                                ║
║    "fullName": "Dr. Tran Van B",                             ║
║    "phone": "0909999999",                                    ║
║    "role": "PHARMACIST",                                     ║
║    "licenseNo": "VN-PHARM-12345"                             ║
║  }                                                          ║
║  EXPECTED: 200 OK (temp password in logs)                    ║
║  ← CAPTURE: DOCTOR_ID (uuid)                                 ║
╚══════════════════════════════════════════════════════════════╝
```

### 4.3 Captured Variables

| Variable        | Example                                | Source                |
|-----------------|----------------------------------------|-----------------------|
| `{{gateway}}`   | `http://localhost:8080`                | static                |
| `{{accessToken}}` | `eyJhbGciOiJIUzI1...`                | POST /auth/login      |
| `{{patientId}}` | `uuid` (Nguyen Van Patient)            | POST /customers       |
| `{{doctorId}}`  | `uuid` (Dr. Tran Van B)                | POST /users           |
| `{{med1}}`      | `uuid` (Amoxicillin 500mg)             | POST /medicines       |
| `{{med2}}`      | `uuid` (Paracetamol 500mg)             | POST /medicines       |
| `{{orderId}}`   | `uuid` (paid order)                    | POST /orders + payment|
| `{{RX_ID}}`     | `uuid`                                 | POST /prescriptions   |
| `{{RX_CODE}}`   | `RX-20260001`                          | auto-generated        |

---

## 5. Test Cases

### 5.1 GET /api/v1/prescriptions (List)

#### Test 5.1.1 - Happy path: list prescriptions

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all prescriptions                                ║
║  METHOD: GET  PATH: /api/v1/prescriptions?page=0&size=20     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "content": [                                              ║
║      {                                                       ║
║        "id": "uuid",                                        ║
║        "code": "RX-20260001",                                ║
║        "patientId": "{{patientId}}",                         ║
║        "doctorId": "{{doctorId}}",                           ║
║        "orderId": null,                                      ║
║        "diagnosis": "Strep throat",                          ║
║        "signatureHash": null,                                ║
║        "status": "DRAFT",                                    ║
║        "issuedAt": null,                                     ║
║        "items": [ ... ]                                      ║
║      }                                                       ║
║    ],                                                        ║
║    "page": 0, "size": 20, "totalElements": 1                 ║
║  }                                                          ║
║  ← VERIFY: content.length >= 1, codes follow RX-{yyyy}{####} ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.2 - Edge: empty database

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty list                                            ║
║  METHOD: GET  PATH: /api/v1/prescriptions?page=0&size=20     ║
║  ← SEED: TRUNCATE prescriptions table (or fresh DB)          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  { "content": [], "totalElements": 0, "totalPages": 0 }     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.1.3 - Negative: invalid page param

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Negative page                                         ║
║  METHOD: GET  PATH: /api/v1/prescriptions?page=-1            ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 GET /api/v1/prescriptions/{id}

#### Test 5.2.1 - Happy path: get by UUID

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get prescription by id                                ║
║  METHOD: GET  PATH: /api/v1/prescriptions/{{RX_ID}}         ║
║  ← SEED: RX_ID created via 5.4                              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "id": "{{RX_ID}}",                                       ║
║    "code": "RX-20260001",                                   ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "status": "DRAFT",                                        ║
║    "diagnosis": "Acute pharyngitis",                         ║
║    "items": [                                                ║
║      {                                                       ║
║        "medicineId": "{{med1}}",                             ║
║        "dosage": "500mg x 3/day",                            ║
║        "durationDays": 7                                     ║
║      }                                                       ║
║    ]                                                         ║
║  }                                                          ║
║  ← VERIFY: code matches RX-\d{4}\d{4} regex                 ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.2 - Negative: UUID not found

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown id                                            ║
║  METHOD: GET  PATH: /api/v1/prescriptions/00000000-0000-0000-0000-000000000000║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.2.3 - Negative: malformed UUID

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Bad UUID                                              ║
║  METHOD: GET  PATH: /api/v1/prescriptions/not-a-uuid         ║
║  EXPECTED: 400 Bad Request (type mismatch)                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 GET /api/v1/prescriptions/code/{code}

#### Test 5.3.1 - Happy path: lookup by code

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get by code                                           ║
║  METHOD: GET  PATH: /api/v1/prescriptions/code/{{RX_CODE}}   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: response.code == RX_CODE                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.3.2 - Negative: unknown code

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unknown code                                          ║
║  METHOD: GET  PATH: /api/v1/prescriptions/code/RX-99999999   ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.4 POST /api/v1/prescriptions (Create)

#### Test 5.4.1 - Happy path: create prescription (auto-sign)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create prescription                                   ║
║  METHOD: POST  PATH: /api/v1/prescriptions                   ║
║  HEADER: Content-Type: application/json                      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "Acute pharyngitis",                         ║
║    "notes": "Patient has penicillin allergy - avoid",        ║
║    "items": [                                                ║
║      {                                                       ║
║        "medicineId": "{{med1}}",                             ║
║        "dosage": "500mg x 3 times/day",                      ║
║        "durationDays": 7                                     ║
║      },                                                      ║
║      {                                                       ║
║        "medicineId": "{{med2}}",                             ║
║        "dosage": "500mg as needed for fever",                ║
║        "durationDays": 5                                     ║
║      }                                                       ║
║    ],                                                        ║
║    "licenseNo": "VN-PHARM-12345"                             ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "code": "RX-20260001",       ← auto-generated             ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "Acute pharyngitis",                         ║
║    "signatureHash": null,        ← not yet signed            ║
║    "status": "DRAFT",                                        ║
║    "issuedAt": null,                                         ║
║    "items": [ ... ]                                          ║
║  }                                                          ║
║  ← CAPTURE: RX_ID, RX_CODE                                   ║
║  ← VERIFY: code format = RX-\d{4}\d{4}                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.2 - Happy path: code sequence increment

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Second prescription                                   ║
║  ← REPEAT 5.4.1 with different diagnosis                    ║
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: code = RX-20260002 (incremented)                 ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.3 - Negative: missing patientId

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Missing required patient                              ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "Test",                                      ║
║    "items": [ { "medicineId": "{{med1}}", "dosage": "1x", "durationDays": 3 } ]║
║  }                                                          ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: fieldErrors has "patientId" with "is required"   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.4 - Negative: empty items list

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No medicines in prescription                          ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "Test",                                      ║
║    "items": []                                               ║
║  }                                                          ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "At least one item is required"                  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.5 - Negative: empty diagnosis

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Blank diagnosis                                       ║
║  REQUEST:                                                    ║
║  {                                                          ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "",                                          ║
║    "items": [ { "medicineId": "{{med1}}", "dosage": "1", "durationDays": 3 } ]║
║  }                                                          ║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "diagnosis is required"                          ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.6 - Negative: durationDays < 1

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: durationDays = 0                                      ║
║  REQUEST items[0]: { "medicineId": "{{med1}}", "dosage": "1", "durationDays": 0 }║
║  EXPECTED: 400 Bad Request                                   ║
║  ← VERIFY: "durationDays must be >= 1"                      ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.5 POST /api/v1/prescriptions/draft (Save as draft)

#### Test 5.5.1 - Happy path: save draft

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Save prescription as draft                            ║
║  METHOD: POST  PATH: /api/v1/prescriptions/draft             ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "Hypertension stage 1",                      ║
║    "notes": "Workup incomplete - finish labs first",         ║
║    "items": [                                                ║
║      { "medicineId": "{{med2}}", "dosage": "5mg/day", "durationDays": 30 }║
║    ]                                                         ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  {                                                          ║
║    "code": "RX-20260003",                                   ║
║    "status": "DRAFT",       ← explicit draft mode           ║
║    "signatureHash": null,                                    ║
║    ...                                                       ║
║  }                                                          ║
║  ← CAPTURE: DRAFT_RX_ID                                      ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.4.7 - Verify POST endpoint behavior with saveAsDraft=true

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: POST /prescriptions with saveAsDraft=true             ║
║  REQUEST BODY: same as 5.4.1 but add "saveAsDraft": true   ║
║  EXPECTED: 200 OK, status=DRAFT                              ║
║  ← VERIFY: behaves identically to /draft endpoint           ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.6 PUT /api/v1/prescriptions/{id} (Update)

#### Test 5.6.1 - Happy path: update DRAFT prescription

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update draft                                          ║
║  METHOD: PUT  PATH: /api/v1/prescriptions/{{DRAFT_RX_ID}}   ║
║  ← SEED: DRAFT_RX_ID from 5.5.1                              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                         ║
║  {                                                          ║
║    "diagnosis": "Hypertension stage 1 (revised)",            ║
║    "notes": "Patient now off ACE-i, switch to ARB",          ║
║    "items": [                                                ║
║      { "medicineId": "{{med2}}", "dosage": "10mg/day", "durationDays": 60 }║
║    ]                                                         ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: diagnosis updated, status still DRAFT, code preserved║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.2 - Negative: update SIGNED prescription

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Try to edit signed Rx                                 ║
║  ← SEED: A signed prescription (status=SIGNED)               ║
║  METHOD: PUT  PATH: /api/v1/prescriptions/{{SIGNED_RX_ID}}   ║
║  REQUEST: any valid update                                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 409 Conflict or 422                               ║
║  ← VERIFY: message "Cannot update SIGNED prescription"       ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.6.3 - Negative: update with empty items

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty items array                                     ║
║  REQUEST: { "diagnosis":"x", "items":[] }                   ║
║  EXPECTED: 400 Bad Request                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.7 PUT /api/v1/prescriptions/{id}/sign (Sign)

#### Test 5.7.1 - Happy path: sign a draft

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Sign prescription                                     ║
║  METHOD: PUT  PATH: /api/v1/prescriptions/{{DRAFT_RX_ID}}/sign║
║  ← SEED: DRAFT_RX_ID, doctor has licenseVN-PHARM-12345      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: (none)                                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{DRAFT_RX_ID}}",                                  ║
║    "code": "RX-20260003",                                   ║
║    "status": "SIGNED",                  ← DRAFT → SIGNED     ║
║    "signatureHash": "a1b2c3d4e5f6...",  ← 64-char SHA-256 hex║
║    "issuedAt": "2026-06-19T11:00:00",    ← auto-set          ║
║    ...                                                       ║
║  }                                                          ║
║  ← VERIFY: signatureHash is non-null, length=64 hex chars    ║
║  ← VERIFY: issuedAt is now populated                         ║
║  ← CAPTURE: SIGNED_RX_ID                                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.2 - TICKET-301: POST alias /sign

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify POST /sign alias works (SDD §6.14)            ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{DRAFT_RX_ID_2}}/sign║
║  ← SEED: Another DRAFT prescription                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, status=SIGNED, signatureHash non-null     ║
║  ← VERIFY: identical behavior to PUT /sign                  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.3 - Negative: double-sign

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Re-sign already-signed                                ║
║  METHOD: PUT  PATH: /api/v1/prescriptions/{{SIGNED_RX_ID}}/sign║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 409 Conflict or 422                               ║
║  ← VERIFY: message "already signed" or "invalid state"      ║
║  ← VERIFY: signatureHash unchanged                           ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.4 - Negative: sign non-existent

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Sign unknown id                                       ║
║  METHOD: PUT  PATH: /api/v1/prescriptions/00000000-.../sign  ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.7.5 - Verify signature determinism

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Check signature changes only when content changes      ║
║  - Create two Rx with identical payload (different patient)  ║
║  - Sign both                                                 ║
║  ← VERIFY: signatureHash is DIFFERENT (patientId part of hash)║
║  ← VERIFY: signature is NOT predictable from client (server-side)║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.8 POST /api/v1/prescriptions/{id}/link-order

#### Test 5.8.1 - Happy path: link to a paid order

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Link Rx to order                                      ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{SIGNED_RX_ID}}/link-order?orderId={{orderId}}║
║  ← SEED: SIGNED_RX_ID, orderId (paid or pending order)      ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY: (none - orderId is query param)               ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{SIGNED_RX_ID}}",                                 ║
║    "orderId": "{{orderId}}",      ← now populated            ║
║    "status": "SIGNED",                                      ║
║    ...                                                       ║
║  }                                                          ║
║  ← VERIFY: orderId field set, equals the query param        ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.2 - Happy path: link DRAFT to order (allowed in dev)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Link draft to order                                   ║
║  ← SEED: A DRAFT prescription                                ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{DRAFT_RX_ID}}/link-order?orderId={{orderId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK or 422 (business rule may require SIGNED first)║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.3 - Negative: missing orderId query param

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: No orderId param                                      ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{SIGNED_RX_ID}}/link-order║
║  EXPECTED: 400 Bad Request (missing required param)         ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.8.4 - Negative: link cancelled Rx

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Link CANCELLED Rx                                     ║
║  ← SEED: A prescription with status=CANCELLED               ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{CANCELLED_ID}}/link-order?orderId={{orderId}}║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 409 or 422                                        ║
║  ← VERIFY: message "Cannot link cancelled prescription"     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.9 GET /api/v1/prescriptions/{id}/print

#### Test 5.9.1 - Happy path: print preview

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Generate print preview                                ║
║  METHOD: GET  PATH: /api/v1/prescriptions/{{SIGNED_RX_ID}}/print║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                          ║
║    200 OK                                                    ║
║  {                                                          ║
║    "id": "{{SIGNED_RX_ID}}",                                 ║
║    "code": "RX-20260003",                                   ║
║    "patientId": "{{patientId}}",                             ║
║    "doctorId": "{{doctorId}}",                               ║
║    "diagnosis": "Hypertension stage 1",                      ║
║    "signatureHash": "a1b2c3d4...",                           ║
║    "status": "SIGNED",                                      ║
║    "issuedAt": "2026-06-19T11:00:00",                        ║
║    "items": [ ... ]                                          ║
║  }                                                          ║
║  ← NOTE: Returned shape is same as /print POST alias         ║
║  ← NOTE: Frontend uses response to render printable view     ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.2 - TICKET-302: POST /print alias

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: POST /print alias                                     ║
║  METHOD: POST  PATH: /api/v1/prescriptions/{{SIGNED_RX_ID}}/print║
║  EXPECTED: 200 OK, same body as GET version                  ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.3 - Print DRAFT prescription

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Print DRAFT (probably allowed)                        ║
║  METHOD: GET  PATH: /api/v1/prescriptions/{{DRAFT_RX_ID}}/print║
║  EXPECTED: 200 OK or 422 if business rule requires SIGNED   ║
╚══════════════════════════════════════════════════════════════╝
```

#### Test 5.9.4 - Negative: print unknown id

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Print non-existent Rx                                 ║
║  METHOD: GET  PATH: /api/v1/prescriptions/00000000-.../print ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Cancel Prescription (DELETE - TICKET-303)

> Bonus endpoint not counted in the 9 but tested for completeness.

### Test 6.1 - Cancel DRAFT

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel draft prescription                             ║
║  METHOD: DELETE  PATH: /api/v1/prescriptions/{{DRAFT_RX_ID}} ║
║  HEADER:                                                     ║
║    X-User-Id: {{doctorId}}                                  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                            ║
║  ← VERIFY: status=CANCELLED                                  ║
╚══════════════════════════════════════════════════════════════╝
```

### Test 6.2 - Cancel SIGNED-but-not-linked

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cancel signed Rx without order link                   ║
║  ← SEED: SIGNED Rx with orderId=null                        ║
║  METHOD: DELETE  PATH: /api/v1/prescriptions/{{SIGNED_UNLINKED}}║
║  EXPECTED: 200 OK, status=CANCELLED                          ║
╚══════════════════════════════════════════════════════════════╝
```

### Test 6.3 - Negative: cancel SIGNED + linked

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Try to cancel Rx linked to a PAID order              ║
║  ← SEED: SIGNED Rx with orderId set (Test 5.8.1)           ║
║  METHOD: DELETE  PATH: /api/v1/prescriptions/{{SIGNED_LINKED}}║
║  EXPECTED: 409 Conflict                                      ║
║  ← VERIFY: message "Cannot cancel prescription linked to order"║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Business Rules Summary

| Rule                                          | Enforcement                                      |
|-----------------------------------------------|--------------------------------------------------|
| Code auto-generated as `RX-{yyyy}{####}`      | Service increments per-year sequence             |
| `signatureHash` is SHA-256 hex (64 chars)     | Set on sign, includes patientId+items+diagnosis  |
| `issuedAt` set on sign                        | Auto-populated with current timestamp            |
| Update only allowed when DRAFT                | Returns 422 if SIGNED/CANCELLED                  |
| Cancel allowed when DRAFT or SIGNED+unlinked  | Returns 409 if SIGNED+linked to order            |
| Link-order requires existing prescription     | Returns 404 if not found                        |

---

## 8. Post-Test Verification

```
╔══════════════════════════════════════════════════════════════╗
║  CHECK after all tests:                                      ║
║    GET /api/v1/prescriptions           → totalElements >= 5  ║
║    GET /api/v1/prescriptions?status=SIGNED → has signed      ║
║    GET /api/v1/prescriptions/code/RX-20260001 → single hit   ║
║    SELECT status, COUNT(*) FROM prescriptions GROUP BY status;║
║      → DRAFT + SIGNED + CANCELLED counts match expectations  ║
╚══════════════════════════════════════════════════════════════╝
```

---

**End of 10-PRESCRIPTION.md** → Next: [`11-NOTIFICATION.md`](./11-NOTIFICATION.md)
