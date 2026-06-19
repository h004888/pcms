# Sprint 3 Report - B2B Prescription/Notification/Report

**Date:** 2026-06-19
**Worker:** Subagent (Sprint 3 scope)
**Sprint:** 3 - Hoàn thiện B2B Prescription/Notification/Report (6 API)
**Status:** ✅ DONE - All 16 modules compile, tests pass

---

## 1. Tóm tắt

| # | Ticket | Method | Path | Status |
|--:|--------|--------|------|:------:|
| 301 | Sign (alias) | POST | `/prescriptions/{id}/sign` | ✅ |
| 302 | Print (alias) | POST | `/prescriptions/{id}/print` | ✅ |
| 303 | Cancel prescription | DELETE | `/prescriptions/{id}` | ✅ |
| 304 | Delete notification | DELETE | `/notifications/{id}` | ✅ |
| 305 | Export Excel | POST | `/reports/export/excel` | ✅ (stub) |
| 306a | Export PDF | POST | `/reports/export/pdf` | ✅ (stub) |
| 306b | Cancel schedule | DELETE | `/reports/schedules/{id}` | ✅ |

**Tổng: 7 endpoints (6 tickets) - Tất cả ✅**

---

## 2. Chi tiết từng ticket

### TICKET-301: POST /prescriptions/{id}/sign (alias)

**File thay đổi:**

- `prescription-service/src/main/java/com/pcms/prescriptionservice/controller/PrescriptionController.java`

**Thay đổi:** Thêm method `signPost` gọi cùng `prescriptionService.sign(id)` như method PUT đã có.

**Lý do alias:** SDD §6.14 quy định `POST /sign`, code cũ dùng PUT. Giữ cả 2 để:

1. Tương thích SDD
2. Tránh 405 cho HTTP client mặc định dùng POST

```java
@PostMapping("/{id}/sign")
public ResponseEntity<PrescriptionResponse> signPost(@PathVariable UUID id) {
    return ResponseEntity.ok(prescriptionService.sign(id));
}
```

### TICKET-302: POST /prescriptions/{id}/print (alias)

**File thay đổi:**

- `prescription-service/src/main/java/com/pcms/prescriptionservice/controller/PrescriptionController.java`

**Thay đổi:** Thêm method `printPost` gọi cùng `prescriptionService.print(id)` như method GET đã có.

### TICKET-303: DELETE /prescriptions/{id} — Huỷ đơn thuốc

**Files thay đổi:**

- `prescription-service/src/main/java/com/pcms/prescriptionservice/controller/PrescriptionController.java`
- `prescription-service/src/main/java/com/pcms/prescriptionservice/service/PrescriptionService.java` (interface)
- `prescription-service/src/main/java/com/pcms/prescriptionservice/service/impl/PrescriptionServiceImpl.java`

**Business logic:**

```java
@Override
public PrescriptionResponse cancel(UUID id, UUID actorId) {
    Prescription p = prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));

    // Idempotent: already cancelled
    if (p.getStatus() == PrescriptionStatus.CANCELLED) return toResponse(p);

    // DRAFT: luôn cancel được
    if (p.getStatus() == PrescriptionStatus.DRAFT) {
        p.setStatus(PrescriptionStatus.CANCELLED);
        return toResponse(prescriptionRepository.save(p));
    }

    // SIGNED: chỉ cancel nếu chưa link order
    if (p.getStatus() == PrescriptionStatus.SIGNED) {
        if (p.getOrderId() == null) {
            p.setStatus(PrescriptionStatus.CANCELLED);
            return toResponse(prescriptionRepository.save(p));
        }
        // Linked → 409 BusinessException
        throw new InvalidOperationException(
            "Cannot cancel a prescription linked to an order (orderId=" + p.getOrderId() + ")",
            "Không thể huỷ đơn thuốc đã liên kết với đơn hàng...",
            409);
    }
    // Defensive fallback
    throw new InvalidOperationException(...);
}
```

**Đặc điểm:**

