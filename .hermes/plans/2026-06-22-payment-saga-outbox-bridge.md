# Plan: PaymentService → Order-Saga Outbox Bridge (Hoàn thiện Task 16 của saga plan)

> **Context:** Sau khi thực hiện plan `2026-06-21-implement-orchestration-saga.md` (commit 74bfbc3), 18/19 tasks đã hoàn thành. Task 16 còn lại — chuyển `PaymentServiceImpl.create()` từ gọi trực tiếp `orderClient.markOrderPaid()` sang ghi `OutboxEvent` — chưa làm vì `payment-service` chưa có OutboxEvent infrastructure.

**Goal:** Biến `payment-service` thành một outbox producer giống `order-service`: ghi `OutboxEvent` cùng transaction với `Payment`, để `OutboxPublisher` của payment-service retry delivery đến order-service cho đến khi thành công — đảm bảo **at-least-once delivery** + **atomic với payment state**.

**Architecture:** Tạo song song cấu trúc OutboxEvent + OutboxEventRepository + OutboxPublisher trong payment-service (mirror từ order-service). `PaymentServiceImpl.create()` ghi `OutboxEvent(targetService="order-service", endpoint="/orders/{id}/pay", payload={"actorId":"..."})` thay cho call trực tiếp. `OutboxPublisher` mới sẽ poll mỗi 30s và gọi `OrderClient.markOrderPaid(...)` từ payload.

**Tech Stack:** Java 21, Spring Boot 4.0.7, Spring Data JPA, Spring Scheduling, OpenFeign.

---

## Quyết định thiết kế

### Tại sao KHÔNG dùng chung OutboxEvent từ pcms-common?

- Order-service và payment-service có 2 DB riêng (`pcms_order` vs `pcms_payment`). JPA entity phải thuộc về một service cụ thể để ddl-auto=create tạo table.
- Order OutboxEvent có FK ngầm tới Saga (qua `eventType="SagaInstance"`); payment không cần Saga.
- Sự tách biệt đảm bảo mỗi service tự quản transactional outbox của mình.

### Endpoint target: `/orders/{id}/pay` (giữ nguyên)

Plan gốc (Task 16) nói `/orders/{id}/saga/start` nhưng endpoint đó không tồn tại. OrderController chỉ có `PUT /orders/{id}/pay` (line 76) gọi `orderService.markAsPaid(id, actorId)` — và `markAsPaid` (sau Task 13) đã trigger `sagaOrchestrator.startOrderFulfillment(...)`. Vậy nên gọi tới `/orders/{id}/pay` chính là trigger saga.

### Payload format

```json
{"actorId": "uuid"}
```
Đơn giản — `OrderClient.markOrderPaid(UUID id, UUID actorId)` chỉ cần 2 param, không cần body. Lưu actorId trong payload để OutboxPublisher lấy ra khi gọi.

---

## File Structure

### Files to Create (in payment-service)
- `payment-service/src/main/java/com/pcms/paymentservice/entity/OutboxEvent.java` — copy + adapt từ order-service
- `payment-service/src/main/java/com/pcms/paymentservice/repository/OutboxEventRepository.java`
- `payment-service/src/main/java/com/pcms/paymentservice/scheduler/PaymentOutboxPublisher.java` — chỉ dispatch targetService="order-service"

### Files to Modify
- `payment-service/src/main/java/com/pcms/paymentservice/service/impl/PaymentServiceImpl.java` — ghi OutboxEvent thay cho call trực tiếp
- `payment-service/src/main/resources/application.yml` (hoặc `config-server/src/main/resources/config/payment-service.yml`) — đảm bảo `@EnableScheduling` + `ddl-auto=update` để tạo bảng `outbox_events`

---

## Task 1: Tạo OutboxEvent entity trong payment-service

**Files:**
- Create: `payment-service/src/main/java/com/pcms/paymentservice/entity/OutboxEvent.java`

**Step 1: Copy cấu trúc từ order-service**

Tạo file với nội dung mirror từ `order-service/.../entity/OutboxEvent.java` (197 dòng), đổi `package` thành `com.pcms.paymentservice.entity`. Entity này KHÔNG cần `aggregate_type` chỉ rõ — giữ nguyên cấu trúc để consistent.

