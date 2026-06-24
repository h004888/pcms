# Agent-3 Summary: B2B Service UAT Documents (09-12)

**Date:** 2026-06-19
**Owner:** Agent-3 (subagent)
**Scope:** Payment, Prescription, Notification, Report services

---

## Files Created

| File                              | Endpoints | Bytes    | Lines  | Test Cases | Status |
|-----------------------------------|----------:|---------:|-------:|-----------:|--------|
| `docs/uat/09-PAYMENT.md`          |         9 |  71,581 |  1,009 |    42      | ✓ Done |
| `docs/uat/10-PRESCRIPTION.md`     |         9 |  51,340 |    769 |    35      | ✓ Done |
| `docs/uat/11-NOTIFICATION.md`     |        15 |  71,392 |  1,035 |    52      | ✓ Done |
| `docs/uat/12-REPORT.md`           |    11(+3) |  68,950 |  1,021 |    53      | ✓ Done |
| **TOTAL**                         |   **44**  | **263,263** | **3,834** | **182** | ✓ |

> The Report file documents 14 endpoints (11 main + 3 alias: POST /export/excel, POST /export/pdf, DELETE /schedules/{id}) per the user's bonus note.

---

## Endpoint Coverage Matrix

### 09-PAYMENT.md (9/9 = 100%)

| # | Method | Path                                          | Tested |
|---|--------|-----------------------------------------------|--------|
| 1 | GET    | `/api/v1/payments`                            | ✓ List, empty page, invalid page |
| 2 | GET    | `/api/v1/payments/{id}`                       | ✓ Happy, 404, malformed UUID |
| 3 | GET    | `/api/v1/payments/invoice/{invoiceNumber}`    | ✓ Happy, 404 |
| 4 | GET    | `/api/v1/payments/order/{orderId}`            | ✓ Happy, 404 |
| 5 | POST   | `/api/v1/payments`                            | ✓ CASH, CARD, QR, LOYALTY_POINTS, scheduled, idempotent, validation×3 |
| 6 | POST   | `/api/v1/payments/{id}/refund`                | ✓ Full, partial, empty body, exceeds paid, =0, double refund, over-length reason |
| 7 | PUT    | `/api/v1/payments/{id}/refund` (legacy)       | ✓ Soft cancel PENDING, negative SUCCESS |
| 8 | GET    | `/api/v1/payments/{id}/refund-history`        | ✓ Single, multi-entry, empty, 404 |
| 9 | POST   | `/api/v1/webhooks/payment-gateway`            | ✓ payment.success, payment.failed, idempotent replay, missing sig, bad HMAC, bad JSON, missing transactionRef, unknown TX, unknown eventType, cross-service verify order paid, TICKET-206 alias |

### 10-PRESCRIPTION.md (9/9 = 100% + bonus)

| # | Method | Path                                  | Tested |
|---|--------|---------------------------------------|--------|
| 1 | GET    | `/api/v1/prescriptions`               | ✓ List, empty, invalid page |
| 2 | GET    | `/api/v1/prescriptions/{id}`          | ✓ Happy, 404, malformed UUID |
| 3 | GET    | `/api/v1/prescriptions/code/{code}`   | ✓ Happy, 404 |
| 4 | POST   | `/api/v1/prescriptions`               | ✓ Happy, code increment, missing patientId, empty items, empty diagnosis, durationDays<1 |
| 5 | POST   | `/api/v1/prescriptions/draft`         | ✓ Happy, saveAsDraft=true |
| 6 | PUT    | `/api/v1/prescriptions/{id}`          | ✓ Happy, SIGNED rejected, empty items |
| 7 | PUT    | `/api/v1/prescriptions/{id}/sign`     | ✓ Happy, TICKET-301 POST alias, double-sign, sign unknown, signature determinism |
| 8 | POST   | `/api/v1/prescriptions/{id}/link-order`| ✓ Happy, draft link, missing orderId, cancelled link |
| 9 | GET    | `/api/v1/prescriptions/{id}/print`    | ✓ Happy, TICKET-302 POST alias, DRAFT print, 404 |
| Bonus | DELETE | `/api/v1/prescriptions/{id}` (TICKET-303) | ✓ DRAFT, SIGNED-unlinked, SIGNED-linked |

### 11-NOTIFICATION.md (15/15 = 100%)

**NotificationController (11)**:

| # | Method | Path                                          | Tested |
|---|--------|-----------------------------------------------|--------|
| 1 | GET    | `/api/v1/notifications?recipientId=...`       | ✓ Happy, status=read, status=unread, missing param, empty |
| 2 | GET    | `/api/v1/notifications/unread`                | ✓ Happy, pagination |
| 3 | GET    | `/api/v1/notifications/{id}`                  | ✓ Happy, 404 |
| 4 | POST   | `/api/v1/notifications`                       | ✓ IN_APP, EMAIL, SMS, scheduled, missing fields×3, bad enum |
| 5 | POST   | `/api/v1/notifications/bulk`                  | ✓ 3 users, empty recipients, single recipient |
| 6 | POST   | `/api/v1/notifications/broadcast`             | ✓ Users, by role, validation×2 |
| 7 | POST   | `/api/v1/notifications/compose`               | ✓ Template, unknown template |
| 8 | POST   | `/api/v1/notifications/{id}/retry`            | ✓ FAILED→SENT, retry SENT, 404 |
| 9 | PUT    | `/api/v1/notifications/{id}/read`             | ✓ Happy, idempotent double-read |
| 10| PUT    | `/api/v1/notifications/read-all`              | ✓ Happy, empty user, missing param |
| 11| DELETE | `/api/v1/notifications/{id}` (TICKET-304)    | ✓ Soft delete, anonymous 403, cross-user 403 |

**NotificationTemplateController (2)**:

| # | Method | Path                                          | Tested |
|---|--------|-----------------------------------------------|--------|
| 12| GET    | `/api/v1/notifications/templates`             | ✓ Active, channel filter, active=false filter |
| 13| POST   | `/api/v1/notifications/templates`             | ✓ Happy, duplicate, missing fields, inactive |

**OutboxConsumerController (3)**:

| # | Method | Path                                          | Tested |
|---|--------|-----------------------------------------------|--------|
| 14| POST   | `/api/v1/notifications/orders/paid`           | ✓ Happy, idempotent, missing orderId |
| 15| POST   | `/api/v1/notifications/inventory/low-stock`   | ✓ Happy, missing qty, idempotent |
| 16| POST   | `/api/v1/notifications/inventory/expiry`      | ✓ Happy, missing batchNo, idempotent |

### 12-REPORT.md (11 + 3 alias = 14 documented)

| # | Method | Path                                              | Tested |
|---|--------|---------------------------------------------------|--------|
| 1 | GET    | `/api/v1/reports/revenue?from=...&to=...&groupBy=day` | ✓ Daily, weekly, monthly, branch filter, missing from, bad date, range reversed, bad groupBy |
| 2 | POST   | `/api/v1/reports/revenue`                         | ✓ Happy, body-branchId override, missing fromDate |
| 3 | GET    | `/api/v1/reports/inventory`                       | ✓ HQ snapshot, branch filter, empty branch |
| 4 | POST   | `/api/v1/reports/inventory`                       | ✓ With date filter, no dates |
| 5 | GET    | `/api/v1/reports/staff`                           | ✓ Happy, branch filter, missing fromDate, empty range |
| 6 | POST   | `/api/v1/reports/staff`                           | ✓ Happy, missing fields |
| 7 | GET    | `/api/v1/reports/realtime/stats`                  | ✓ Happy KPIs, branch filter |
| 8 | GET    | `/api/v1/reports/realtime/recent-orders`          | ✓ Default limit, custom limit, huge limit, negative limit |
| 9 | GET    | `/api/v1/reports/export?type=...&format=...`      | ✓ Revenue excel, revenue pdf, inventory excel, staff pdf, bad type, bad format, missing dates, xlsx alias |
| 10| POST   | `/api/v1/reports/schedule`                        | ✓ Weekly, daily, no creator, empty email, bad cron, type too long |
| 11| GET    | `/api/v1/reports/schedules`                       | ✓ List all, empty |
| 12| POST   | `/api/v1/reports/export/excel` (TICKET-305)      | ✓ Queue job, bad type, missing type, default 7-day window |
| 13| POST   | `/api/v1/reports/export/pdf` (TICKET-306)        | ✓ Queue job, missing type |
| 14| DELETE | `/api/v1/reports/schedules/{id}` (TICKET-306)   | ✓ Cancel, idempotent cancel, 404 |

---

## Format Compliance Checklist

