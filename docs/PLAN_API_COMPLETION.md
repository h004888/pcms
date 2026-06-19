# Kế hoạch hoàn thiện đầy đủ tất cả API còn thiếu - PCMS

**Version:** 1.1
**Ngày cập nhật:** 2026-06-19
**Trạng thái:** ✅ **TẤT CẢ SPRINT ĐÃ HOÀN THÀNH** (11/11)
**Stack hiện tại:** Spring Boot 4.0.7 + Java 21 + MySQL 8 + Python 3.11 + FastAPI + PostgreSQL/pgvector + Redis
**Phạm vi:** 15 API B2B (quick win) + ~140 API B2C + AI/Pharmacist/Mobile/Health/Ecom-Ops services

---

## Tổng kết

| Sprint | Trạng thái | API | Service |
|--------|------------|----:|---------|
| 1 - B2B Auth/User | ✅ Done | 6 | user-service, customer-service |
| 2 - B2B Catalog/Inv/Order/Pay | ✅ Done | 6 | inventory, order, payment |
| 3 - B2B Prescription/Notif/Report | ✅ Done | 7 | prescription, notification, report |
| 4 - customer-portal scaffold | ✅ Done | 8 | customer-portal-service (NEW) |
| 5 - Cart/Checkout/Order/Voucher | ✅ Done | 12 | customer-portal-service |
| 6 - Vaccine/Health/Verify/Video | ✅ Done | 13 | customer-portal-service |
| 7 - Customer Account | ✅ Done | 23 | customer-portal-service |
| 8 - ai-engine-service (Python) | ✅ Done | 18 | ai-engine-service (NEW) |
| 9 - pharmacist-workbench | ✅ Done | 15 | pharmacist-workbench-service (NEW) |
| 10 - mobile-bff + health + ecom-ops | ✅ Done | 15 | mobile-bff, health-tools, ecom-ops (NEW) |
| **TỔNG** | **11/11** | **128 API mới** | **5 service mới** |

---

## Mục lục

