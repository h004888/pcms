# 🚧 PCMS — Ràng buộc chéo & Điểm vướng mắc cần giải quyết

> **Ngày lập:** 2026-06-16
> **Căn cứ:** Phân tích codebase hiện tại (16 modules, 198 Java files) + 10 điểm chung đã giải quyết
> **Mục đích:** Liệt kê TẤT CẢ ràng buộc đang chặn 5 team, phân loại theo mức độ nghiêm trọng

---

## 🎯 Tổng quan

| Mức độ | Số lượng | Ý nghĩa |
|--------|----------|---------|
| 🔴 **Critical blocker** | **12** | Team không thể start được work nếu thiếu |
| 🟠 **High priority** | **15** | Team start được nhưng integration sẽ fail |
| 🟡 **Medium** | **11** | Có workaround tạm thời, nên fix sớm |
| 🟢 **Low / Nice-to-have** | **8** | Polish, cải thiện chất lượng |

**Tổng cộng: 46 blockers** đang cản team.

---

## 🔴 CRITICAL BLOCKERS (12) — Phải xong trước khi team khác move on

### 🔴 B-01: API Gateway KHÔNG validate JWT

**Hiện trạng:** `api-gateway/ApiGatewayApplication.java` chỉ có CORS filter, KHÔNG có filter nào check `Authorization: Bearer <token>`.

```java
// HIỆN TẠI (chỉ CORS):
@Bean
public CorsWebFilter corsWebFilter() { ... }
// THIẾU: JwtAuthenticationFilter
```

**Ảnh hưởng:**
- 🟥 Bất kỳ ai gọi `/api/v1/orders` không cần token → không bảo mật
- 🟥 5 role (Admin/CEO/Manager/Pharmacist/Customer) không có nghĩa
- 🟥 Frontend dev cũng không thể test authorization

**Owner:** T1 (Foundation)
**Block:** TẤT CẢ team khác
**Effort:** 0.5 ngày (JwtAuthenticationFilter dùng `JwtUtils` từ common)
**Cần làm:**
```java
@Component @Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class JwtAuthenticationFilter implements GlobalFilter {
    public Mono<Void> filter(...) {
        // Extract Bearer token
        // Validate via JwtUtils.parseAndValidate(token, secret)
        // Set userId/role/branchId as headers for downstream
    }
}
```
Đặt trong `pcms-common` để dùng lại được.

---

### 🔴 B-02: 11/12 services KHÔNG có SecurityConfig

**Hiện trạng:** Chỉ `user-service` có `SecurityConfig.java`. 11 service còn lại dùng default Spring Security = block tất cả.

```
user-service: SecurityConfig.java ✅ (nhưng permitAll)
branch-service: KHÔNG có ❌
catalog-service, category-service, supplier-service: KHÔNG có ❌
inventory-service, customer-service, order-service, payment-service: KHÔNG có ❌
prescription-service, notification-service, report-service: KHÔNG có ❌
```

**Ảnh hưởng:**
- 🟥 11 services khi start sẽ tự động block mọi HTTP request
- 🟥 Test local bằng Postman sẽ fail 401/403
- 🟥 Frontend không gọi được backend

**Owner:** T1 (Foundation)
**Block:** TẤT CẢ team
**Effort:** 0.5 ngày
**Cần làm:** Tạo `BaseSecurityConfig` trong `pcms-common` cho phép:
- `/actuator/**`, `/healthz`, `/readyz` — public
- Mọi endpoint khác — cần JWT (validate qua gateway)
- CORS config

---

### 🔴 B-03: `pcms-common` thiếu BaseEntity — mỗi service tự viết audit fields

**Hiện trạng:** Mỗi entity tự khai báo:
```java
@Id @GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
@CreatedDate @Column(name = "created_at", ...)
private LocalDateTime createdAt;
@LastModifiedDate @Column(name = "updated_at", ...)
private LocalDateTime updatedAt;
```
Lặp lại ở **12 entities × 16 services** = ~30 chỗ duplicate.

**Ảnh hưởng:**
- 🟥 Khi đổi convention (vd: thêm `deleted_at`) phải sửa cả chục file
- 🟥 Dễ sai lệch giữa các service
- 🟥 Reviewer tốn thời gian check lặp