```java
package com.pcms.paymentservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OutboxEvent {
    public enum Status { PENDING, SENT, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "target_service", nullable = false, length = 50)
    private String targetService;

    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public OutboxEvent() {}

    public OutboxEvent(String aggregateType, UUID aggregateId, String eventType,
            String targetService, String endpoint, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.targetService = targetService;
        this.endpoint = endpoint;
        this.payload = payload;
    }
    // ... getters + setters y hệt order-service
}
```

**Step 2: Verify package & import đúng**

Run: `head -10 payment-service/src/main/java/com/pcms/paymentservice/entity/OutboxEvent.java`
Expected: `package com.pcms.paymentservice.entity;`

---

## Task 2: Tạo OutboxEventRepository

**Files:**
- Create: `payment-service/src/main/java/com/pcms/paymentservice/repository/OutboxEventRepository.java`

```java
package com.pcms.paymentservice.repository;

import com.pcms.paymentservice.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
            SELECT e FROM OutboxEvent e
            WHERE e.status = :status
              AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
            ORDER BY e.createdAt ASC
            """)
    List<OutboxEvent> findReadyToPublish(OutboxEvent.Status status, LocalDateTime now, Pageable pageable);
}
```

---

## Task 3: Tạo PaymentOutboxPublisher

**Files:**
- Create: `payment-service/src/main/java/com/pcms/paymentservice/scheduler/PaymentOutboxPublisher.java`

Publisher này chỉ cần dispatch tới order-service. Đơn giản hơn OutboxPublisher của order-service.

```java
package com.pcms.paymentservice.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.paymentservice.client.OrderClient;
import com.pcms.paymentservice.entity.OutboxEvent;
import com.pcms.paymentservice.repository.OutboxEventRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Outbox publisher for payment-service.
 *
 * <p>Polls every 30s for PENDING outbox events targeting order-service,
 * dispatches via Feign, marks SENT on success, retries on failure with
 * exponential backoff up to 5 attempts before moving to FAILED.
 */
@Component
public class PaymentOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxRepo;
    private final OrderClient orderClient;
    private final ObjectMapper objectMapper;

    public PaymentOutboxPublisher(OutboxEventRepository outboxRepo,
            OrderClient orderClient, ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.orderClient = orderClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 30_000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepo.findReadyToPublish(
                OutboxEvent.Status.PENDING, LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE));
        if (pending.isEmpty()) {
            return;
        }
        log.info("[payment-outbox] Publishing {} pending events", pending.size());
        for (OutboxEvent event : pending) {
            try {
                dispatch(event);
                event.setStatus(OutboxEvent.Status.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxRepo.save(event);
                log.info("[payment-outbox] Event {} ({}) SENT", event.getId(), event.getEventType());
            } catch (FeignException | InvalidOperationException e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                    log.error("[payment-outbox] Event {} failed after {} retries: {}",
                            event.getId(), MAX_RETRIES, e.getMessage());
                } else {
                    event.setNextAttemptAt(LocalDateTime.now().plusSeconds(
                            calculateBackoffSeconds(event.getRetryCount())));
                    log.warn("[payment-outbox] Event {} failed (retry {}/{}): {}",
                            event.getId(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
                }
                outboxRepo.save(event);
            }
        }
    }

    private void dispatch(OutboxEvent event) {
        if (!"order-service".equals(event.getTargetService())) {
            throw new InvalidOperationException(
                    "Unknown payment outbox target: " + event.getTargetService(),
                    "Outbox payment chưa hỗ trợ target: " + event.getTargetService());
        }
        UUID orderId = event.getAggregateId();
        Map<String, Object> payload = parsePayload(event);
        UUID actorId = payload.get("actorId") != null
                ? UUID.fromString(payload.get("actorId").toString())
                : null;
        orderClient.markOrderPaid(orderId, actorId);
    }

    private Map<String, Object> parsePayload(OutboxEvent event) {
        try {
            return objectMapper.readValue(event.getPayload(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new InvalidOperationException(
                    "Invalid outbox payload for event " + event.getId(),
                    "Payload outbox payment không hợp lệ");
        }
    }

    private long calculateBackoffSeconds(int retryCount) {
        return switch (retryCount) {
            case 1 -> 1L;
            case 2 -> 5L;
            case 3 -> 30L;
            case 4 -> 120L;
            default -> 600L;
        };
    }
}
```

---

## Task 4: Sửa PaymentServiceImpl để ghi OutboxEvent

**Files:**
- Modify: `payment-service/src/main/java/com/pcms/paymentservice/service/impl/PaymentServiceImpl.java`

