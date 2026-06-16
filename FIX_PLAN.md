# 🛠️ PCMS — Kế hoạch FIX toàn bộ Blockers

> **Ngày lập:** 2026-06-16
> **Căn cứ:** `BLOCKERS.md` (46 blockers)
> **Mục tiêu:** Giải quyết 100% blockers trong 4 tuần (3 tuần fix + 1 tuần polish)
> **Phương pháp:** Parallel workstreams theo team, critical path trước

---

## 🎯 Executive Summary

| Tuần | Mục tiêu | Output |
|------|----------|--------|
| **Week 1** | T1 unblock tất cả + T2/T3 fix critical | API Gateway có auth, 11 services có SecurityConfig, BaseEntity, seed data |
| **Week 2** | T4 production-ready + T5 reports/notification | Payment webhook, Order sequence, FIFO tested, Report export |
| **Week 3** | Integration: RabbitMQ, Idempotency, Outbox | Event bus hoạt động, 0 duplicate, saga compensation |
| **Week 4** | Tests + Polish | ≥60% coverage, Swagger, CI/CD, deploy-ready |

**Capacity:**
- 5 người × 5 ngày/tuần × 4 tuần = **100 person-days**
- 46 blockers × trung bình 0.7 ngày = **~32 ngày công** (parallel)
- **Buffer: 60+ ngày** → rất thoải mái

---

## 📊 Dependency Graph giữa các blockers

```
                    B-01 Gateway JWT
                         │
                         ↓
              B-02 BaseSecurityConfig
              B-03 BaseEntity
              B-04 Seed data  ─────┐
              B-05 CatalogClient    │
                         │         │
                         ↓         ↓
              B-06 Inventory Feign + B-07 minStock
                         │
                         ↓
              B-20 Customer validate + B-21 Branch validate
                         │
                         ↓
              B-08 Order sequence + B-09 Payment webhook
                         │
                         ↓
              B-10 FIFO test + B-11 Notification thật + B-12 Report export
                         │
                         ↓
              B-13 Event bus + B-14 Idempotency thật + B-17 Outbox
                         │
                         ↓
              B-18 Tests + B-28 Validation + B-29 Swagger + B-31 CI/CD
```

**Critical Path:** B-01 → B-02 → B-04 → B-06 → B-20 → B-09 → B-13 → B-18 (15 ngày)

---

## 📅 WEEK 1 — Foundation (T1 unblock mọi người)

### 🎯 Goal tuần 1
- API Gateway validate JWT → tất cả 11 services có thể start
- BaseEntity → tất cả team dùng chung
- Seed data cho T2 → T3/T4/T5 có data để test
- T2/T3 fix critical blockers của mình

### Day 1 (Mon) — T1 độc quyền

**Sáng (4h) — T1 fix B-01 (Gateway JWT filter):**
- Tạo `pcms-common/security/JwtAuthenticationFilter` (implements `GlobalFilter`)
- Extract Bearer token, validate qua `JwtUtils`
- Set `X-User-Id`, `X-User-Role`, `X-Branch-Id` headers cho downstream
- Update `api-gateway/ApiGatewayApplication` để register filter
- **Done when:** Request không có token → 401, có token hợp lệ → pass qua

**Chiều (4h) — T1 fix B-02 (BaseSecurityConfig):**
- Tạo `pcms-common/security/BaseSecurityConfig` (class abstract với common config)
- Permit `/actuator/**`, `/auth/**`, `/healthz`, `/readyz`
- Require auth cho tất cả endpoint khác
- Mỗi service chỉ cần extend và thêm role-specific rules
- Migrate `user-service/SecurityConfig` sang dùng BaseSecurityConfig
- **Done when:** `mvn -pl user-service clean package` OK, các service khác compile OK

**Parallel work cho T2/T3/T4/T5 (nếu rảnh):**
- Đọc docs, fix typo, cleanup code nhỏ

### Day 2 (Tue) — T1 tiếp + T2 bắt đầu song song