**Owner:** T1 (Foundation)
**Block:** T2, T3, T4, T5 (mỗi khi tạo entity mới)
**Effort:** 0.5 ngày
**Cần làm:** Tạo trong `pcms-common`:
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @CreatedDate @Column(name = "created_at", ...)
    private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "updated_at", ...)
    private LocalDateTime updatedAt;
    // equals/hashCode dựa trên id
}
```
Migrate từng entity (12 files).

---

### 🔴 B-04: T2 (Master Data) chưa có SEED DATA

**Hiện trạng:** Khi start `branch-service`, `catalog-service`, ... bảng sẽ trống rỗng. Không có:
- Categories mẫu (Thuốc cảm, Thuốc kháng sinh, ...)
- Suppliers mẫu
- Branches mẫu
- Medicines mẫu

**Ảnh hưởng:**
- 🟥 T3 (Inventory) không test được vì không có medicine để import
- 🟥 T4 (Order) không test được vì không có catalog
- 🟥 T5 (Report) tính revenue = 0 vì không có order
- 🟥 Frontend dev không có data để hiển thị

**Owner:** T2 (Master Data)
**Block:** T3, T4, T5
**Effort:** 1 ngày
**Cần làm:**
- `catalog-service/src/main/resources/data.sql` — 20+ medicines (panadol, paracetamol, vitamin C, ...)
- `category-service/src/main/resources/data.sql` — 10+ categories
- `supplier-service/src/main/resources/data.sql` — 5+ suppliers
- `branch-service/src/main/resources/data.sql` — 3+ branches (HCM, HN, DN)
- Activate via `spring.jpa.defer-datasource-initialization: true` + `spring.sql.init.mode: always`

---

### 🔴 B-05: `catalog-service` không có Feign client cho branch validation

**Hiện trạng:** `catalog-service` có `openfeign` dep nhưng KHÔNG có package `client/` (chỉ có controller, service, repository, entity).

```
catalog-service/src/main/java/com/pcms/catalogservice/
├── controller/
├── service/ (impl/SearchServiceImpl.java + MedicineServiceImpl.java)
├── repository/
├── entity/
├── enums/
└── dto/   ← KHÔNG có client/!
```

**Ảnh hưởng:**
- 🟥 Khi medicine cần validate category tồn tại, không có cách nào check
- 🟥 Hoặc tạo Medicine với `categoryId` không tồn tại → FK fail ở runtime (sau khi FK constraint thêm)

**Owner:** T2 (Master Data)
**Block:** T2 (chính nó khi cần validate), T4 (Order cần tạo Medicine lúc order)
**Effort:** 0.5 ngày
**Cần làm:**
```java
@FeignClient(name = "category-service")
public interface CategoryClient {
    @GetMapping("/categories/{id}")
    Map<String, Object> getById(@PathVariable UUID id);
}
```

---

### 🔴 B-06: T3 (Inventory) thiếu Feign clients + không validate medicine/branch

**Hiện trạng:** `inventory-service` có `openfeign` dep nhưng KHÔNG có package `client/`.

Khi `importStock(medicineId, branchId, ...)`:
- Không validate `medicineId` có tồn tại trong catalog-service
- Không validate `branchId` có tồn tại trong branch-service
- FK trong DB chưa có (vì multi-database) → invalid data có thể lọt vào

**Ảnh hưởng:**
- 🟥 Có thể tạo batch cho medicine không tồn tại
- 🟥 Report sẽ hiển thị batch "orphan" không join được
- 🟥 Integration test fail khi test E2E

**Owner:** T3 (Inventory)
**Block:** T4 (Order cần inventory batch hợp lệ), T5 (Report)
**Effort:** 1 ngày
**Cần làm:** Tạo `CatalogClient` + `BranchClient` trong `inventory-service/.../client/`

---

### 🔴 B-07: T3 (Inventory) — BR02 low-stock threshold chưa có entity field

**Hiện trạng:** `InventoryBatch` entity không có field `minStockLevel`. BR02 yêu cầu:
> "Khi stock < min_stock_level → cảnh báo"

Nhưng `lowStockAlerts()` chỉ trả batch mà KHÔNG check min stock → luôn trả empty hoặc trả tất cả.

**Ảnh hưởng:**
- 🟥 BR02 không thể verify
- 🟥 UI sẽ không hiển thị alert đúng
- 🟥 NSF-02 không hoạt động

**Owner:** T3 (Inventory)
**Block:** T5 (Notification sẽ không có gì để gửi), T5 (Report thiếu data)
**Effort:** 0.5 ngày
**Cần làm:**
```java
@Entity class InventoryBatch {
    // ... existing fields
    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 10;  // default
}
```
Update `lowStockAlerts()` để filter `qty_on_hand < min_stock_level`.

---

### 🔴 B-08: T4 (Order) — generateOrderNumber có race condition

**Hiện trạng:** `OrderServiceImpl.generateOrderNumber()`:
```java
private String generateOrderNumber() {
    String datePrefix = LocalDate.now().format(DATE_FMT);
    List<Order> latest = orderRepository.findByDatePrefix(datePrefix, PageRequest.of(0, 1));
    int nextNum = 1;
    if (!latest.isEmpty()) {
        String numPart = latest.get(0).getOrderNumber().substring(...);
        try { nextNum = Integer.parseInt(numPart) + 1; } catch (...) {}
    }
    return String.format("ORD-%s-%04d", datePrefix, nextNum);
}
```

**Vấn đề:**
- 🟥 Race condition: 2 orders tạo cùng lúc → cùng đọc max=N → cùng tạo `ORD-...-N+1` → duplicate ORD-yyyymmdd-####
- 🟥 Vi phạm UNIQUE constraint → 1 trong 2 fail
- 🟥 Người dùng phải retry → UX kém

**Owner:** T4 (Transaction)
**Block:** Production deploy (không thể scale horizontal)
**Effort:** 0.5 ngày
**Cần làm:** Dùng DB sequence hoặc SELECT FOR UPDATE:
```java
@Transactional
public String generateOrderNumber() {
    // SELECT max(...) FROM orders WHERE order_number LIKE 'ORD-yyyymmdd-%' FOR UPDATE
    // hoặc dùng MySQL AUTO_INCREMENT sequence riêng cho order_number_seq
}
```

---

### 🔴 B-09: T4 (Payment) — webhook NOT IMPLEMENTED

**Hiện trạng:** `payment-service` có endpoint `/payments/webhook` được khai báo trong `ReportController` (nhầm chỗ) và một số chỗ khác, nhưng KHÔNG có implementation:
- Không có HMAC verify
- Không có idempotency check
- Không có state transition logic

**Ảnh hưởng:**
- 🟥 Card/QR payment từ gateway sẽ không hoạt động
- 🟥 Order sẽ không bao giờ chuyển sang PAID (chỉ CASH hoạt động)
- 🟥 UC07 không thể UAT

**Owner:** T4 (Transaction)
**Block:** Production
**Effort:** 1 ngày
**Cần làm:**
```java
@PostMapping("/webhook")
public ResponseEntity<...> handleWebhook(
    @RequestHeader("X-Signature") String hmac,
    @RequestBody String rawBody) {
    // 1. Verify HMAC with shared secret
    // 2. Idempotency check (idempotencyKey header)
    // 3. Find payment by gateway transaction ref
    // 4. Mark PAID/FAILED
    // 5. Call orderService.markAsPaid
    // 6. Return 200 OK (idempotent)
}
```

---

### 🔴 B-10: T4 ↔ T3 — `consumeStock` chưa được T3 thật sự implement đúng FIFO

**Hiện trạng:** `InventoryServiceImpl.consumeStock()` tồn tại nhưng cần verify logic. Từ code review trước:
```java
// Sắp xếp batch theo expiry tăng dần, pick từng batch cho đủ qty
// Cần test với multi-batch để verify
```

**Ảnh hưởng:**
- 🟥 NSF-05 (FIFO) có thể sai → lấy batch mới trước batch cũ
- 🟥 BR06 (restore stock on cancel) chưa test với multi-batch
- 🟥 Report inventory sai → mismatch với stock thực tế

**Owner:** T3 (Inventory) + T4 (test)
**Block:** Production
**Effort:** 1 ngày (test) + 0.5 ngày (fix nếu sai)
**Cần làm:** Unit test với scenarios:
- 1 batch, consume full → OK
- 1 batch, consume partial → OK
- 3 batches (expiry 2024, 2025, 2026), consume đủ → phải lấy 2024 trước
- 3 batches, không đủ → throw `InsufficientStockException`

---

### 🔴 B-11: T5 (Notification) — Email/SMS là MOCK, chưa thật sự gửi

**Hiện trạng:** `NotificationSenderServiceImpl` có:
```java
private final JavaMailSender mailSender; // may be null when mail is not configured
```
- IN_APP: chỉ persist DB ✅
- EMAIL: nếu `mailSender` null → log warn, không gửi ❌
- SMS: log "would send SMS to..." ❌

**Ảnh hưởng:**
- 🟥 UC13 không thực sự gửi notification
- 🟥 User không nhận được email khi order paid
- 🟥 NSF-09 retry không test được vì không có gì fail

**Owner:** T5 (Notification)
**Block:** Production deploy, UAT
**Effort:** 1 ngày
**Cần làm:**
- Setup SMTP dev (MailHog container trong docker-compose)
- Setup SMS mock (chỉ log + persist)
- Add `application.yml` config cho mail host

---

### 🔴 B-12: T5 (Report) — Excel/PDF export NOT IMPLEMENTED

**Hiện trạng:** `ReportController.export()`:
```java
return ResponseEntity.status(501).body(reportService.export(type, format, from, to));
```
và `ReportServiceImpl.export()`:
```java
// Placeholder until Apache POI / iText wiring is finalised.
payload.put("status", "pending");
```

**Ảnh hưởng:**
- 🟥 FR9.3, FR9.4 không hoàn thành
- 🟥 Manager không xuất được báo cáo
- 🟥 UAT Report UC09 fail

**Owner:** T5 (Report)
**Block:** UAT, Production
**Effort:** 1.5 ngày
**Cần làm:**
- Apache POI cho `.xlsx` (đã có sẵn trong pom)
- iText cho `.pdf` (đã có sẵn trong pom)
- Implement `ExcelExportService`, `PdfExportService`
- Generate file + return as `byte[]` với `Content-Disposition`

---

## 🟠 HIGH PRIORITY (15) — Team start được nhưng integration sẽ fail

### 🟠 B-13: Event bus (Kafka/RabbitMQ) chưa setup

**Hiện trạng:** 
- `pcms-common` có `LoggingEventPublisher` (chỉ log)
- KHÔNG có Kafka/RabbitMQ config
- KHÔNG có topic nào được tạo

**Ảnh hưởng:**
- 🟥 T3 (Inventory) TODO: "emit notification via notification-service" — không thể làm
- 🟥 T4 (Order) TODO: "emit notification to customer (MSG19)" — không thể làm
- 🟥 T5 (Notification) chỉ nhận qua REST push, không subscribe event

**Owner:** T5 (Customer + Insights — set up infra)
**Block:** T3, T4 (TODO comments)
**Effort:** 1 ngày (dùng RabbitMQ đơn giản hơn Kafka)
**Cần làm:**
```yaml
# docker-compose.yml
rabbitmq:
  image: rabbitmq:3-management
  ports: 5672, 15672