**Step 1: Thêm imports + dependency**

Sau các import hiện tại, thêm:
```java
import com.pcms.paymentservice.entity.OutboxEvent;
import com.pcms.paymentservice.repository.OutboxEventRepository;
```

Trong field declarations (sau line 50 `private final OrderClient orderClient;`), thêm:
```java
private final OutboxEventRepository outboxEventRepository;
```

Trong constructor, thêm parameter:
```java
public PaymentServiceImpl(PaymentRepository paymentRepository,
        OrderClient orderClient,
        OutboxEventRepository outboxEventRepository) {
    this.paymentRepository = paymentRepository;
    this.orderClient = orderClient;
    this.outboxEventRepository = outboxEventRepository;
}
```

**Step 2: Thay block gọi orderClient (line 130-135)**

Replace:
```java
        // Notify order-service that this order is paid (consumes stock, awards points)
        try {
            orderClient.markOrderPaid(request.orderId(), request.staffId());
        } catch (Exception e) {
            log.warn("Failed to notify order-service for order {}: {}", request.orderId(), e.getMessage());
        }
```

Bằng:
```java
        // B-17: Emit OutboxEvent instead of synchronous orderClient call.
        // Publisher will retry until SENT, triggering saga in order-service.
        outboxEventRepository.save(new OutboxEvent(
                "Payment", saved.getId(), "PAYMENT_COMPLETED",
                "order-service",
                "/orders/" + request.orderId() + "/pay",
                String.format("{\"actorId\":\"%s\"}", request.staffId())));
        log.info("[payment-outbox] Emitted PAYMENT_COMPLETED for order {}", request.orderId());
```

Lưu ý: KHÔNG try/catch — nếu ghi OutboxEvent fail thì cả transaction rollback, payment không được ghi nhận. Đây là tính nguyên tử mong muốn.

---

## Task 5: Đảm bảo @EnableScheduling + Auditing

**Files:**
- Check: `payment-service/src/main/java/com/pcms/paymentservice/PaymentServiceApplication.java`

**Step 1: Verify @EnableScheduling đã có**

Run: `grep -n "@EnableScheduling\|@EnableJpaAuditing" payment-service/src/main/java/com/pcms/paymentservice/PaymentServiceApplication.java`

Expected (nếu có): cả 2 annotation đều có. Nếu thiếu `@EnableJpaAuditing`, thêm vào vì `OutboxEvent.createdAt` dùng `@CreatedDate`.

**Step 2: Verify config ddl-auto**

Run: `grep -n "ddl-auto" config-server/src/main/resources/config/payment-service.yml`

Nếu `ddl-auto: create` → đổi thành `update` để không xóa data khi restart. Nếu giữ `create`, ddl-auto vẫn tạo bảng `outbox_events` mới mỗi lần restart (OK cho dev nhưng xóa data — không nên).

---

## Task 6: Build verify

**Files:** None (testing)

**Step 1: Compile payment-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn compile -pl payment-service 2>&1 | tail -5`

Expected: `BUILD SUCCESS`

**Step 2: Package payment-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests -pl payment-service 2>&1 | tail -3`

Expected: `BUILD SUCCESS`

---

## Task 7: Restart và test end-to-end

**Files:** None (testing)

**Step 1: Restart payment-service**

Run:
```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
netstat -ano | grep ":8089" | head -1 | awk '{print $5}' | xargs -I {} cmd //c "taskkill /F /PID {}" 2>&1 | head -1
sleep 5
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" \
    -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar \
    > logs/payment-service.log 2>&1 &
sleep 30
netstat -an | grep ":8089" | grep LISTENING
```

Expected: Port 8089 listening.

**Step 2: Verify bảng outbox_events tồn tại**

Run: 
```bash
mysql -u pcms_user -ppcms_pass -h 127.0.0.1 -P 3307 pcms_payment \
    -e "DESCRIBE outbox_events;" 2>&1 | head -20
```

Expected: Bảng có các cột `id`, `aggregate_type`, `aggregate_id`, `event_type`, `target_service`, `endpoint`, `payload`, `status`, `retry_count`, `last_error`, `next_attempt_at`, `created_at`, `sent_at`.

**Step 3: Test flow payment → saga**