**T1 — Sáng (4h) — B-03 BaseEntity:**
- Tạo `pcms-common/entity/BaseEntity` (abstract `@MappedSuperclass`)
- Fields: `id (UUID)`, `createdAt`, `updatedAt`
- Implement `equals/hashCode` dựa trên `id`
- Migrate 12 entities (Order, User, Medicine, ...) sang extend BaseEntity
- Build verify toàn project
- **Done when:** `mvn clean package` thành công, 12 entities extend BaseEntity

**T1 — Chiều (4h) — B-27 (Auth refresh + logout):**
- Tạo `RefreshToken` entity + repository
- `RefreshTokenService` lưu/validate refresh token
- `TokenBlacklistService` lưu JTI revoked (in-memory cho phase 1)
- Update `AuthController.refresh()` + `logout()`
- **Done when:** Login → access (15min) + refresh (7d), refresh → new access, logout → blacklist access token

**T2 — Cả ngày — B-04 (Seed data):**
- Tạo `catalog-service/src/main/resources/data.sql` (20+ medicines)
- Tạo `category-service/src/main/resources/data.sql` (10+ categories)
- Tạo `supplier-service/src/main/resources/data.sql` (5+ suppliers)
- Tạo `branch-service/src/main/resources/data.sql` (3+ branches)
- Update `application.yml` mỗi service: `spring.sql.init.mode: always`
- **Done when:** Start service → có data sẵn

### Day 3 (Wed) — T2 + T3 + T4 song song

**T2 — Sáng (4h) — B-05 (CatalogClient) + B-24 (UserClient for branch):**
- `catalog-service/.../client/CategoryClient.java` — get category by ID
- `branch-service` add dep openfeign + `client/UserClient.java` — get user by ID + validate role
- `branch-service/PUT /branches/{id}/manager` validate user.role == BRANCH_MANAGER
- **Done when:** Catalog service có thể validate category, branch service validate manager role

**T2 — Chiều (4h) — B-25 (Category delete validation):**
- `CategoryServiceImpl.delete()`: check `medicineRepository.countByCategoryId(id) > 0` → throw
- Hoặc dùng `inventory-service` Feign (nếu cần)
- **Done when:** Delete category có medicine → 400 error

**T3 — Cả ngày — B-06 (Inventory Feign clients):**
- Tạo `inventory-service/.../client/CatalogClient.java` (get medicine)
- Tạo `inventory-service/.../client/BranchClient.java` (get branch)
- `InventoryServiceImpl.importStock()` validate medicine + branch trước khi tạo
- **Done when:** Import batch với medicine/branch không tồn tại → 404

**T4 — Cả ngày — B-20 + B-21 (Customer + Branch validation):**
- `OrderServiceImpl.create()`: gọi `customerClient.getCustomerById()` → 404 nếu không tồn tại
- Tạo `BranchClient` trong `order-service`
- `OrderServiceImpl.create()`: gọi `branchClient.getById()` → 404 nếu không tồn tại
- **Done when:** Order với customerId/branchId invalid → 400

### Day 4 (Thu) — T1 + T3 + T4 + T5 song song

**T3 — Sáng (4h) — B-07 (minStockLevel):**
- Add field `minStockLevel Integer` vào `InventoryBatch`
- Default = 10
- Migration script: `ALTER TABLE inventory_batches ADD COLUMN min_stock_level INT NOT NULL DEFAULT 10;`
- Update `lowStockAlerts()`: filter `qty_on_hand < min_stock_level`
- Update tests nếu có
- **Done when:** GET `/inventory/alerts/low-stock` trả batch có stock thấp

**T3 — Chiều (4h) — B-22 (TODO emit notification):**
- Tạm thời: dùng `EventPublisher` từ `pcms-common` + `LoggingEventPublisher` đã có
- `ExpiryCheckScheduler` → publish `inventory.expiry` event (chỉ log trong phase 1)
- `InventoryServiceImpl.consumeStock()` → publish `inventory.low_stock` event nếu batch < min
- **Done when:** Log thấy event khi chạy scheduler

**T4 — Sáng (4h) — B-08 (Order number sequence):**
- Tạo `pcms_order.sequences` table (max_seq, prefix)
- Hoặc: dùng SELECT ... FOR UPDATE trong transaction
- Tốt nhất: dùng MySQL `AUTO_INCREMENT` table riêng
- **Done when:** 10 concurrent threads tạo order → 10 unique order numbers

