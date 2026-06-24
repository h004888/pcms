# UAT Test Scenario: Branch Management

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `branch-service` (port 8082)
**UAT Doc Reference:** `02-BRANCH.md`
**Coverage:** UC03 (Manage Branches — AT1, AT2, AT3, AT4)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  BRANCH-SERVICE                                                             ║
║  Tier    : B2B                                                                ║
║  Port    : 8082 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/branches/**                          ║
║  Auth    : JWT HS256 - permitAll in dev, role-based in prod                   ║
║  DB      : MySQL 8 (schema = branch_db)                                       ║
║  Tests   : 8 endpoints, ~25 cases, est. 45 min                               ║
║                                                                              ║
║  Note   : BranchController also defines 2 inline @GetMapping                 ║
║           (/users/{id} and /users) that act as a soft dependency on          ║
║           user-service. These are out of scope here and tested in 01-AUTH.   ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller        | Method | Path                          | Description                | Test Cases |
|----|-------------------|--------|-------------------------------|----------------------------|-----------:|
| 1  | BranchController  | GET    | `/branches`                   | Danh sách chi nhánh        |          4 |
| 2  | BranchController  | GET    | `/branches/{id}`              | Chi tiết theo UUID         |          2 |
| 3  | BranchController  | GET    | `/branches/code/{code}`       | Tìm theo mã (HQ, Q1, Q7)   |          2 |
| 4  | BranchController  | GET    | `/branches/{id}/staff`        | Danh sách nhân viên CN     |          2 |
| 5  | BranchController  | POST   | `/branches`                   | Tạo chi nhánh              |          4 |
| 6  | BranchController  | PUT    | `/branches/{id}`              | Cập nhật thông tin         |          3 |
| 7  | BranchController  | PUT    | `/branches/{id}/manager`      | Gán quản lý (AT2)          |          3 |
| 8  | BranchController  | DELETE | `/branches/{id}`              | Xoá mềm chi nhánh          |          3 |
| **TOTAL**              |        |                               |                            |     **~23**|

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `branch_db` migrated
- [x] Eureka: `BRANCH-SERVICE` registered
- [x] Gateway route `/api/v1/branches/**` → branch-service
- [x] At least 1 user-service user exists (to test manager assign + staff list)
- [x] Admin JWT available (reuse `{{accessToken}}` from `01-AUTH-USER.md` TC-01)

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/BRANCH-SERVICE        ║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8082/actuator/health | jq .status     ║
║  → "UP"                                                        ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  Branches seeded by SQL (see Master Plan §7.3):              ║
║                                                              ║
║  1. HQ                                                       ║
║     code : HQ                                                ║
║     name : Headquarter                                       ║
║     addr : 12 Le Loi, District 1, HCMC                       ║
║     phone: 0281234567                                        ║
║                                                              ║
║  2. Q1 (District 1 Branch)                                   ║
║  3. Q7 (District 7 Branch)                                   ║
╚══════════════════════════════════════════════════════════════╝
```

If not seeded, see Master Plan §7.3 Step 3 for bootstrap API calls.

---

## 4. Variables to Capture

| Variable             | Example                                | Captured from                |
|----------------------|----------------------------------------|------------------------------|
| `{{branchHQ}}`       | `uuid`                                 | Master Plan §7.3 step 3      |
| `{{branchQ1}}`       | `uuid`                                 | Master Plan §7.3 step 3      |
| `{{branchQ7}}`       | `uuid`                                 | Master Plan §7.3 step 3      |
| `{{newBranchId}}`    | `uuid`                                 | TC-05 below                  |
| `{{newBranchCode}}`  | `e.g. Q2`                              | TC-05 below                  |

---

## TC-01: GET /branches (List with pagination)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List branches, default page                          ║
║  METHOD: GET  PATH: /api/v1/branches                        ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                              ║
║    page=0&size=20                                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "data": [                                                ║
║      { "id":"{{branchHQ}}","code":"HQ",                     ║
║        "name":"Headquarter","address":"12 Le Loi, D1",      ║
║        "phone":"0281234567","managerId":null,               ║
║        "status":"ACTIVE",                                   ║
║        "createdAt":"...","updatedAt":"..." },               ║
║      { "id":"{{branchQ1}}","code":"Q1", ... },              ║
║      { "id":"{{branchQ7}}","code":"Q7", ... }               ║
║    ],                                                       ║
║    "page":0,"size":20,"total":3,"totalPages":1             ║
║  }                                                          ║
║  ← VERIFY: sorted by code ASC (HQ → Q1 → Q7)                ║
║  ← VERIFY: total = 3                                        ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.1: GET /branches (Search filter)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?search=District                                     ║
║  EXPECTED: 200, data[] contains Q1 + Q7 (HQ excluded)       ║
║  ← VERIFY: search is case-insensitive                       ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.2: GET /branches (Pagination — second page)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?page=1&size=2                                       ║
║  EXPECTED: 200, data[] has 1 entry (Q7)                     ║
║  ← VERIFY: totalPages=2                                     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.3: GET /branches (Size cap)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?size=99999                                          ║
║  EXPECTED: 200, response.size capped at 100                 ║
║  ← VERIFY: controller uses Math.min(size, 100)              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /branches/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get branch detail by UUID                             ║
║  METHOD: GET  PATH: /api/v1/branches/{{branchHQ}}           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{branchHQ}}",                                    ║
║    "code": "HQ",                                            ║
║    "name": "Headquarter",                                   ║
║    "address": "12 Le Loi, District 1, HCMC",                 ║
║    "phone": "0281234567",                                   ║
║    "managerId": "{{managerId}}",     ← after TC-07          ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "...", "updatedAt": "..."                   ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-02.1: GET /branches/{id} (Not found)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/branches/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                    ║
║  { "error":"BRANCH_NOT_FOUND",                              ║
║    "message":"Không tìm thấy chi nhánh" }                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: GET /branches/code/{code}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Look up by short code                                 ║
║  METHOD: GET  PATH: /api/v1/branches/code/HQ                ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{branchHQ}}","code":"HQ","name":"Headquarter",...}║
║  ← VERIFY: only one result                                  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.1: GET /branches/code/{code} (Unknown code)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/branches/code/UNKNOWN                        ║
║  EXPECTED: 404 Not Found                                    ║
║  { "error":"BRANCH_NOT_FOUND" }                             ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: GET /branches/{id}/staff

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List staff assigned to a branch (cross-service)       ║
║  METHOD: GET  PATH: /api/v1/branches/{{branchQ1}}/staff     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{managerId}}","email":"manager.q1@pcms.vn",     ║
║      "fullName":"Nguyen Van Manager","phone":"0902222222",  ║
║      "role":"BRANCH_MANAGER","status":"ACTIVE" },           ║
║    { "id":"{{pharmaId}}","email":"pharmacist.q1@pcms.vn",   ║
║      "fullName":"Nguyen Thi Pharma",                        ║
║      "role":"PHARMACIST","status":"ACTIVE" }                ║
║  ]                                                          ║
║  ← VERIFY: response is JSON array (not PageResponse)         ║
║  ← VERIFY: all entries have branchId={{branchQ1}}           ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.1: GET /branches/{id}/staff (Empty branch)

```
╔══════════════════════════════════════════════════════════════╗
║  SETUP   : Create new branch in TC-05 (no staff assigned)   ║
║  REQUEST : GET /api/v1/branches/{{newBranchId}}/staff        ║
║  EXPECTED: 200, []  (empty array, NOT null)                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: POST /branches (Create new branch)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create new branch Q2 (District 2)                     ║
║  METHOD: POST  PATH: /api/v1/branches                       ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    Authorization : Bearer {{accessToken}}                   ║
║    Content-Type  : application/json                          ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "code": "Q2",                                            ║
║    "name": "District 2 Branch",                             ║
║    "address": "50 Tran Hung Dao, District 2, HCMC",         ║
║    "phone": "0281234570"                                    ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "code": "Q2",                                            ║
║    "name": "District 2 Branch",                             ║
║    "address": "50 Tran Hung Dao, District 2, HCMC",         ║
║    "phone": "0281234570",                                   ║
║    "managerId": null,                                       ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "...", "updatedAt": "..."                   ║
║  }                                                          ║
║  ← CAPTURE: {{newBranchId}}, {{newBranchCode}}=Q2           ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.1: POST /branches (Duplicate code)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: same code as HQ ("HQ")                             ║
║  EXPECTED: 409 Conflict                                     ║
║  { "error":"BRANCH_CODE_EXISTS",                            ║
║    "message":"Mã chi nhánh đã tồn tại" }                   ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.2: POST /branches (Missing required field)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "code":"Q3","name":"Q3" }   ← no address/phone ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"address","msg":"không được để trống" }          ║
║  { "field":"phone",   "msg":"không được để trống" }          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.3: POST /branches (Code too long)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "code":"TOOLONG-CODE-12345", ... }              ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"code","msg":"không được vượt quá 10 ký tự" }   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: PUT /branches/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update branch Q2 (rename + change phone)             ║
║  METHOD: PUT  PATH: /api/v1/branches/{{newBranchId}}        ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "name": "District 2 Branch (Renamed)",                   ║
║    "address": "60 Tran Hung Dao, D2, HCMC",                 ║
║    "phone": "0281234580",                                   ║
║    "status": "ACTIVE"                                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{newBranchId}}","code":"Q2",                      ║
║    "name":"District 2 Branch (Renamed)", ... }              ║
║  ← VERIFY: updatedAt is later than createdAt               ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-06.1: PUT /branches/{id} (404)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/branches/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-06.2: PUT /branches/{id} (Status=INACTIVE)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "name":"...", "address":"...", "phone":"...",   ║
║             "status":"INACTIVE" }                           ║
║  EXPECTED: 200, status=INACTIVE                              ║
║  ← VERIFY: branch disappears from active lists              ║
║  ← RESET  : PUT status=ACTIVE before next test              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-07: PUT /branches/{id}/manager (AT2 — Reassign manager)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Assign branch manager to Q1                          ║
║  METHOD: PUT  PATH: /api/v1/branches/{{branchQ1}}/manager  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "managerId": "{{managerId}}" }                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{branchQ1}}","managerId":"{{managerId}}",... }   ║
║  ← VERIFY: GET /branches/{{branchQ1}} → managerId set      ║
║  ← VERIFY: audit log entry created (action=ASSIGN_MANAGER)  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-07.1: PUT /branches/{id}/manager (Reassign to different user)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "managerId": "{{pharmaId}}" }                   ║
║  EXPECTED: 200, managerId updated                           ║
║  NOTE    : Original manager no longer assigned              ║
║  ← VERIFY: GET /users/{{managerId}} → branchId may reset   ║
║  ← RESET  : reassign back to {{managerId}}                  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-07.2: PUT /branches/{id}/manager (Invalid managerId)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "managerId":"00000000-0000-0000-0000-000000000000" }║
║  EXPECTED: 422 Unprocessable Entity                         ║
║  { "error":"USER_NOT_FOUND" }                               ║
║  ← NOTE   : Cross-service check via user-service           ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-08: DELETE /branches/{id} (Soft delete)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Soft-delete the Q2 branch we created                 ║
║  METHOD: DELETE  PATH: /api/v1/branches/{{newBranchId}}     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    204 No Content (empty body)                              ║
║  ← VERIFY: GET /branches/{{newBranchId}} → 404              ║
║  ← VERIFY: GET /branches → Q2 no longer in active list      ║
║  ← VERIFY: DB row still exists with status=INACTIVE         ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-08.1: DELETE /branches/{id} (Already deleted — idempotent?)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: DELETE /branches/{{newBranchId}}  (run twice)     ║
║  EXPECTED:                                                  ║
║    1st : 204 No Content                                     ║
║    2nd : 404 Not Found (or 204 if idempotent)                ║
║  ← VERIFY: behavior matches service-layer policy            ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-08.2: DELETE /branches/{id} (Branch with active staff — guard?)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: DELETE /branches/{{branchQ1}} (has 2 staff)       ║
║  EXPECTED: 422 Unprocessable Entity                         ║
║  { "error":"BRANCH_HAS_STAFF",                              ║
║    "message":"Chi nhánh đang có nhân viên, vui lòng          ║
║               chuyển trước khi xoá" }                       ║
║  ← IF   service does not enforce guard → 204 (re-assign first)║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART E — Cross-Endpoint Integration

## TC-09: Full branch lifecycle (E2E within this service)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Exercise complete branch lifecycle                   ║
╠══════════════════════════════════════════════════════════════╣
║  1. POST   /branches                       → create (TC-05) ║
║  2. GET    /branches/{id}                  → confirm exists  ║
║  3. GET    /branches/code/{code}           → lookup by code  ║
║  4. PUT    /branches/{id}                  → rename (TC-06)  ║
║  5. PUT    /branches/{id}/manager         → assign (TC-07)  ║
║  6. GET    /branches/{id}/staff            → manager appears ║
║  7. DELETE /branches/{id}                 → soft delete      ║
║  8. GET    /branches/{id}                  → 404              ║
║  ← CAPTURE final state for downstream services             ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Summary of Test Variables

| Variable             | Final value                            | Source                |
|----------------------|----------------------------------------|------------------------|
| `{{branchHQ}}`       | uuid                                   | Master Plan §7.3 step 3 |
| `{{branchQ1}}`       | uuid                                   | Master Plan §7.3 step 3 |
| `{{branchQ7}}`       | uuid                                   | Master Plan §7.3 step 3 |
| `{{newBranchId}}`    | uuid (then soft-deleted)               | TC-05                  |
| `{{newBranchCode}}`  | "Q2"                                   | TC-05                  |

---

## 6. Known Issues / Quirks

```
╔══════════════════════════════════════════════════════════════╗
║  OBS-BRANCH-001: BranchController also exposes /users/{id}    ║
║                  and /users (cross-service soft dependency).  ║
║                  Tested in 01-AUTH-USER.md, not here.         ║
║  OBS-BRANCH-002: /branches/{id}/staff makes a sync HTTP call ║
║                  to user-service. If user-service is down,    ║
║                  expect 503.                                  ║
║  OBS-BRANCH-003: Size param capped at 100 (Math.min guard).   ║
║  OBS-BRANCH-004: List endpoint sorts by code ASC (not createdAt).║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Sign-off

| Role          | Name | Date | Pass / Fail | Notes |
|---------------|------|------|--------------|-------|
| Tester        |      |      | ☐            |       |
| Dev Lead      |      |      | ☐            |       |
| QA Manager    |      |      | ☐            |       |

**End of `02-BRANCH.md`** → Next: [`04-CATEGORY.md`](./04-CATEGORY.md)