Get token + order ID:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@pcms.vn","password":"admin123"}' \
    | grep -oP '"accessToken":"[^"]+' | cut -d'"' -f4)
ORDER_ID=$(curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/v1/orders \
    | grep -oP '"id":"[a-f0-9-]+"' | head -1 | cut -d'"' -f4)
echo "Order: $ORDER_ID"
```

Tạo payment:
```bash
curl -s -X POST -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"orderId\":\"$ORDER_ID\",\"paymentMethod\":\"CASH\",\"amount\":100000,\"tenderedAmount\":100000,\"staffId\":\"00000000-0000-0000-0000-000000000001\"}" \
    http://localhost:8080/api/v1/payments
```

Wait 35s for outbox publisher, check log:
```bash
grep "PAYMENT_COMPLETED\|SENT" logs/payment-service.log | tail -5
```

Expected: Log cho thấy `[payment-outbox] Event xxx (PAYMENT_COMPLETED) SENT`.

**Step 4: Verify saga được tạo trong order-service**

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/v1/admin/saga/by-aggregate/Order/$ORDER_ID \
    | head -c 400
```

Expected: Saga tồn tại với status IN_PROGRESS hoặc COMPLETED, có 3 forward steps (STOCK_CONSUMED, POINTS_AWARDED, ORDER_PAID_NOTIFICATION).

---

## Task 8: Commit

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add \
    payment-service/src/main/java/com/pcms/paymentservice/entity/OutboxEvent.java \
    payment-service/src/main/java/com/pcms/paymentservice/repository/OutboxEventRepository.java \
    payment-service/src/main/java/com/pcms/paymentservice/scheduler/PaymentOutboxPublisher.java \
    payment-service/src/main/java/com/pcms/paymentservice/service/impl/PaymentServiceImpl.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" commit -m "feat(payment): emit outbox event for saga bridge (B-17)

- Add OutboxEvent entity + repository mirroring order-service
- Add PaymentOutboxPublisher to retry delivery to order-service
- Replace synchronous orderClient.markOrderPaid with outbox event
- Ensures atomicity between payment record and saga notification
- Supports exponential backoff (1s/5s/30s/2m/10m) up to 5 retries"
```

---

## Risks & Trade-offs

1. **Saga chậm hơn ~30s** so với gọi trực tiếp vì outbox publisher poll mỗi 30s. Có thể giảm xuống 5s nếu cần real-time hơn.
2. **Bảng `outbox_events` tăng trưởng** vì không có cleanup job. Có thể thêm cron xóa SENT events >7 ngày sau (YAGNI cho lúc này).
3. **Không có DeadLetter table** trong payment-service như order-service. Nếu cần nghiêm ngặt, mirror `DeadLetterEvent` entity từ order-service.
4. **Frontend hiện đang gọi `/orders/{id}/pay` trực tiếp** thay vì qua payment flow. Nếu frontend chuyển sang dùng payment-service để trigger saga, flow mới sẽ có độ trễ ~30s. Hiện tại 2 đường song song tồn tại — không sao, idempotent nhờ SagaInstance.findByAggregateTypeAndAggregateId check.

## Open Questions (cần user xác nhận)

- **Q1:** Có muốn thêm DeadLetter table cho payment-service không? (YAGNI nếu chưa cần)
- **Q2:** Publisher interval 30s có OK không, hay muốn 5s để phản hồi nhanh hơn?
- **Q3:** Plan có cần test thêm 1 case manual order-cancel để verify compensation không, hay đã đủ từ commit trước?

---

## Verification Checklist

- [ ] payment-service compile sạch
- [ ] Bảng `outbox_events` tự tạo trong DB `pcms_payment`
- [ ] Payment POST → ghi 1 row vào `outbox_events` với `status=PENDING`
- [ ] Sau ~30s → `status=SENT`, `sent_at` có giá trị
- [ ] Order-service có saga tương ứng với order vừa pay
- [ ] Saga có 3 forward steps
- [ ] Nếu order-service tạm down → outbox retry với backoff, không mất event

---

## Estimated Effort

| Task | Loại | Dòng (ước lượng) |
|------|------|------------------|
| 1. OutboxEvent entity | New | ~200 (mirror) |
| 2. OutboxEventRepository | New | ~25 |
| 3. PaymentOutboxPublisher | New | ~130 |
| 4. Sửa PaymentServiceImpl | Modify | ~10 dòng đổi |
| 5. EnableJpaAuditing check | Verify | 0-2 |
| 6. Build verify | Test | 0 |
| 7. End-to-end test | Test | 0 |
| 8. Commit | Git | 0 |
| **TOTAL** | | **~370 lines** |