```
```java
// pcms-common — thêm RabbitMQ config
// Tạo KafkaConfig / RabbitConfig
```

---

### 🟠 B-14: Idempotency-Key chỉ log, chưa thực sự deduplicate

**Hiện trạng:** `IdempotencyKeyFilter` chỉ WARN log khi thiếu key, không thực sự check duplicate.

**Ảnh hưởng:**
- 🟥 POST /orders twice với cùng payload → tạo 2 orders
- 🟥 Webhook payment retry → credit points 2 lần (đã fix cho Customer.addPoints, nhưng các endpoint khác chưa)
- 🟥 Duplicate side-effects khắp nơi

**Owner:** T1 (Foundation) + T5 (Customer — đã xong addPoints)
**Block:** Tất cả write endpoints
**Effort:** 1 ngày
**Cần làm:**
- Thêm Redis hoặc Caffeine cache
- Tạo `IdempotencyStore` interface
- Implement lưu (key, request-hash, response, ttl=24h)
- Filter check cache trước khi gọi controller

---

### 🟠 B-15: CustomerService `softDelete` thực ra là HARD delete

**Hiện trạng:** `CustomerServiceImpl.softDelete`:
```java
@Override
public void softDelete(UUID id) {
    Customer c = customerRepository.findById(id).orElseThrow(...);
    customerRepository.delete(c);  // ← HARD delete!
}
```
Comment trong code: "Entity has no deletedAt flag yet; perform a hard delete as soft-delete placeholder."

**Ảnh hưởng:**
- 🟥 FR2.5 (soft delete) vi phạm
- 🟥 Audit trail mất
- 🟥 Loi cảnh báo "soft-delete placeholder" sẽ vẫn còn sau khi deploy

**Owner:** T5 (Customer)
**Block:** Production
**Effort:** 0.5 ngày
**Cần làm:**
- Thêm field `status` (ACTIVE/INACTIVE) vào Customer
- Set `status=INACTIVE` thay vì `delete(c)`

---

### 🟠 B-16: `prescription-service` chưa liên kết với Order thực tế

**Hiện trạng:** `PrescriptionServiceImpl` có `signature_hash`, `code RX-yyyy####` nhưng KHÔNG có code link to order.