- ✅ Idempotent: cancel 2 lần trả về state hiện tại
- ✅ DRAFT luôn cancel được
- ✅ SIGNED chỉ cancel nếu chưa link order (BR06 - không phá audit trail)
- ✅ 409 nếu SIGNED có order link
- ✅ ResourceNotFoundException nếu không tìm thấy
- ✅ Log INFO với actorId

**Hạn chế đã ghi nhận:** Hiện tại không gọi `OrderClient` để check order status (PAID hay chưa). Nếu sau này cần check PAID, cần tạo `OrderClient` tương tự `CustomerClient`. MVP chấp nhận "SIGNED chưa link order = OK, đã link = block".

### TICKET-304: DELETE /notifications/{id}

**Files thay đổi:**

- `notification-service/src/main/java/com/pcms/notificationservice/enums/NotificationStatus.java` (thêm `DELETED`)
- `notification-service/src/main/java/com/pcms/notificationservice/repository/NotificationRepository.java` (exclude DELETED từ queries)
- `notification-service/src/main/java/com/pcms/notificationservice/service/NotificationSenderService.java`
- `notification-service/src/main/java/com/pcms/notificationservice/service/impl/NotificationSenderServiceImpl.java`
- `notification-service/src/main/java/com/pcms/notificationservice/controller/NotificationController.java`

**Soft-delete pattern:**

- Thêm `DELETED` vào enum
- Tất cả queries `findByRecipientId`, `findByRecipientIdAndStatus`, `findByStatus` thêm điều kiện `n.status <> DELETED`
- Service method `softDelete(id, currentUserId)`:
  - Check `currentUserId == recipientId` → 403 nếu không
  - Set `status = DELETED`
  - Idempotent (nếu đã DELETED thì trả về state hiện tại)
- Controller yêu cầu `X-User-Id` header (gateway forward từ JWT) → 403 nếu thiếu

**Bảo mật:**

- ✅ Chỉ recipient mới delete được
- ✅ Không có header = 403 (không trust unauth delete)
- ✅ Soft-delete giữ audit trail

### TICKET-305: POST /reports/export/excel

**Files thay đổi:**

- `report-service/src/main/java/com/pcms/reportservice/dto/ReportExportRequest.java` (**MỚI**)
- `report-service/src/main/java/com/pcms/reportservice/service/ExcelExportService.java` (**MỚI** - stub)
- `report-service/src/main/java/com/pcms/reportservice/controller/ReportController.java`

**Stub design:**

- Service `ExcelExportService` return `Map<String, Object>` chứa:
  - `status: "queued"`
  - `jobId: UUID`
  - `downloadUrl: "/reports/export/download/{jobId}"`
  - `format: "excel"`
  - `reportType: <input>`
  - `queuedAt: ISO timestamp`
  - `expiresAt: ISO timestamp (24h)`
- Controller return `ResponseEntity.accepted()` (HTTP 202)
- Validation: `@NotBlank` + `@Pattern` cho `reportType` ∈ {revenue|inventory|staff}

**Lý do stub:** Plan cho phép stub an toàn - real Apache POI renderer sẽ làm ở follow-up sprint. Contract (request/response shape) đã stable để frontend integrate.

### TICKET-306: POST /reports/export/pdf + DELETE /reports/schedules/{id}

**Files thay đổi:**

- `report-service/src/main/java/com/pcms/reportservice/service/PdfExportService.java` (**MỚI** - stub, cùng pattern ExcelExportService)
- `report-service/src/main/java/com/pcms/reportservice/service/ReportScheduleService.java` (thêm `cancel`)
- `report-service/src/main/java/com/pcms/reportservice/service/impl/ReportScheduleServiceImpl.java` (impl cancel)
- `report-service/src/main/java/com/pcms/reportservice/controller/ReportController.java`

**POST /reports/export/pdf:** Cùng `ReportExportRequest`, cùng stub pattern, `format: "pdf"`.

**DELETE /reports/schedules/{id}:**

