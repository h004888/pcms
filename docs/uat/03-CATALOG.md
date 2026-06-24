# UAT - Catalog Service (Medicine + Search)

**Version:** 1.0
**Date:** 2026-06-19
**Backend code state:** Local `main` after Sprint 1-10
**Service:** catalog-service (Spring Boot 4.0.7, Java 21)
**Port:** 8083 (internal) — accessed via Gateway `:8080/api/v1/medicines/**` và `/api/v1/search/**`
**Total endpoints covered:** 16 (12 Medicine + 4 Search)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  catalog-service                                                                ║
║  ──────────────────────────────────────────────────────────────────────────    ║
║  Module      : Medicine (CRUD + Image) + Search (autocomplete/full)             ║
║  Database    : pcms_catalog                                                    ║
║  Storage     : Local filesystem (image upload)                                  ║
║  Auth        : permitAll in dev profile (JWT HS256 in prod)                     ║
║  Related FKs : categoryId → category-service, supplierId → supplier-service    ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

### 1.1 MedicineController (`/medicines`) — 12 endpoints

| # | Method | Path                              | Mô tả                       | Auth | Test Status |
|---|--------|-----------------------------------|-----------------------------|------|-------------|
| 1 | GET    | `/medicines`                      | List + filter + paginate    | ✓    | ☐           |
| 2 | GET    | `/medicines/{id}`                 | Chi tiết                    | ✓    | ☐           |
| 3 | GET    | `/medicines/sku/{sku}`            | Tra theo SKU                | ✓    | ☐           |
| 4 | GET    | `/medicines/count`                | Đếm theo category           | ✓    | ☐           |
| 5 | GET    | `/medicines/export`               | Export Excel                | ✓    | ☐           |
| 6 | POST   | `/medicines`                      | Tạo mới (JSON)              | ✓    | ☐           |
| 7 | POST   | `/medicines` (multipart)          | Tạo mới (multipart + image) | ✓    | ☐           |
| 8 | PUT    | `/medicines/{id}`                 | Cập nhật (JSON)             | ✓    | ☐           |
| 9 | PUT    | `/medicines/{id}` (multipart)     | Cập nhật (multipart + image)| ✓    | ☐           |
|10 | POST   | `/medicines/{id}/image`           | Upload/replace ảnh          | ✓    | ☐           |
|11 | GET    | `/medicines/{id}/image`           | Tải ảnh về                  | ✓    | ☐           |
|12 | DELETE | `/medicines/{id}`                 | Soft delete                 | ✓    | ☐           |

### 1.2 SearchController (`/search`) — 4 endpoints

| #  | Method | Path                                  | Mô tả                          | Auth | Test Status |
|----|--------|---------------------------------------|--------------------------------|------|-------------|
| 13 | GET    | `/search`                             | Autocomplete (backward-compat) | ✓    | ☐           |
| 14 | GET    | `/search/medicines/autocomplete`      | Autocomplete (top 5)           | ✓    | ☐           |
| 15 | GET    | `/search/medicines`                   | Search + filters               | ✓    | ☐           |
| 16 | GET    | `/search/full`                        | Full search (backward-compat)  | ✓    | ☐           |

---

## 2. Prerequisites

Xem **[`00-MASTER-PLAN.md`](./00-MASTER-PLAN.md)** §3 cho:

- Môi trường (Java 21, Maven 3.9+, MySQL 8, Redis 7, Eureka, Config Server)
- Startup sequence (`mvn -pl catalog-service spring-boot:run`)
- Health check: `curl http://localhost:8083/actuator/health` → 200

### 2.1 Pre-seed yêu cầu (phải có TRƯỚC khi test catalog)

Catalog có 2 khóa ngoại quan trọng cần tồn tại trước:

| Biến         | Lấy từ endpoint                              | Bắt buộc? |
|--------------|----------------------------------------------|-----------|
| `{{catPR}}`  | `POST /api/v1/categories` (Pain Relief)     | ✓ Có       |
| `{{catVT}}`  | `POST /api/v1/categories` (Vitamins)         | ✓ Có       |
| `{{sup1}}`   | `POST /api/v1/suppliers` (DHG Pharma)        | Tuỳ chọn  |
| `{{adminId}}`| `POST /api/v1/auth/login` (<admin@pcms.vn>)    | Tuỳ chọn  |

### 2.2 Seed nhanh từ MASTER PLAN §7