**Ảnh hưởng:**
- 🟥 FR12.3 (link prescription to order) chưa có
- 🟥 Order entity có `prescriptionId` field nhưng Prescription service không populate

**Owner:** T4 (Transaction)
**Block:** UC12 E2E test
**Effort:** 0.5 ngày
**Cần làm:** Thêm `linkToOrder(prescriptionId, orderId)` endpoint + Order API để set `prescriptionId`

---

### 🟠 B-17: OrderService.markAsPaid() gọi Feign nhưng KHÔNG validate response

**Hiện trạng:** 
```java
try {
    inventoryClient.consumeStock(...);
} catch (Exception e) {
    log.warn("Failed to consume stock...");
}
```
Catch TẤT CẢ exception → swallow → không rollback. Tương tự cho `customerClient.addPoints`.

**Ảnh hưởng:**
- 🟥 Order PAID nhưng stock không trừ → inventory overflow
- 🟥 Order PAID nhưng customer không nhận points → loyalty bug
- 🟥 User phải manual reconcile

**Owner:** T4 (Transaction)
**Block:** Production
**Effort:** 1 ngày (cần thiết kế saga)
**Cần làm:**
- Dùng Outbox pattern: lưu `pending_stock_consume` vào DB, scheduler retry
- Hoặc: fail-fast + manual reconciliation flow