| Requirement                                | 09 PAYMENT | 10 PRESCRIPTION | 11 NOTIFICATION | 12 REPORT |
|--------------------------------------------|:----------:|:---------------:|:---------------:|:---------:|
| Header with Title/Version/Date             | ✓          | ✓               | ✓               | ✓         |
| Service info box ASCII                     | ✓          | ✓               | ✓               | ✓         |
| Endpoint summary table                     | ✓          | ✓               | ✓               | ✓         |
| Prerequisites section                      | ✓          | ✓               | ✓               | ✓         |
| Test data seeding                          | ✓          | ✓               | ✓               | ✓         |
| Per-endpoint section                       | ✓          | ✓               | ✓               | ✓         |
| ASCII box format (per spec)                | ✓          | ✓               | ✓               | ✓         |
| Happy path + negative path per endpoint    | ✓          | ✓               | ✓               | ✓         |
| Edge cases for create/update/delete        | ✓          | ✓               | ✓               | ✓         |
| Webhook HMAC signature test (Python/Bash)  | ✓          | n/a             | n/a             | n/a       |
| Outbox consumer marked INTERNAL            | n/a        | n/a             | ✓               | n/a       |
| Idempotency tests (X-Outbox-Event-Id)      | n/a        | n/a             | ✓               | n/a       |
| Capture variables section                  | ✓          | ✓               | ✓               | ✓         |
| Post-test verification block               | ✓          | ✓               | ✓               | ✓         |
| Bug severity quick reference               | ✓          | n/a             | n/a             | ✓         |
| End-of-file marker                         | ✓          | ✓               | ✓               | ✓         |
| Indicator legend                           | ✓          | ✓               | ✓               | ✓         |
| Authorization matrix                       | ✓          | ✓               | ✓               | ✓         |

---

## Special Features Implemented

### 09-PAYMENT.md

- **HMAC-SHA256 signature computation** with both `openssl` bash one-liner and Python `hmac` snippet (using the dev webhook secret)
- **11-step webhook testing** including: success/failed, replay idempotency, missing sig, bad HMAC, malformed JSON, missing field, unknown transactionRef, unknown eventType, cross-service order-paid verification, TICKET-206 alias
- **Test 5.5.8** idempotent payment replay (CR-05 / Idempotency-Key)
- **4 payment methods** tested: CASH, CARD, QR, LOYALTY_POINTS
- **Full/partial refund** with business rule validation (refund > paid, double refund, etc.)
- **Legacy PUT refund alias** documented (TICKET-205)

### 10-PRESCRIPTION.md

- **Code auto-generation** verification: `RX-{yyyy}{####}` format with year + sequence
- **Signature determinism** test (test 5.7.5) verifying SHA-256 hash is non-predictable but consistent
- **TICKET-301/302/303** aliases tested: POST /sign, POST /print, DELETE /cancel
- **State machine** thoroughly tested: DRAFT → SIGNED → CANCELLED with business rule enforcement

### 11-NOTIFICATION.md

- **3 controllers** documented with separate sections
- **Outbox consumer** clearly marked as INTERNAL (called by order/inventory services)
- **Idempotency via X-Outbox-Event-Id** for all 3 outbox endpoints
- **TICKET-304** privacy: anonymous deletes rejected with 403; cross-user deletes rejected
- **Broadcast/composition** workflows with role/branch audience resolution
- **5 channel/status enum** values exercised across tests

### 12-REPORT.md

- **All GET/POST pairs** tested for revenue, inventory, staff (consistent body/query parity)
- **Date range validation** including reversed range, malformed dates
- **Branch scoping** via X-Branch-Id header AND query/body parameter
- **Group-by** enum (DAY/WEEK/MONTH) verified for revenue
- **Async export jobs** (TICKET-305/306) with `202 Accepted` + jobId pattern
- **Cron validation** including malformed expressions
- **Schedule cancel** as soft-delete via `active=false` flag

---

## Total Stats

```
╔══════════════════════════════════════════════════════════════╗
║  Agent-3 Output Summary                                     ║
║  Files created       : 4                                     ║
║  Endpoints documented: 44 main + 3 bonus = 47                ║
║  Test cases written : 182                                    ║
║  Total bytes         : 263,263                               ║
║  Total lines         : 3,834                                 ║
║  Average lines/file  : 958                                   ║
║  Avg test cases/endpoint: ~4                                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

## Cross-References

- Master Plan: `docs/uat/00-MASTER-PLAN.md`
- Source code inspected: `payment-service/`, `prescription-service/`, `notification-service/`, `report-service/`
- Endpoint list: `docs/api-list.json` (matched controller+method 1:1)
- Webhook secret source: `config-server/src/main/resources/config/payment-service.yml:24`

---

## Notes for Test Execution

1. **Webhook secret** for payment-service is `pcms-payment-gateway-webhook-secret-2026-do-not-share` (dev only). For HMAC computation use openssl or Python snippet in file 09 §5.9.
2. **Outbox endpoints** (notification 5.14-5.16) are internal — call them directly during UAT to simulate the order/inventory service outbox publisher.
3. **Report 12** requires `X-Branch-Id` header for multi-tenant scoping; in dev with no auth, omit for cross-branch aggregate.
4. **Prescription code** `RX-{yyyy}{####}` is per-year — verify against current year.
5. **Idempotency-Key** header (CR-05) should be a UUID v4 for payment POST.

---

**End of Agent-3 Summary**