```
╔══════════════════════════════════════════════════════════════╗
║  BẮT BUỘC chạy Scenario 0.1 ở master plan TRƯỚC:            ║
║  - Step 4: tạo 5 categories                                  ║
║  - Step 5: tạo 3 suppliers                                  ║
║  - Step 6: tạo 5 medicines (để có data cho search)          ║
║  Sau đó capture:                                              ║
║    {{catPR}}, {{catAB}}, {{catVT}}, {{catCF}}, {{catDG}}      ║
║    {{sup1}}, {{sup2}}, {{sup3}}                              ║
║    {{med1}}...{{med5}} (đã có từ master)                     ║
╚══════════════════════════════════════════════════════════════╝
```

### 2.3 Biến dùng chung cho file này

| Biến          | Ví dụ                              | Nguồn                  |
|---------------|------------------------------------|------------------------|
| `{{gateway}}` | `http://localhost:8080`            | (static)               |
| `{{med1}}`    | uuid (Paracetamol)                 | POST /medicines        |
| `{{medSKU1}}` | `MD001`                            | POST /medicines        |
| `{{medImg}}`  | `medicines/<uuid>.jpg`             | POST /medicines/{id}/image |
| `{{catPR}}`   | uuid (Pain Relief)                 | POST /categories       |
| `{{sup1}}`    | uuid (DHG Pharma)                  | POST /suppliers        |

---

## 3. Indicator Legend (áp dụng cả file)

| Symbol     | Ý nghĩa                                              |
|------------|-------------------------------------------------------|
| ← INPUT    | Dữ liệu gửi trong request                            |
| ← SELECT   | Lựa chọn giữa các options                             |
| ← VERIFY   | Assertion cần kiểm tra trong response                |
| ← SEED     | Điều kiện tiên quyết (data phải tồn tại)             |
| ← CAPTURE  | Lưu giá trị này thành biến để dùng step sau          |
| ← ITERATE  | Lặp lại N lần                                         |
| ← UPLOAD   | Upload file (multipart)                               |

---

## 4. Test Cases — MedicineController

### 4.1 GET /medicines — List + Filter

**Test case 4.1.A: List happy path (no filter)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List medicines (no filter)                          ║
║  METHOD: GET  PATH: /api/v1/medicines                     ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY:                                                    ║
║    page=0&size=20                                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                 ║
║  {                                                        ║
║    "content": [                                           ║
║      {                                                    ║
║        "id": "uuid",                                      ║
║        "sku": "MD001",                                    ║
║        "name": "Paracetamol 500mg",                       ║
║        "categoryId": "uuid",                              ║
║        "price": 5000,                                     ║
║        "status": "ACTIVE"                                 ║
║      }                                                    ║
║    ],                                                     ║
║    "page": 0, "size": 20, "totalElements": 5,             ║
║    "totalPages": 1                                        ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: content.length >= 5 (từ seed master plan)      ║
║  ← VERIFY: page=0, size=20, totalElements >= 5            ║
║  ← VERIFY: tất cả status = ACTIVE                         ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.B: Filter by category + search**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Filter by category=PR                               ║
║  METHOD: GET  PATH: /api/v1/medicines                     ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY:                                                    ║
║    categoryId={{catPR}}&search=Para                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE: 200 OK                                 ║
║  {                                                        ║
║    "content": [                                           ║
║      { "sku":"MD001", "name":"Paracetamol 500mg", ... }   ║
║    ],                                                     ║
║    "totalElements": 1                                     ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{catPR}} phải tồn tại, có ít nhất 1 medicine    ║
║  ← VERIFY: mọi item đều có categoryId = {{catPR}}        ║
║  ← VERIFY: name chứa "Para" (case-insensitive LIKE)       ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.C: Filter by price range**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Filter price 1000..10000                            ║
║  METHOD: GET  PATH: /api/v1/medicines                     ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY: minPrice=1000&maxPrice=10000                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200, content[].price ∈ [1000, 10000]            ║
║  ← VERIFY: không có item nào price < 1000 hoặc > 10000    ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.1.D: Empty result**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search không khớp                                   ║
║  METHOD: GET  PATH: /api/v1/medicines?search=NoSuchXYZ123  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "content":[], "totalElements":0, "totalPages":0 }       ║
║  ← VERIFY: content là array rỗng (KHÔNG phải null)         ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.2 GET /medicines/{id} — Get by ID