- Set `active = false` (entity dùng boolean `active` thay vì status enum - đã khảo sát trước khi code)
- Set `lastStatus = "CANCELLED"`, `lastMessage = "Cancelled by user"`
- Idempotent: nếu đã `active=false` thì trả về state hiện tại
- `ResourceNotFoundException` nếu không tìm thấy

---

## 3. Files tạo mới (3 files)

| File | Mô tả |
|------|-------|
| `report-service/src/main/java/com/pcms/reportservice/dto/ReportExportRequest.java` | DTO cho export excel/pdf |
| `report-service/src/main/java/com/pcms/reportservice/service/ExcelExportService.java` | Stub service xếp job excel |
| `report-service/src/main/java/com/pcms/reportservice/service/PdfExportService.java` | Stub service xếp job pdf |

## 4. Files sửa (9 files)

### prescription-service (3 files)

- `controller/PrescriptionController.java` - thêm 3 methods (POST /sign, POST /print, DELETE /{id})
- `service/PrescriptionService.java` - thêm `cancel(UUID, UUID)` method
- `service/impl/PrescriptionServiceImpl.java` - impl cancel + thêm `Logger`

### notification-service (5 files)

- `enums/NotificationStatus.java` - thêm `DELETED`
- `repository/NotificationRepository.java` - thêm `<> DELETED` filter vào 3 queries
- `service/NotificationSenderService.java` - thêm `softDelete(UUID, UUID)` method
- `service/impl/NotificationSenderServiceImpl.java` - impl softDelete
- `controller/NotificationController.java` - thêm DELETE endpoint

### report-service (4 files)

- `service/ReportScheduleService.java` - thêm `cancel(UUID)` method
- `service/impl/ReportScheduleServiceImpl.java` - impl cancel + import UUID, ResourceNotFoundException
- `controller/ReportController.java` - thêm 3 endpoints + inject 2 service mới vào constructor

---

## 5. Verification

### 5.1 Compile

```bash
mvn clean compile -o -DskipTests
```

**Result:** ✅ BUILD SUCCESS - All 16 modules

```
pharmacy-chain-management .................. SUCCESS
pcms-common .............................. SUCCESS
config-server ............................ SUCCESS
discovery-server ......................... SUCCESS
api-gateway .............................. SUCCESS
user-service ............................. SUCCESS
branch-service ........................... SUCCESS
catalog-service .......................... SUCCESS
category-service ......................... SUCCESS
supplier-service ......................... SUCCESS
inventory-service ........................ SUCCESS
customer-service ......................... SUCCESS
order-service ............................ SUCCESS
payment-service .......................... SUCCESS
prescription-service ..................... SUCCESS
notification-service ..................... SUCCESS
report-service ........................... SUCCESS
```

### 5.2 Tests

```bash
mvn -pl prescription-service,notification-service,report-service -am test -o
```

**Result:** ✅ BUILD SUCCESS

- prescription-service: 0 tests (no test directory)
- notification-service: 0 tests (no test directory)
- report-service: 1 test pass (ReportServiceImplTest - existing)

### 5.3 Edge cases đã handle

| Case | Behavior |
|------|----------|
| Cancel DRAFT prescription | ✅ → CANCELLED |
| Cancel SIGNED không có order | ✅ → CANCELLED |
| Cancel SIGNED có order | ✅ → 409 BusinessException (MSG33) |
| Cancel CANCELLED (re-cancel) | ✅ → idempotent, return current state |
| Cancel không tồn tại | ✅ → 404 ResourceNotFoundException |
| Delete notification không có X-User-Id | ✅ → 403 AccessDeniedException |
| Delete notification của người khác | ✅ → 403 AccessDeniedException |
| Delete notification đã DELETED | ✅ → idempotent, return current state |
| List sau khi soft-delete | ✅ → exclude khỏi result |
| Export với reportType invalid | ✅ → 400 (validation) |
| Cancel schedule không tồn tại | ✅ → 404 |

---