---

### 🟠 B-18: 0% test coverage trên toàn project

**Hiện trạng:** `find . -name "src/test" -type d` không có folder test nào. `find . -name "*Test.java"` trả 0.

**Ảnh hưởng:**
- 🟥 Không thể refactor an toàn
- 🟥 Bug regression không được catch
- 🟥 Code review chỉ dựa trên "đọc code" — tốn thời gian
- 🟥 CI/CD không có gate nào chặn bug

**Owner:** MỖI team (cho service của mình)
**Block:** Production readiness
**Effort:** 5-7 ngày (1 ngày/team) cho happy path
**Cần làm:**
- JUnit 5 + Mockito cho unit test
- `@SpringBootTest` cho integration test
- Testcontainers cho MySQL thực
- Mục tiêu: ≥ 80% line coverage cho service mới

---

### 🟠 B-19: `prescription-service` chưa có `@EnableFeignClients`

**Hiện trạng:** Có dep `spring-cloud-starter-openfeign` + `resilience4j` nhưng `PrescriptionServiceApplication` không có `@EnableFeignClients`. Ngoài ra không có Feign client nào trong package client/.

**Ảnh hưởng:**
- 🟥 Khi cần verify doctor (user-service) hoặc customer → không có cách
- 🟥 Hiện tại pass compile nhưng vô dụng

**Owner:** T4 (Transaction)
**Block:** T4 (Prescription) khi cần validate doctor
**Effort:** 0.5 ngày
**Cần làm:** Thêm `@EnableFeignClients` + tạo `UserClient` (validate doctor) + `CustomerClient` (validate patient)

---

### 🟠 B-20: OrderService chưa verify customer tồn tại

**Hiện trạng:** `OrderServiceImpl.create`:
```java
public OrderResponse create(CreateOrderRequest request) {
    if (request.items() == null || request.items().isEmpty()) {
        throw new InvalidOperationException(...);
    }
    // KHÔNG check customer có tồn tại không!
    Order order = new Order();
    order.setCustomerId(request.customerId());
    ...
}
```

**Ảnh hưởng:**
- 🟥 Order có thể tạo với `customerId` không tồn tại → orphan
- 🟥 T5 (Customer) bị gọi `addPoints` với ID không tồn tại → error cascade

**Owner:** T4 (Transaction)
**Block:** Production
**Effort:** 0.5 ngày
**Cần làm:**
```java
try {
    customerClient.getCustomerById(request.customerId());
} catch (FeignException.NotFound) {
    throw new ResourceNotFoundException("Customer", request.customerId());
}
```

---

### 🟠 B-21: OrderService chưa verify branch tồn tại

**Hiện trạng:** Tương tự B-20, không check `branchId` tồn tại.

**Owner:** T4 (Transaction)
**Block:** Production
**Effort:** 0.5 ngày
**Cần làm:** Thêm `BranchClient` + check.

---

### 🟠 B-22: InventoryEntity thiếu minStockLevel (xem B-07)

**Trùng với B-07 — đã note.**

---

### 🟠 B-23: 2 `*.original` files trong target/

**Hiện trạng:** Build để lại file `.jar.original` (Spring Boot repackage). Không phải lỗi, nhưng .gitignore nên loại bỏ.