**T4 — Chiều (4h) — B-09 (Payment webhook):**
- Tạo `WebhookEvent` entity để log + idempotency
- Tạo `WebhookController` ở `payment-service`
- Implement HMAC verify (secret từ Config Server)
- Map gateway event → `Payment` entity
- Trigger `orderService.markAsPaid` qua Feign
- **Done when:** Postman POST webhook với HMAC hợp lệ → order PAID

**T5 — Cả ngày — B-15 (Customer soft delete):**
- Add field `status` (ACTIVE/INACTIVE) vào `Customer`
- Update `softDelete()`: set `status = INACTIVE` thay vì `delete(c)`
- Update `list()`: filter `status = ACTIVE` by default
- Migration script
- **Done when:** Delete customer → status=INACTIVE, list không trả về

### Day 5 (Fri) — Verification + Fix còn lại

**Cả team — Integration test Day 1:**
- T1: chạy full stack (Eureka + Config + Gateway + User), test login + token validate
- T2: start branches + catalog + categories, verify seed data
- T3: import stock thật với seed data, consume stock
- T4: tạo order với customer/branch thật, mark as paid
- T5: tạo customer thật, list → thấy ACTIVE only

**Bug fix sprint cuối tuần 1 (reserve 2-3h):**
- Mỗi team fix bug phát hiện trong integration
- Commit + push

**Weekly review (1h cuối ngày):**
- Demo cho team
- Update BLOCKERS.md (close resolved)
- Điều chỉnh plan tuần sau nếu cần

### Week 1 Deliverables Checklist

- [ ] B-01: JwtAuthenticationFilter ở Gateway ✅
- [ ] B-02: BaseSecurityConfig ở common ✅
- [ ] B-03: BaseEntity ở common, 12 entities migrated ✅
- [ ] B-27: Auth refresh + logout ✅
- [ ] B-04: Seed data (catalog, category, supplier, branch) ✅
- [ ] B-05: CatalogClient trong catalog-service ✅
- [ ] B-06: CatalogClient + BranchClient trong inventory-service ✅
- [ ] B-07: minStockLevel field + lowStockAlerts ✅
- [ ] B-24: UserClient trong branch-service ✅
- [ ] B-25: Category delete validation ✅
- [ ] B-08: Order number sequence (no race) ✅
- [ ] B-09: Payment webhook + HMAC ✅
- [ ] B-20: Order validate customer ✅
- [ ] B-21: Order validate branch ✅
- [ ] B-15: Customer soft delete ✅
- [ ] B-22: Inventory emit events (log) ✅

**Critical blockers fixed: 12/12** ✅

---

## 📅 WEEK 2 — Production-ready core

### 🎯 Goal tuần 2
- T4 payment/order chạy ổn định
- T5 notification + report thực sự gửi/export
- T3 FIFO test pass
- Bắt đầu test scaffold

### Day 6-7 (Mon-Tue) — T5

**T5 — B-11 (Email/SMS thật):**
- Add `MailHog` container vào docker-compose
- Add `spring-boot-starter-mail` config (đã có)
- `NotificationService` thực sự gửi email qua SMTP (MailHog dev)
- SMS: tạm thời chỉ log + persist (sẽ dùng Twilio/ESMS sau)
- **Done when:** Tạo notification với channel=EMAIL → email thực sự gửi tới MailHog UI

**T5 — B-12 (Report Excel/PDF):**
- Add `ExcelExportService` (dùng Apache POI, đã có dep)
- Add `PdfExportService` (dùng iText, đã có dep)
- `ReportController.export()` trả `byte[]` thay vì 501
- **Done when:** GET `/reports/export?type=revenue&format=excel` → file .xlsx download

### Day 8-9 (Wed-Thu) — T3 + T4

**T3 + T4 — B-10 (FIFO test):**
- T3 viết test cho `consumeStock`:
  - 1 batch, full
  - 1 batch, partial
  - 3 batches (expiry: 2024, 2025, 2026) → phải pick 2024 trước
  - Không đủ stock → throw