**Test case 4.2.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get medicine by id                                  ║
║  METHOD: GET  PATH: /api/v1/medicines/{{med1}}            ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{med1}} từ master plan Step 6                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE: 200 OK                                 ║
║  {                                                        ║
║    "id": "{{med1}}",                                      ║
║    "sku": "MD001",                                        ║
║    "name": "Paracetamol 500mg",                           ║
║    "categoryId": "{{catPR}}",                             ║
║    "supplierId": "{{sup1}}",                              ║
║    "price": 5000,                                         ║
║    "unit": "box",                                         ║
║    "prescriptionRequired": false,                         ║
║    "status": "ACTIVE",                                    ║
║    "createdAt": "2026-06-18T...",                         ║
║    "updatedAt": "2026-06-18T..."                          ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: id = {{med1}}, sku = "MD001"                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.B: Not found — UUID không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get medicine by random UUID                         ║
║  METHOD: GET  PATH: /api/v1/medicines/00000000-0000-0000- ║
║                              0000-000000000000            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE: 404 Not Found                          ║
║  {                                                        ║
║    "code": "MSG31",                                       ║
║    "message": "Medicine not found"                        ║
║  }                                                        ║
║  ← VERIFY: status 404, error code MSG31                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.2.C: Invalid UUID format**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get medicine với id không phải UUID                 ║
║  METHOD: GET  PATH: /api/v1/medicines/not-a-uuid          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                 ║
║  { "code":"VALIDATION", "message":"Invalid UUID format" } ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.3 GET /medicines/sku/{sku} — Get by SKU

**Test case 4.3.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get medicine by SKU                                 ║
║  METHOD: GET  PATH: /api/v1/medicines/sku/{{medSKU1}}      ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: medicine với sku "MD001" đã tạo                  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, response.sku = "MD001"                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.3.B: SKU not found**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get medicine by unknown SKU                         ║
║  METHOD: GET  PATH: /api/v1/medicines/sku/UNKNOWN-XYZ-999  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                   ║
║  ← VERIFY: error code MSG31                               ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.4 GET /medicines/count — Count by category

**Test case 4.4.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Đếm thuốc theo danh mục                            ║
║  METHOD: GET  PATH: /api/v1/medicines/count?categoryId=    ║
║                              {{catPR}}                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  (raw body): 1                                             ║
║  ← VERIFY: response là số >= 1 (ít nhất Paracetamol)      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.4.B: Missing required query param**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Đếm thuốc không truyền categoryId                  ║
║  METHOD: GET  PATH: /api/v1/medicines/count               ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                 ║
║  { "code":"VALIDATION", "message":"categoryId is required"}║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.5 GET /medicines/export — Export Excel

**Test case 4.5.A: Happy path — không filter**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Export toàn bộ medicines                            ║
║  METHOD: GET  PATH: /api/v1/medicines/export              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║    Content-Type: application/vnd.openxmlformats-           ║
║                  officedocument.spreadsheetml.sheet         ║
║    Content-Disposition: attachment; filename=              ║
║                         medicines.xlsx                     ║
║    (binary body: file xlsx)                                ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: header Content-Type chứa "spreadsheetml.sheet"  ║
║  ← VERIFY: body bắt đầu bằng magic bytes PK (zip header)  ║
║  ← VERIFY: file size > 1KB                                 ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.5.B: Filter by category**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Export medicines của 1 danh mục                     ║
║  METHOD: GET  PATH: /api/v1/medicines/export?categoryId=   ║
║                              {{catPR}}                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, file xlsx chỉ chứa medicines thuộc PR  ║
║  ← VERIFY: mở file bằng Excel → chỉ có dòng Paracetamol  ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.6 POST /medicines (JSON) — Create

**Test case 4.6.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo medicine mới (JSON)                             ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  {                                                        ║
║    "sku": "MD-NEW-001",                                   ║
║    "name": "Aspirin 100mg",                               ║
║    "categoryId": "{{catPR}}",                             ║
║    "supplierId": "{{sup1}}",                              ║
║    "price": 3000,                                         ║
║    "unit": "box",                                         ║
║    "prescriptionRequired": false,                         ║
║    "imageUrl": null                                       ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE: 200 OK (controller trả về body)        ║
║  {                                                        ║
║    "id": "uuid-{{medNew}}",     ← CAPTURE                  ║
║    "sku": "MD-NEW-001",                                   ║
║    "name": "Aspirin 100mg",                               ║
║    "categoryId": "{{catPR}}",                             ║
║    "price": 3000,                                         ║
║    "unit": "box",                                         ║
║    "status": "ACTIVE",                                    ║
║    "createdAt": "2026-06-19T..."                          ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  ← CAPTURE: id → {{medNew}}                                ║
║  ← VERIFY: status = ACTIVE                                ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.B: Validation — thiếu name**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create thiếu trường name                            ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  {                                                        ║
║    "sku": "MD-NEW-002",                                   ║
║    "name": "",                                            ║
║    "categoryId": "{{catPR}}",                             ║
║    "price": 3000,                                         ║
║    "unit": "box"                                          ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                 ║
║  {                                                        ║
║    "code":"VALIDATION",                                   ║
║    "errors":[ {"field":"name","message":"Tên thuốc       ║
║                không được để trống"} ]                    ║
║  }                                                        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.C: Validation — price âm**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create với price = -1                               ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "sku":"MD-X", "name":"X", "categoryId":"{{catPR}}",    ║
║    "price":-1, "unit":"box" }                             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 400 Bad Request                                 ║
║  ← VERIFY: error chứa "Giá bán phải lớn hơn 0"          ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.D: Duplicate SKU**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create với SKU đã tồn tại                          ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "sku":"MD001", "name":"Duplicate",                      ║
║    "categoryId":"{{catPR}}", "price":1000, "unit":"box" }  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 409 Conflict                                    ║
║  ← VERIFY: message chứa "SKU đã tồn tại"                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.6.E: Category không tồn tại (FK violation)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create với categoryId giả                          ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST:                                                  ║
║  { "sku":"MD-ORPHAN", "name":"Orphan",                     ║
║    "categoryId":"00000000-0000-0000-0000-000000000000",   ║
║    "price":1000, "unit":"box" }                            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 422 Unprocessable Entity hoặc 400               ║
║  ← VERIFY: message chứa "category không tồn tại"          ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.7 POST /medicines (multipart) — Create with image

**Test case 4.7.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tạo medicine kèm ảnh                               ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
║  HEADER: Content-Type: multipart/form-data                 ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA:                ← INPUT / ← UPLOAD              ║
║    payload (JSON string):                                  ║
║    {                                                      ║
║      "sku":"MD-MP-001",                                   ║
║      "name":"Cough Syrup X",                              ║
║      "categoryId":"{{catCF}}",                            ║
║      "price":25000,                                       ║
║      "unit":"bottle",                                     ║
║      "prescriptionRequired":false                         ║
║    }                                                      ║
║    image: <file> cough-syrup.jpg (≤ 2MB, image/*)          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"uuid-{{medMp}}", "sku":"MD-MP-001",              ║
║    "imageUrl":"medicines/<uuid>.jpg", ← CAPTURE            ║
║    "status":"ACTIVE" }                                    ║
╠══════════════════════════════════════════════════════════════╣
║  ← CAPTURE: id → {{medMp}}, imageUrl → {{medImg}}         ║
║  ← VERIFY: imageUrl kết thúc bằng .jpg/.png               ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.7.B: Multipart không có image (optional)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create multipart không có image                     ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: chỉ có payload, KHÔNG có image                ║
║  EXPECTED: 200 OK, imageUrl = null                         ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.7.C: Image quá lớn (>5MB)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload image 6MB                                    ║
║  METHOD: POST  PATH: /api/v1/medicines                    ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: payload hợp lệ + image 6MB                    ║
║  EXPECTED: 400 hoặc 413 Payload Too Large                  ║
║  ← VERIFY: message chứa "file quá lớn" hoặc "max size"    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.8 PUT /medicines/{id} (JSON) — Update

**Test case 4.8.A: Happy path — đổi giá**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Cập nhật giá + tên                                  ║
║  METHOD: PUT  PATH: /api/v1/medicines/{{med1}}            ║
║  HEADER: Content-Type: application/json                    ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:                  ← INPUT                     ║
║  {                                                        ║
║    "name": "Paracetamol 500mg (updated)",                 ║
║    "price": 5500,                                         ║
║    "unit": "box"                                          ║
║  }                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{med1}}", "name":"Paracetamol 500mg (updated)", ║
║    "price":5500, "updatedAt":"2026-06-19T..." }           ║
║  ← VERIFY: price = 5500, updatedAt > createdAt            ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.8.B: Update không tồn tại (404)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update random UUID                                  ║
║  METHOD: PUT  PATH: /api/v1/medicines/00000000-0000-0000- ║
║                              0000-000000000000            ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST: { "name":"X", "price":1, "unit":"box" }         ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.8.C: Validation — name rỗng**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update name = ""                                    ║
║  METHOD: PUT  PATH: /api/v1/medicines/{{med1}}            ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST: { "name":"", "price":5000, "unit":"box" }       ║
║  EXPECTED: 400, error "Tên thuốc không được để trống"     ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.9 PUT /medicines/{id} (multipart) — Update with image

**Test case 4.9.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update medicine + replace ảnh                       ║
║  METHOD: PUT  PATH: /api/v1/medicines/{{medMp}}           ║
║  HEADER: Content-Type: multipart/form-data                 ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA:                                                ║
║    payload: { "name":"Cough Syrup X (v2)", "price":27000, ║
║              "unit":"bottle" }                            ║
║    image: <file> new-cough-syrup.jpg                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{medMp}}", "imageUrl":"medicines/<new-uuid>.jpg",║
║    "name":"Cough Syrup X (v2)", "price":27000 }           ║
║  ← VERIFY: imageUrl đã thay đổi so với {{medImg}}         ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.10 POST /medicines/{id}/image — Upload image

**Test case 4.10.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload ảnh cho medicine đã tạo                      ║
║  METHOD: POST  PATH: /api/v1/medicines/{{med1}}/image     ║
║  HEADER: Content-Type: multipart/form-data                 ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: image=<file> paracetamol.jpg    ← UPLOAD       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  { "id":"{{med1}}", "imageUrl":"medicines/<uuid>.jpg",    ║
║    "name":"Paracetamol 500mg", ... }                      ║
║  ← CAPTURE: imageUrl → {{medImg}}                         ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.10.B: Replace existing image**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload đè ảnh                                       ║
║  METHOD: POST  PATH: /api/v1/medicines/{{med1}}/image     ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: image=<file> paracetamol-v2.jpg                ║
║  EXPECTED: 200 OK, imageUrl mới khác imageUrl cũ           ║
║  ← VERIFY: GET /medicines/{{med1}}/image trả về ảnh mới   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.10.C: Medicine không tồn tại**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Upload ảnh cho UUID giả                             ║
║  METHOD: POST  PATH: /api/v1/medicines/00000000-0000-0000- ║
║                              0000-000000000000/image       ║
╠══════════════════════════════════════════════════════════════╣
║  FORM DATA: image=<file> x.jpg                             ║
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.11 GET /medicines/{id}/image — Get image

**Test case 4.11.A: Happy path — có ảnh**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Tải ảnh về                                          ║
║  METHOD: GET  PATH: /api/v1/medicines/{{med1}}/image      ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: medicine {{med1}} đã upload ảnh (4.10)           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║    Content-Type: image/jpeg  (hoặc image/png tùy file)     ║
║    (binary body)                                           ║
║  ← VERIFY: header Content-Type bắt đầu "image/"            ║
║  ← VERIFY: file mở được bằng image viewer                  ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.11.B: Medicine chưa có ảnh**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: GET ảnh cho medicine không có imageUrl              ║
║  METHOD: GET  PATH: /api/v1/medicines/{{med2}}/image      ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{med2}} tạo JSON không có ảnh                   ║
║  EXPECTED: 404 Not Found                                   ║
║  ← VERIFY: error "image not found"                        ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 4.12 DELETE /medicines/{id} — Soft delete

**Test case 4.12.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Xoá mềm medicine                                   ║
║  METHOD: DELETE  PATH: /api/v1/medicines/{{medNew}}       ║
╠══════════════════════════════════════════════════════════════╣
║  ← SEED: {{medNew}} đã tạo ở 4.6.A                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, body rỗng                               ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.12.B: Verify soft delete**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: GET medicine đã xoá                                 ║
║  METHOD: GET  PATH: /api/v1/medicines/{{medNew}}          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK (vẫn trả về nhưng status = INACTIVE)    ║
║  ← VERIFY: status = "INACTIVE"                            ║
║  ← VERIFY: GET /medicines (list) không còn {{medNew}}      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 4.12.C: Delete không tồn tại (404)**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Delete UUID giả                                     ║
║  METHOD: DELETE  PATH: /api/v1/medicines/00000000-0000-0000-║
║                              0000-000000000000            ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 404 Not Found                                   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Test Cases — SearchController

### 5.1 GET /search — Autocomplete (backward-compat)

**Test case 5.1.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Autocomplete "para"                                 ║
║  METHOD: GET  PATH: /api/v1/search?q=para                  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  [                                                        ║
║    { "id":"{{med1}}", "sku":"MD001",                      ║
║      "name":"Paracetamol 500mg", ... },                   ║
║    ...  (tối đa 5 items)                                  ║
║  ]                                                        ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: response.length <= 5                           ║
║  ← VERIFY: mỗi item.name chứa "para" (case-insensitive)   ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.B: No match**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Autocomplete "xyznotmatch"                          ║
║  METHOD: GET  PATH: /api/v1/search?q=xyznotmatch          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, []                                     ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.1.C: Empty query**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Autocomplete không truyền q                         ║
║  METHOD: GET  PATH: /api/v1/search                        ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, [] hoặc [top 5 medicines] (tuỳ impl)    ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.2 GET /search/medicines/autocomplete — Spec endpoint

**Test case 5.2.A: Happy path — top 5**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Spec autocomplete                                    ║
║  METHOD: GET  PATH: /api/v1/search/medicines/autocomplete  ║
║                              ?q=para                       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, [≤ 5 items], mỗi item chứa "para"      ║
║  ← VERIFY: items.length <= 5                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.2.B: Vietnamese keyword**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Autocomplete tiếng Việt                             ║
║  METHOD: GET  PATH: /api/v1/search/medicines/autocomplete  ║
║                              ?q=ho                         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, có thể trả về 0 items hoặc match theo   ║
║           SKU/sku chứa "ho" (không search theo description)║
║  ← VERIFY: tất cả item đều hợp lệ (không 500)             ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.3 GET /search/medicines — Search with filters

**Test case 5.3.A: Filter q + category + price**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search medicine + filter                            ║
║  METHOD: GET  PATH: /api/v1/search/medicines?q=para        ║
║                              &categoryId={{catPR}}         ║
║                              &minPrice=1000&maxPrice=10000 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK                                          ║
║  [ { "id":"{{med1}}", "name":"Paracetamol 500mg",          ║
║      "price":5000, ... } ]                                ║
║  ← VERIFY: mọi item.categoryId = {{catPR}}                ║
║  ← VERIFY: mọi item.price ∈ [1000, 10000]                 ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.3.B: Filter inStock=true**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search chỉ lấy thuốc còn hàng                      ║
║  METHOD: GET  PATH: /api/v1/search/medicines?inStock=true  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, chỉ trả về medicines có tồn kho > 0    ║
║  ← VERIFY: kết hợp với /api/v1/inventory xác nhận        ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.3.C: No filter, no q**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search không filter                                 ║
║  METHOD: GET  PATH: /api/v1/search/medicines              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, [all medicines ACTIVE]                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

### 5.4 GET /search/full — Full search (backward-compat)

**Test case 5.4.A: Happy path**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Full search                                         ║
║  METHOD: GET  PATH: /api/v1/search/full?q=para             ║
║                              &categoryId={{catPR}}         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, tương đương 5.3.A                      ║
╚══════════════════════════════════════════════════════════════╝
```

**Test case 5.4.B: Empty result**

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Full search no match                                ║
║  METHOD: GET  PATH: /api/v1/search/full?q=NoMatch123XYZ    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED: 200 OK, []                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Capture Variables (chốt cho file này)

Sau khi chạy xong tất cả test cases trên, các biến sau cần được lưu:

| Biến          | Mô tả                       | Đã capture ở step |
|---------------|------------------------------|-------------------|
| `{{med1}}`    | Paracetamol 500mg            | 4.6 / từ master   |
| `{{medNew}}`  | Aspirin 100mg (test create)  | 4.6.A             |
| `{{medMp}}`   | Cough Syrup X (multipart)    | 4.7.A             |
| `{{medSKU1}}` | "MD001"                      | master Step 6     |
| `{{medImg}}`  | "medicines/<uuid>.jpg"       | 4.10.A            |

---

## 7. Sign-off

| Role          | Name | Date | Signature |
|---------------|------|------|-----------|
| Tester        |      |      |           |
| Dev Lead      |      |      |           |
| Product Owner |      |      |           |

**End of 03-CATALOG.md** → Next: [`04-CATEGORY.md`](./04-CATEGORY.md)