**Owner:** T1
**Block:** Repo cleanliness
**Effort:** 5 phút
**Cần làm:** Đã có `*.jar` trong `.gitignore` — kiểm tra `target/` đã ignore chưa.

---

### 🟠 B-24: `branch-service` cần Feign client cho User validation

**Hiện trạng:** `branch-service` KHÔNG có dep `openfeign`. Khi admin assign manager, không validate user có role BRANCH_MANAGER.

**Owner:** T2 (Master Data)
**Block:** T2 (Branch), T1 (User cần check role)
**Effort:** 0.5 ngày
**Cần làm:** Add Feign dep + `UserClient` + check role ở `PUT /branches/{id}/manager`

---

### 🟠 B-25: `category-service` cần validate medicine FK khi delete

**Hiện trạng:** `CategoryServiceImpl`:
```java
public void delete(UUID id) {
    categoryRepository.deleteById(id);
    // KHÔNG check có medicine nào reference không!
}
```

**Ảnh hưởng:**
- 🟥 Nếu có medicine với `categoryId = X`, delete category → FK fail ở runtime
- 🟥 Hoặc orphan medicine (nếu FK không enforce)

**Owner:** T2 (Master Data)
**Block:** Production
**Effort:** 0.5 ngày
**Cần làm:** Check medicine count, throw `InvalidOperationException` nếu > 0

---

### 🟠 B-26: T2 KHÔNG có `customer-service` Feign client (cho xem purchase history)

**Hiện trạng:** `customer-service` endpoint `/customers/{id}/orders` và `/customers/{id}/history` đã có, nhưng:
- KHÔNG có Feign client nào gọi nó
- Order-service KHÔNG gọi `/customers/{id}/orders` để verify customer tồn tại
- Report-service KHÔNG gọi để lấy customer segment

**Owner:** T2 (hoặc T4 cho Order, T5 cho Report)
**Block:** Multi-service integration
**Effort:** 0.5 ngày
**Cần làm:** Tạo Feign client cần thiết.

---

### 🟠 B-27: `user-service` AuthController.refresh + logout = stub

**Hiện trạng:**
```java
public ResponseEntity<?> refresh(...) {
    // TODO: validate refresh token, generate new access token
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)...;
}
public ResponseEntity<?> logout() {
    // TODO: push revoked token to gateway blacklist
    return ResponseEntity.ok(Map.of("message", "Logged out"));
}
```

**Ảnh hưởng:**
- 🟥 User phải login lại sau 15 phút (vì refresh fail)
- 🟥 Logout không có tác dụng (token vẫn valid 15 phút)
- 🟥 BR05 (lockout) + NSF-07 (blacklist) không hoạt động đúng

**Owner:** T1 (Foundation)
**Block:** TẤT CẢ team khi test auth flow
**Effort:** 1.5 ngày
**Cần làm:**
- `RefreshTokenService` lưu refresh token ở DB/Redis
- `TokenBlacklist` lưu JTI revoked
- Update JwtService với `jti` claim

---

## 🟡 MEDIUM (11) — Có workaround tạm thời

### 🟡 B-28: Validation message tiếng Việt chưa đầy đủ

**Hiện trạng:** Một số DTO có `@NotBlank(message = "Email không được để trống")`, một số thì không.

**Owner:** Mỗi team
**Effort:** 1 ngày tổng
**Cần làm:** Rà soát tất cả DTO, thêm message VI.

---

### 🟡 B-29: Không có OpenAPI/Swagger UI

**Hiện trạng:** 0 endpoint có `@Operation` / `@ApiResponse`.

**Owner:** T1 (Foundation)
**Effort:** 1 ngày
**Cần làm:** Add `springdoc-openapi` + config.

---

### 🟡 B-30: Không có API versioning strategy cho breaking changes

**Hiện trạng:** Tất cả endpoint đều `/api/v1`. Khi cần breaking change → đụng độ.

**Owner:** T1
**Effort:** 0.5 ngày
**Cần làm:** Document rule (vd: v1 ổn định 1 năm, v2 cho breaking change mới).

---

### 🟡 B-31: Không có CI/CD pipeline

**Hiện trạng:** Không có `.github/workflows`, không có `Jenkinsfile`.

**Owner:** T1
**Effort:** 1 ngày
**Cần làm:** GitHub Actions:
- `mvn verify` on PR
- Run test
- Build Docker image
- Deploy to staging

---

### 🟡 B-32: Không có rate limiting (CR-09)

**Hiện trạng:** SDD nói "100 req/min/IP" ở API Gateway, nhưng chưa implement.