- T4 viết integration test order → consume:
  - Order có 3 lines, mỗi line 5 qty
  - Mỗi medicine có 3 batches
  - Verify FIFO order đúng
- **Done when:** Tests pass, có report coverage

**T4 — B-16 (Prescription ↔ Order link):**
- `PrescriptionServiceImpl.create()` tạo prescription
- `PrescriptionController.POST /prescriptions/{id}/link-order` → set Order.prescriptionId
- Update `Order.prescriptionId` field
- **Done when:** Tạo Rx → link to order → order.prescriptionId có giá trị

**T4 — B-17 (Outbox pattern for markAsPaid):**
- Tạo `outbox_events` table (id, aggregateType, aggregateId, type, payload, status, createdAt)
- `OrderServiceImpl.markAsPaid()` insert outbox event thay vì gọi Feign directly
- Tạo `OutboxPublisher` scheduler (every 30s) → publish pending events
- **Done when:** Order PAID nhưng inventory down → event vẫn trong outbox, khi inventory up → consume

### Day 10 (Fri) — Test scaffold

**T1 — B-18 (Test scaffold):**
- Tạo module test mẫu cho 1 service (vd: `order-service`)
- JUnit 5 + Mockito + AssertJ config
- Testcontainers MySQL setup
- Sample unit test + integration test
- Document pattern trong `TESTING_GUIDE.md`
- **Done when:** order-service có 1 unit test + 1 integration test pass

---

## 📅 WEEK 3 — Integration (Event bus, Idempotency)

### 🎯 Goal tuần 3
- RabbitMQ chạy, event flow end-to-end
- Idempotency-Key thực sự deduplicate
- Test coverage lên 50%+

### Day 11-12 — RabbitMQ

**T5 (lead) — B-13 (Event bus):**
- Add RabbitMQ container vào docker-compose
- Add `spring-boot-starter-amqp` to pcms-common (optional)
- Tạo `RabbitConfig` với topic exchange `pcms.events`
- Tạo `RabbitEventPublisher` implements `EventPublisher`
- Update 3 services dùng event: order-service (publish), inventory-service (publish), notification-service (consume)
- **Done when:** Order PAID → event tới RabbitMQ → notification-service consume + gửi

### Day 13-14 — Idempotency

**T1 — B-14 (Idempotency-Key thật):**
- Add Caffeine cache dependency
- Tạo `IdempotencyStore` (Caffeine, 24h TTL)
- Update `IdempotencyKeyFilter`: check cache trước khi gọi controller
- Lưu response vào cache
- Nếu same key + same hash → trả cached response
- Nếu same key + different hash → 409 Conflict
- **Done when:** POST 2 lần cùng key + payload → chỉ tạo 1 resource, lần 2 trả same response

### Day 15 — Test expansion

**Mỗi team — viết thêm test cho service mình:**
- T1: UserServiceTest, AuthControllerTest
- T2: BranchServiceTest, MedicineServiceTest
- T3: InventoryServiceTest (FIFO chắc chắn pass)
- T4: OrderServiceTest, PaymentWebhookTest
- T5: CustomerServiceTest, NotificationServiceTest
- **Done when:** Tổng coverage ≥ 50%

---

## 📅 WEEK 4 — Polish & Deploy

### 🎯 Goal tuần 4
- Coverage ≥ 70%
- Swagger UI ready
- CI/CD pipeline
- Production-ready

### Day 16-17 — Tests + Swagger

**Mỗi team — Test coverage lên 70%:**
- Thêm test cho edge cases, error paths
- **Done when:** `mvn verify` pass, JaCoCo report ≥ 70%

**T1 — B-29 (Swagger):**
- Add `springdoc-openapi-starter-webmvc-ui`
- Annotate `@Operation`, `@ApiResponse` cho 1-2 controllers mẫu
- Swagger UI ở `/swagger-ui.html`
- **Done when:** Truy cập `/swagger-ui.html` thấy API docs

### Day 18-19 — CI/CD + Medium priority

**T1 — B-31 (CI/CD):**
- Tạo `.github/workflows/ci.yml`
- Steps: checkout → setup java 21 → mvn verify → build docker image
- Run on PR
- **Done when:** PR mới trigger CI, fail nếu test fail

