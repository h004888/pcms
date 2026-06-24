# UAT Test Scenario: Auth & User Management

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `user-service` (port 8081)
**UAT Doc Reference:** `01-AUTH-USER.md`
**Coverage:** UC01 (Auth) + UC02 (User Mgmt + Dashboard + AuditLog)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  USER-SERVICE                                                                ║
║  Tier    : B2B                                                                ║
║  Port    : 8081 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/{auth|users|dashboard|audit-logs}/** ║
║  Auth    : JWT HS256 - permitAll in dev, role-based in prod                   ║
║  DB      : MySQL 8 (schema = user_db)                                         ║
║  Tests   : 18 endpoints, ~50 cases, est. 90 min                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller         | Method | Path                              | Description                  | Test Cases |
|----|--------------------|--------|-----------------------------------|------------------------------|-----------:|
| 1  | AuthController     | POST   | `/auth/login`                     | Đăng nhập                    |          4 |
| 2  | AuthController     | POST   | `/auth/forgot-password`           | Quên mật khẩu                |          3 |
| 3  | AuthController     | POST   | `/auth/reset-password`            | Đặt lại mật khẩu             |          3 |
| 4  | AuthController     | POST   | `/auth/refresh`                   | Refresh access token         |          3 |
| 5  | AuthController     | POST   | `/auth/logout`                    | Đăng xuất                    |          2 |
| 6  | AuthController     | GET    | `/auth/me`                        | Current user profile         |          3 |
| 7  | AuthController     | PUT    | `/auth/password`                  | Đổi mật khẩu                |          4 |
| 8  | AuthController     | POST   | `/auth/verify-email`              | Xác thực email               |          2 |
| 9  | AuthController     | POST   | `/auth/resend-verification`       | Gửi lại email xác thực       |          3 |
| 10 | UserController     | GET    | `/users`                          | Danh sách user (paged)       |          4 |
| 11 | UserController     | GET    | `/users/export`                   | Export CSV                   |          2 |
| 12 | UserController     | GET    | `/users/{id}`                     | Chi tiết user                |          2 |
| 13 | UserController     | POST   | `/users`                          | Tạo user                     |          4 |
| 14 | UserController     | PUT    | `/users/{id}`                     | Cập nhật user                |          3 |
| 15 | UserController     | PUT    | `/users/{id}/role`                | Đổi role                     |          3 |
| 16 | UserController     | PUT    | `/users/{id}/status`              | Đổi trạng thái               |          3 |
| 17 | UserController     | POST   | `/users/{id}/unlock`              | Mở khoá tài khoản            |          2 |
| 18 | UserController     | DELETE | `/users/{id}`                     | Xoá mềm user                 |          2 |
| 19 | UserController     | PUT    | `/users/{id}/branch`              | Gán user vào branch          |          3 |
| 20 | UserController     | GET    | `/users/role/{role}`              | List user theo role          |          2 |
| 21 | DashboardController| GET    | `/dashboard/stats`                | KPI tổng quan                |          2 |
| 22 | DashboardController| GET    | `/dashboard/recent-logins`        | Login gần đây                |          2 |
| 23 | AuditLogController | GET    | `/audit-logs`                     | Lịch sử hoạt động           |          2 |
| **TOTAL**             |        |                                   |                              |     **~61**|

> Note: Endpoint count is 18 in the master plan, but this file documents
> 23 routing mappings (UC01 + UC02 controllers). The count discrepancy comes
> from counting HTTP method-path pairs vs. controller-level summary.

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `user_db` migrated
- [x] Eureka (`8761`) registered → `USER-SERVICE` instance visible
- [x] API Gateway (`8080`) running with route `/api/v1/auth/**` and `/api/v1/users/**` mapped to user-service
- [x] `scripts/seed-test-data.sql` loaded (admin user exists)
- [x] Mail sender disabled in dev (verification tokens logged to console)

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8080/actuator/health | jq .status     ║
║  → "UP"                                                        ║
║  $ curl http://localhost:8761/eureka/apps/USER-SERVICE          ║
║  → 1+ instance registered                                      ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  Admin user (seeded by SQL):                                ║
║    email    : admin@pcms.vn                                  ║
║    password : admin123                                      ║
║    role     : ADMIN                                         ║
║                                                              ║
║  Branch manager (seeded):                                    ║
║    email    : manager.q1@pcms.vn                             ║
║    password : manager123                                    ║
║    role     : BRANCH_MANAGER                                 ║
║                                                              ║
║  Pharmacist (seeded):                                        ║
║    email    : pharmacist.q1@pcms.vn                          ║
║    password : pharma123                                     ║
║    role     : PHARMACIST                                    ║
║                                                              ║
║  Note: B2B UAT only. B2C customer login is in 07-CUSTOMER   ║
╚══════════════════════════════════════════════════════════════╝
```

To bootstrap via API instead of SQL, see Master Plan §7.1 (POST `/api/v1/users`).

---

## 4. Variables to Capture

| Variable             | Example                                | Captured from                |
|----------------------|----------------------------------------|------------------------------|
| `{{accessToken}}`    | `eyJhbGciOiJIUzI1...`                  | POST /auth/login             |
| `{{refreshToken}}`   | `eyJhbGciOiJIUzI1...`                  | POST /auth/login             |
| `{{adminId}}`        | `uuid`                                 | POST /auth/login → userId    |
| `{{userId}}`         | `uuid`                                 | POST /auth/login             |
| `{{managerId}}`      | `uuid`                                 | POST /auth/login (manager)   |
| `{{pharmaId}}`       | `uuid`                                 | POST /auth/login (pharmacist)|
| `{{createdUserId}}`  | `uuid`                                 | POST /users                  |
| `{{resetToken}}`     | `opaque string`                        | POST /auth/forgot-password   |
| `{{verifyToken}}`    | `opaque string`                        | POST /auth/resend-verification |
| `{{branchHQ}}`       | `uuid`                                 | (from branch service)        |

---

# PART A — AuthController (`/auth`)

---

## TC-01: POST /auth/login (Happy path — Admin)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Login as Admin                                       ║
║  METHOD: POST  PATH: /api/v1/auth/login                     ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    Content-Type : application/json                          ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "email": "admin@pcms.vn",                                ║
║    "password": "admin123"                                   ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",                ║
║    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",               ║
║    "tokenType": "Bearer",                                   ║
║    "expiresIn": 3600,                                       ║
║    "userId": "uuid",                                        ║
║    "email": "admin@pcms.vn",                                ║
║    "fullName": "System Admin",                              ║
║    "role": "ADMIN",                                         ║
║    "branchId": null                                         ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: tokens are non-empty JWTs (3 segments split by .)║
║  ← CAPTURE: {{accessToken}}, {{refreshToken}}, {{adminId}}  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.1: POST /auth/login (Happy path — Manager)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Login as Branch Manager                             ║
║  REQUEST:                                                   ║
║  {                                                          ║
║    "email": "manager.q1@pcms.vn",                           ║
║    "password": "manager123"                                 ║
║  }                                                          ║
║  EXPECTED: 200, role="BRANCH_MANAGER", branchId=<Q1 UUID>   ║
║  ← CAPTURE: {{managerId}}                                  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.2: POST /auth/login (Negative — Wrong password)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Wrong password (BR05 lockout counter)                ║
║  REQUEST:                                                   ║
║  {                                                          ║
║    "email": "admin@pcms.vn",                                ║
║    "password": "wrongpassword"                              ║
║  }                                                          ║
║  EXPECTED: 401 Unauthorized                                 ║
║  { "error": "INVALID_CREDENTIALS",                         ║
║    "message": "Email hoặc mật khẩu không đúng" }            ║
║  ← VERIFY: response has no token, no userId                ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.3: POST /auth/login (Negative — Account locked)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Trigger BR05 lockout (5 wrong passwords)             ║
║  REPEAT TC-01.2 five times in a row                         ║
║  EXPECTED:                                                  ║
║    Attempts 1-4: 401 INVALID_CREDENTIALS                   ║
║    Attempt 5   : 423 LOCKED                                 ║
║  { "error": "ACCOUNT_LOCKED",                               ║
║    "message": "Tài khoản đã bị khoá sau 5 lần đăng nhập    ║
║                không thành công" }                          ║
║  ← VERIFY: subsequent attempts keep returning 423           ║
║  ← RESET: call POST /api/v1/users/{{adminId}}/unlock        ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-01.4: POST /auth/login (Negative — Validation)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Empty body                                           ║
║  REQUEST: {}                                                ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "error": "VALIDATION_ERROR",                             ║
║    "fields": [                                              ║
║      { "field":"email", "msg":"Email không được để trống" },║
║      { "field":"password","msg":"Mật khẩu không được để trống"} ║
║    ]}                                                       ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: POST /auth/forgot-password

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Request reset token for existing email               ║
║  METHOD: POST  PATH: /api/v1/auth/forgot-password           ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "email": "admin@pcms.vn" }                               ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "message": "Email hướng dẫn đặt lại mật khẩu đã được gửi",║
║    "resetToken": "<opaque-token>",   ← dev only             ║
║    "expiresAt": "2026-06-19T11:00:00Z"                     ║
║  }                                                          ║
║  ← CAPTURE: {{resetToken}}                                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-02.1: POST /auth/forgot-password (Non-existing email)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "email": "ghost@nowhere.vn" }                   ║
║  EXPECTED: 200 OK with generic message                      ║
║  { "message": "Email hướng dẫn đặt lại mật khẩu đã được gửi"║
║    "resetToken": null }                                     ║
║  ← VERIFY: same response shape (no email enumeration leak)  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-02.2: POST /auth/forgot-password (Validation)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "email": "not-an-email" }                       ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"email","msg":"Email không đúng định dạng" }     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /auth/reset-password

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Apply new password using reset token                 ║
║  METHOD: POST  PATH: /api/v1/auth/reset-password            ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "token": "{{resetToken}}",                               ║
║    "newPassword": "NewAdmin@2026"                           ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "message": "Password reset successfully" }                ║
║  ← VERIFY: subsequent login with new password succeeds      ║
║  ← RESET  : restore old password via PUT /auth/password     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.1: POST /auth/reset-password (Expired token)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "token":"EXPIRED-TOKEN", "newPassword":"X1@x" } ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "error":"TOKEN_EXPIRED",                                ║
║    "message":"Token đã hết hạn" }                           ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-03.2: POST /auth/reset-password (Weak password)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "token":"VALID","newPassword":"123" }            ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"newPassword",                                  ║
║    "msg":"Mật khẩu phải có độ dài từ 8 đến 64 ký tự" }      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: POST /auth/refresh

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Exchange refresh token for new access token          ║
║  METHOD: POST  PATH: /api/v1/auth/refresh                   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "refreshToken": "{{refreshToken}}" }                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "accessToken": "eyJhbGciOiJIUzI1NiJ9...NEW...",         ║
║    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...NEW...",        ║
║    "tokenType": "Bearer",                                   ║
║    "expiresIn": 3600,                                       ║
║    "userId": "{{adminId}}",                                 ║
║    "role": "ADMIN"                                          ║
║  }                                                          ║
║  ← VERIFY: tokens differ from TC-01 (rotation)             ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.1: POST /auth/refresh (Invalid token)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "refreshToken": "not-a-jwt" }                   ║
║  EXPECTED: 401 Unauthorized                                 ║
║  { "error":"INVALID_TOKEN" }                                ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-04.2: POST /auth/refresh (Missing field)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: {}                                                ║
║  EXPECTED: 500 (NullPointerException) or 400                ║
║  NOTE   : Service throws NPE on null - observed behavior.   ║
║           Bug report: BUG-USER-001 should return 400.       ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: POST /auth/logout

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Logout (blacklist access token)                      ║
║  METHOD: POST  PATH: /api/v1/auth/logout                    ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    Authorization : Bearer {{accessToken}}                   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT (optional)             ║
║  { "refreshToken": "{{refreshToken}}" }                     ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "message": "Logged out" }                                ║
║  ← VERIFY: subsequent call with same token → 401 BLACKLISTED║
╚══════════════════════════════════════════════════════════════╝
```

## TC-05.1: POST /auth/logout (Idempotent — no body, no header)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: empty body, no Authorization header                ║
║  EXPECTED: 200 OK { "message": "Logged out" }               ║
║  ← VERIFY: endpoint should not throw 401 in dev permitAll   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: GET /auth/me

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get current user profile (TICKET-101)                ║
║  METHOD: GET  PATH: /api/v1/auth/me                         ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    X-User-Id : {{adminId}}    ← set by API Gateway          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{adminId}}",                                     ║
║    "email": "admin@pcms.vn",                                ║
║    "fullName": "System Admin",                              ║
║    "phone": "0900000001",                                   ║
║    "role": "ADMIN",                                         ║
║    "branchId": null,                                        ║
║    "status": "ACTIVE",                                      ║
║    "emailVerified": true,                                   ║
║    "lastLoginAt": "2026-06-19T10:00:00Z",                   ║
║    "createdAt": "2026-01-01T00:00:00Z",                     ║
║    "permissions": [                                         ║
║      "USER_MGMT","BRANCH_MGMT","MEDICINE_MGMT",             ║
║      "CATEGORY_MGMT","SUPPLIER_MGMT","INVENTORY_MGMT",      ║
║      "ORDER_MGMT","PAYMENT_MGMT","CUSTOMER_MGMT",           ║
║      "REPORT_VIEW","PRESCRIPTION_MGMT","NOTIF_MGMT",        ║
║      "AUDIT_VIEW"                                           ║
║    ]                                                        ║
║  }                                                          ║
║  ← VERIFY: ADMIN permissions has 13 keys                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-06.1: GET /auth/me (as Pharmacist — permission scope)

```
╔══════════════════════════════════════════════════════════════╗
║  HEADER: X-User-Id: {{pharmaId}}                            ║
║  EXPECTED: 200 OK                                           ║
║  { "role": "PHARMACIST",                                    ║
║    "permissions": ["MEDICINE_MGMT","INVENTORY_VIEW",        ║
║                    "ORDER_MGMT","PAYMENT_PROCESS",          ║
║                    "CUSTOMER_MGMT","PRESCRIPTION_MGMT",      ║
║                    "NOTIF_VIEW"] }                          ║
║  ← VERIFY: only 7 permission keys (vs ADMIN's 13)           ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-06.2: GET /auth/me (Missing X-User-Id header)

```
╔══════════════════════════════════════════════════════════════╗
║  HEADER: (none)                                             ║
║  EXPECTED: 200 with body { "id": null, ... }                ║
║  NOTE   : Controller uses parseUuid() → returns null on     ║
║           missing header. Service then returns empty shell. ║
║  ← VERIFY: response status 200, no exception                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-07: PUT /auth/password

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Change own password (TICKET-102, FR1.3)              ║
║  METHOD: PUT  PATH: /api/v1/auth/password                   ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    X-User-Id : {{adminId}}                                  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "currentPassword": "admin123",                           ║
║    "newPassword": "NewAdmin@2026",                          ║
║    "confirmPassword": "NewAdmin@2026"                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "message": "Đổi mật khẩu thành công" }                   ║
║  ← VERIFY: all refresh tokens revoked (force re-login)      ║
║  ← RESET : change back to admin123 for subsequent tests     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-07.1: PUT /auth/password (Wrong current password)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST:                                                   ║
║  { "currentPassword":"WRONG",                               ║
║    "newPassword":"NewAdmin@2026",                           ║
║    "confirmPassword":"NewAdmin@2026" }                      ║
║  EXPECTED: 401 Unauthorized                                 ║
║  { "error":"INVALID_CURRENT_PASSWORD" }                     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-07.2: PUT /auth/password (Weak new password — no uppercase)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "currentPassword":"admin123",                   ║
║             "newPassword":"newadmin123",                    ║
║             "confirmPassword":"newadmin123" }               ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"newPassword",                                  ║
║    "msg":"Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 số       ║
║           và 1 ký tự đặc biệt" }                           ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-07.3: PUT /auth/password (Password mismatch)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "currentPassword":"admin123",                   ║
║             "newPassword":"NewAdmin@2026",                  ║
║             "confirmPassword":"Different@2026" }            ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"confirmPassword",                             ║
║    "msg":"Xác nhận mật khẩu không khớp" }                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-08: POST /auth/verify-email

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify email with token (TICKET-103)                 ║
║  METHOD: POST  PATH: /api/v1/auth/verify-email              ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "token": "{{verifyToken}}" }                             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "message": "Email đã được xác thực thành công" }        ║
║  ← VERIFY: GET /auth/me → emailVerified: true               ║
║  NOTE   : {{verifyToken}} captured from TC-09 (resend)      ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-08.1: POST /auth/verify-email (Invalid token)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "token": "INVALID-TOKEN-STRING" }               ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "error":"INVALID_TOKEN",                                ║
║    "message":"Token không hợp lệ hoặc đã hết hạn" }         ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-09: POST /auth/resend-verification

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Request fresh verification token (TICKET-104)        ║
║  METHOD: POST  PATH: /api/v1/auth/resend-verification       ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "email": "newuser@pcms.vn" }                             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "message": "Đã gửi lại email xác thực",                 ║
║    "token": "<opaque-token>",  ← dev only                   ║
║    "expiresAt": "2026-06-19T11:30:00Z"                     ║
║  }                                                          ║
║  ← CAPTURE: {{verifyToken}}                                ║
║  ← VERIFY: token length > 20 chars                          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-09.1: POST /auth/resend-verification (Non-existing email)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "email":"ghost@nowhere.vn" }                    ║
║  EXPECTED: 200 OK (same response shape, token=null)         ║
║  { "message":"Nếu email tồn tại và chưa xác thực,          ║
║            hệ thống đã gửi lại hướng dẫn xác thực",        ║
║    "token": null, "expiresAt": null }                       ║
║  ← VERIFY: identical structure to TC-09 (no leak)          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-09.2: POST /auth/resend-verification (Rate limit)

```
╔══════════════════════════════════════════════════════════════╗
║  REPEAT TC-09 twice within 60s for same email              ║
║  EXPECTED:                                                  ║
║    1st: 200 with new token                                 ║
║    2nd: 429 Too Many Requests                              ║
║  { "error":"RATE_LIMITED",                                 ║
║    "message":"Vui lòng chờ 60 giây trước khi yêu cầu lại" }║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART B — UserController (`/users`)

---

## TC-10: GET /users

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List users with pagination (UC02 main view)          ║
║  METHOD: GET  PATH: /api/v1/users?page=0&size=20            ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                              ║
║    page=0&size=20&search=admin&role=ADMIN                  ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "data": [                                                ║
║      { "id":"uuid","email":"admin@pcms.vn",                ║
║        "fullName":"System Admin","role":"ADMIN",            ║
║        "status":"ACTIVE", "branchId":null, ... }            ║
║    ],                                                       ║
║    "page": 0, "size": 20, "total": 4, "totalPages": 1     ║
║  }                                                          ║
║  ← VERIFY: total = admin + manager + pharmacist + customer  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-10.1: GET /users (Filter by role + branch)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?role=PHARMACIST&branchId={{branchQ1}}              ║
║  EXPECTED: 200, data[] contains only PHARMACIST users       ║
║           assigned to branch Q1                             ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-10.2: GET /users (Pagination — last page)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?page=999&size=20                                   ║
║  EXPECTED: 200, data=[] empty array, totalPages=N           ║
║  ← VERIFY: no 404 for out-of-range page                     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-10.3: GET /users (Invalid role enum)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?role=GOD                                           ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "error":"INVALID_ENUM_VALUE" }                           ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-11: GET /users/export

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Export user list as CSV                              ║
║  METHOD: GET  PATH: /api/v1/users/export                    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║    Content-Type: text/csv                                   ║
║    Content-Disposition: attachment; filename=users.csv     ║
║                                                              ║
║  BODY (CSV):                                                ║
║  id,email,fullName,phone,role,status,...                    ║
║  uuid-1,admin@pcms.vn,System Admin,0900000001,ADMIN,ACTIVE  ║
║  uuid-2,manager.q1@pcms.vn,...                              ║
║  ← VERIFY: header row + at least 1 data row                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-11.1: GET /users/export (Filtered)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?role=PHARMACIST                                    ║
║  EXPECTED: CSV body contains only PHARMACIST rows           ║
║  ← VERIFY: CSV can be saved and parsed by spreadsheet apps  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-12: GET /users/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get user detail by UUID                              ║
║  METHOD: GET  PATH: /api/v1/users/{{adminId}}               ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{adminId}}",                                     ║
║    "email": "admin@pcms.vn",                                ║
║    "fullName": "System Admin",                              ║
║    "phone": "0900000001",                                   ║
║    "role": "ADMIN",                                         ║
║    "branchId": null,                                        ║
║    "status": "ACTIVE",                                      ║
║    "lastLoginAt": "...", "createdAt": "...", "updatedAt": "..." ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-12.1: GET /users/{id} (Not found)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/users/00000000-0000-0000-0000-000000000000   ║
║  EXPECTED: 404 Not Found                                    ║
║  { "error":"USER_NOT_FOUND",                                ║
║    "message":"Không tìm thấy người dùng" }                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-13: POST /users

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Create new user (Admin only)                         ║
║  METHOD: POST  PATH: /api/v1/users                          ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    Authorization : Bearer {{accessToken}}                   ║
║    Content-Type  : application/json                          ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "email": "newuser@pcms.vn",                              ║
║    "fullName": "Nguyen Van New",                            ║
║    "phone": "0909876543",                                   ║
║    "role": "PHARMACIST",                                    ║
║    "branchId": "{{branchQ1}}"                               ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "uuid",                                            ║
║    "email": "newuser@pcms.vn",                              ║
║    "fullName": "Nguyen Van New",                            ║
║    "role": "PHARMACIST",                                    ║
║    "branchId": "{{branchQ1}}",                              ║
║    "status": "ACTIVE",                                      ║
║    "createdAt": "..."                                       ║
║  }                                                          ║
║  ← CAPTURE: {{createdUserId}}                              ║
║  NOTE    : temp password generated server-side, logged      ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-13.1: POST /users (Duplicate email)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: same email as TC-13                               ║
║  EXPECTED: 409 Conflict                                     ║
║  { "error":"EMAIL_ALREADY_EXISTS",                          ║
║    "message":"Email đã được sử dụng" }                      ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-13.2: POST /users (Missing role)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "email":"x@y.vn","fullName":"X","phone":"0",   ║
║             "branchId":"uuid" }   ← no role field            ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"role","msg":"Vai trò không được để trống" }     ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-13.3: POST /users (Invalid email)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "email":"not-an-email","fullName":"X",          ║
║             "phone":"0","role":"ADMIN" }                    ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "field":"email","msg":"Email không đúng định dạng" }     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-14: PUT /users/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Update user profile                                  ║
║  METHOD: PUT  PATH: /api/v1/users/{{createdUserId}}         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  {                                                          ║
║    "fullName": "Nguyen Van Updated",                        ║
║    "phone": "0901112222",                                   ║
║    "role": "PHARMACIST",                                    ║
║    "branchId": "{{branchQ7}}",                              ║
║    "status": "ACTIVE"                                       ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{createdUserId}}", "fullName":"Nguyen Van Updated",║
║    "branchId":"{{branchQ7}}", "updatedAt":"...", ... }      ║
║  ← VERIFY: branchId changed from Q1 to Q7                   ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-14.1: PUT /users/{id} (404)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/users/00000000-0000-0000-0000-000000000000   ║
║  EXPECTED: 404 Not Found                                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-14.2: PUT /users/{id} (Status=LOCKED — edge case)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { ... "status":"LOCKED" }                          ║
║  EXPECTED: 200 OK (status changed to LOCKED)                ║
║  ← VERIFY: GET /users/{id} → status=LOCKED                  ║
║  ← RESET  : PUT /users/{id}/status {status:ACTIVE}          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-15: PUT /users/{id}/role

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Change user role (promote to BRANCH_MANAGER)         ║
║  METHOD: PUT  PATH: /api/v1/users/{{createdUserId}}/role   ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "role": "BRANCH_MANAGER" }                               ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{createdUserId}}", "role":"BRANCH_MANAGER", ... } ║
║  ← VERIFY: GET /users/{id} → role changed                  ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-15.1: PUT /users/{id}/role (Invalid role)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "role": "SUPER_USER" }                          ║
║  EXPECTED: 400 Bad Request                                  ║
║  { "error":"INVALID_ENUM_VALUE" }                           ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-15.2: PUT /users/{id}/role (Self-demotion guard)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: PUT /users/{{adminId}}/role { "role":"PHARMACIST" }║
║  EXPECTED: 422 Unprocessable Entity (or 200 if no guard)    ║
║  NOTE    : Behavior depends on guard logic in service layer. ║
║            If guard exists: cannot demote last ADMIN.       ║
║  ← VERIFY: at minimum role changes if guard is permissive   ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-16: PUT /users/{id}/status

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Suspend user account                                 ║
║  METHOD: PUT  PATH: /api/v1/users/{{createdUserId}}/status  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "status": "INACTIVE" }                                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{createdUserId}}", "status":"INACTIVE", ... }     ║
║  ← VERIFY: user cannot log in with INACTIVE status          ║
║  ← RESET  : PUT status {status:ACTIVE} before next tests    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-16.1: PUT /users/{id}/status (404)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/users/00000000-0000-0000-0000-000000000000/status║
║  EXPECTED: 404 Not Found                                    ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-16.2: PUT /users/{id}/status (Same value — idempotent)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "status":"INACTIVE" } on already-INACTIVE user  ║
║  EXPECTED: 200 OK (no-op, idempotent)                       ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-17: POST /users/{id}/unlock

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Unlock account after BR05 lockout                    ║
║  METHOD: POST  PATH: /api/v1/users/{{adminId}}/unlock       ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{adminId}}", "status":"ACTIVE", ... }              ║
║  ← VERIFY: GET /auth/login with correct password succeeds   ║
║  PREREQ  : TC-01.3 ran (account was locked)                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-17.1: POST /users/{id}/unlock (Not locked — idempotent)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: unlock a user already in ACTIVE status             ║
║  EXPECTED: 200 OK (no error)                                ║
║  ← VERIFY: status remains ACTIVE                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-18: DELETE /users/{id}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Soft delete user (FR2.5)                             ║
║  METHOD: DELETE  PATH: /api/v1/users/{{createdUserId}}      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    204 No Content                                           ║
║  ← VERIFY: GET /users/{id} still returns 200 (soft delete)  ║
║  ← VERIFY: user cannot log in (auth blocked on soft delete) ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-18.1: DELETE /users/{id} (404)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/users/00000000-0000-0000-0000-000000000000   ║
║  EXPECTED: 404 Not Found (or 204 idempotent depending on impl)║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-19: PUT /users/{id}/branch (TICKET-106)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Assign user to a branch (FR2.3)                      ║
║  METHOD: PUT  PATH: /api/v1/users/{{createdUserId}}/branch  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                    ║
║    X-User-Id : {{adminId}}   ← actor id for audit trail     ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:               ← INPUT                        ║
║  { "branchId": "{{branchHQ}}" }                             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "id":"{{createdUserId}}","branchId":"{{branchHQ}}",... }║
║  ← VERIFY: GET /audit-logs?action=ASSIGN_BRANCH includes     ║
║           actorId={{adminId}}, targetId={{createdUserId}}   ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-19.1: PUT /users/{id}/branch (Un-assign — branchId=null)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "branchId": null }                              ║
║  EXPECTED: 200 OK, branchId becomes null                    ║
║  ← VERIFY: GET /users/{id} → branchId=null                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-19.2: PUT /users/{id}/branch (Invalid branchId)

```
╔══════════════════════════════════════════════════════════════╗
║  REQUEST: { "branchId":"00000000-0000-0000-0000-000000000000"}║
║  EXPECTED: 422 Unprocessable Entity                         ║
║  { "error":"BRANCH_NOT_FOUND",                             ║
║    "message":"Chi nhánh không tồn tại" }                    ║
║  NOTE    : Validation may happen at gateway or in service.  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-20: GET /users/role/{role}

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all users with a specific role                  ║
║  METHOD: GET  PATH: /api/v1/users/role/PHARMACIST           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  [ { "id":"uuid","role":"PHARMACIST", ... },                ║
║    { "id":"uuid","role":"PHARMACIST", ... } ]               ║
║  ← VERIFY: response is a JSON array (not paginated)         ║
║  ← VERIFY: all entries have role=PHARMACIST                 ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-20.1: GET /users/role/{role} (Unknown role → 400)

```
╔══════════════════════════════════════════════════════════════╗
║  PATH: /api/v1/users/role/SUPER_HERO                        ║
║  EXPECTED: 400 Bad Request (enum binding failure)           ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART C — DashboardController (`/dashboard`)

---

## TC-21: GET /dashboard/stats

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get KPI summary for admin home screen                ║
║  METHOD: GET  PATH: /api/v1/dashboard/stats                 ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  {                                                          ║
║    "totalUsers": 5,                                         ║
║    "activeUsers": 4,                                        ║
║    "lockedUsers": 0,                                        ║
║    "inactiveUsers": 1,                                      ║
║    "branchManagers": 1,                                     ║
║    "pharmacists": 2,                                        ║
║    "administrators": 2                                      ║
║  }                                                          ║
║  ← VERIFY: totalUsers = activeUsers + lockedUsers + inactive ║
║  ← VERIFY: sum of role counters = totalUsers                ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-21.1: GET /dashboard/stats (After seeding changes)

```
╔══════════════════════════════════════════════════════════════╗
║  ACTION : Create new PHARMACIST, then re-fetch stats         ║
║  EXPECTED: totalUsers += 1, pharmacists += 1                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-22: GET /dashboard/recent-logins

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get recent login audit (last 20)                     ║
║  METHOD: GET  PATH: /api/v1/dashboard/recent-logins         ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS: ?page=0&size=20                              ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "data":[ { "id":"uuid","email":"admin@pcms.vn",         ║
║              "lastLoginAt":"2026-06-19T10:00:00Z", ... },   ║
║           { "id":"uuid","email":"manager.q1@pcms.vn", ... } ],║
║    "page":0,"size":20,"total":3,"totalPages":1 }            ║
║  ← VERIFY: sorted by lastLoginAt DESC                       ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-22.1: GET /dashboard/recent-logins (Pagination)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?size=5                                              ║
║  EXPECTED: 200, data.length <= 5                             ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART D — AuditLogController (`/audit-logs`)

---

## TC-23: GET /audit-logs

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Search audit logs                                    ║
║  METHOD: GET  PATH: /api/v1/audit-logs                      ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS: ?userId={{adminId}}&page=0&size=20           ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                         ║
║    200 OK                                                   ║
║  { "data":[                                                 ║
║      { "id":"uuid","userId":"{{adminId}}",                  ║
║        "action":"LOGIN_SUCCESS",                            ║
║        "targetId":null,"ipAddress":"127.0.0.1",             ║
║        "description":"...", "createdAt":"..." },            ║
║      { "id":"uuid","action":"CHANGE_PASSWORD", ... }        ║
║    ],                                                       ║
║    "page":0,"size":20,"total":N,"totalPages":1 }             ║
║  ← VERIFY: sorted by createdAt DESC                         ║
║  ← VERIFY: includes ASSIGN_BRANCH from TC-19                ║
╚══════════════════════════════════════════════════════════════╝
```

## TC-23.1: GET /audit-logs (Filter by action)

```
╔══════════════════════════════════════════════════════════════╗
║  QUERY: ?action=LOGIN_SUCCESS                                ║
║  EXPECTED: 200, data[] contains only LOGIN_SUCCESS entries  ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PART E — Cross-Endpoint Integration

## TC-24: Full user lifecycle (E2E within this service)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Exercise complete user lifecycle                     ║
╠══════════════════════════════════════════════════════════════╣
║  1. POST /users                      → create user (TC-13)   ║
║  2. POST /auth/resend-verification  → get token (TC-09)    ║
║  3. POST /auth/verify-email         → verify email (TC-08) ║
║  4. GET  /auth/me                   → confirm verified      ║
║  5. PUT  /users/{id}/branch         → assign branch (TC-19)║
║  6. PUT  /users/{id}/role           → promote to MANAGER   ║
║  7. PUT  /users/{id}/status         → set INACTIVE          ║
║  8. POST /users/{id}/unlock         → no-op                 ║
║  9. GET  /audit-logs?action=...     → verify audit entries  ║
║  10. DELETE /users/{id}             → soft delete            ║
║  11. GET  /users/{id}               → still 200 (soft)     ║
║  ← VERIFY: all 10 audit log entries exist with correct      ║
║           actor, target, ipAddress, timestamps              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Summary of Test Variables (capture at end)

| Variable             | Final value                            | Source                |
|----------------------|----------------------------------------|------------------------|
| `{{accessToken}}`    | (re-capture after each login)          | TC-01                 |
| `{{refreshToken}}`   | (re-capture)                           | TC-01                 |
| `{{adminId}}`        | uuid                                   | TC-01                 |
| `{{managerId}}`      | uuid                                   | TC-01.1               |
| `{{pharmaId}}`       | uuid                                   | (seeded)              |
| `{{createdUserId}}`  | uuid                                   | TC-13                 |
| `{{resetToken}}`     | opaque-string                          | TC-02                 |
| `{{verifyToken}}`    | opaque-string                          | TC-09                 |

---

## 6. Known Issues / Quirks

```
╔══════════════════════════════════════════════════════════════╗
║  BUG-USER-001: POST /auth/refresh with empty body returns    ║
║                500 instead of 400 (NullPointerException).    ║
║                Severity: Minor                               ║
║  BUG-USER-002: GET /auth/me without X-User-Id returns 200   ║
║                with null fields. Behavior depends on gateway.║
║                Severity: Trivial                             ║
║  OBS-USER-003: TEMP_PASSWORD is logged to console in dev     ║
║                (grep "Generated password" user-service.log). ║
║                Production should send via email only.        ║
║  OBS-USER-004: POST /auth/resend-verification uses 60s rate  ║
║                limit. Test fixtures should reset or wait.    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Sign-off

| Role          | Name | Date | Pass / Fail | Notes |
|---------------|------|------|--------------|-------|
| Tester        |      |      | ☐            |       |
| Dev Lead      |      |      | ☐            |       |
| QA Manager    |      |      | ☐            |       |

**End of `01-AUTH-USER.md`** → Next: [`02-BRANCH.md`](./02-BRANCH.md)