**Owner:** T1
**Effort:** 0.5 ngày
**Cần làm:** Add `spring-cloud-gateway` rate limiter filter.

---

### 🟡 B-33: Không có search service (UC10)

**Hiện trạng:** `SearchServiceImpl` ở `catalog-service` chỉ là placeholder. SDD đề cập `search-service` riêng.

**Owner:** T5
**Effort:** 2 ngày (nếu dùng Elasticsearch) hoặc 0.5 ngày (nếu dùng JPA LIKE)
**Cần làm:** Decide architecture (Elasticsearch vs JPA), implement.

---

### 🟡 B-34: Customer `getByPhone` controller dùng filter hack

**Hiện trạng:** 
```java
return customerService.list(phone, 0, 1).getContent().stream()
    .filter(c -> c.phone() != null && c.phone().contains(phone))  // contains, not exact
    .findFirst()
    .map(ResponseEntity::ok)
    .orElseThrow(...);
```

**Ảnh hưởng:**
- 🟡 Phone "0123" sẽ match "0123456" → wrong result
- 🟡 Performance: load toàn bộ customer rồi filter trong memory

**Owner:** T5 (Customer)
**Effort:** 0.5 ngày
**Cần làm:** Thêm method `findByPhone(String phone)` exact match trong repository.

---

### 🟡 B-35: `notification-service` chưa có template system

**Hiện trạng:** `Notification` entity có field `template` (NTPL-LOW-STOCK, NTPL-EXPIRY, ...) nhưng KHÔNG có `NotificationTemplate` entity, không có resolver.

**Owner:** T5 (Notification)
**Effort:** 1 ngày
**Cần làm:** Tạo `NotificationTemplate` table + `TemplateResolver` (Map.of("name", "John", ...)).

---

### 🟡 B-36: Không có health check tổng quát

**Hiện trạng:** Mỗi service có `/actuator/health` riêng. Nhưng không có tổng hợp "toàn hệ thống có healthy không".

**Owner:** T1
**Effort:** 0.5 ngày
**Cần làm:** Custom HealthIndicator check Eureka + tất cả service quan trọng.

---

### 🟡 B-37: Config Server chưa có encryption cho secret

**Hiện trạng:** `user-service.yml` có `secret: "pcms-jwt-secret-key-..."` plain text.

**Owner:** T1
**Effort:** 0.5 ngày
**Cần làm:** Spring Cloud Config encryption với public key.

---

### 🟡 B-38: Không có distributed tracing (Zipkin/Jaeger)

**Hiện trạng:** Correlation ID có, nhưng chưa visualize được.

**Owner:** T1
**Effort:** 1 ngày
**Cần làm:** Add Micrometer Tracing + Zipkin server trong docker-compose.

---

## 🟢 LOW / NICE-TO-HAVE (8)

| ID | Vấn đề | Owner | Effort |
|----|--------|-------|--------|
| 🟢 B-39 | Không có `@Cacheable` trên reference data (categories, suppliers) | T2 | 0.5d |
| 🟢 B-40 | Không có structured logging (JSON format) | T1 | 0.5d |
| 🟢 B-41 | Không có actuator custom metrics (orders/day, payments/day) | T5 | 0.5d |
| 🟢 B-42 | Docker image chưa optimize (multi-stage build) | T1 | 0.5d |
| 🟢 B-43 | Không có Prometheus + Grafana dashboard | T1 | 1d |
| 🟢 B-44 | Các entity chưa có `equals/hashCode` dựa trên UUID | T1 | 0.5d |
| 🟢 B-45 | Không có API rate limit per user (chỉ per IP) | T1 | 0.5d |
| 🟢 B-46 | DTO chưa dùng JavaDoc đầy đủ (chỉ một số) | Tất cả | ongoing |

---

## 📊 Phân bổ blockers theo team

| Team | Critical | High | Medium | Low | Tổng |
|------|----------|------|--------|-----|------|
| **T1 Foundation** | 3 (B-01, B-02, B-03) | 1 (B-27) | 5 (B-29..38) | 4 | **13** |
| **T2 Master Data** | 2 (B-04, B-05) | 2 (B-24, B-25) | 0 | 1 | **5** |
| **T3 Inventory** | 2 (B-06, B-07) | 0 | 0 | 0 | **2** |
| **T4 Transaction** | 3 (B-08, B-09, B-10) | 4 (B-16, B-17, B-19, B-20, B-21) | 0 | 0 | **7** |
| **T5 Customer+Insights** | 2 (B-11, B-12) | 1 (B-15) | 2 (B-33, B-34, B-35) | 0 | **5** |
| **Mọi team** | 0 | 1 (B-14 idempotency) | 1 (B-28 validation) | 2 (B-18 test, B-46) | **4** |
| **Cross-cutting (T1)** | 0 | 1 (B-13 event bus) | 0 | 0 | **1** |