1. [Tóm tắt điều hành](#1-tóm-tắt-điều-hành)
2. [Convention bắt buộc khi code](#2-convention-bắt-buộc-khi-code)
3. [Sprint Breakdown](#3-sprint-breakdown)
4. [Sprint 1 – Hoàn thiện B2B Auth/User](#sprint-1--hoàn-thiện-b2b-authuser)
5. [Sprint 2 – Hoàn thiện B2B Catalog/Inventory/Order/Payment](#sprint-2--hoàn-thiện-b2b-cataloginventoryorderpayment)
6. [Sprint 3 – Hoàn thiện B2B Prescription/Notification/Report](#sprint-3--hoàn-thiện-b2b-prescriptionnotificationreport)
7. [Sprint 4 – `customer-portal-service` (B2C foundation)](#sprint-4--customer-portal-service-b2c-foundation)
8. [Sprint 5 – B2C E-commerce (Cart/Checkout/Order Tracking)](#sprint-5--b2c-e-commerce-cartcheckoutorder-tracking)
9. [Sprint 6 – B2C Lookup + Store Locator + Vaccine](#sprint-6--b2c-lookup--store-locator--vaccine)
10. [Sprint 7 – B2C Customer Account (Address/Family/Wallet/Favorites)](#sprint-7--b2c-customer-account-addressfamilywalletfavorites)
11. [Sprint 8 – `ai-engine-service` (Python/FastAPI)](#sprint-8--ai-engine-service-pythonfastapi)
12. [Sprint 9 – `pharmacist-workbench-service`](#sprint-9--pharmacist-workbench-service)
13. [Sprint 10 – Mobile BFF + Health Tools + Ecom-Ops](#sprint-10--mobile-bff--health-tools--ecom-ops)
14. [Verification & Acceptance](#14-verification--acceptance)
15. [Risk & Mitigation](#15-risk--mitigation)
16. [Effort tổng thể](#16-effort-tổng-thể)

---

## 1. Tóm tắt điều hành

### 1.1 Bối cảnh

Báo cáo `docs/API_VS_DOCS_COMPARISON.md` cho thấy:

| Nhóm | Mức độ phủ | Hành động |
|------|-----------|-----------|
| B2B Authenticated App (28 màn hình SCR-*) | 76% khớp chính xác, 24% thiếu/bổ sung | **Sprint 1–3** (3 tuần) |
| B2C Customer Portal (52 màn hình, UC14–UC19) | ~6% | **Sprint 4–10** (10–14 tuần) |

### 1.2 Tổng API cần bổ sung

- **B2B**: 15 API thực sự thiếu (Sprint 1–3)
- **B2C**: ~100–130 API + 6 service mới (Sprint 4–10)
- **Tổng effort ước tính**: 13–17 tuần với 1 squad 4–5 người

### 1.3 Nguyên tắc thiết kế

1. **Tái sử dụng code hiện có** – entity, exception, security đã chuẩn, chỉ bổ sung controller/service method
2. **Không phá vỡ API hiện hữu** – path alias (vd `/categories` thay cho `/medicines/categories`) đã được chấp nhận trong báo cáo
3. **Outbox pattern** – các event nghiệp vụ (order paid, low stock) đi qua `EventPublisher` (pcms-common)
4. **Idempotency** – tất cả `POST/PUT/DELETE` hỗ trợ header `Idempotency-Key` (CR-05)
5. **Audit + i18n** – sử dụng `BaseEntity` cho audit field, `ErrorResponse` cho error envelope (RFC 7807)

---

## 2. Convention bắt buộc khi code

### 2.1 Cấu trúc package

```
<service>-service/
  src/main/java/com/pcms/<service>service/
    controller/        # REST controller (thin, delegate to service)
    service/           # Business logic interface + impl
    repository/        # Spring Data JPA
    entity/            # JPA entities (extend BaseEntity nếu mới)
    dto/
      request/         # @Valid request DTO
      response/        # Response DTO
    enums/             # Enum cho status, role, type
    config/            # Security, Async, OpenAPI, etc.
    scheduler/         # CronJob (NSF-*)
    exception/         # Custom BusinessException nếu cần
  src/test/java/       # Unit test + integration test
  src/main/resources/
    application.yml
    db/migration/      # Flyway (nếu áp dụng)
```

### 2.2 Controller template

```java
@RestController
@RequestMapping("/<resource>")
@RequiredArgsConstructor
@Tag(name = "<UC id> - <Name>")  // springdoc
public class XxxController {
    private final XxxService xxxService;

    /** GET /api/v1/<resource> - Mô tả + UC ref */
    @GetMapping
    public ResponseEntity<PageResponse<XxxResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(
                xxxService.list(search, page, size), XxxResponse::from));
    }
}
```

### 2.3 Service method template

- Throw `BusinessException` với MSG code thay vì `RuntimeException`
- Đặt transaction boundary tại service method với `@Transactional`
- Sử dụng `EventPublisher` từ pcms-common cho domain events
- Log theo format `log.info("[{}] action by user={}", correlationId, userId)`

### 2.4 Error handling

- **KHÔNG** tự build ResponseEntity với status – để `GlobalExceptionHandler` xử lý
- Throw `ResourceNotFoundException("MSG31", "Not found")` thay vì return null
- Throw `DuplicateResourceException("MSG09", "Duplicate")` thay vì return error response
- Throw `BusinessException` với custom code + httpStatus cho các case đặc biệt

### 2.5 Validation

- Dùng `jakarta.validation.constraints.*` (`@NotBlank`, `@Email`, `@Size`, `@DecimalMin`)
- Validate ở DTO (`@Valid` trên `@RequestBody`)
- Message key phải i18n-ready (`{validation.notblank}`)

### 2.6 Security

- `POST/PUT/DELETE` yêu cầu JWT bearer token (trừ `/auth/**`, `/webhooks/**`)
- Authorization bằng `@PreAuthorize` hoặc check trong service dùng `JwtClaims` từ pcms-common
- Truyền `actorId` qua header `X-User-Id` (gateway forward từ JWT `sub`)

### 2.7 Audit

- Tất cả entity extends `BaseEntity` (đã có sẵn ở pcms-common) – tự động có `id`, `createdAt`, `updatedAt`
- Soft-delete bằng `status = INACTIVE` (CR-08)
- Ghi `AuditLog` cho mọi write action trên User/Order/Payment/Inventory

### 2.8 Testing

- Mỗi API mới phải có **≥ 1 unit test** (Mockito) + **1 integration test** (`@SpringBootTest` + Testcontainers hoặc H2)
- Test case bao gồm: happy path, validation failure (400), not found (404), conflict (409), forbidden (403)

---

## 3. Sprint Breakdown

| Sprint | Thời gian | API thêm mới | Service thay đổi | Output chính |
|--------|-----------|-------------:|------------------|--------------|
| **Sprint 0** (Setup) | 2 ngày | 0 | — | Postman collection mở rộng, OpenAPI 3.0 tổng hợp, CI script |
| **Sprint 1** | 1 tuần | 6 | user-service, customer-service | Auth/User hoàn chỉnh |
| **Sprint 2** | 1 tuần | 6 | catalog-service, inventory-service, order-service, payment-service | Order/Payment/Inventory hoàn chỉnh |
| **Sprint 3** | 1 tuần | 6 | prescription-service, notification-service, report-service | Prescription/Notification/Report hoàn chỉnh |
| **Sprint 4** | 1 tuần | 8 | **customer-portal-service (NEW)** | Catalog browse, PDP, Lookup, Store Locator |
| **Sprint 5** | 1.5 tuần | 12 | customer-portal-service | Cart, Checkout, Order Tracking, Voucher |
| **Sprint 6** | 1 tuần | 10 | customer-portal-service | Health Article, Vaccine, Family Account, Reviews |
| **Sprint 7** | 1 tuần | 8 | customer-portal-service | Wallet, Favorites, Notification Settings, Flash Sale |
| **Sprint 8** | 2 tuần | 18 | **ai-engine-service (NEW, Python)** | Chatbot, OCR, Drug Check, Semantic Search |
| **Sprint 9** | 1.5 tuần | 14 | **pharmacist-workbench-service (NEW)** | RX-CONSOLE, 360° profile, Follow-up |
| **Sprint 10** | 2 tuần | 15 | **mobile-bff (NEW)**, **health-tools-service (NEW)**, **ecom-ops-service (NEW)** | Mobile API, Health Quiz, Ecom Ops |
| **TỔNG** | **~13 tuần** | **~103** | 4 service mới + 9 service cải tiến | **246 API (đạt 100% SDD)** |

> **Lưu ý quan trọng**: Nếu áp dụng song song (2 squad), có thể rút từ Sprint 4–10 thành 7–8 tuần.

---

## Sprint 1 – Hoàn thiện B2B Auth/User

**Thời gian:** 1 tuần | **API mới:** 6 | **Service:** `user-service`, `customer-service`

### TICKET-101: `GET /auth/me` — Lấy profile user hiện tại

| Mục | Chi tiết |
|-----|---------|
| **Service** | user-service |
| **Màn hình SDD** | SCR-HOME |
| **Use Case** | UC01 + UC02 (sau login) |
| **FR** | FR1.4, FR2.1 |
| **Auth** | Bắt buộc (Bearer JWT) |
| **Files tạo/sửa** | `controller/AuthController.java` (thêm method)<br>`service/UserService.java` + impl (thêm method `me(UUID userId)`)<br>`dto/response/CurrentUserResponse.java` (mới) |
| **Response DTO** | `{ id, email, fullName, role, branchId, status, lastLoginAt, permissions[] }` |
| **Business logic** | Lấy user từ `JwtClaims.sub` → trả về profile. Trả 401 nếu user không tồn tại. |
| **Test cases** | ✓ Token hợp lệ → 200<br>✓ Token không hợp lệ → 401 MSG01<br>✓ User bị soft-delete → 401 |
| **Acceptance** | `curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/auth/me` → 200 với JSON profile |

### TICKET-102: `PUT /auth/password` — Đổi mật khẩu

| Mục | Chi tiết |
|-----|---------|
| **Service** | user-service |
| **Màn hình** | SCR-USER-FORM (modal "Đổi mật khẩu") |
| **FR** | FR1.3, FR1.5 |
| **Files** | `controller/AuthController.java`<br>`service/UserService.java`<br>`dto/request/ChangePasswordRequest.java` (mới)<br>`dto/response/MessageResponse.java` (mới) |
| **Request** | `{ currentPassword, newPassword, confirmPassword }` |
| **Validation** | `@NotBlank` cho cả 3, `@Size(min=8, max=64)` + custom validator (uppercase + number + special) cho `newPassword` |
| **Business logic** | Verify `currentPassword` bằng BCrypt → update `passwordHash` → invalidate tất cả refresh tokens → ghi `AuditLog(action=PASSWORD_CHANGE)` |
| **Test cases** | ✓ Đổi thành công → 200 + `MSG32` "Đổi mật khẩu thành công"<br>✓ Sai mật khẩu cũ → 400 MSG33<br>✓ Mật khẩu mới yếu → 400 MSG33<br>✓ `newPassword != confirmPassword` → 400 MSG33 |
| **Acceptance** | Response trong vòng 200ms, audit log được ghi |

### TICKET-103: `POST /auth/verify-email` — Xác thực email

| Mục | Chi tiết |
|-----|---------|
| **Service** | user-service |
| **Màn hình** | Email link từ SCR-LOGIN (AT3) |
| **FR** | FR1.6 |
| **Files** | `controller/AuthController.java`<br>`entity/EmailVerificationToken.java` (mới)<br>`repository/EmailVerificationTokenRepository.java` (mới)<br>`service/EmailVerificationService.java` (mới)<br>`dto/request/VerifyEmailRequest.java` |
| **Request** | `{ token: string }` |
| **Business logic** | Lookup token → check expired (>24h) → mark user.emailVerified=true → delete token → return success |
| **Schema mới** | `email_verification_tokens(id, user_id, token_hash, expires_at, used_at)` |
| **Test cases** | ✓ Token hợp lệ → 200<br>✓ Token hết hạn → 400 MSG03<br>✓ Token không tồn tại → 400 |
| **Acceptance** | User sau khi verify có thể login bình thường |

### TICKET-104: `POST /auth/resend-verification` — Gửi lại email xác thực

| Mục | Chi tiết |
|-----|---------|
| **Service** | user-service |
| **FR** | FR1.6 |
| **Rate limit** | 1 request/60s/user |
| **Files** | `controller/AuthController.java`<br>`service/EmailVerificationService.java` (extend) |
| **Request** | `{ email }` |
| **Business logic** | Lookup user → check chưa verify → tạo token mới → publish event `user.email.resend_verification` → notification-service sẽ gửi email |
| **Test cases** | ✓ Gửi thành công → 200 (kể cả email không tồn tại – tránh enumeration)<br>✓ Spam → 429 rate-limit |
| **Acceptance** | Email được gửi qua notification-service trong vòng 5s |

### TICKET-105: `GET /customers/code/{code}` — Tìm khách theo mã CUST-yyyy ####

| Mục | Chi tiết |
|-----|---------|
| **Service** | customer-service |
| **Màn hình** | SCR-CUST-LIST (search bar) |
| **FR** | FR8.2 |
| **Files** | `controller/CustomerController.java`<br>`service/CustomerService.java` + impl<br>`repository/CustomerRepository.java` (thêm `findByCode`) |
| **Path** | `GET /customers/code/{code}` |
| **Response** | `CustomerResponse` (đã có) |
| **Business logic** | Repository `findByCode(code)` → throw `ResourceNotFoundException("MSG31")` nếu không có |
| **Test cases** | ✓ Tìm thấy → 200<br>✓ Không tìm thấy → 404 MSG31 |
| **Acceptance** | Query time < 50ms (đã có index trên `code`) |

### TICKET-106: `PUT /users/{id}/branch` — Gán user vào chi nhánh

| Mục | Chi tiết |
|-----|---------|
| **Service** | user-service |
| **Màn hình** | SCR-USER-FORM |
| **FR** | FR2.3 |
| **Files** | `controller/UserController.java`<br>`service/UserService.java`<br>`dto/request/AssignBranchRequest.java` |
| **Request** | `{ branchId: UUID }` |
| **Business logic** | Validate branch tồn tại (gọi `branch-service` qua Feign client) → update user.branchId → audit log |
| **Test cases** | ✓ Gán thành công → 200<br>✓ Branch không tồn tại → 404<br>✓ User không tồn tại → 404 |
| **Acceptance** | Sau khi gán, login user đó nhận JWT có claim `branchId` đúng |

### Sprint 1 — Definition of Done

- [ ] Tất cả 6 API pass unit test + integration test (coverage ≥ 80%)
- [ ] OpenAPI spec regenerate, document cập nhật
- [ ] Postman collection thêm 6 request mới
- [ ] UAT script `docs/uat/01-AUTH-USER.md` cập nhật
- [ ] API Gateway route được thêm cho `/customers/code/**` (chưa có trong routes hiện tại)
- [ ] Code review ≥ 2 approvers
- [ ] Build `mvn clean verify` pass

---

## Sprint 2 – Hoàn thiện B2B Catalog/Inventory/Order/Payment

**Thời gian:** 1 tuần | **API mới:** 6

### TICKET-201: `GET /inventory/batches` và `GET /inventory/batches/{id}`

| Mục | Chi tiết |
|-----|---------|
| **Service** | inventory-service |
| **Màn hình** | SCR-INV-LIST |
| **FR** | FR5.1, FR5.7 |
| **Files** | `controller/InventoryController.java` (thêm 2 method)<br>`service/InventoryService.java` |
| **Path 1** | `GET /inventory/batches?branchId=&medicineId=&page=0&size=20` |
| **Path 2** | `GET /inventory/batches/{id}` |
| **Quyết định** | **Alias cho `/inventory` và `/inventory/{id}`** – code hiện đã flatten path. **Không bắt buộc thêm**, nhưng để đồng bộ SDD, nên tạo method mới trỏ về cùng service impl. |
| **Acceptance** | `GET /api/v1/inventory/batches?branchId=X` trả về danh sách batch giống `/inventory?branchId=X` |

### TICKET-202: `POST /orders/{id}/cancel` — Huỷ đơn (alias)

| Mục | Chi tiết |
|-----|---------|
| **Service** | order-service |
| **Màn hình** | SCR-ORDER-LIST |
| **FR** | FR6.3, BR06 |
| **Files** | `controller/OrderController.java` (thêm method `@PostMapping("/{id}/cancel")`)<br>`service/OrderService.java` |
| **Logic** | Tương tự `DELETE /orders/{id}` hiện có (cancel + restore stock BR06) |
| **Acceptance** | Cả `DELETE /orders/{id}` và `POST /orders/{id}/cancel` đều hoạt động như nhau |

### TICKET-203: `POST /orders/{id}/recompute` — Tính lại discount + kiểm tra stock

| Mục | Chi tiết |
|-----|---------|
| **Service** | order-service |
| **Màn hình** | SCR-ORDER-NEW (khi user thay đổi số lượng) |
| **FR** | FR6.4, BR04 |
| **Files** | `controller/OrderController.java`<br>`service/OrderService.java` (thêm method `recompute(UUID orderId)`)<br>`dto/response/OrderRecomputeResponse.java` (mới) |
| **Logic** | Lấy order PENDING_PAYMENT → apply BR04 (5% discount khi qty≥10 cùng medicine) → check stock từ inventory-service (Feign) → return new totals |
| **Response** | `{ subtotal, discount, total, stockWarnings[{medicineId, requestedQty, availableQty}] }` |
| **Test cases** | ✓ Qty < 10 → discount=0<br>✓ Qty = 10 → discount=5%<br>✓ Stock không đủ → warning kèm available qty |
| **Acceptance** | Response time < 100ms, logic BR04 đúng với happy path |

### TICKET-204: `GET /payments/{id}/invoice` — Lấy hoá đơn để in

| Mục | Chi tiết |
|-----|---------|
| **Service** | payment-service |
| **Màn hình** | SCR-INVOICE |
| **FR** | FR7.4, FR7.5 |
| **Files** | `controller/PaymentController.java`<br>`service/PaymentService.java`<br>`dto/response/InvoiceResponse.java` (mới) |
| **Response** | `{ invoiceNumber, orderNumber, customerName, branchName, items[], subtotal, discount, total, paymentMethod, paidAt, cashierName }` |
| **Logic** | Aggregate Payment + Order + OrderItems + Customer + Branch qua Feign client → return InvoiceResponse |
| **Test cases** | ✓ Tìm thấy → 200<br>✓ Payment không tồn tại → 404<br>✓ Order/Customer không truy cập được → 403 |
| **Acceptance** | API trả về JSON đầy đủ, frontend có thể render A4 template |

### TICKET-205: `POST /payments/{id}/print` — Trigger máy in

| Mục | Chi tiết |
|-----|---------|
| **Service** | payment-service |
| **Màn hình** | SCR-INVOICE |
| **FR** | FR7.5 |
| **Files** | `controller/PaymentController.java`<br>`service/PrintService.java` (mới)<br>`integration/PrinterGatewayClient.java` (Feign, optional) |
| **Logic** | Lấy InvoiceResponse → render template HTML/PDF → gửi tới printer qua IPP/LAN (hoặc return PDF blob để frontend download) |
| **Phương án** | **Option A**: Return PDF blob (`produces=APPLICATION_PDF_VALUE`) – đơn giản, không cần infra<br>**Option B**: Tích hợp CUPS/IPP – cần thêm infra, để Sprint sau |
| **Acceptance** | PDF tải về được, định dạng A4, font tiếng Việt OK |

### TICKET-206: `POST /payments/webhook` — Callback từ payment gateway

| Mục | Chi tiết |
|-----|---------|
| **Service** | payment-service |
| **Màn hình** | (Backend integration) |
| **FR** | FR7.2, FR7.3 |
| **Auth** | HMAC signature header (không JWT) |
| **Files** | `controller/WebhookController.java` (thêm method)<br>**Lưu ý**: code hiện đã có `POST /webhooks/payment-gateway` – cần alias `/payments/webhook` cho compliance với SDD<br>`service/WebhookService.java` |
| **Logic** | Verify HMAC signature → parse payload → idempotency check theo `event_id` → update payment.status → trigger order paid flow |
| **Schema mới** | `webhook_events(id, gateway, event_id UNIQUE, payload, status, processed_at)` |
| **Test cases** | ✓ Signature hợp lệ → 200<br>✓ Signature sai → 401<br>✓ Duplicate event → 200 idempotent (no-op) |
| **Acceptance** | Webhook từ MoMo/VNPay sandbox pass |

### Sprint 2 — DoD

- [ ] 6 API pass test
- [ ] Code đã có (`DELETE /orders/{id}` và `/webhooks/payment-gateway`) được giữ nguyên + alias thêm
- [ ] BR04 + BR06 unit test coverage
- [ ] PDF invoice template được test với font tiếng Việt

---

## Sprint 3 – Hoàn thiện B2B Prescription/Notification/Report

**Thời gian:** 1 tuần | **API mới:** 6

### TICKET-301: `POST /prescriptions/{id}/sign` — Ký số (alias PUT)

| Mục | Chi tiết |
|-----|---------|
| **Service** | prescription-service |
| **Màn hình** | SCR-RX |
| **FR** | FR12.1, FR12.2 |
| **Files** | `controller/PrescriptionController.java` (alias cho `PUT /sign`) |
| **Logic** | Tương tự PUT hiện có – validate pharmacist có license → generate `signature_hash` (SHA256 of prescription content + pharmacist's key) → status DRAFT → SIGNED |
| **Acceptance** | Cả `PUT /sign` và `POST /sign` đều hoạt động |

### TICKET-302: `POST /prescriptions/{id}/print` — In đơn thuốc (alias GET)

| Mục | Chi tiết |
|-----|---------|
| **Service** | prescription-service |
| **Màn hình** | SCR-RX |
| **FR** | FR12.4 |
| **Files** | `controller/PrescriptionController.java` (alias cho `GET /print`) |
| **Logic** | Trả về PDF A5 (kích thước đơn thuốc) thay vì JSON |
| **Acceptance** | PDF in được, có chữ ký số (QR code chứa signature_hash) |

### TICKET-303: `DELETE /prescriptions/{id}` — Huỷ đơn thuốc

| Mục | Chi tiết |
|-----|---------|
| **Service** | prescription-service |
| **Màn hình** | SCR-RX |
| **FR** | FR12.3 |
| **Files** | `controller/PrescriptionController.java`<br>`service/PrescriptionService.java` |
| **Logic** | Chỉ cancel được nếu `status = DRAFT` hoặc chưa link order đã PAID → soft-delete |
| **Test cases** | ✓ Huỷ DRAFT → 200<br>✓ Huỷ SIGNED chưa link → 200<br>✓ Huỷ SIGNED đã link PAID order → 409 MSG19 |
| **Acceptance** | Không cho phép huỷ toa đã bán |

### TICKET-304: `DELETE /notifications/{id}` — Xoá notification

| Mục | Chi tiết |
|-----|---------|
| **Service** | notification-service |
| **Màn hình** | SCR-NOTIF-LIST |
| **Files** | `controller/NotificationController.java`<br>`service/NotificationService.java` |
| **Logic** | Soft-delete (status=DELETED) – chỉ recipient mới xoá được |
| **Auth** | Check `notification.recipientId == currentUserId` |
| **Acceptance** | Notification biến mất khỏi list sau khi xoá |

### TICKET-305: `POST /reports/export/excel` — Xuất Excel

| Mục | Chi tiết |
|-----|---------|
| **Service** | report-service |
| **Màn hình** | SCR-REPORT-EXPORT |
| **FR** | FR9.3 |
| **Files** | `controller/ReportController.java`<br>`service/ExcelExportService.java` (mới, dùng Apache POI) |
| **Request** | `{ reportType: "revenue|inventory|staff", filters: {...} }` |
| **Logic** | Build report theo filter → render Excel bằng Apache POI → return blob (`APPLICATION_OCTET_STREAM`) |
| **Acceptance** | File .xlsx mở được, có header + data đúng |

### TICKET-306: `POST /reports/export/pdf` + `DELETE /reports/schedules/{id}`

| Mục | Chi tiết |
|-----|---------|
| **Service** | report-service |
| **FR** | FR9.4, FR9.5 |
| **Files** | `controller/ReportController.java`<br>`service/PdfExportService.java` (mới, dùng iText/OpenPDF)<br>`service/ReportScheduleService.java` (thêm `cancel(id)`) |
| **POST /reports/export/pdf** | Tương tự TICKET-305 nhưng output PDF |
| **DELETE /reports/schedules/{id}** | Soft-delete schedule, status=CANCELLED |
| **Acceptance** | PDF có chart + table, schedule biến mất khỏi list sau khi cancel |

### Sprint 3 — DoD

- [ ] 6 API pass test
- [ ] Apache POI + OpenPDF dependencies thêm vào `report-service/pom.xml`
- [ ] UAT docs cập nhật cho SCR-RX, SCR-NOTIF-LIST, SCR-REPORT-EXPORT

---

## Sprint 4 – `customer-portal-service` (B2C foundation)

**Thời gian:** 1 tuần | **API mới:** 8 | **Service mới**

### TICKET-401: Scaffold service mới

| Mục | Chi tiết |
|-----|---------|
| **Service** | `customer-portal-service` |
| **Port** | 8093 |
| **Gateway path** | `/api/v1/shop/**`, `/api/v1/store/**`, `/api/v1/cust/**` |
| **Database** | Tách schema riêng `pcms_customer_portal` (theo pattern mỗi service 1 DB) |
| **Files** | `pom.xml` (copy từ `catalog-service`, đổi artifactId)<br>`src/main/java/com/pcms/customerportal/CustomerPortalApplication.java`<br>`src/main/resources/application.yml`<br>`src/main/resources/bootstrap.yml`<br>`Dockerfile` (copy từ service khác) |
| **Dependencies** | `pcms-common`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`, `mysql-connector-j`, `springdoc-openapi-starter-webmvc-ui`, `lombok` |
| **Acceptance** | Service start được, register Eureka, có `/actuator/health` = UP |

### TICKET-402: `GET /shop/home` — Trang chủ B2C

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-HOME |
| **Response** | `{ heroBanners[], bestSellers[12], featuredCategories[6], brands[8], healthQuizTeaser, videosTeaser }` |
| **Logic** | Aggregate: bestSellers (top medicines 30 ngày qua từ `order-service` Feign), categories (`category-service` Feign), banners từ bảng `home_banners` (mới) |
| **Schema mới** | `home_banners(id, title, image_url, link_url, sort_order, status, start_at, end_at)` |
| **Auth** | Optional (Guest OK, Customer có thêm personalized section) |
| **Acceptance** | Response time < 500ms |

### TICKET-403: `GET /shop/catalog/{slug}` và `GET /shop/pdp/{id}` — Danh mục + PDP

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-CAT-1, SHOP-CAT-2, SHOP-PDP |
| **Logic** | PDP: aggregate `catalog-service` (medicine detail) + `inventory-service` (stock per branch near user) + reviews từ `product_reviews` (bảng mới) + related products |
| **Auth** | Optional (Guest OK) |
| **Schema mới** | `product_reviews(id, medicine_id, customer_id, rating 1-5, body, images[], status, created_at)` |
| **Acceptance** | PDP load đầy đủ: ảnh, mô tả, công dụng, thành phần, reviews |

### TICKET-404: `GET /shop/search` — Tìm kiếm B2C với AI semantic

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-SEARCH |
| **Logic** | Full-text search `catalog-service` + AI semantic từ `ai-engine-service` (Sprint 8) – trước Sprint 8 thì chỉ trả text search |
| **Auth** | Optional |
| **Acceptance** | Search "đau đầu" trả về thuốc liên quan (sau Sprint 8) |

### TICKET-405: `GET /store/locator` — Tìm nhà thuốc gần nhất

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | STORE-LOCATOR, STORE-LIST-PROVINCE, STORE-DETAIL |
| **Logic** | Query `branch-service` + filter theo lat/lng (PostGIS – đợi infra) hoặc theo tỉnh/huyện (đơn giản hơn cho MVP) |
| **Auth** | Optional |
| **Acceptance** | Response trả về danh sách branch + map link |

### TICKET-406: `GET /shop/lookup/{type}` — Tra cứu thuốc/dược chất/dược liệu

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-LOOKUP-DRUG, SHOP-LOOKUP-INGREDIENT, SHOP-LOOKUP-HERB |
| **Logic** | 3 loại: drug (medicine name), ingredient (active substance), herb (vị thuốc cổ truyền) – cần schema mới |
| **Schema mới** | `ingredients(id, name_vi, name_en, synonyms[])`<br>`herbs(id, name_vi, name_en, traditional_use, image_url)` |
| **Acceptance** | Filter A-Z, tìm kiếm full-text |

### Sprint 4 — DoD

- [ ] Service mới start + register Eureka
- [ ] Gateway thêm 3 routes (`/shop/**`, `/store/**`, `/lookup/**`)
- [ ] 6 bảng mới (home_banners, product_reviews, ingredients, herbs, voucher_*, customer_addresses)
- [ ] Flyway migration scripts
- [ ] OpenAPI spec

---

## Sprint 5 – B2C E-commerce (Cart/Checkout/Order Tracking)

**Thời gian:** 1.5 tuần | **API mới:** 12

### TICKET-501: Cart APIs (CRUD)

| Mục | Chi tiết |
|-----|---------|
| **Service** | customer-portal-service |
| **Màn hình** | SHOP-CART |
| **Schema** | `carts(id, customer_id UNIQUE, items JSON, voucher_code, total, updated_at)` |
| **API** | `GET /cart`, `POST /cart/items` (add), `PUT /cart/items/{itemId}` (update qty), `DELETE /cart/items/{itemId}`, `DELETE /cart` (clear) |
| **Auth** | Required (Customer) |
| **Logic** | Persistent cart trong DB (theo FR14.5) – nếu guest, lưu Redis/localStorage |
| **Acceptance** | Add/update/remove cập nhật `total` đúng với BR04 discount |

### TICKET-502: `POST /cart/checkout` — Checkout 4 bước

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-CHECKOUT |
| **Logic** | Step 1: chọn address → Step 2: shipping method → Step 3: payment method → Step 4: confirm |
| **API** | `POST /cart/checkout/preview` (tính ship + tax)<br>`POST /cart/checkout/confirm` (tạo order) |
| **Integration** | Gọi `order-service` để tạo order, `payment-service` để xử lý thanh toán online |
| **Acceptance** | Order được tạo trong `order-service`, payment session được khởi tạo |

### TICKET-503: `GET /orders/{id}/track` — Theo dõi đơn B2C

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-ORDER-TRACK |
| **Auth** | Required (Owner only) |
| **Response** | `{ orderNumber, status, timeline[{status, timestamp, note}], currentLocation, estimatedDelivery }` |
| **Logic** | Aggregate từ `order-service` + tracking events từ bảng `shipment_tracking` (mới) |
| **Schema mới** | `shipment_tracking(id, order_id, status, location, note, occurred_at)` |
| **Acceptance** | Timeline chính xác với 4 trạng thái: CONFIRMED → SHIPPING → DELIVERED |

### TICKET-504: `GET /orders/history` — Lịch sử đơn B2C

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-ORDER-HISTORY |
| **Auth** | Required (Customer) |
| **Logic** | Query `order-service` với `customerId = currentUser` |
| **Acceptance** | Customer chỉ thấy đơn của mình (filter by JWT.sub) |

### TICKET-505: Voucher/Coupon (User side)

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-VOUCHER |
| **API** | `GET /vouchers` (khả dụng), `POST /vouchers/apply` (validate), `GET /vouchers/history` |
| **Logic** | Tận dụng `coupons` table hiện có trong `order-service` (PERCENT / FIXED / FREE_SHIP theo FR19.1) |
| **Schema mới** | `voucher_usages(id, voucher_id, customer_id, order_id, used_at)` – để enforce per-user limit |
| **Acceptance** | Apply voucher giảm giá đúng, không cho dùng 2 lần |

### TICKET-506: Installment API

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-INSTALLMENT |
| **Logic** | Tích hợp Home Credit / FE Credit API (đối tác) – sandbox trước |
| **API** | `POST /installment/quote`, `POST /installment/confirm` |
| **Acceptance** | Trả về plan trả góp (số tháng, lãi suất, monthly payment) |

### Sprint 5 — DoD

- [ ] Cart persistent (DB)
- [ ] Checkout flow tạo order + payment session
- [ ] Voucher system validate + apply
- [ ] Order tracking realtime (qua SSE hoặc WebSocket optional)

---

## Sprint 6 – B2C Lookup + Store Locator + Vaccine

**Thời gian:** 1 tuần | **API mới:** 10

### TICKET-601: Vaccine booking APIs

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | VACCINE-HOME, VACCINE-BOOKING, VACCINE-LEDGER |
| **Schema** | `vaccines(id, name, manufacturer, doses_required, days_between_doses, price)`<br>`vaccine_slots(id, vaccine_id, branch_id, slot_date, slot_time, available_qty)`<br>`vaccine_bookings(id, customer_id, vaccine_id, slot_id, family_member_id, status)`<br>`vaccination_ledger(id, customer_id, family_member_id, vaccine_id, dose_number, administered_at, batch_no, branch_id)` |
| **API** | `GET /vaccines`, `GET /vaccines/{id}/slots?branchId=&date=`, `POST /vaccine-bookings`, `GET /vaccine-bookings/me`, `DELETE /vaccine-bookings/{id}`, `GET /vaccination-ledger/me?memberId=` |
| **Acceptance** | Customer book được slot, nhận confirmation |

### TICKET-602: Health Articles + Disease Info

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-HEALTH-ARTICLE, SHOP-DISEASE-INFO, SHOP-CANCER-INFO |
| **Schema** | `health_articles(id, title, slug UNIQUE, body_markdown, category, author, published_at, status)`<br>`disease_info(id, name, slug, target_audience[], season[], body, severity)` |
| **API** | `GET /health-articles?category=&page=`, `GET /health-articles/{slug}`, `GET /diseases?audience=&season=` |
| **Auth** | Optional |
| **Acceptance** | CMS cơ bản cho admin (tạo/sửa/xoá) + public read |

### TICKET-603: Verify Origin (QR scan)

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-VERIFY-ORIGIN |
| **API** | `POST /verify-origin/scan` (nhận QR code hoặc barcode) |
| **Logic** | Decode → lookup `medicine.batch_no` → return batch info + manufacturer + verify status |
| **Schema mới** | `batch_verification(id, batch_no UNIQUE, medicine_id, manufacturer, manufactured_at, verified_at)` |
| **Acceptance** | Scan QR trả về chi tiết nguồn gốc |

### TICKET-604: Video ngắn

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-VIDEO |
| **API** | `GET /videos?category=&page=` |
| **Schema** | `videos(id, title, youtube_id, source, duration_sec, category)` |
| **Acceptance** | Embed YouTube |

### Sprint 6 — DoD

- [ ] Vaccine booking end-to-end
- [ ] Health article CMS (admin) + read (public)
- [ ] QR verify (mock manufacturer OK)

---

## Sprint 7 – B2C Customer Account

**Thời gian:** 1 tuần | **API mới:** 8

### TICKET-701: Customer Address Book

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | CUST-ADDRESS |
| **API** | `GET /addresses`, `POST /addresses`, `PUT /addresses/{id}`, `DELETE /addresses/{id}`, `PUT /addresses/{id}/default` |
| **Schema** | `customer_addresses(id, customer_id, label, receiver_name, phone, province, district, ward, street, is_default, lat, lng)` |
| **Acceptance** | CRUD + đánh dấu default (transactional update) |

### TICKET-702: Family Account

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | CUST-FAMILY |
| **API** | `GET /family`, `POST /family`, `PUT /family/{id}`, `DELETE /family/{id}` |
| **Schema** | `customer_family(id, owner_id, member_name, relationship, dob, gender, allergies[], chronic_conditions[])` |

### TICKET-703: Health Wallet

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | CUST-HEALTH-WALLET |
| **API** | `GET /wallet`, `GET /wallet/transactions`, `POST /wallet/redeem` |
| **Logic** | Aggregate points từ `customer-service` + bonus theo tier |
| **Schema** | `wallet_tiers(id, name, min_spend, discount_pct, perks[])` |

### TICKET-704: Favorites + Notification Settings

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | CUST-FAVORITES, CUST-NOTIF-SETTINGS |
| **API** | `GET /favorites`, `POST /favorites`, `DELETE /favorites/{medicineId}`<br>`GET /notif-settings`, `PUT /notif-settings` |
| **Schema** | `customer_favorites(customer_id, medicine_id)` UNIQUE<br>`customer_notif_settings(customer_id UNIQUE, push_enabled, email_enabled, sms_enabled, marketing_enabled, order_updates, low_stock, expiry_alert)` |

### TICKET-705: Prescription History (B2C view)

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | CUST-RX-HISTORY |
| **API** | `GET /prescriptions/me`, `GET /prescriptions/{id}/re-download` |

### Sprint 7 — DoD

- [ ] Address book + family + wallet + favorites + notification preferences
- [ ] All APIs check owner authorization

---

## Sprint 8 – `ai-engine-service` (NEW, Python)

**Thời gian:** 2 tuần | **API mới:** 18 | **Stack:** Python 3.11 + FastAPI + LangChain + pgvector

### TICKET-801: Scaffold Python service

| Mục | Chi tiết |
|-----|---------|
| **Service** | `ai-engine-service` |
| **Port** | 8094 |
| **Stack** | FastAPI + SQLAlchemy + LangChain + pgvector + Celery + Redis |
| **Database** | PostgreSQL 14+ (cần thêm vào docker-compose vì pgvector) |
| **Files** | `pyproject.toml` hoặc `requirements.txt`<br>`app/main.py`<br>`app/core/config.py`<br>`Dockerfile`<br>`docker-compose.yml` (extend) |
| **Acceptance** | Service start + `/healthz` UP + register Eureka (qua sidecar `eureka-client-python`) |

### TICKET-802: AI Chatbot APIs

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | CHAT-AI |
| **API** | `POST /ai/chat` (send message), `GET /ai/chat/sessions/{id}` (history), `POST /ai/chat/sessions/{id}/escalate` |
| **Logic** | RAG: retrieve top-5 medicines/symptoms từ pgvector → prompt GPT-4o-mini → return response + citations + confidence |
| **Schema** | `ai_conversations(id, customer_id, messages JSON, status, escalated_at, pharmacist_id)` |
| **Acceptance** | Chat được với câu hỏi mẫu, response < 3s |

### TICKET-803: OCR Prescription

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-RX-UPLOAD → AI-RX-OCR |
| **API** | `POST /ai/ocr/prescription` (upload image) |
| **Logic** | Lưu ảnh vào S3/MinIO → gọi Google Vision API hoặc model custom → extract tên thuốc + liều → queue cho pharmacist review |
| **Schema** | `ocr_jobs(id, image_url, status, extracted_data JSON, pharmacist_id, reviewed_at)` |
| **Acceptance** | OCR accuracy ≥ 95% trên ảnh mẫu (test set 50 ảnh) |

### TICKET-804: Drug Interaction Check

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | AI-DRUG-CHECK |
| **API** | `POST /ai/drug-check` (danh sách medicine IDs) |
| **Logic** | Query `drug_interaction_rules` table (pre-loaded từ DrugBank/twELVET) → return list interactions với severity (MINOR/MODERATE/MAJOR/CONTRAINDICATED) |
| **Schema** | `drug_interaction_rules(id, drug_a_id, drug_b_id, severity, description, source)` |
| **Acceptance** | Check 2 thuốc có tương tác → return đúng severity |

### TICKET-805: Semantic Search

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | AI-SEMANTIC-SEARCH |
| **API** | `GET /ai/semantic-search?q=đau+đầu` |
| **Logic** | Embed query bằng OpenAI text-embedding-3-small → cosine similarity với vectors trong pgvector → return top 10 |
| **Schema** | `ai_embeddings(id, entity_type, entity_id, vector vector(1536), text)` |
| **Acceptance** | Query "đau đầu" → trả về thuốc giảm đau |

### TICKET-806: Demand Forecast + Anomaly Detection

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | (Reports, SCR-RX) |
| **API** | `GET /ai/forecast/{medicineId}?days=30`, `POST /ai/prescription/anomaly-check` |
| **Logic** | Prophet/ARIMA model cho forecast; rule-based cho anomaly |
| **Acceptance** | Forecast 30/60/90 ngày, anomaly flag đúng case duplicate/max-dose |

### Sprint 8 — DoD

- [ ] Python service chạy được
- [ ] Tất cả 12 AI features (UC15-AI-01..AI-12) có API
- [ ] RAG pipeline với OpenAI/GPT-4o-mini
- [ ] OCR với ≥95% accuracy
- [ ] Drug interaction lookup functional
- [ ] Semantic search với embedding

---

## Sprint 9 – `pharmacist-workbench-service`

**Thời gian:** 1.5 tuần | **API mới:** 14

### TICKET-901: Scaffold

| Mục | Chi tiết |
|-----|---------|
| **Service** | `pharmacist-workbench-service` |
| **Port** | 8095 |
| **Gateway path** | `/api/v1/rx/**` |
| **Stack** | Spring Boot (reuse convention) |
| **Acceptance** | Service start, register Eureka |

### TICKET-902: RX-CUST-PROFILE-360 + Consultation

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | RX-CUST-PROFILE-360, RX-CONSULT |
| **API** | `GET /customers/{id}/profile-360`, `POST /consultations` (start session), `POST /consultations/{id}/messages`, `GET /consultations/{id}` |
| **Logic** | Aggregate: customer + recent orders + prescriptions + allergies + chronic + AI summary (gọi `ai-engine-service`) |
| **Schema** | `consultations(id, customer_id, pharmacist_id, channel text/voice/video, status, started_at, ended_at, transcript)` |
| **Acceptance** | Pharmacist thấy 360° view trong 1 call |

### TICKET-903: Cross-Sell, Follow-up, VIP

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | RX-CROSS-SELL, RX-FOLLOW-UP, RX-VIP-MARK |
| **API** | `GET /customers/{id}/cross-sell`, `POST /customers/{id}/follow-up` (3/7/14 day), `PUT /customers/{id}/vip` |
| **Schema** | `follow_up_tasks(id, customer_id, order_id, type, scheduled_at, status, response)`<br>`vip_marks(customer_id UNIQUE, marked_by, reason, tier, marked_at)` |
| **Acceptance** | Suggest cross-sell (gọi AI), schedule follow-up push qua notification-service |

### TICKET-904: RX-CONSULT Realtime

| Mục | Chi tiết |
|-----|---------|
| **Logic** | WebSocket `/ws/consult/{id}` cho text chat realtime<br>Tích hợp WebRTC cho voice/video (đợi infra TURN/STUN) |
| **Acceptance** | Realtime chat 2 chiều, < 200ms latency |

### Sprint 9 — DoD

- [ ] 5 màn hình RX-* đầy đủ chức năng
- [ ] WebSocket cho consultation
- [ ] 3 cảnh báo real-time: allergy, drug interaction, max-dose (gọi ai-engine-service)

---

## Sprint 10 – Mobile BFF + Health Tools + Ecom-Ops

**Thời gian:** 2 tuần | **API mới:** 15

### TICKET-1001: `mobile-bff` Service

| Mục | Chi tiết |
|-----|---------|
| **Service** | `mobile-bff` |
| **Port** | 8096 |
| **Gateway path** | `/api/v1/mobile/**` |
| **Logic** | Backend-for-Frontend: aggregate nhiều service cho mobile app, response tối ưu cho React Native |
| **API** | `GET /mobile/home` (gộp notifications + orders + reminders), `GET /mobile/nearby-pharmacies?lat=&lng=`, `POST /mobile/medication-reminders`, `GET /mobile/medication-reminders`, `PUT /mobile/medication-reminders/{id}/taken` |
| **Schema** | `medication_reminders(id, customer_id, schedule JSON, start_date, end_date, family_member_id)`<br>`medication_intakes(id, reminder_id, scheduled_at, taken_at, status TAKEN/SKIPPED/LATE)` |
| **Acceptance** | Mobile app gọi 1 endpoint thay vì 5 |

### TICKET-1002: Health Tools Service

| Mục | Chi tiết |
|-----|---------|
| **Service** | `health-tools-service` |
| **Port** | 8097 |
| **Gateway path** | `/api/v1/health/**` |
| **API** | `GET /quizzes`, `GET /quizzes/{slug}`, `POST /quizzes/{slug}/submit`, `GET /quiz-results/me` |
| **Schema** | `health_quizzes(id, slug UNIQUE, name, questions JSON, scoring_logic)`<br>`health_quiz_results(id, customer_id, quiz_id, answers JSON, score, risk_level, advice, completed_at)` |
| **8 Quizzes** | QZ-01 Memory, QZ-02 Pre-diabetes, QZ-03 Thyroid, QZ-04 Asthma ACT, QZ-05 Cardiac Framingham, QZ-06 Alzheimer mini-cog, QZ-07 GERD-Q, QZ-08 Inhaler addiction |
| **Acceptance** | 8 quiz với 10-15 câu hỏi mỗi cái, scoring chính xác |

### TICKET-1003: E-commerce Ops Service

| Mục | Chi tiết |
|-----|---------|
| **Service** | `ecom-ops-service` |
| **Port** | 8098 |
| **Gateway path** | `/api/v1/ecom-ops/**` |
| **API** | CRUD vouchers (admin), CRUD flash sales, CRUD product reviews moderation, combo deals, live chat |
| **Schema** | `flash_sales(id, name, starts_at, ends_at, status)`<br>`flash_sale_items(id, flash_sale_id, medicine_id, sale_price, qty_limit, sold_qty)` |
| **Acceptance** | Admin CRUD vouchers + flash sales, atomic stock control (NSF-21) |

### TICKET-1004: Live Chat Realtime

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-LIVE-CHAT |
| **Stack** | WebSocket + Redis Pub/Sub |
| **API** | `WS /ws/live-chat`, `GET /live-chat/sessions/me`, `POST /live-chat/sessions` |
| **Acceptance** | Customer ↔ Pharmacist realtime chat |

### TICKET-1005: Reviews + Moderation

| Mục | Chi tiết |
|-----|---------|
| **Màn hình** | SHOP-REVIEW |
| **API** | `POST /reviews` (customer gửi), `PUT /reviews/{id}/moderate` (admin approve/reject), `GET /medicines/{id}/reviews` |
| **Logic** | Auto-AI moderation (toxicity check gọi `ai-engine-service`) trước khi publish |

### Sprint 10 — DoD

- [ ] Mobile BFF giảm số round-trip từ mobile xuống còn 1-2
- [ ] 8 health quiz hoạt động
- [ ] Voucher/Flash sale admin UI
- [ ] Live chat realtime

---

## 14. Verification & Acceptance

### 14.1 Definition of Done chung cho mọi ticket

- [ ] Code pass `mvn clean verify` (compile + test + checkstyle nếu có)
- [ ] Unit test coverage ≥ 80% cho service layer mới
- [ ] Integration test với `@SpringBootTest` + H2 hoặc Testcontainers
- [ ] OpenAPI annotation (`@Operation`, `@ApiResponse`) đầy đủ cho Swagger UI
- [ ] Error handling qua `BusinessException` (không trả `ResponseEntity.status(...)` từ controller)
- [ ] Authorization check (role-based) đúng
- [ ] Audit log được ghi cho write actions
- [ ] Idempotency-Key header được respect cho POST/PUT/DELETE
- [ ] Correlation ID được log
- [ ] Code review ≥ 2 approvers
- [ ] Postman collection cập nhật với happy + error path
- [ ] UAT script (`docs/uat/*.md`) cập nhật

### 14.2 Verification per Sprint

| Sprint | Verification |
|--------|--------------|
| 1–3 (B2B) | Re-run `docs/uat/01-10*.md` UAT scripts → 100% PASS |
| 4–7 (B2C Portal) | Postman + manual smoke test qua Gateway |
| 8 (AI Engine) | OpenAI API key sandbox, test 12 AI features với bộ test data |
| 9 (Pharmacist) | Manual test pharmacist flow end-to-end |
| 10 (Mobile + Health + Ecom) | Mobile app (React Native) + admin UI |

### 14.3 Final Acceptance

- [ ] Tất cả 246 API (146 hiện có + ~100 mới) hoạt động
- [ ] OpenAPI 3.0 spec generate thành công từ tất cả service
- [ ] Postman collection đầy đủ (test cả error paths)
- [ ] UAT scripts `docs/uat/*.md` cover 100% use cases
- [ ] Load test: 500 concurrent users (theo SRS §4.2) pass với P95 < 1s

---

## 15. Risk & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Migration DB cho B2C** (15+ bảng mới) | High | Dùng Flyway, rollback script sẵn, dev/test DB riêng |
| **AI Engine vendor lock-in** (OpenAI) | Medium | Abstraction layer LangChain → dễ swap sang local Llama |
| **OCR accuracy thấp** (target 95%) | High | Bắt đầu với Google Vision API (paid, chính xác) trước, custom model sau |
| **PostGIS cần thiết cho store locator** | Medium | MVP dùng lat/lng thường + Java tính khoảng cách (Haversine), PostGIS cho scale |
| **WebSocket cho live chat/realtime** | Medium | Bắt đầu với SSE (server-sent events) đơn giản, scale WebSocket khi cần |
| **Mobile push (FCM/APNs)** | Low | Dùng Firebase Admin SDK qua notification-service |
| **Multi-language (vi/en)** | Low | Resource bundle i18n từ đầu, không hard-code string |
| **GDPR/PDPD compliance** (PII data) | Medium | Encryption at rest cho PII field, audit log immutable |
| **Squad coordination** (4 services song song) | High | Daily standup, shared Slack channel, contract test giữa services |
| **Scope creep** | High | Strict Definition of Done, ticket phải có estimate + acceptance rõ ràng |

---

## 16. Effort tổng thể

### 16.1 Effort ước tính (person-day)

| Sprint | Effort (PD) | Lưu ý |
|--------|-------------|-------|
| Sprint 0 - Setup | 2 | Postman + OpenAPI + CI |
| Sprint 1 - B2B Auth/User | 5 | |
| Sprint 2 - B2B Catalog/Inv/Order/Pay | 5 | |
| Sprint 3 - B2B Prescription/Notif/Report | 5 | |
| Sprint 4 - customer-portal scaffold | 5 | |
| Sprint 5 - B2C E-commerce | 8 | Cart + Checkout + Voucher phức tạp |
| Sprint 6 - B2C Lookup/Store/Vaccine | 5 | |
| Sprint 7 - B2C Account | 5 | |
| Sprint 8 - ai-engine (Python) | 12 | OCR + RAG + semantic search |
| Sprint 9 - pharmacist-workbench | 8 | WebSocket + realtime |
| Sprint 10 - Mobile BFF + Health + Ecom | 10 | 3 service nhỏ + WebSocket |
| **TỔNG** | **70 PD** | |

### 16.2 Timeline (1 squad 4–5 người)

- **13–14 tuần** nếu chạy tuần tự
- **7–8 tuần** nếu 2 squad song song:
  - Squad A: Sprint 1–3 (B2B) + Sprint 4–7 (customer-portal)
  - Squad B: Sprint 8 (ai-engine) + Sprint 9 (pharmacist) + Sprint 10 (mobile + health + ecom)

### 16.3 Team allocation gợi ý

| Role | Số người | Phụ trách |
|------|---------:|-----------|
| Tech Lead | 1 | Review architecture, code review |
| Senior Backend (Java) | 2 | B2B + customer-portal + pharmacist |
| Senior Backend (Python) | 1 | ai-engine |
| Mid Backend (Java) | 1 | mobile-bff + health-tools + ecom-ops |
| QA / SDET | 1 | Test + Postman + UAT scripts |

### 16.4 Deliverable cuối cùng

- **246 API endpoints** (đạt 100% SDD §6 + B2C)
- **15 services** (12 hiện tại + 4 mới: customer-portal, ai-engine, pharmacist-workbench, mobile-bff + health-tools + ecom-ops)
- **OpenAPI 3.0 unified spec**
- **Postman collection** đầy đủ (test + error path)
- **UAT scripts** `docs/uat/*.md` cover 100% use cases UC01–UC19
- **Documentation**: ARCHITECTURE.md, RUNBOOK.md, API_CHANGELOG.md

---

## Phụ lục A: Danh sách đầy đủ 246 API mong đợi

Xem file `docs/api-list-final.json` (sẽ được sinh tự động từ script khi tất cả sprint hoàn thành).

## Phụ lục B: Migration order

```sql
-- Sprint 0: setup
-- Sprint 1: user-service
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
CREATE TABLE email_verification_tokens (...);
CREATE TABLE password_reset_tokens (...); -- (đã có)

-- Sprint 2: payment-service + report-service
CREATE TABLE webhook_events (...); -- (đã có)
CREATE TABLE report_schedules (...); -- (đã có)

-- Sprint 4: customer-portal-service (DB riêng)
CREATE DATABASE pcms_customer_portal;
CREATE TABLE home_banners, product_reviews, ingredients, herbs, ...;

-- Sprint 5: cart, voucher_usage
CREATE TABLE carts, cart_items, voucher_usages, ...;

-- Sprint 6: vaccine, health_articles
CREATE TABLE vaccines, vaccine_slots, vaccine_bookings, vaccination_ledger, ...;

-- Sprint 7: customer_account extensions
CREATE TABLE customer_addresses, customer_family, customer_favorites, customer_notif_settings, ...;

-- Sprint 8: ai-engine (PostgreSQL + pgvector)
CREATE EXTENSION vector;
CREATE TABLE ai_conversations, ai_embeddings, ocr_jobs, drug_interaction_rules, ...;

-- Sprint 9: pharmacist-workbench
CREATE TABLE consultations, follow_up_tasks, vip_marks, ...;

-- Sprint 10: mobile + health + ecom-ops
CREATE TABLE medication_reminders, medication_intakes, health_quizzes, health_quiz_results, flash_sales, ...;
```

## Phụ lục C: Reference

- Báo cáo so sánh: `docs/API_VS_DOCS_COMPARISON.md`
- Danh sách API hiện có: `docs/API_LIST.md`, `docs/api-list.json`
- So sánh JSON: `docs/api-comparison.json`
- SRS: `SRS_PhamacyChainManagementSystem_v1.0.0.md`
- SDD: `SDD_PhamacyChainManagementSystem_v1.0.0.md`
- UAT scripts: `docs/uat/00-MASTER-PLAN.md`, `docs/uat/01-AUTH-USER.md`, ...

---

**Người phụ trách:** Backend Squad
**Review:** Tech Lead + Product Owner
**Cập nhật lần cuối:** 2026-06-19