**T1 — B-32 (Rate limiting):**
- Add `spring-cloud-gateway` rate limiter filter
- 100 req/min/IP
- **Done when:** Spam request → 429 Too Many Requests

**T1 — B-36 (Health check tổng):**
- Custom `HealthIndicator` aggregate Eureka status
- **Done when:** GET `/actuator/health` → bao gồm các service con

### Day 20 — Final verification

**Cả team — Final review:**
- Update README.md cho mỗi service
- Update BLOCKERS.md (close all)
- Sprint demo cho stakeholder
- Plan sprint tiếp theo

---

## 📋 Backlog chi tiết theo team

### T1 Foundation (13 blockers)

| ID | Effort | Status | Plan day |
|----|--------|--------|----------|
| B-01 | 0.5d | 🟡 Planned | Day 1 AM |
| B-02 | 0.5d | 🟡 Planned | Day 1 PM |
| B-03 | 0.5d | 🟡 Planned | Day 2 AM |
| B-27 | 1d | 🟡 Planned | Day 2 PM |
| B-29 | 1d | 🟡 Planned | Day 16 |
| B-31 | 1d | 🟡 Planned | Day 18 |
| B-32 | 0.5d | 🟡 Planned | Day 19 |
| B-36 | 0.5d | 🟡 Planned | Day 19 |
| B-14 | 1d | 🟡 Planned | Day 13-14 |
| B-37 | 0.5d | 🟢 Low | Week 4+ |
| B-40 | 0.5d | 🟢 Low | Week 4+ |
| B-38 | 1d | 🟢 Low | Week 4+ |
| B-43 | 1d | 🟢 Low | Week 4+ |

**Tổng T1: ~9 ngày** (đủ cho 4 tuần với test + integration)

### T2 Master Data (5 blockers)

| ID | Effort | Status | Plan day |
|----|--------|--------|----------|
| B-04 | 1d | 🟡 Planned | Day 2 (cả ngày) |
| B-05 | 0.5d | 🟡 Planned | Day 3 AM |
| B-24 | 0.5d | 🟡 Planned | Day 3 AM |
| B-25 | 0.5d | 🟡 Planned | Day 3 PM |
| B-39 | 0.5d | 🟢 Low | Week 4+ |

**Tổng T2: ~3 ngày** (có thời gian cho test + polish)

### T3 Inventory (2 blockers)

| ID | Effort | Status | Plan day |
|----|--------|--------|----------|
| B-06 | 1d | 🟡 Planned | Day 3 |
| B-07 | 0.5d | 🟡 Planned | Day 4 AM |
| B-10 | 1d | 🟡 Planned | Day 8-9 |
| B-22 | 0.5d | 🟡 Planned | Day 4 PM |

**Tổng T3: ~3 ngày** (có thời gian cho FIFO test chi tiết)

### T4 Transaction (7 blockers)

| ID | Effort | Status | Plan day |
|----|--------|--------|----------|
| B-08 | 0.5d | 🟡 Planned | Day 4 AM |
| B-09 | 1d | 🟡 Planned | Day 4 PM |
| B-16 | 0.5d | 🟡 Planned | Day 8 |
| B-17 | 1d | 🟡 Planned | Day 9 |
| B-19 | 0.5d | 🟡 Planned | Day 8 |
| B-20 | 0.5d | 🟡 Planned | Day 3 |
| B-21 | 0.5d | 🟡 Planned | Day 3 |

**Tổng T4: ~4.5 ngày** (phức tạp nhất)

### T5 Customer + Insights (5 blockers)

| ID | Effort | Status | Plan day |
|----|--------|--------|----------|
| B-11 | 1d | 🟡 Planned | Day 6 |
| B-12 | 1.5d | 🟡 Planned | Day 7 |
| B-15 | 0.5d | 🟡 Planned | Day 4 |
| B-13 | 1d | 🟡 Planned | Day 11-12 |
| B-26 | 0.5d | 🟡 Planned | Day 12 |
| B-33 | 2d | 🟢 Low | Week 4+ |
| B-34 | 0.5d | 🟡 Planned | Day 13 |
| B-35 | 1d | 🟡 Planned | Day 14 |