---

## 🎯 Đề xuất thứ tự giải quyết

### Tuần này (5 ngày) — Unblock tất cả

| Day | Task | Owner | Unblocks |
|-----|------|-------|----------|
| **Mon AM** | B-01: JwtAuthenticationFilter ở gateway + B-02: BaseSecurityConfig ở common | T1 | Tất cả 11 service |
| **Mon PM** | B-03: BaseEntity ở common + migrate entities | T1 | T2/T3/T4/T5 |
| **Tue** | B-04: Seed data cho T2 + B-05: CatalogClient + B-24: UserClient | T2 | T3, T4, T5 |
| **Wed** | B-06: T3 Feign clients + B-07: minStockLevel + B-22 | T3 | T4, T5 |
| **Thu** | B-08: Order number sequence + B-09: Payment webhook + B-20: Customer validate | T4 | Production readiness |
| **Fri** | B-10: T3 FIFO test + B-11: Notification email/SMS thật + B-12: Report export | T3, T5 | UAT |

### Tuần sau (5 ngày) — Polish + Integration

| Day | Task | Owner |
|-----|------|-------|
| Mon | B-13: RabbitMQ setup + B-14: Idempotency-Key thật | T1, T5 |
| Tue | B-15: Customer soft delete + B-16: Rx↔Order link | T5, T4 |
| Wed | B-17: Outbox pattern cho Order.markAsPaid | T4 |
| Thu | B-18: Test scaffold (JUnit 5 + Testcontainers mẫu cho 1 service) | T1 |
| Fri | B-19..21: Feign clients còn thiếu + bug fix | Tất cả |

### Tuần 3-4 — Production readiness

- 🟠 B-22..27 còn lại
- 🟡 B-28..38 (medium)
- 🟢 B-39..46 (low — chỉ khi rảnh)

---

## 🔗 Cross-Team Dependency Graph

```
┌────────────┐  jwt-secret/config    ┌────────────┐
│    T1      │ ───────────────────> │  T2 Master │
│ Foundation │                      │  Data      │
│            │ <─────────────────── │            │
└──────┬─────┘   seed data          └─────┬──────┘
       │                                 │
       │ BaseEntity + SecurityConfig     │ Medicine + Branch APIs
       │                                 │
       v                                 v
┌────────────┐  consumeStock API   ┌────────────┐
│    T3      │ <────────────────── │  T4 Trans  │
│ Inventory  │ ──────────────────> │  (Order+   │
│            │  medicine/branch     │  Payment+  │
└──────┬─────┘   validation        │   Rx)      │
       │                            └─────┬──────┘
       │ low_stock / expiry event         │ order.paid / cancel event
       │ (cần RabbitMQ)                   │ (cần RabbitMQ)
       │                                  │
       v                                  v
┌────────────────────────────────────────────┐
│              T5 Customer + Insights         │
│  - Customer: nhận addPoints từ T4          │
│  - Notification: subscribe events từ T3/T4 │
│  - Report: aggregate từ T3/T4              │
│  - Search: index từ T2 catalog             │
└────────────────────────────────────────────┘
```

**3 critical paths (sequential):**
1. **T1 → T2 → T3 → T4** (chuỗi chính — phải xong lần lượt)
2. **T1 → T4** (auth cho payment, refresh, logout)
3. **T2 → T5** (search index cần catalog data)

---

## ✅ Action items cho Daily Standup

Mỗi team báo cáo mỗi sáng:
1. **Hôm qua:** Giải quyết blocker nào (B-XX)?
2. **Hôm nay:** Sẽ giải blocker nào?
3. **Blocker:** Đang chờ gì từ team khác? (cite B-XX cụ thể)

Ví dụ báo cáo của T4:
> "Hôm qua: xong B-08 (sequence), B-09 (webhook stub). Hôm nay: làm B-20 (customer validate). Blocker: chờ T2 publish seed data (B-04) để test E2E order flow."

---

> **Tổng kết:** 12 critical blockers cần giải quyết trong tuần này để cả team unblock. Nếu T1 finish B-01 + B-02 + B-03 và T2 finish B-04 + B-05 thì 80% vướng mắc sẽ được gỡ.
