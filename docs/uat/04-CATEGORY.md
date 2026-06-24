# UAT Test Scenario: Category Management

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `category-service` (port 8084)
**UAT Doc Reference:** `04-CATEGORY.md`
**Coverage:** UC04b (Manage Categories)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  CATEGORY-SERVICE                                                           ║
║  Tier    : B2B                                                                ║
║  Port    : 8084 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/categories/**                       ║
║  Auth    : JWT HS256 - permitAll in dev, role-based in prod                   ║
║  DB      : MySQL 8 (schema = category_db)                                     ║
║  Tests   : 5 endpoints, ~15 cases, est. 30 min                               ║
║                                                                              ║
║  Note   : CategoryController also defines 1 inline @GetMapping              ║
║           /medicines/count (cross-service aggregation). Tested in            ║
║           03-CATALOG.md, not here. Listed at end as observation.             ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller          | Method | Path                  | Description              | Test Cases |
|----|---------------------|--------|-----------------------|--------------------------|-----------:|
| 1  | CategoryController  | GET    | `/categories`         | Danh sách danh mục       |          3 |
| 2  | CategoryController  | GET    | `/categories/{id}`    | Chi tiết theo UUID       |          2 |
| 3  | CategoryController  | POST   | `/categories`         | Tạo danh mục             |          4 |
| 4  | CategoryController  | PUT    | `/categories/{id}`    | Cập nhật danh mục        |          3 |
| 5  | CategoryController  | DELETE | `/categories/{id}`    | Xoá danh mục             |          3 |
| **TOTAL**                |        |                       |                          |     **~15**|

> Inline /medicines/count is documented separately under §6 Observations.

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `category_db` migrated
- [x] Eureka: `CATEGORY-SERVICE` registered
- [x] Gateway route `/api/v1/categories/**` → category-service
- [x] Admin JWT available (reuse `{{accessToken}}` from `01-AUTH-USER.md`)

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/CATEGORY-SERVICE     ║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8084/actuator/health | jq .status     ║
║  → "UP"                                                        ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  Categories seeded by SQL (see Master Plan §7.4):            ║
║                                                              ║
║  1. Pain Relief      (PR)                                    ║
║  2. Antibiotics      (AB)                                    ║
║  3. Vitamins         (VT)                                    ║
║  4. Cold & Flu       (CF)                                    ║
║  5. Digestive        (DG)                                    ║
║                                                              ║
║  All categories have: status = ACTIVE                        ║
╚══════════════════════════════════════════════════════════════╝
```

If not seeded, see Master Plan §7.4 Step 4 for bootstrap.

---

## 4. Variables to Capture

| Variable             | Example                                | Captured from              |
|----------------------|----------------------------------------|----------------------------|
| `{{catPR}}`          | `uuid` (Pain Relief)                   | Master Plan §7.4           |
| `{{catAB}}`          | `uuid` (Antibiotics)                   | Master Plan §7.4           |
| `{{catVT}}`          | `uuid` (Vitamins)                      | Master Plan §7.4           |
| `{{catCF}}`          | `uuid` (Cold & Flu)                    | Master Plan §7.4           |
| `{{catDG}}`          | `uuid` (Digestive)                     | Master Plan §7.4           |
| `{{newCatId}}`       | `uuid`                                 | TC-03 below                |
| `{{newCatName}}`     | `e.g. "Cardiology"`                    | TC-03 below                |

---

## TC-01: GET /categories

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all categories (default page)                   ║
║  METHOD: GET  PATH: /api/v1/categories                      ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                              ║
║    page=0&size=20                                           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "data": [                                                ║
║      { "id":"{{catPR}}","name":"Pain Relief",               ║
║        "description":"Thuốc giảm đau",                       ║
║        "status":"ACTIVE",                                   ║
║        "createdAt":"...","updatedAt":"..." },               ║
║      { "id":"{{catAB}}","name":"Antibiotics", ... },        ║
║      { "id":"{{catVT}}","name":"Vitamins", ... },           ║
║      { "id":"{{catCF}}","name":"Cold & Flu", ... },         ║
║      { "id":"{{catDG}}","name":"Digestive", ... }           ║
║    ],                                                       ║
║    "page":0,"size":20,"total":5,"totalPages":1             ║
║  }                                                          ║
║  ← VERIFY: total = 5                                        ║
║  ← VERIFY: all status = ACTIVE                              ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.1: GET /categories (Search filter)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?search=Pain                                        ║
║  EXPECTED: 200, data[] contains only "Pain Relief"          ║
║  ← VERIFY: search is case-insensitive, partial-match        ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.2: GET /categories (Pagination edge case)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?page=99&size=20                                    ║
║  EXPECTED: 200, data=[] empty array                          ║
║  ← VERIFY: no 404 for out-of-range page                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /categories/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get one category detail                              ║
║  METHOD: GET  PATH: /api/v1/categories/{{catPR}}            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{catPR}}",                                       ║
║    "name": "Pain Relief",                                   ║
║    "description": "Thuốc giảm đau",                         ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "...", "updatedAt": "..."                   ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-02.1: GET /categories/{id} (Not found)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/categories/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                    ║
║  { "error":"CATEGORY_NOT_FOUND",                            ║
║    "message":"Không tìm thấy danh mục" }                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /categories (Create new category)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create "Cardiology" category                         ║
║  METHOD: POST  PATH: /api/v1/categories                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    Authorization : Bearer {{accessToken}}                   ║
║    Content-Type  : application/json                          ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "name": "Cardiology",                                    ║
║    "description": "Thuốc tim mạch / huyết áp"               ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "name": "Cardiology",                                    ║
║    "description": "Thuốc tim mạch / huyết áp",              ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "...", "updatedAt": "..."                   ║
║  }                                                          ║
║  ← CAPTURE: {{newCatId}}, {{newCatName}}="Cardiology"       ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.1: POST /categories (Duplicate name)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "name":"Pain Relief", "description":"..." }     ║
║  EXPECTED: 409 Conflict                                     ║
║  { "error":"CATEGORY_NAME_EXISTS",                          ║
║    "message":"Tên danh mục đã tồn tại" }                   ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.2: POST /categories (Empty name)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "name":"", "description":"x" }                  ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"name",                                         ║
║    "msg":"Tên danh mục không được để trống" }               ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.3: POST /categories (Name > 100 chars)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "name":"<150-char-string>", "description":"x" } ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"name",                                         ║
║    "msg":"Tên danh mục không được vượt quá 100 ký tự" }   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: PUT /categories/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update "Cardiology" → rename + change description    ║
║  METHOD: PUT  PATH: /api/v1/categories/{{newCatId}}         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "name": "Cardiology (Updated)",                          ║
║    "description": "Thuốc tim mạch / huyết áp / mỡ máu",     ║
║    "status": "ACTIVE"                                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{newCatId}}",                                     ║
║    "name":"Cardiology (Updated)",                           ║
║    "description":"Thuốc tim mạch / huyết áp / mỡ máu",       ║
║    "status":"ACTIVE", ... }                                 ║
║  ← VERIFY: updatedAt > createdAt                            ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.1: PUT /categories/{id} (404)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/categories/00000000-0000-0000-0000-000000000000║
║  EXPECTED: 404 Not Found                                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.2: PUT /categories/{id} (status=INACTIVE)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "name":"Cardiology (Updated)",                  ║
║             "description":"...", "status":"INACTIVE" }      ║
║  EXPECTED: 200, status=INACTIVE                              ║
║  ← VERIFY: GET /categories → Cardiology missing from active list║
║  ← RESET  : PUT status=ACTIVE before next test              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: DELETE /categories/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Hard-delete the Cardiology category                  ║
║  METHOD: DELETE  PATH: /api/v1/categories/{{newCatId}}      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    204 No Content (empty body)                              ║
║  ← VERIFY: GET /categories/{{newCatId}} → 404              ║
║  ← VERIFY: GET /categories → total = 5 (back to seeded)    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.1: DELETE /categories/{id} (Cascade guard — has medicines)

```
╔══════════════════════════════════════════════════════════════╗
║  SETUP   : Create a medicine in PR category (see 03-CATALOG) ║
║  REQUEST : DELETE /api/v1/categories/{{catPR}}              ║
║  EXPECTED: 422 Unprocessable Entity                         ║
║  { "error":"CATEGORY_HAS_MEDICINES",                        ║
║    "message":"Danh mục đang có thuốc, vui lòng               ║
║               chuyển trước khi xoá" }                       ║
║  NOTE    : Behavior depends on FK constraint strategy.      ║
║            If hard FK: SQL error → 500 (bug).               ║
║            If soft check: 422 (correct).                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.2: DELETE /categories/{id} (Idempotency)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: DELETE /api/v1/categories/{{newCatId}} (twice)   ║
║  EXPECTED:                                                  ║
║    1st : 204 No Content                                     ║
║    2nd : 404 Not Found                                      ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART E — Cross-Endpoint Integration

## TC-06: Full category lifecycle

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Exercise complete category lifecycle                 ║
╠══════════════════════════════════════════════════════════════╣
║  1. POST   /categories               → create (TC-03)       ║
║  2. GET    /categories/{id}          → confirm exists       ║
║  3. GET    /categories?search=...    → appears in search    ║
║  4. PUT    /categories/{id}          → rename (TC-04)       ║
║  5. PUT    /categories/{id}          → set INACTIVE         ║
║  6. GET    /categories               → not in active list   ║
║  7. DELETE /categories/{id}          → hard delete          ║
║  8. GET    /categories/{id}          → 404                  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-07: Cross-service (smoke) — Verify medicine count by category

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cross-service smoke test                             ║
║  PRE    : Create a few medicines under {{catPR}} (PR_ID)    ║
║  ACTION : GET /api/v1/categories/{{catPR}}                  ║
║  ACTION : GET /api/v1/medicines?categoryId={{catPR}}        ║
║  EXPECTED:                                                   ║
║    category endpoint returns category meta                  ║
║    medicine endpoint returns list, count > 0                ║
║  ← NOTE  : Inline /medicines/count in CategoryController     ║
║            is not exposed in master API list. Tested as smoke║
║            via catalog endpoint here.                        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Summary of Test Variables

| Variable             | Final value                            | Source                |
|----------------------|----------------------------------------|------------------------|
| `{{catPR}}`          | uuid (Pain Relief)                     | Master Plan §7.4       |
| `{{catAB}}`          | uuid (Antibiotics)                     | Master Plan §7.4       |
| `{{catVT}}`          | uuid (Vitamins)                        | Master Plan §7.4       |
| `{{catCF}}`          | uuid (Cold & Flu)                      | Master Plan §7.4       |
| `{{catDG}}`          | uuid (Digestive)                       | Master Plan §7.4       |
| `{{newCatId}}`       | uuid (then deleted in TC-05)           | TC-03                  |

---

## 6. Observations / Cross-Service Notes

```
╔══════════════════════════════════════════════════════════════╗
║  OBS-CATEGORY-001: CategoryController also defines inline    ║
║                    @GetMapping /medicines/count — aggregates ║
║                    medicine count per category.              ║
║                    Not in master API list. Tested as part of ║
║                    cross-service smoke (TC-07).              ║
║                                                              ║
║  OBS-CATEGORY-002: DELETE is HARD delete (not soft) per the  ║
║                    controller code. Audit trail is not       ║
║                    preserved. Compare with User (soft) and  ║
║                    Branch (soft).                            ║
║                                                              ║
║  OBS-CATEGORY-003: Name uniqueness is NOT enforced by DB     ║
║                    constraint — relies on service-layer      ║
║                    check that returns 409.                   ║
║                                                              ║
║  OBS-CATEGORY-004: Sort order is by id (default Pageable),   ║
║                    not alphabetical.                        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Sign-off

| Role          | Name | Date | Pass / Fail | Notes |
|---------------|------|------|--------------|-------|
| Tester        |      |      | ☐            |       |
| Dev Lead      |      |      | ☐            |       |
| QA Manager    |      |      | ☐            |       |

**End of `04-CATEGORY.md`** → Next: [`05-SUPPLIER.md`](./05-SUPPLIER.md)