**Tổng T5: ~8 ngày** (nhiều nhất vì scope rộng)

### Cross-cutting (4 blockers)

| ID | Owner | Effort | Status | Plan |
|----|-------|--------|--------|------|
| B-13 | T5 (lead) | 1d | 🟡 | Day 11-12 |
| B-14 | T1 | 1d | 🟡 | Day 13-14 |
| B-18 | T1 (template) + all | 5d | 🟡 | Day 10 + ongoing |
| B-28 | Mỗi team | 1d total | 🟡 | Week 4 |

---

## 🎯 Definition of Done cho mỗi Blocker

Mỗi blocker phải pass checklist trước khi close:

- [ ] Code implemented theo plan
- [ ] Build pass: `mvn clean package -DskipTests` thành công
- [ ] Nếu là bug fix: test reproducer + test fix
- [ ] Update documentation liên quan (nếu có)
- [ ] Commit + push với message `fix(<scope>): resolve B-XX - <description>`
- [ ] Update BLOCKERS.md: chuyển status từ `🟡 Planned` → `✅ Done`

---

## ⚠️ Risk & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| T1 chậm → block T2/T3/T4/T5 | 🔴 Critical | T1 ưu tiên B-01, B-02, B-03. Nếu T1 thiếu người → escalate |
| Seed data sai (T2) → test T3/T4/T5 fail | 🟠 High | T2 verify seed data bằng cách SELECT trước khi announce |
| Order sequence sai (B-08) vẫn có race | 🟠 High | T4 viết test concurrent (10 thread tạo order) |
| Webhook HMAC sai (B-09) → security hole | 🟠 High | Dùng constant-time compare, log warn khi fail |
| Outbox pattern (B-17) phức tạp → deadline miss | 🟡 Medium | Có thể dùng `synchronized` đơn giản trước, refactor sau |
| Event bus (B-13) RabbitMQ config sai | 🟡 Medium | Dùng default config, test với 1 event trước khi scale |
| Test coverage 70% quá cao cho 4 tuần | 🟡 Medium | Mục tiêu thực tế: 50% (Day 15), 70% (Day 17) |
| Phụ thuộc Spring Boot 4.0.7 (mới) | 🟡 Medium | Nếu lỗi → tạm downgrade Spring Boot 3.x |

---

## 📈 Success Metrics

Sau 4 tuần, đo lường:

| Metric | Target | Hiện tại |
|--------|--------|----------|
| **Critical blockers resolved** | 12/12 | 0/12 |
| **High blockers resolved** | 15/15 | 0/15 |
| **Total blockers resolved** | ≥ 35/46 (76%) | 0/46 |
| **Build success** | 17/17 modules | 17/17 ✅ |
| **Test coverage** | ≥ 50% | 0% |
| **Services start được** | 12/12 | 0/12 (security block) |
| **E2E order flow pass** | Yes | No |
| **Payment webhook works** | Yes | No |
| **Notification thật sự gửi** | Yes | No (mock) |
| **Report export file** | Yes | No (501) |

---

## 🚀 Action ngay bây giờ

Tôi sẽ bắt đầu fix theo plan, parallel với subagent. Thứ tự ưu tiên:

1. **Ngay bây giờ (parallel):**
   - Subagent 1: T1 fix B-01, B-02 (auth + security)
   - Subagent 2: T2 fix B-04 (seed data)

2. **Sau khi T1 xong B-01/B-02:**
   - Subagent 3: T1 fix B-03 (BaseEntity)
   - Subagent 4: T3 fix B-06, B-07 (Feign + minStock)

3. **Sau khi T2 xong B-04:**
   - Subagent 5: T4 fix B-08, B-09, B-20, B-21
   - Subagent 6: T5 fix B-11, B-12, B-15

4. **Final:**
   - All teams: B-18 (tests)
   - T1: B-29, B-31, B-32 (Swagger, CI/CD, rate limit)

---

> **Ngày bắt đầu:** Ngay khi plan approved
> **Review:** Cuối mỗi tuần (Friday)
> **Adjust:** Plan có thể điều chỉnh theo velocity thực tế