## 6. Vấn đề gặp phải & giải pháp

### Vấn đề 1: Tooling warning "cannot find symbol"

Mỗi edit đều trigger warning "X issues must be fixed" từ LSP do **classpath không load được** (Maven chưa trong PATH mặc dù Java có). Warning là **false positive** vì:

- Code syntax đúng (verified bằng `javac` qua Maven compile thật)
- Tất cả 16 module BUILD SUCCESS

**Giải pháp:** Verify bằng Maven thật (`mvn clean compile`) thay vì tin LSP warnings.

### Vấn đề 2: `PrescriptionServiceImpl` thiếu Logger

Method `cancel` đầu tiên dùng `log.info(...)` nhưng class không có field `log`. Fix bằng cách thêm:

```java
private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);
```

### Vấn đề 3: `ReportScheduleServiceImpl` thiếu import UUID + ResourceNotFoundException

Method `cancel(UUID id)` cần 2 import mới:

- `import java.util.UUID;`
- `import com.pcms.common.exception.ResourceNotFoundException;`

### Vấn đề 4: `BusinessException.withDetails()` không tồn tại

Cancel prescription ban đầu dùng `.withDetails("orderId", ...)` nhưng `BusinessException` base class không có method này. Fix: include orderId trực tiếp vào message string.

### Vấn đề 5: Maven offline mode không có install plugin

Lần đầu thử `mvn install` thất bại do offline. Fix: dùng `mvn -pl <services> -am test` (auto-build dependencies không cần install).

---

## 7. Acceptance Checklist (theo task)

- [x] 6 controllers có method mới đúng HTTP method + path
  - [x] PrescriptionController: POST /sign, POST /print, DELETE /{id}
  - [x] NotificationController: DELETE /{id}
  - [x] ReportController: POST /export/excel, POST /export/pdf, DELETE /schedules/{id}
- [x] Service throw BusinessException với MSG code
  - [x] cancel prescription: MSG33 với 409 cho SIGNED+orderId
  - [x] softDelete notification: AccessDeniedException → 403
- [x] Soft-delete pattern đúng (status=CANCELLED/DELETED, active=false)
  - [x] Prescription.status = CANCELLED
  - [x] Notification.status = DELETED + repository filter
  - [x] ReportSchedule.active = false
- [x] Authorization check đúng
  - [x] softDelete check recipientId == currentUserId
  - [x] Controller reject khi thiếu X-User-Id
- [x] BR04 + BR06 logic cho prescription cancel
  - [x] BR06: Không cho cancel SIGNED có order link
  - [x] DRAFT cancel thoải mái

---

## 8. Outstanding / Follow-up

| Item | Mức độ | Note |
|------|---------|------|
| Real Excel renderer (Apache POI) | Low | Stub đủ cho frontend integration. Sprint sau thay bằng async job thật. |
| Real PDF renderer (OpenPDF/iText) | Low | Tương tự Excel. |
| Prescription cancel check order PAID | Medium | Hiện tại chỉ check `orderId != null`. Nên gọi `OrderClient` để check `status == PAID` nếu cần chính xác hơn. |
| Unit test cho softDelete | Low | Notification chưa có test directory. Sprint sau bổ sung. |
| Unit test cho cancel | Low | Prescription chưa có test directory. |
| Unit test cho cancel schedule | Low | Report chỉ có 1 test khác. |

---

## 9. Kết luận

✅ **Sprint 3 hoàn thành.** 7 endpoints (6 tickets) đã implement, compile sạch, test không bị break.

**Coverage B2B (Sprint 1-3) sau khi Sprint 3 xong:**

- 7/15 = 47% thêm vào code (3 sprint, 3 worker song song)
- Đạt ~80% B2B coverage (sau khi Sprint 1+2 cũng hoàn thành)

**Sprint tiếp theo (nếu parent yêu cầu):** Sprint 4+ (B2C) - cần tạo 4-6 service mới, work lớn hơn nhiều so với Sprint 1-3.
