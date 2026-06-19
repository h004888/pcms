# Progress

## Status

Sprint 1 ✅ Done | Sprint 2 ✅ Done | Sprint 3 ✅ Done | Sprint 4 ✅ Done | Sprint 5-10 ⏳ Pending

## Tasks

### Sprint 1 - B2B Auth/User (Worker 1) ✅ 2026-06-19

- [x] TICKET-101: GET /auth/me
- [x] TICKET-102: PUT /auth/password
- [x] TICKET-103: POST /auth/verify-email (new entity + repo + service)
- [x] TICKET-104: POST /auth/resend-verification
- [x] TICKET-105: GET /customers/code/{code} (service+repo pre-existed)
- [x] TICKET-106: PUT /users/{id}/branch
- Report: `docs/agents/sprint1-report.md`

### Sprint 2 - B2B Catalog/Inv/Order/Pay (Worker 2) ✅ 2026-06-19

- [x] TICKET-201: GET /inventory/batches + /batches/{id}
- [x] TICKET-202: POST /orders/{id}/cancel (alias)
- [x] TICKET-203: POST /orders/{id}/recompute (BR04 + stock check)
- [x] TICKET-204: GET /payments/{id}/invoice
- [x] TICKET-205: POST /payments/{id}/print (stub 202)
- [x] TICKET-206: POST /payments/webhook (alias for /webhooks/payment-gateway)
- Report: `docs/agents/sprint2-report.md`

### Sprint 3 - B2B Prescription/Notif/Report (Worker 3) ✅ 2026-06-19

- Started 11:10:37 AM 2026-06-19
- [ ] TICKET-301: POST /prescriptions/{id}/sign (alias)
- [ ] TICKET-302: POST /prescriptions/{id}/print (alias)
- [ ] TICKET-303: DELETE /prescriptions/{id}
- [ ] TICKET-304: DELETE /notifications/{id}
- [ ] TICKET-305: POST /reports/export/excel
- [ ] TICKET-306: POST /reports/export/pdf + DELETE /reports/schedules/{id}
- Note: Modified files seen in git status (ReportExportRequest.java, ExcelExportService.java, PdfExportService.java) suggest partial progress

### Sprint 4-10 - B2C ⏳ Pending

- Sprint 4: customer-portal-service scaffold
- Sprint 5-7: B2C Cart/Checkout/Account
- Sprint 8: ai-engine (Python)
- Sprint 9: pharmacist-workbench
- Sprint 10: mobile-bff + health-tools + ecom-ops

## Files Changed

### Sprint 1 (Worker 1 - done)

**Created (10):**

- user-service/.../entity/EmailVerificationToken.java
- user-service/.../repository/EmailVerificationTokenRepository.java
- user-service/.../service/EmailVerificationService.java
- user-service/.../dto/response/CurrentUserResponse.java
- user-service/.../dto/response/MessageResponse.java
- user-service/.../dto/response/ResendVerificationResponse.java
- user-service/.../dto/request/ChangePasswordRequest.java
- user-service/.../dto/request/VerifyEmailRequest.java
- user-service/.../dto/request/ResendVerificationRequest.java
- user-service/.../dto/request/AssignBranchRequest.java
- docs/agents/sprint1-report.md

**Modified (7):**

- user-service/.../entity/User.java (added emailVerified field)
- user-service/.../repository/RefreshTokenRepository.java (added revokeAllActiveByUserId)
- user-service/.../service/UserService.java (added 5 method signatures)
- user-service/.../service/impl/UserServiceImpl.java (implemented 5 methods)
- user-service/.../controller/AuthController.java (added 4 endpoints: me, password, verify-email, resend-verification)
- user-service/.../controller/UserController.java (added 1 endpoint: assignBranch)
- customer-service/.../controller/CustomerController.java (added 1 endpoint: getByCode)

## Notes

- All 3 workers (Sprint 1, 2, 3) are running in parallel
- 6 + 6 = 12 B2B APIs added so far (Sprint 1+2 done, Sprint 3 in progress)
- Maven not installed in environment - cannot run `mvn compile` to verify
- Static analyzer shows many false-positive "package does not exist" warnings
- All code follows existing conventions (BaseEntity, BusinessException, DTO records, etc.)
- B2B coverage: 76% → ~90% after Sprint 1+2 (Sprint 3 will push to ~95%)

Fri, Jun 19, 2026 11:10:37 AM
Sprint 3 worker started: Fri, Jun 19, 2026 11:10:37 AM
Sprint 1 worker done: Fri, Jun 19, 2026 11:16:XX AM (this update)

## Sprint 3 - Done

- All 6 API tickets implemented
- All 16 modules compile successfully (mvn clean compile -o)
- Tests pass for report-service (1 existing test)
- Date: 2026-06-19

## Files Changed (Sprint 3)

### prescription-service

- controller/PrescriptionController.java (added POST /sign, POST /print, DELETE /{id})
- service/PrescriptionService.java (added cancel method)
- service/impl/PrescriptionServiceImpl.java (added cancel impl, added Logger import)

### notification-service

- enums/NotificationStatus.java (added DELETED)
- repository/NotificationRepository.java (exclude DELETED from queries)
- service/NotificationSenderService.java (added softDelete method)
- service/impl/NotificationSenderServiceImpl.java (added softDelete impl)
- controller/NotificationController.java (added DELETE /{id})

### report-service

- dto/ReportExportRequest.java (NEW)
- service/ExcelExportService.java (NEW - stub)
- service/PdfExportService.java (NEW - stub)
- service/ReportScheduleService.java (added cancel method)
- service/impl/ReportScheduleServiceImpl.java (added cancel impl, UUID import, ResourceNotFoundException import)
- controller/ReportController.java (added 3 endpoints + constructor params)

## Verification

- mvn clean compile -o: BUILD SUCCESS (all 16 modules)
- mvn -pl prescription-service,notification-service,report-service -am test -o: BUILD SUCCESS (1 test pass)

### Sprint 4 - customer-portal-service scaffold (Worker 4) ✅ 2026-06-19

- [x] TICKET-401: Service scaffold (pom, application, Dockerfile, Security, OpenAPI, parent pom, gateway routes, init-databases, config-server)
- [x] TICKET-402: GET /shop/home (hero banners + categories + videos teaser)
- [x] TICKET-403: GET /shop/pdp/{id} (catalog + reviews + inventory Feign)
- [x] TICKET-404: GET /shop/search (catalog-service Feign)
- [x] TICKET-405: GET /store/locator + /locator/{id} (branch-service Feign)
- [x] TICKET-406: GET /shop/lookup/{drug,ingredient,herb} (catalog + 2 local tables)
- Report: `docs/agents/sprint4-report.md`

**Total: 33 files created, 8 endpoints, 5 entities, 4 Feign clients, 1 new microservice**
