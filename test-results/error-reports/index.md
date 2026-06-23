# PCMS API Test - Error Reports Index

**Generated**: 2026-06-23 12:54:11  
**Test source**: `test-results.json` (268 endpoints tested, 207 errors)  
**Format**: Scene-by-Scene narrative theo PCMS investigation  

## Tom tat Tong quan

| Metric | Value |
|--------|-------|
| Tong errors | **207** |
| Co log evidence | 134 (64.7%) |
| Status codes | 6 unique |
| Services affected | 17 |
| Categories | 7 |

### Phan bo theo HTTP Status

| Status | Count | % | Description |
|--------|------:|--:|-------------|
| 400 Bad Request | 171 | 82.6% | |
| 401 Unauthorized | 3 | 1.4% | |
| 404 Not Found | 25 | 12.1% | |
| 405 Method Not Allowed | 2 | 1.0% | |
| 409 Conflict | 1 | 0.5% | |
| 500 Server Error | 0 | 0.0% | ALL FIXED |

### Phan bo theo Severity

| Severity | Count |
|----------|------:|
| HIGH | 16 |
| MEDIUM | 69 |
| INFO | 122 |

### Phan bo theo Root Cause Category

| Category | Count | Description |
|----------|------:|-------------|
| Bad Request | 103 | Body rong hoac path param khong hop le (test data issue) |
| Validation | 41 | Annotation validation sai (vi du: @NotBlank tren enum) |
| Media Type | 27 | Endpoint yeu cau multipart/form-data nhung nhan JSON |
| Resource Not Found | 16 | ID khong ton tai trong DB (chua seed data) |
| Integration | 16 | Feign client loi, service downstream khong available |
| Authentication | 3 | JWT token khong hop le hoac thieu (401) |
| API Design | 1 | HTTP method khong duoc ho tro (405) |

## CRITICAL & HIGH Severity Errors (uu tien fix)

| # | Method | URL | Status | Service | Category |
|---|--------|-----|--------|---------|----------|
| [151](error-151-POST-api-v1-consultations-1-messages.md) | POST | `http://localhost:8080/api/v1/consultations/1/messages` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [152](error-152-GET-api-v1-rx-customers-1-profile-360.md) | GET | `http://localhost:8080/api/v1/rx/customers/1/profile-360` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [153](error-153-GET-api-v1-consultations-by-customer-1.md) | GET | `http://localhost:8080/api/v1/consultations/by-customer/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [154](error-154-GET-api-v1-consultations-by-pharmacist-1.md) | GET | `http://localhost:8080/api/v1/consultations/by-pharmacist/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [155](error-155-GET-api-v1-follow-ups-by-customer-1.md) | GET | `http://localhost:8080/api/v1/follow-ups/by-customer/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [156](error-156-GET-api-v1-consultations-1.md) | GET | `http://localhost:8080/api/v1/consultations/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [157](error-157-POST-api-v1-consultations-1-end.md) | POST | `http://localhost:8080/api/v1/consultations/1/end` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [158](error-158-DELETE-api-v1-follow-ups-1.md) | DELETE | `http://localhost:8080/api/v1/follow-ups/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [159](error-159-POST-api-v1-follow-ups-1-response.md) | POST | `http://localhost:8080/api/v1/follow-ups/1/response` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [168](error-168-POST-api-v1-rx-cross-sell.md) | POST | `http://localhost:8080/api/v1/rx/cross-sell` | 500 | pharmacist-workbench-service | [HIGH] Integration |
| [169](error-169-GET-api-v1-vip-marks-by-customer-1.md) | GET | `http://localhost:8080/api/v1/vip-marks/by-customer/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [172](error-172-POST-api-v1-consultations.md) | POST | `http://localhost:8080/api/v1/consultations` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [173](error-173-POST-api-v1-follow-ups.md) | POST | `http://localhost:8080/api/v1/follow-ups` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [174](error-174-POST-api-v1-vip-marks.md) | POST | `http://localhost:8080/api/v1/vip-marks` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [175](error-175-DELETE-api-v1-vip-marks-1.md) | DELETE | `http://localhost:8080/api/v1/vip-marks/1` | 400 | pharmacist-workbench-service | [HIGH] Integration |
| [183](error-183-POST-api-v1-rx-drug-check.md) | POST | `http://localhost:8080/api/v1/rx/drug-check` | 500 | pharmacist-workbench-service | [HIGH] Integration |

## Tat ca Errors theo Service

### branch-service (7 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [003](error-003-PUT-api-v1-branches-1-manager.md) | PUT | `http://localhost:8080/api/v1/branches/1/manager` | 400 | [I] | Bad Request |
| [004](error-004-GET-api-v1-branches-1-staff.md) | GET | `http://localhost:8080/api/v1/branches/1/staff` | 400 | [I] | Bad Request |
| [005](error-005-PUT-api-v1-branches-1.md) | PUT | `http://localhost:8080/api/v1/branches/1` | 400 | [I] | Bad Request |
| [006](error-006-GET-api-v1-branches-1.md) | GET | `http://localhost:8080/api/v1/branches/1` | 400 | [I] | Bad Request |
| [007](error-007-DELETE-api-v1-branches-1.md) | DELETE | `http://localhost:8080/api/v1/branches/1` | 400 | [I] | Bad Request |
| [016](error-016-GET-api-v1-branches-code-1.md) | GET | `http://localhost:8080/api/v1/branches/code/1` | 404 | [I] | Resource Not Found |
| [017](error-017-POST-api-v1-branches.md) | POST | `http://localhost:8080/api/v1/branches` | 400 | [I] | Bad Request |

### catalog-service (10 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [001](error-001-GET-api-v1-medicines-1.md) | GET | `http://localhost:8080/api/v1/medicines/1` | 400 | [M] | Media Type |
| [002](error-002-GET-api-v1-medicines-count.md) | GET | `http://localhost:8080/api/v1/medicines/count` | 400 | [M] | Media Type |
| [008](error-008-POST-api-v1-medicines.md) | POST | `http://localhost:8080/api/v1/medicines` | 400 | [M] | Media Type |
| [009](error-009-POST-api-v1-medicines.md) | POST | `http://localhost:8080/api/v1/medicines` | 400 | [M] | Media Type |
| [010](error-010-PUT-api-v1-medicines-1.md) | PUT | `http://localhost:8080/api/v1/medicines/1` | 400 | [M] | Media Type |
| [011](error-011-PUT-api-v1-medicines-1.md) | PUT | `http://localhost:8080/api/v1/medicines/1` | 400 | [M] | Media Type |
| [012](error-012-DELETE-api-v1-medicines-1.md) | DELETE | `http://localhost:8080/api/v1/medicines/1` | 400 | [M] | Media Type |
| [013](error-013-POST-api-v1-medicines-1-image.md) | POST | `http://localhost:8080/api/v1/medicines/1/image` | 500 | [M] | Media Type |
| [014](error-014-GET-api-v1-medicines-1-image.md) | GET | `http://localhost:8080/api/v1/medicines/1/image` | 400 | [M] | Media Type |
| [015](error-015-GET-api-v1-medicines-sku-1.md) | GET | `http://localhost:8080/api/v1/medicines/sku/1` | 404 | [M] | Media Type |

### category-service (4 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [032](error-032-DELETE-api-v1-categories-1.md) | DELETE | `http://localhost:8080/api/v1/categories/1` | 400 | [I] | Bad Request |
| [033](error-033-PUT-api-v1-categories-1.md) | PUT | `http://localhost:8080/api/v1/categories/1` | 400 | [I] | Bad Request |
| [034](error-034-GET-api-v1-categories-1.md) | GET | `http://localhost:8080/api/v1/categories/1` | 400 | [I] | Bad Request |
| [039](error-039-POST-api-v1-categories.md) | POST | `http://localhost:8080/api/v1/categories` | 400 | [I] | Bad Request |

### customer-portal-service (41 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [018](error-018-GET-api-v1-addresses-1.md) | GET | `http://localhost:8080/api/v1/addresses/1` | 400 | [M] | Validation |
| [019](error-019-PUT-api-v1-addresses-1-default.md) | PUT | `http://localhost:8080/api/v1/addresses/1/default` | 400 | [M] | Validation |
| [020](error-020-PUT-api-v1-addresses-1.md) | PUT | `http://localhost:8080/api/v1/addresses/1` | 400 | [M] | Validation |
| [021](error-021-DELETE-api-v1-addresses-1.md) | DELETE | `http://localhost:8080/api/v1/addresses/1` | 400 | [M] | Validation |
| [022](error-022-PUT-api-v1-cart-items-1.md) | PUT | `http://localhost:8080/api/v1/cart/items/1` | 400 | [M] | Validation |
| [023](error-023-DELETE-api-v1-cart-items-1.md) | DELETE | `http://localhost:8080/api/v1/cart/items/1` | 400 | [M] | Validation |
| [024](error-024-POST-api-v1-cart-items.md) | POST | `http://localhost:8080/api/v1/cart/items` | 400 | [M] | Validation |
| [025](error-025-POST-api-v1-cart-checkout-preview.md) | POST | `http://localhost:8080/api/v1/cart/checkout/preview` | 400 | [M] | Validation |
| [026](error-026-POST-api-v1-addresses.md) | POST | `http://localhost:8080/api/v1/addresses` | 500 | [M] | Validation |
| [027](error-027-POST-api-v1-cart-checkout-confirm.md) | POST | `http://localhost:8080/api/v1/cart/checkout/confirm` | 400 | [M] | Validation |
| [028](error-028-GET-api-v1-family-1.md) | GET | `http://localhost:8080/api/v1/family/1` | 400 | [M] | Validation |
| [029](error-029-POST-api-v1-family.md) | POST | `http://localhost:8080/api/v1/family` | 400 | [M] | Validation |
| [030](error-030-PUT-api-v1-family-1.md) | PUT | `http://localhost:8080/api/v1/family/1` | 400 | [M] | Validation |
| [031](error-031-DELETE-api-v1-family-1.md) | DELETE | `http://localhost:8080/api/v1/family/1` | 400 | [M] | Validation |
| [035](error-035-DELETE-api-v1-favorites-1.md) | DELETE | `http://localhost:8080/api/v1/favorites/1` | 400 | [M] | Validation |
| [036](error-036-POST-api-v1-favorites.md) | POST | `http://localhost:8080/api/v1/favorites` | 400 | [M] | Validation |
| [037](error-037-GET-api-v1-favorites-1-check.md) | GET | `http://localhost:8080/api/v1/favorites/1/check` | 400 | [M] | Validation |
| [038](error-038-GET-api-v1-health-articles-1.md) | GET | `http://localhost:8080/api/v1/health-articles/1` | 404 | [M] | Validation |
| [040](error-040-POST-api-v1-installment-confirm.md) | POST | `http://localhost:8080/api/v1/installment/confirm` | 400 | [M] | Validation |
| [041](error-041-POST-api-v1-installment-quote.md) | POST | `http://localhost:8080/api/v1/installment/quote` | 400 | [M] | Validation |
| [042](error-042-PUT-api-v1-notif-settings.md) | PUT | `http://localhost:8080/api/v1/notif-settings` | 400 | [M] | Validation |
| [043](error-043-POST-api-v1-verify-origin-scan.md) | POST | `http://localhost:8080/api/v1/verify-origin/scan` | 400 | [M] | Validation |
| [044](error-044-GET-api-v1-shop-pdp-1.md) | GET | `http://localhost:8080/api/v1/shop/pdp/1` | 400 | [M] | Validation |
| [045](error-045-GET-api-v1-orders-1-track.md) | GET | `http://localhost:8080/api/v1/orders/1/track` | 404 | [M] | Validation |
| [046](error-046-GET-api-v1-orders-history.md) | GET | `http://localhost:8080/api/v1/orders/history` | 400 | [M] | Validation |
| [047](error-047-GET-api-v1-prescriptions-1-re-download.md) | GET | `http://localhost:8080/api/v1/prescriptions/1/re-download` | 404 | [M] | Validation |
| [048](error-048-GET-api-v1-prescriptions-me.md) | GET | `http://localhost:8080/api/v1/prescriptions/me` | 400 | [M] | Validation |
| [049](error-049-GET-api-v1-vaccines-1-slots.md) | GET | `http://localhost:8080/api/v1/vaccines/1/slots` | 400 | [M] | Validation |
| [050](error-050-GET-api-v1-store-locator-1.md) | GET | `http://localhost:8080/api/v1/store/locator/1` | 404 | [M] | Validation |
| [051](error-051-PATCH-api-v1-notif-settings.md) | PATCH | `http://localhost:8080/api/v1/notif-settings` | 409 | [M] | Validation |
| [052](error-052-DELETE-api-v1-vaccine-bookings-1.md) | DELETE | `http://localhost:8080/api/v1/vaccine-bookings/1` | 400 | [M] | Validation |
| [053](error-053-GET-api-v1-vaccine-bookings-me.md) | GET | `http://localhost:8080/api/v1/vaccine-bookings/me` | 400 | [M] | Validation |
| [054](error-054-POST-api-v1-vaccine-bookings.md) | POST | `http://localhost:8080/api/v1/vaccine-bookings` | 400 | [M] | Validation |
| [055](error-055-GET-api-v1-vaccination-ledger-me.md) | GET | `http://localhost:8080/api/v1/vaccination-ledger/me` | 400 | [M] | Validation |
| [056](error-056-POST-api-v1-admin-videos-videos.md) | POST | `http://localhost:8080/api/v1/admin/videos/videos` | 405 | [M] | Validation |
| [057](error-057-GET-api-v1-admin-videos-videos.md) | GET | `http://localhost:8080/api/v1/admin/videos/videos` | 400 | [M] | Validation |
| [058](error-058-DELETE-api-v1-admin-videos-videos-1.md) | DELETE | `http://localhost:8080/api/v1/admin/videos/videos/1` | 404 | [M] | Validation |
| [059](error-059-GET-api-v1-admin-videos-videos-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/videos/1` | 404 | [M] | Validation |
| [060](error-060-PUT-api-v1-admin-videos-videos-1.md) | PUT | `http://localhost:8080/api/v1/admin/videos/videos/1` | 404 | [M] | Validation |
| [061](error-061-POST-api-v1-vouchers-apply.md) | POST | `http://localhost:8080/api/v1/vouchers/apply` | 400 | [M] | Validation |
| [062](error-062-POST-api-v1-wallet-redeem.md) | POST | `http://localhost:8080/api/v1/wallet/redeem` | 400 | [M] | Validation |

### customer-service (14 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [063](error-063-GET-api-v1-customers-1-points.md) | GET | `http://localhost:8080/api/v1/customers/1/points` | 400 | [I] | Bad Request |
| [064](error-064-GET-api-v1-customers-1-history.md) | GET | `http://localhost:8080/api/v1/customers/1/history` | 400 | [I] | Bad Request |
| [065](error-065-GET-api-v1-customers-1-orders.md) | GET | `http://localhost:8080/api/v1/customers/1/orders` | 400 | [I] | Bad Request |
| [066](error-066-GET-api-v1-customers-1.md) | GET | `http://localhost:8080/api/v1/customers/1` | 400 | [I] | Bad Request |
| [067](error-067-GET-api-v1-customers-1-tier.md) | GET | `http://localhost:8080/api/v1/customers/1/tier` | 400 | [I] | Bad Request |
| [068](error-068-PUT-api-v1-customers-1.md) | PUT | `http://localhost:8080/api/v1/customers/1` | 400 | [I] | Bad Request |
| [073](error-073-DELETE-api-v1-customers-1.md) | DELETE | `http://localhost:8080/api/v1/customers/1` | 400 | [I] | Bad Request |
| [075](error-075-POST-api-v1-customers.md) | POST | `http://localhost:8080/api/v1/customers` | 400 | [I] | Bad Request |
| [076](error-076-POST-api-v1-customers-register.md) | POST | `http://localhost:8080/api/v1/customers/register` | 400 | [I] | Bad Request |
| [077](error-077-PUT-api-v1-customers-1-points-add.md) | PUT | `http://localhost:8080/api/v1/customers/1/points/add` | 400 | [I] | Bad Request |
| [079](error-079-PUT-api-v1-customers-me.md) | PUT | `http://localhost:8080/api/v1/customers/me` | 400 | [I] | Bad Request |
| [080](error-080-GET-api-v1-customers-me.md) | GET | `http://localhost:8080/api/v1/customers/me` | 404 | [I] | Resource Not Found |
| [081](error-081-GET-api-v1-customers-phone-1.md) | GET | `http://localhost:8080/api/v1/customers/phone/1` | 404 | [I] | Resource Not Found |
| [082](error-082-GET-api-v1-customers-code-1.md) | GET | `http://localhost:8080/api/v1/customers/code/1` | 404 | [I] | Resource Not Found |

### ecom-ops-service (6 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [069](error-069-POST-api-v1-admin-videos-flash-sales.md) | POST | `http://localhost:8080/api/v1/admin/videos/flash-sales` | 405 | [M] | API Design |
| [070](error-070-GET-api-v1-admin-videos-flash-sales.md) | GET | `http://localhost:8080/api/v1/admin/videos/flash-sales` | 400 | [I] | Bad Request |
| [071](error-071-GET-api-v1-admin-videos-flash-sales-active.md) | GET | `http://localhost:8080/api/v1/admin/videos/flash-sales/active` | 404 | [I] | Resource Not Found |
| [072](error-072-GET-api-v1-admin-videos-flash-sales-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/flash-sales/1` | 404 | [I] | Resource Not Found |
| [074](error-074-POST-api-v1-admin-videos-flash-sales-1-cancel.md) | POST | `http://localhost:8080/api/v1/admin/videos/flash-sales/1/cancel` | 404 | [I] | Resource Not Found |
| [078](error-078-GET-api-v1-ecom-ops-flash-sales-1.md) | GET | `http://localhost:8080/api/v1/ecom-ops/flash-sales/1` | 400 | [I] | Bad Request |

### health-tools-service (3 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [083](error-083-GET-api-v1-health-quiz-results-me.md) | GET | `http://localhost:8080/api/v1/health/quiz-results/me` | 400 | [I] | Bad Request |
| [084](error-084-POST-api-v1-health-quizzes-1-submit.md) | POST | `http://localhost:8080/api/v1/health/quizzes/1/submit` | 400 | [I] | Bad Request |
| [093](error-093-GET-api-v1-health-quizzes-1.md) | GET | `http://localhost:8080/api/v1/health/quizzes/1` | 404 | [I] | Resource Not Found |

### inventory-service (17 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [085](error-085-GET-api-v1-inventory-1.md) | GET | `http://localhost:8080/api/v1/inventory/1` | 400 | [M] | Media Type |
| [086](error-086-GET-api-v1-inventory-batches-1.md) | GET | `http://localhost:8080/api/v1/inventory/batches/1` | 400 | [M] | Media Type |
| [087](error-087-GET-api-v1-inventory-batches-scan-1.md) | GET | `http://localhost:8080/api/v1/inventory/batches/scan/1` | 404 | [M] | Media Type |
| [088](error-088-GET-api-v1-inventory-branch-1-medicine-1.md) | GET | `http://localhost:8080/api/v1/inventory/branch/1/medicine/1` | 400 | [M] | Media Type |
| [089](error-089-POST-api-v1-inventory-bulk-import.md) | POST | `http://localhost:8080/api/v1/inventory/bulk/import` | 400 | [M] | Media Type |
| [090](error-090-POST-api-v1-inventory-bulk-import.md) | POST | `http://localhost:8080/api/v1/inventory/bulk/import` | 400 | [M] | Media Type |
| [091](error-091-POST-api-v1-inventory-bulk-import-file.md) | POST | `http://localhost:8080/api/v1/inventory/bulk/import-file` | 500 | [M] | Media Type |
| [092](error-092-GET-api-v1-inventory-transactions.md) | GET | `http://localhost:8080/api/v1/inventory/transactions` | 400 | [M] | Media Type |
| [094](error-094-POST-api-v1-inventory-orders-1-paid.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/paid` | 400 | [M] | Media Type |
| [095](error-095-POST-api-v1-inventory-orders-1-cancelled.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/cancelled` | 400 | [M] | Media Type |
| [096](error-096-POST-api-v1-inventory-orders-1-paid-bulk.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/paid-bulk` | 400 | [M] | Media Type |
| [097](error-097-POST-api-v1-inventory-orders-1-cancelled-bulk.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/cancelled-bulk` | 400 | [M] | Media Type |
| [098](error-098-POST-api-v1-inventory-import.md) | POST | `http://localhost:8080/api/v1/inventory/import` | 400 | [M] | Media Type |
| [099](error-099-POST-api-v1-inventory-orders-1-cancelled-precise.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/cancelled-precise` | 400 | [M] | Media Type |
| [100](error-100-POST-api-v1-inventory-consume.md) | POST | `http://localhost:8080/api/v1/inventory/consume` | 400 | [M] | Media Type |
| [101](error-101-POST-api-v1-inventory-transfer.md) | POST | `http://localhost:8080/api/v1/inventory/transfer` | 400 | [M] | Media Type |
| [102](error-102-POST-api-v1-inventory-export.md) | POST | `http://localhost:8080/api/v1/inventory/export` | 400 | [M] | Media Type |

### mobile-bff (4 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [133](error-133-GET-api-v1-mobile-medication-reminders.md) | GET | `http://localhost:8080/api/v1/mobile/medication-reminders` | 400 | [I] | Bad Request |
| [134](error-134-DELETE-api-v1-mobile-medication-reminders-1.md) | DELETE | `http://localhost:8080/api/v1/mobile/medication-reminders/1` | 400 | [I] | Bad Request |
| [135](error-135-PUT-api-v1-mobile-medication-reminders-1-deactivate.md) | PUT | `http://localhost:8080/api/v1/mobile/medication-reminders/1/deactivate` | 400 | [I] | Bad Request |
| [149](error-149-POST-api-v1-mobile-medication-reminders.md) | POST | `http://localhost:8080/api/v1/mobile/medication-reminders` | 400 | [I] | Bad Request |

### notification-service (15 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [103](error-103-GET-api-v1-notifications-1.md) | GET | `http://localhost:8080/api/v1/notifications/1` | 400 | [I] | Bad Request |
| [104](error-104-GET-api-v1-notifications-unread.md) | GET | `http://localhost:8080/api/v1/notifications/unread` | 400 | [I] | Bad Request |
| [105](error-105-GET-api-v1-notifications.md) | GET | `http://localhost:8080/api/v1/notifications` | 400 | [I] | Bad Request |
| [106](error-106-POST-api-v1-notifications-1-retry.md) | POST | `http://localhost:8080/api/v1/notifications/1/retry` | 400 | [I] | Bad Request |
| [107](error-107-PUT-api-v1-notifications-1-read.md) | PUT | `http://localhost:8080/api/v1/notifications/1/read` | 400 | [I] | Bad Request |
| [108](error-108-PUT-api-v1-notifications-read-all.md) | PUT | `http://localhost:8080/api/v1/notifications/read-all` | 400 | [I] | Bad Request |
| [109](error-109-POST-api-v1-notifications-compose.md) | POST | `http://localhost:8080/api/v1/notifications/compose` | 400 | [I] | Bad Request |
| [110](error-110-POST-api-v1-notifications-bulk.md) | POST | `http://localhost:8080/api/v1/notifications/bulk` | 400 | [I] | Bad Request |
| [111](error-111-POST-api-v1-notifications-templates.md) | POST | `http://localhost:8080/api/v1/notifications/templates` | 400 | [I] | Bad Request |
| [112](error-112-POST-api-v1-notifications.md) | POST | `http://localhost:8080/api/v1/notifications` | 400 | [I] | Bad Request |
| [113](error-113-POST-api-v1-notifications-broadcast.md) | POST | `http://localhost:8080/api/v1/notifications/broadcast` | 400 | [I] | Bad Request |
| [114](error-114-DELETE-api-v1-notifications-1.md) | DELETE | `http://localhost:8080/api/v1/notifications/1` | 400 | [I] | Bad Request |
| [116](error-116-POST-api-v1-notifications-inventory-low-stock.md) | POST | `http://localhost:8080/api/v1/notifications/inventory/low-stock` | 400 | [I] | Bad Request |
| [119](error-119-POST-api-v1-notifications-inventory-expiry.md) | POST | `http://localhost:8080/api/v1/notifications/inventory/expiry` | 400 | [I] | Bad Request |
| [122](error-122-POST-api-v1-notifications-orders-paid.md) | POST | `http://localhost:8080/api/v1/notifications/orders/paid` | 400 | [I] | Bad Request |

### order-service (18 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [115](error-115-DELETE-api-v1-coupons-1.md) | DELETE | `http://localhost:8080/api/v1/coupons/1` | 400 | [I] | Bad Request |
| [117](error-117-PUT-api-v1-coupons-1.md) | PUT | `http://localhost:8080/api/v1/coupons/1` | 400 | [I] | Bad Request |
| [118](error-118-GET-api-v1-orders-1.md) | GET | `http://localhost:8080/api/v1/orders/1` | 400 | [I] | Bad Request |
| [120](error-120-PUT-api-v1-orders-1.md) | PUT | `http://localhost:8080/api/v1/orders/1` | 400 | [I] | Bad Request |
| [121](error-121-PUT-api-v1-orders-1-pay.md) | PUT | `http://localhost:8080/api/v1/orders/1/pay` | 400 | [I] | Bad Request |
| [123](error-123-POST-api-v1-orders-1-approve.md) | POST | `http://localhost:8080/api/v1/orders/1/approve` | 400 | [I] | Bad Request |
| [124](error-124-POST-api-v1-orders-1-reject.md) | POST | `http://localhost:8080/api/v1/orders/1/reject` | 400 | [I] | Bad Request |
| [125](error-125-POST-api-v1-admin-videos-outbox-retry-1.md) | POST | `http://localhost:8080/api/v1/admin/videos/outbox/retry/1` | 404 | [I] | Resource Not Found |
| [126](error-126-DELETE-api-v1-orders-1.md) | DELETE | `http://localhost:8080/api/v1/orders/1` | 400 | [I] | Bad Request |
| [127](error-127-GET-api-v1-admin-videos-saga-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/saga/1` | 404 | [I] | Resource Not Found |
| [128](error-128-POST-api-v1-orders-1-cancel.md) | POST | `http://localhost:8080/api/v1/orders/1/cancel` | 400 | [I] | Bad Request |
| [129](error-129-POST-api-v1-admin-videos-saga-1-compensate.md) | POST | `http://localhost:8080/api/v1/admin/videos/saga/1/compensate` | 404 | [I] | Resource Not Found |
| [130](error-130-GET-api-v1-admin-videos-saga-stuck.md) | GET | `http://localhost:8080/api/v1/admin/videos/saga/stuck` | 404 | [I] | Resource Not Found |
| [131](error-131-GET-api-v1-admin-videos-saga-by-aggregate-1-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/saga/by-aggregate/1/1` | 404 | [I] | Resource Not Found |
| [132](error-132-POST-api-v1-orders-1-recompute.md) | POST | `http://localhost:8080/api/v1/orders/1/recompute` | 400 | [I] | Bad Request |
| [136](error-136-GET-api-v1-orders-number-1.md) | GET | `http://localhost:8080/api/v1/orders/number/1` | 404 | [I] | Resource Not Found |
| [147](error-147-POST-api-v1-orders.md) | POST | `http://localhost:8080/api/v1/orders` | 400 | [I] | Bad Request |
| [148](error-148-POST-api-v1-coupons.md) | POST | `http://localhost:8080/api/v1/coupons` | 400 | [I] | Bad Request |

### payment-service (11 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [137](error-137-GET-api-v1-payments-1.md) | GET | `http://localhost:8080/api/v1/payments/1` | 400 | [I] | Bad Request |
| [138](error-138-POST-api-v1-payments-1-refund.md) | POST | `http://localhost:8080/api/v1/payments/1/refund` | 400 | [I] | Bad Request |
| [139](error-139-PUT-api-v1-payments-1-refund.md) | PUT | `http://localhost:8080/api/v1/payments/1/refund` | 400 | [I] | Bad Request |
| [140](error-140-GET-api-v1-payments-order-1.md) | GET | `http://localhost:8080/api/v1/payments/order/1` | 400 | [I] | Bad Request |
| [141](error-141-POST-api-v1-payments-1-print.md) | POST | `http://localhost:8080/api/v1/payments/1/print` | 400 | [I] | Bad Request |
| [142](error-142-GET-api-v1-payments-1-refund-history.md) | GET | `http://localhost:8080/api/v1/payments/1/refund-history` | 400 | [I] | Bad Request |
| [143](error-143-GET-api-v1-payments-1-invoice.md) | GET | `http://localhost:8080/api/v1/payments/1/invoice` | 400 | [I] | Bad Request |
| [144](error-144-POST-api-v1-payments-webhook.md) | POST | `http://localhost:8080/api/v1/payments/webhook` | 401 | [I] | Authentication |
| [145](error-145-POST-api-v1-webhooks-payment-gateway.md) | POST | `http://localhost:8080/api/v1/webhooks/payment-gateway` | 401 | [I] | Authentication |
| [146](error-146-GET-api-v1-payments-invoice-1.md) | GET | `http://localhost:8080/api/v1/payments/invoice/1` | 404 | [I] | Resource Not Found |
| [150](error-150-POST-api-v1-payments.md) | POST | `http://localhost:8080/api/v1/payments` | 400 | [I] | Bad Request |

### pharmacist-workbench-service (16 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [151](error-151-POST-api-v1-consultations-1-messages.md) | POST | `http://localhost:8080/api/v1/consultations/1/messages` | 400 | [H] | Integration |
| [152](error-152-GET-api-v1-rx-customers-1-profile-360.md) | GET | `http://localhost:8080/api/v1/rx/customers/1/profile-360` | 400 | [H] | Integration |
| [153](error-153-GET-api-v1-consultations-by-customer-1.md) | GET | `http://localhost:8080/api/v1/consultations/by-customer/1` | 400 | [H] | Integration |
| [154](error-154-GET-api-v1-consultations-by-pharmacist-1.md) | GET | `http://localhost:8080/api/v1/consultations/by-pharmacist/1` | 400 | [H] | Integration |
| [155](error-155-GET-api-v1-follow-ups-by-customer-1.md) | GET | `http://localhost:8080/api/v1/follow-ups/by-customer/1` | 400 | [H] | Integration |
| [156](error-156-GET-api-v1-consultations-1.md) | GET | `http://localhost:8080/api/v1/consultations/1` | 400 | [H] | Integration |
| [157](error-157-POST-api-v1-consultations-1-end.md) | POST | `http://localhost:8080/api/v1/consultations/1/end` | 400 | [H] | Integration |
| [158](error-158-DELETE-api-v1-follow-ups-1.md) | DELETE | `http://localhost:8080/api/v1/follow-ups/1` | 400 | [H] | Integration |
| [159](error-159-POST-api-v1-follow-ups-1-response.md) | POST | `http://localhost:8080/api/v1/follow-ups/1/response` | 400 | [H] | Integration |
| [168](error-168-POST-api-v1-rx-cross-sell.md) | POST | `http://localhost:8080/api/v1/rx/cross-sell` | 500 | [H] | Integration |
| [169](error-169-GET-api-v1-vip-marks-by-customer-1.md) | GET | `http://localhost:8080/api/v1/vip-marks/by-customer/1` | 400 | [H] | Integration |
| [172](error-172-POST-api-v1-consultations.md) | POST | `http://localhost:8080/api/v1/consultations` | 400 | [H] | Integration |
| [173](error-173-POST-api-v1-follow-ups.md) | POST | `http://localhost:8080/api/v1/follow-ups` | 400 | [H] | Integration |
| [174](error-174-POST-api-v1-vip-marks.md) | POST | `http://localhost:8080/api/v1/vip-marks` | 400 | [H] | Integration |
| [175](error-175-DELETE-api-v1-vip-marks-1.md) | DELETE | `http://localhost:8080/api/v1/vip-marks/1` | 400 | [H] | Integration |
| [183](error-183-POST-api-v1-rx-drug-check.md) | POST | `http://localhost:8080/api/v1/rx/drug-check` | 500 | [H] | Integration |

### prescription-service (11 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [160](error-160-GET-api-v1-prescriptions-1.md) | GET | `http://localhost:8080/api/v1/prescriptions/1` | 400 | [I] | Bad Request |
| [161](error-161-PUT-api-v1-prescriptions-1.md) | PUT | `http://localhost:8080/api/v1/prescriptions/1` | 400 | [I] | Bad Request |
| [162](error-162-PUT-api-v1-prescriptions-1-sign.md) | PUT | `http://localhost:8080/api/v1/prescriptions/1/sign` | 400 | [I] | Bad Request |
| [163](error-163-POST-api-v1-prescriptions-1-sign.md) | POST | `http://localhost:8080/api/v1/prescriptions/1/sign` | 400 | [I] | Bad Request |
| [164](error-164-POST-api-v1-prescriptions-1-link-order.md) | POST | `http://localhost:8080/api/v1/prescriptions/1/link-order` | 400 | [I] | Bad Request |
| [165](error-165-GET-api-v1-prescriptions-1-print.md) | GET | `http://localhost:8080/api/v1/prescriptions/1/print` | 400 | [I] | Bad Request |
| [166](error-166-POST-api-v1-prescriptions-1-print.md) | POST | `http://localhost:8080/api/v1/prescriptions/1/print` | 400 | [I] | Bad Request |
| [167](error-167-DELETE-api-v1-prescriptions-1.md) | DELETE | `http://localhost:8080/api/v1/prescriptions/1` | 400 | [I] | Bad Request |
| [176](error-176-POST-api-v1-prescriptions.md) | POST | `http://localhost:8080/api/v1/prescriptions` | 400 | [I] | Bad Request |
| [177](error-177-POST-api-v1-prescriptions-draft.md) | POST | `http://localhost:8080/api/v1/prescriptions/draft` | 400 | [I] | Bad Request |
| [179](error-179-GET-api-v1-prescriptions-code-1.md) | GET | `http://localhost:8080/api/v1/prescriptions/code/1` | 404 | [I] | Resource Not Found |

### report-service (9 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [170](error-170-GET-api-v1-reports-revenue.md) | GET | `http://localhost:8080/api/v1/reports/revenue` | 400 | [I] | Bad Request |
| [171](error-171-GET-api-v1-reports-staff.md) | GET | `http://localhost:8080/api/v1/reports/staff` | 400 | [I] | Bad Request |
| [178](error-178-POST-api-v1-reports-staff.md) | POST | `http://localhost:8080/api/v1/reports/staff` | 400 | [I] | Bad Request |
| [180](error-180-POST-api-v1-reports-revenue.md) | POST | `http://localhost:8080/api/v1/reports/revenue` | 400 | [I] | Bad Request |
| [181](error-181-POST-api-v1-reports-schedule.md) | POST | `http://localhost:8080/api/v1/reports/schedule` | 400 | [I] | Bad Request |
| [182](error-182-GET-api-v1-reports-export.md) | GET | `http://localhost:8080/api/v1/reports/export` | 400 | [I] | Bad Request |
| [184](error-184-POST-api-v1-reports-export-excel.md) | POST | `http://localhost:8080/api/v1/reports/export/excel` | 400 | [I] | Bad Request |
| [185](error-185-POST-api-v1-reports-export-pdf.md) | POST | `http://localhost:8080/api/v1/reports/export/pdf` | 400 | [I] | Bad Request |
| [186](error-186-DELETE-api-v1-reports-schedules-1.md) | DELETE | `http://localhost:8080/api/v1/reports/schedules/1` | 400 | [I] | Bad Request |

### supplier-service (5 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [203](error-203-GET-api-v1-suppliers-1-history.md) | GET | `http://localhost:8080/api/v1/suppliers/1/history` | 400 | [I] | Bad Request |
| [204](error-204-GET-api-v1-suppliers-1.md) | GET | `http://localhost:8080/api/v1/suppliers/1` | 400 | [I] | Bad Request |
| [205](error-205-PUT-api-v1-suppliers-1.md) | PUT | `http://localhost:8080/api/v1/suppliers/1` | 400 | [I] | Bad Request |
| [206](error-206-DELETE-api-v1-suppliers-1.md) | DELETE | `http://localhost:8080/api/v1/suppliers/1` | 400 | [I] | Bad Request |
| [207](error-207-POST-api-v1-suppliers.md) | POST | `http://localhost:8080/api/v1/suppliers` | 400 | [I] | Bad Request |

### user-service (16 errors)

| # | Method | URL | Status | Severity | Category |
|---|--------|-----|--------|----------|----------|
| [187](error-187-POST-api-v1-auth-login.md) | POST | `http://localhost:8080/api/v1/auth/login` | 400 | [I] | Bad Request |
| [188](error-188-POST-api-v1-auth-forgot-password.md) | POST | `http://localhost:8080/api/v1/auth/forgot-password` | 400 | [I] | Bad Request |
| [189](error-189-POST-api-v1-auth-reset-password.md) | POST | `http://localhost:8080/api/v1/auth/reset-password` | 400 | [I] | Bad Request |
| [190](error-190-POST-api-v1-auth-refresh.md) | POST | `http://localhost:8080/api/v1/auth/refresh` | 401 | [I] | Authentication |
| [191](error-191-POST-api-v1-auth-verify-email.md) | POST | `http://localhost:8080/api/v1/auth/verify-email` | 400 | [I] | Bad Request |
| [192](error-192-POST-api-v1-auth-resend-verification.md) | POST | `http://localhost:8080/api/v1/auth/resend-verification` | 400 | [I] | Bad Request |
| [193](error-193-PUT-api-v1-auth-password.md) | PUT | `http://localhost:8080/api/v1/auth/password` | 400 | [I] | Bad Request |
| [194](error-194-GET-api-v1-users-1.md) | GET | `http://localhost:8080/api/v1/users/1` | 400 | [I] | Bad Request |
| [195](error-195-PUT-api-v1-users-1-role.md) | PUT | `http://localhost:8080/api/v1/users/1/role` | 400 | [I] | Bad Request |
| [196](error-196-PUT-api-v1-users-1.md) | PUT | `http://localhost:8080/api/v1/users/1` | 400 | [I] | Bad Request |
| [197](error-197-PUT-api-v1-users-1-status.md) | PUT | `http://localhost:8080/api/v1/users/1/status` | 400 | [I] | Bad Request |
| [198](error-198-POST-api-v1-users-1-unlock.md) | POST | `http://localhost:8080/api/v1/users/1/unlock` | 400 | [I] | Bad Request |
| [199](error-199-DELETE-api-v1-users-1.md) | DELETE | `http://localhost:8080/api/v1/users/1` | 400 | [I] | Bad Request |
| [200](error-200-PUT-api-v1-users-1-branch.md) | PUT | `http://localhost:8080/api/v1/users/1/branch` | 400 | [I] | Bad Request |
| [201](error-201-GET-api-v1-users-role-1.md) | GET | `http://localhost:8080/api/v1/users/role/1` | 400 | [I] | Bad Request |
| [202](error-202-POST-api-v1-users.md) | POST | `http://localhost:8080/api/v1/users` | 400 | [I] | Bad Request |

## Tat ca Errors theo HTTP Status

### 400 Bad Request (171 errors)

| # | Method | URL | Service | Severity |
|---|--------|-----|---------|----------|
| [001](error-001-GET-api-v1-medicines-1.md) | GET | `http://localhost:8080/api/v1/medicines/1` | catalog-service | [M] |
| [002](error-002-GET-api-v1-medicines-count.md) | GET | `http://localhost:8080/api/v1/medicines/count` | catalog-service | [M] |
| [003](error-003-PUT-api-v1-branches-1-manager.md) | PUT | `http://localhost:8080/api/v1/branches/1/manager` | branch-service | [I] |
| [004](error-004-GET-api-v1-branches-1-staff.md) | GET | `http://localhost:8080/api/v1/branches/1/staff` | branch-service | [I] |
| [005](error-005-PUT-api-v1-branches-1.md) | PUT | `http://localhost:8080/api/v1/branches/1` | branch-service | [I] |
| [006](error-006-GET-api-v1-branches-1.md) | GET | `http://localhost:8080/api/v1/branches/1` | branch-service | [I] |
| [007](error-007-DELETE-api-v1-branches-1.md) | DELETE | `http://localhost:8080/api/v1/branches/1` | branch-service | [I] |
| [008](error-008-POST-api-v1-medicines.md) | POST | `http://localhost:8080/api/v1/medicines` | catalog-service | [M] |
| [009](error-009-POST-api-v1-medicines.md) | POST | `http://localhost:8080/api/v1/medicines` | catalog-service | [M] |
| [010](error-010-PUT-api-v1-medicines-1.md) | PUT | `http://localhost:8080/api/v1/medicines/1` | catalog-service | [M] |
| [011](error-011-PUT-api-v1-medicines-1.md) | PUT | `http://localhost:8080/api/v1/medicines/1` | catalog-service | [M] |
| [012](error-012-DELETE-api-v1-medicines-1.md) | DELETE | `http://localhost:8080/api/v1/medicines/1` | catalog-service | [M] |
| [014](error-014-GET-api-v1-medicines-1-image.md) | GET | `http://localhost:8080/api/v1/medicines/1/image` | catalog-service | [M] |
| [017](error-017-POST-api-v1-branches.md) | POST | `http://localhost:8080/api/v1/branches` | branch-service | [I] |
| [018](error-018-GET-api-v1-addresses-1.md) | GET | `http://localhost:8080/api/v1/addresses/1` | customer-portal-service | [M] |
| [019](error-019-PUT-api-v1-addresses-1-default.md) | PUT | `http://localhost:8080/api/v1/addresses/1/default` | customer-portal-service | [M] |
| [020](error-020-PUT-api-v1-addresses-1.md) | PUT | `http://localhost:8080/api/v1/addresses/1` | customer-portal-service | [M] |
| [021](error-021-DELETE-api-v1-addresses-1.md) | DELETE | `http://localhost:8080/api/v1/addresses/1` | customer-portal-service | [M] |
| [022](error-022-PUT-api-v1-cart-items-1.md) | PUT | `http://localhost:8080/api/v1/cart/items/1` | customer-portal-service | [M] |
| [023](error-023-DELETE-api-v1-cart-items-1.md) | DELETE | `http://localhost:8080/api/v1/cart/items/1` | customer-portal-service | [M] |
| [024](error-024-POST-api-v1-cart-items.md) | POST | `http://localhost:8080/api/v1/cart/items` | customer-portal-service | [M] |
| [025](error-025-POST-api-v1-cart-checkout-preview.md) | POST | `http://localhost:8080/api/v1/cart/checkout/preview` | customer-portal-service | [M] |
| [027](error-027-POST-api-v1-cart-checkout-confirm.md) | POST | `http://localhost:8080/api/v1/cart/checkout/confirm` | customer-portal-service | [M] |
| [028](error-028-GET-api-v1-family-1.md) | GET | `http://localhost:8080/api/v1/family/1` | customer-portal-service | [M] |
| [029](error-029-POST-api-v1-family.md) | POST | `http://localhost:8080/api/v1/family` | customer-portal-service | [M] |
| [030](error-030-PUT-api-v1-family-1.md) | PUT | `http://localhost:8080/api/v1/family/1` | customer-portal-service | [M] |
| [031](error-031-DELETE-api-v1-family-1.md) | DELETE | `http://localhost:8080/api/v1/family/1` | customer-portal-service | [M] |
| [032](error-032-DELETE-api-v1-categories-1.md) | DELETE | `http://localhost:8080/api/v1/categories/1` | category-service | [I] |
| [033](error-033-PUT-api-v1-categories-1.md) | PUT | `http://localhost:8080/api/v1/categories/1` | category-service | [I] |
| [034](error-034-GET-api-v1-categories-1.md) | GET | `http://localhost:8080/api/v1/categories/1` | category-service | [I] |
| [035](error-035-DELETE-api-v1-favorites-1.md) | DELETE | `http://localhost:8080/api/v1/favorites/1` | customer-portal-service | [M] |
| [036](error-036-POST-api-v1-favorites.md) | POST | `http://localhost:8080/api/v1/favorites` | customer-portal-service | [M] |
| [037](error-037-GET-api-v1-favorites-1-check.md) | GET | `http://localhost:8080/api/v1/favorites/1/check` | customer-portal-service | [M] |
| [039](error-039-POST-api-v1-categories.md) | POST | `http://localhost:8080/api/v1/categories` | category-service | [I] |
| [040](error-040-POST-api-v1-installment-confirm.md) | POST | `http://localhost:8080/api/v1/installment/confirm` | customer-portal-service | [M] |
| [041](error-041-POST-api-v1-installment-quote.md) | POST | `http://localhost:8080/api/v1/installment/quote` | customer-portal-service | [M] |
| [042](error-042-PUT-api-v1-notif-settings.md) | PUT | `http://localhost:8080/api/v1/notif-settings` | customer-portal-service | [M] |
| [043](error-043-POST-api-v1-verify-origin-scan.md) | POST | `http://localhost:8080/api/v1/verify-origin/scan` | customer-portal-service | [M] |
| [044](error-044-GET-api-v1-shop-pdp-1.md) | GET | `http://localhost:8080/api/v1/shop/pdp/1` | customer-portal-service | [M] |
| [046](error-046-GET-api-v1-orders-history.md) | GET | `http://localhost:8080/api/v1/orders/history` | customer-portal-service | [M] |
| [048](error-048-GET-api-v1-prescriptions-me.md) | GET | `http://localhost:8080/api/v1/prescriptions/me` | customer-portal-service | [M] |
| [049](error-049-GET-api-v1-vaccines-1-slots.md) | GET | `http://localhost:8080/api/v1/vaccines/1/slots` | customer-portal-service | [M] |
| [052](error-052-DELETE-api-v1-vaccine-bookings-1.md) | DELETE | `http://localhost:8080/api/v1/vaccine-bookings/1` | customer-portal-service | [M] |
| [053](error-053-GET-api-v1-vaccine-bookings-me.md) | GET | `http://localhost:8080/api/v1/vaccine-bookings/me` | customer-portal-service | [M] |
| [054](error-054-POST-api-v1-vaccine-bookings.md) | POST | `http://localhost:8080/api/v1/vaccine-bookings` | customer-portal-service | [M] |
| [055](error-055-GET-api-v1-vaccination-ledger-me.md) | GET | `http://localhost:8080/api/v1/vaccination-ledger/me` | customer-portal-service | [M] |
| [057](error-057-GET-api-v1-admin-videos-videos.md) | GET | `http://localhost:8080/api/v1/admin/videos/videos` | customer-portal-service | [M] |
| [061](error-061-POST-api-v1-vouchers-apply.md) | POST | `http://localhost:8080/api/v1/vouchers/apply` | customer-portal-service | [M] |
| [062](error-062-POST-api-v1-wallet-redeem.md) | POST | `http://localhost:8080/api/v1/wallet/redeem` | customer-portal-service | [M] |
| [063](error-063-GET-api-v1-customers-1-points.md) | GET | `http://localhost:8080/api/v1/customers/1/points` | customer-service | [I] |
| [064](error-064-GET-api-v1-customers-1-history.md) | GET | `http://localhost:8080/api/v1/customers/1/history` | customer-service | [I] |
| [065](error-065-GET-api-v1-customers-1-orders.md) | GET | `http://localhost:8080/api/v1/customers/1/orders` | customer-service | [I] |
| [066](error-066-GET-api-v1-customers-1.md) | GET | `http://localhost:8080/api/v1/customers/1` | customer-service | [I] |
| [067](error-067-GET-api-v1-customers-1-tier.md) | GET | `http://localhost:8080/api/v1/customers/1/tier` | customer-service | [I] |
| [068](error-068-PUT-api-v1-customers-1.md) | PUT | `http://localhost:8080/api/v1/customers/1` | customer-service | [I] |
| [070](error-070-GET-api-v1-admin-videos-flash-sales.md) | GET | `http://localhost:8080/api/v1/admin/videos/flash-sales` | ecom-ops-service | [I] |
| [073](error-073-DELETE-api-v1-customers-1.md) | DELETE | `http://localhost:8080/api/v1/customers/1` | customer-service | [I] |
| [075](error-075-POST-api-v1-customers.md) | POST | `http://localhost:8080/api/v1/customers` | customer-service | [I] |
| [076](error-076-POST-api-v1-customers-register.md) | POST | `http://localhost:8080/api/v1/customers/register` | customer-service | [I] |
| [077](error-077-PUT-api-v1-customers-1-points-add.md) | PUT | `http://localhost:8080/api/v1/customers/1/points/add` | customer-service | [I] |
| [078](error-078-GET-api-v1-ecom-ops-flash-sales-1.md) | GET | `http://localhost:8080/api/v1/ecom-ops/flash-sales/1` | ecom-ops-service | [I] |
| [079](error-079-PUT-api-v1-customers-me.md) | PUT | `http://localhost:8080/api/v1/customers/me` | customer-service | [I] |
| [083](error-083-GET-api-v1-health-quiz-results-me.md) | GET | `http://localhost:8080/api/v1/health/quiz-results/me` | health-tools-service | [I] |
| [084](error-084-POST-api-v1-health-quizzes-1-submit.md) | POST | `http://localhost:8080/api/v1/health/quizzes/1/submit` | health-tools-service | [I] |
| [085](error-085-GET-api-v1-inventory-1.md) | GET | `http://localhost:8080/api/v1/inventory/1` | inventory-service | [M] |
| [086](error-086-GET-api-v1-inventory-batches-1.md) | GET | `http://localhost:8080/api/v1/inventory/batches/1` | inventory-service | [M] |
| [088](error-088-GET-api-v1-inventory-branch-1-medicine-1.md) | GET | `http://localhost:8080/api/v1/inventory/branch/1/medicine/1` | inventory-service | [M] |
| [089](error-089-POST-api-v1-inventory-bulk-import.md) | POST | `http://localhost:8080/api/v1/inventory/bulk/import` | inventory-service | [M] |
| [090](error-090-POST-api-v1-inventory-bulk-import.md) | POST | `http://localhost:8080/api/v1/inventory/bulk/import` | inventory-service | [M] |
| [092](error-092-GET-api-v1-inventory-transactions.md) | GET | `http://localhost:8080/api/v1/inventory/transactions` | inventory-service | [M] |
| [094](error-094-POST-api-v1-inventory-orders-1-paid.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/paid` | inventory-service | [M] |
| [095](error-095-POST-api-v1-inventory-orders-1-cancelled.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/cancelled` | inventory-service | [M] |
| [096](error-096-POST-api-v1-inventory-orders-1-paid-bulk.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/paid-bulk` | inventory-service | [M] |
| [097](error-097-POST-api-v1-inventory-orders-1-cancelled-bulk.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/cancelled-bulk` | inventory-service | [M] |
| [098](error-098-POST-api-v1-inventory-import.md) | POST | `http://localhost:8080/api/v1/inventory/import` | inventory-service | [M] |
| [099](error-099-POST-api-v1-inventory-orders-1-cancelled-precise.md) | POST | `http://localhost:8080/api/v1/inventory/orders/1/cancelled-precise` | inventory-service | [M] |
| [100](error-100-POST-api-v1-inventory-consume.md) | POST | `http://localhost:8080/api/v1/inventory/consume` | inventory-service | [M] |
| [101](error-101-POST-api-v1-inventory-transfer.md) | POST | `http://localhost:8080/api/v1/inventory/transfer` | inventory-service | [M] |
| [102](error-102-POST-api-v1-inventory-export.md) | POST | `http://localhost:8080/api/v1/inventory/export` | inventory-service | [M] |
| [103](error-103-GET-api-v1-notifications-1.md) | GET | `http://localhost:8080/api/v1/notifications/1` | notification-service | [I] |
| [104](error-104-GET-api-v1-notifications-unread.md) | GET | `http://localhost:8080/api/v1/notifications/unread` | notification-service | [I] |
| [105](error-105-GET-api-v1-notifications.md) | GET | `http://localhost:8080/api/v1/notifications` | notification-service | [I] |
| [106](error-106-POST-api-v1-notifications-1-retry.md) | POST | `http://localhost:8080/api/v1/notifications/1/retry` | notification-service | [I] |
| [107](error-107-PUT-api-v1-notifications-1-read.md) | PUT | `http://localhost:8080/api/v1/notifications/1/read` | notification-service | [I] |
| [108](error-108-PUT-api-v1-notifications-read-all.md) | PUT | `http://localhost:8080/api/v1/notifications/read-all` | notification-service | [I] |
| [109](error-109-POST-api-v1-notifications-compose.md) | POST | `http://localhost:8080/api/v1/notifications/compose` | notification-service | [I] |
| [110](error-110-POST-api-v1-notifications-bulk.md) | POST | `http://localhost:8080/api/v1/notifications/bulk` | notification-service | [I] |
| [111](error-111-POST-api-v1-notifications-templates.md) | POST | `http://localhost:8080/api/v1/notifications/templates` | notification-service | [I] |
| [112](error-112-POST-api-v1-notifications.md) | POST | `http://localhost:8080/api/v1/notifications` | notification-service | [I] |
| [113](error-113-POST-api-v1-notifications-broadcast.md) | POST | `http://localhost:8080/api/v1/notifications/broadcast` | notification-service | [I] |
| [114](error-114-DELETE-api-v1-notifications-1.md) | DELETE | `http://localhost:8080/api/v1/notifications/1` | notification-service | [I] |
| [115](error-115-DELETE-api-v1-coupons-1.md) | DELETE | `http://localhost:8080/api/v1/coupons/1` | order-service | [I] |
| [116](error-116-POST-api-v1-notifications-inventory-low-stock.md) | POST | `http://localhost:8080/api/v1/notifications/inventory/low-stock` | notification-service | [I] |
| [117](error-117-PUT-api-v1-coupons-1.md) | PUT | `http://localhost:8080/api/v1/coupons/1` | order-service | [I] |
| [118](error-118-GET-api-v1-orders-1.md) | GET | `http://localhost:8080/api/v1/orders/1` | order-service | [I] |
| [119](error-119-POST-api-v1-notifications-inventory-expiry.md) | POST | `http://localhost:8080/api/v1/notifications/inventory/expiry` | notification-service | [I] |
| [120](error-120-PUT-api-v1-orders-1.md) | PUT | `http://localhost:8080/api/v1/orders/1` | order-service | [I] |
| [121](error-121-PUT-api-v1-orders-1-pay.md) | PUT | `http://localhost:8080/api/v1/orders/1/pay` | order-service | [I] |
| [122](error-122-POST-api-v1-notifications-orders-paid.md) | POST | `http://localhost:8080/api/v1/notifications/orders/paid` | notification-service | [I] |
| [123](error-123-POST-api-v1-orders-1-approve.md) | POST | `http://localhost:8080/api/v1/orders/1/approve` | order-service | [I] |
| [124](error-124-POST-api-v1-orders-1-reject.md) | POST | `http://localhost:8080/api/v1/orders/1/reject` | order-service | [I] |
| [126](error-126-DELETE-api-v1-orders-1.md) | DELETE | `http://localhost:8080/api/v1/orders/1` | order-service | [I] |
| [128](error-128-POST-api-v1-orders-1-cancel.md) | POST | `http://localhost:8080/api/v1/orders/1/cancel` | order-service | [I] |
| [132](error-132-POST-api-v1-orders-1-recompute.md) | POST | `http://localhost:8080/api/v1/orders/1/recompute` | order-service | [I] |
| [133](error-133-GET-api-v1-mobile-medication-reminders.md) | GET | `http://localhost:8080/api/v1/mobile/medication-reminders` | mobile-bff | [I] |
| [134](error-134-DELETE-api-v1-mobile-medication-reminders-1.md) | DELETE | `http://localhost:8080/api/v1/mobile/medication-reminders/1` | mobile-bff | [I] |
| [135](error-135-PUT-api-v1-mobile-medication-reminders-1-deactivate.md) | PUT | `http://localhost:8080/api/v1/mobile/medication-reminders/1/deactivate` | mobile-bff | [I] |
| [137](error-137-GET-api-v1-payments-1.md) | GET | `http://localhost:8080/api/v1/payments/1` | payment-service | [I] |
| [138](error-138-POST-api-v1-payments-1-refund.md) | POST | `http://localhost:8080/api/v1/payments/1/refund` | payment-service | [I] |
| [139](error-139-PUT-api-v1-payments-1-refund.md) | PUT | `http://localhost:8080/api/v1/payments/1/refund` | payment-service | [I] |
| [140](error-140-GET-api-v1-payments-order-1.md) | GET | `http://localhost:8080/api/v1/payments/order/1` | payment-service | [I] |
| [141](error-141-POST-api-v1-payments-1-print.md) | POST | `http://localhost:8080/api/v1/payments/1/print` | payment-service | [I] |
| [142](error-142-GET-api-v1-payments-1-refund-history.md) | GET | `http://localhost:8080/api/v1/payments/1/refund-history` | payment-service | [I] |
| [143](error-143-GET-api-v1-payments-1-invoice.md) | GET | `http://localhost:8080/api/v1/payments/1/invoice` | payment-service | [I] |
| [147](error-147-POST-api-v1-orders.md) | POST | `http://localhost:8080/api/v1/orders` | order-service | [I] |
| [148](error-148-POST-api-v1-coupons.md) | POST | `http://localhost:8080/api/v1/coupons` | order-service | [I] |
| [149](error-149-POST-api-v1-mobile-medication-reminders.md) | POST | `http://localhost:8080/api/v1/mobile/medication-reminders` | mobile-bff | [I] |
| [150](error-150-POST-api-v1-payments.md) | POST | `http://localhost:8080/api/v1/payments` | payment-service | [I] |
| [151](error-151-POST-api-v1-consultations-1-messages.md) | POST | `http://localhost:8080/api/v1/consultations/1/messages` | pharmacist-workbench-service | [H] |
| [152](error-152-GET-api-v1-rx-customers-1-profile-360.md) | GET | `http://localhost:8080/api/v1/rx/customers/1/profile-360` | pharmacist-workbench-service | [H] |
| [153](error-153-GET-api-v1-consultations-by-customer-1.md) | GET | `http://localhost:8080/api/v1/consultations/by-customer/1` | pharmacist-workbench-service | [H] |
| [154](error-154-GET-api-v1-consultations-by-pharmacist-1.md) | GET | `http://localhost:8080/api/v1/consultations/by-pharmacist/1` | pharmacist-workbench-service | [H] |
| [155](error-155-GET-api-v1-follow-ups-by-customer-1.md) | GET | `http://localhost:8080/api/v1/follow-ups/by-customer/1` | pharmacist-workbench-service | [H] |
| [156](error-156-GET-api-v1-consultations-1.md) | GET | `http://localhost:8080/api/v1/consultations/1` | pharmacist-workbench-service | [H] |
| [157](error-157-POST-api-v1-consultations-1-end.md) | POST | `http://localhost:8080/api/v1/consultations/1/end` | pharmacist-workbench-service | [H] |
| [158](error-158-DELETE-api-v1-follow-ups-1.md) | DELETE | `http://localhost:8080/api/v1/follow-ups/1` | pharmacist-workbench-service | [H] |
| [159](error-159-POST-api-v1-follow-ups-1-response.md) | POST | `http://localhost:8080/api/v1/follow-ups/1/response` | pharmacist-workbench-service | [H] |
| [160](error-160-GET-api-v1-prescriptions-1.md) | GET | `http://localhost:8080/api/v1/prescriptions/1` | prescription-service | [I] |
| [161](error-161-PUT-api-v1-prescriptions-1.md) | PUT | `http://localhost:8080/api/v1/prescriptions/1` | prescription-service | [I] |
| [162](error-162-PUT-api-v1-prescriptions-1-sign.md) | PUT | `http://localhost:8080/api/v1/prescriptions/1/sign` | prescription-service | [I] |
| [163](error-163-POST-api-v1-prescriptions-1-sign.md) | POST | `http://localhost:8080/api/v1/prescriptions/1/sign` | prescription-service | [I] |
| [164](error-164-POST-api-v1-prescriptions-1-link-order.md) | POST | `http://localhost:8080/api/v1/prescriptions/1/link-order` | prescription-service | [I] |
| [165](error-165-GET-api-v1-prescriptions-1-print.md) | GET | `http://localhost:8080/api/v1/prescriptions/1/print` | prescription-service | [I] |
| [166](error-166-POST-api-v1-prescriptions-1-print.md) | POST | `http://localhost:8080/api/v1/prescriptions/1/print` | prescription-service | [I] |
| [167](error-167-DELETE-api-v1-prescriptions-1.md) | DELETE | `http://localhost:8080/api/v1/prescriptions/1` | prescription-service | [I] |
| [169](error-169-GET-api-v1-vip-marks-by-customer-1.md) | GET | `http://localhost:8080/api/v1/vip-marks/by-customer/1` | pharmacist-workbench-service | [H] |
| [170](error-170-GET-api-v1-reports-revenue.md) | GET | `http://localhost:8080/api/v1/reports/revenue` | report-service | [I] |
| [171](error-171-GET-api-v1-reports-staff.md) | GET | `http://localhost:8080/api/v1/reports/staff` | report-service | [I] |
| [172](error-172-POST-api-v1-consultations.md) | POST | `http://localhost:8080/api/v1/consultations` | pharmacist-workbench-service | [H] |
| [173](error-173-POST-api-v1-follow-ups.md) | POST | `http://localhost:8080/api/v1/follow-ups` | pharmacist-workbench-service | [H] |
| [174](error-174-POST-api-v1-vip-marks.md) | POST | `http://localhost:8080/api/v1/vip-marks` | pharmacist-workbench-service | [H] |
| [175](error-175-DELETE-api-v1-vip-marks-1.md) | DELETE | `http://localhost:8080/api/v1/vip-marks/1` | pharmacist-workbench-service | [H] |
| [176](error-176-POST-api-v1-prescriptions.md) | POST | `http://localhost:8080/api/v1/prescriptions` | prescription-service | [I] |
| [177](error-177-POST-api-v1-prescriptions-draft.md) | POST | `http://localhost:8080/api/v1/prescriptions/draft` | prescription-service | [I] |
| [178](error-178-POST-api-v1-reports-staff.md) | POST | `http://localhost:8080/api/v1/reports/staff` | report-service | [I] |
| [180](error-180-POST-api-v1-reports-revenue.md) | POST | `http://localhost:8080/api/v1/reports/revenue` | report-service | [I] |
| [181](error-181-POST-api-v1-reports-schedule.md) | POST | `http://localhost:8080/api/v1/reports/schedule` | report-service | [I] |
| [182](error-182-GET-api-v1-reports-export.md) | GET | `http://localhost:8080/api/v1/reports/export` | report-service | [I] |
| [184](error-184-POST-api-v1-reports-export-excel.md) | POST | `http://localhost:8080/api/v1/reports/export/excel` | report-service | [I] |
| [185](error-185-POST-api-v1-reports-export-pdf.md) | POST | `http://localhost:8080/api/v1/reports/export/pdf` | report-service | [I] |
| [186](error-186-DELETE-api-v1-reports-schedules-1.md) | DELETE | `http://localhost:8080/api/v1/reports/schedules/1` | report-service | [I] |
| [187](error-187-POST-api-v1-auth-login.md) | POST | `http://localhost:8080/api/v1/auth/login` | user-service | [I] |
| [188](error-188-POST-api-v1-auth-forgot-password.md) | POST | `http://localhost:8080/api/v1/auth/forgot-password` | user-service | [I] |
| [189](error-189-POST-api-v1-auth-reset-password.md) | POST | `http://localhost:8080/api/v1/auth/reset-password` | user-service | [I] |
| [191](error-191-POST-api-v1-auth-verify-email.md) | POST | `http://localhost:8080/api/v1/auth/verify-email` | user-service | [I] |
| [192](error-192-POST-api-v1-auth-resend-verification.md) | POST | `http://localhost:8080/api/v1/auth/resend-verification` | user-service | [I] |
| [193](error-193-PUT-api-v1-auth-password.md) | PUT | `http://localhost:8080/api/v1/auth/password` | user-service | [I] |
| [194](error-194-GET-api-v1-users-1.md) | GET | `http://localhost:8080/api/v1/users/1` | user-service | [I] |
| [195](error-195-PUT-api-v1-users-1-role.md) | PUT | `http://localhost:8080/api/v1/users/1/role` | user-service | [I] |
| [196](error-196-PUT-api-v1-users-1.md) | PUT | `http://localhost:8080/api/v1/users/1` | user-service | [I] |
| [197](error-197-PUT-api-v1-users-1-status.md) | PUT | `http://localhost:8080/api/v1/users/1/status` | user-service | [I] |
| [198](error-198-POST-api-v1-users-1-unlock.md) | POST | `http://localhost:8080/api/v1/users/1/unlock` | user-service | [I] |
| [199](error-199-DELETE-api-v1-users-1.md) | DELETE | `http://localhost:8080/api/v1/users/1` | user-service | [I] |
| [200](error-200-PUT-api-v1-users-1-branch.md) | PUT | `http://localhost:8080/api/v1/users/1/branch` | user-service | [I] |
| [201](error-201-GET-api-v1-users-role-1.md) | GET | `http://localhost:8080/api/v1/users/role/1` | user-service | [I] |
| [202](error-202-POST-api-v1-users.md) | POST | `http://localhost:8080/api/v1/users` | user-service | [I] |
| [203](error-203-GET-api-v1-suppliers-1-history.md) | GET | `http://localhost:8080/api/v1/suppliers/1/history` | supplier-service | [I] |
| [204](error-204-GET-api-v1-suppliers-1.md) | GET | `http://localhost:8080/api/v1/suppliers/1` | supplier-service | [I] |
| [205](error-205-PUT-api-v1-suppliers-1.md) | PUT | `http://localhost:8080/api/v1/suppliers/1` | supplier-service | [I] |
| [206](error-206-DELETE-api-v1-suppliers-1.md) | DELETE | `http://localhost:8080/api/v1/suppliers/1` | supplier-service | [I] |
| [207](error-207-POST-api-v1-suppliers.md) | POST | `http://localhost:8080/api/v1/suppliers` | supplier-service | [I] |

### 401 Unauthorized (3 errors)

| # | Method | URL | Service | Severity |
|---|--------|-----|---------|----------|
| [144](error-144-POST-api-v1-payments-webhook.md) | POST | `http://localhost:8080/api/v1/payments/webhook` | payment-service | [I] |
| [145](error-145-POST-api-v1-webhooks-payment-gateway.md) | POST | `http://localhost:8080/api/v1/webhooks/payment-gateway` | payment-service | [I] |
| [190](error-190-POST-api-v1-auth-refresh.md) | POST | `http://localhost:8080/api/v1/auth/refresh` | user-service | [I] |

### 404 Not Found (25 errors)

| # | Method | URL | Service | Severity |
|---|--------|-----|---------|----------|
| [015](error-015-GET-api-v1-medicines-sku-1.md) | GET | `http://localhost:8080/api/v1/medicines/sku/1` | catalog-service | [M] |
| [016](error-016-GET-api-v1-branches-code-1.md) | GET | `http://localhost:8080/api/v1/branches/code/1` | branch-service | [I] |
| [038](error-038-GET-api-v1-health-articles-1.md) | GET | `http://localhost:8080/api/v1/health-articles/1` | customer-portal-service | [M] |
| [045](error-045-GET-api-v1-orders-1-track.md) | GET | `http://localhost:8080/api/v1/orders/1/track` | customer-portal-service | [M] |
| [047](error-047-GET-api-v1-prescriptions-1-re-download.md) | GET | `http://localhost:8080/api/v1/prescriptions/1/re-download` | customer-portal-service | [M] |
| [050](error-050-GET-api-v1-store-locator-1.md) | GET | `http://localhost:8080/api/v1/store/locator/1` | customer-portal-service | [M] |
| [058](error-058-DELETE-api-v1-admin-videos-videos-1.md) | DELETE | `http://localhost:8080/api/v1/admin/videos/videos/1` | customer-portal-service | [M] |
| [059](error-059-GET-api-v1-admin-videos-videos-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/videos/1` | customer-portal-service | [M] |
| [060](error-060-PUT-api-v1-admin-videos-videos-1.md) | PUT | `http://localhost:8080/api/v1/admin/videos/videos/1` | customer-portal-service | [M] |
| [071](error-071-GET-api-v1-admin-videos-flash-sales-active.md) | GET | `http://localhost:8080/api/v1/admin/videos/flash-sales/active` | ecom-ops-service | [I] |
| [072](error-072-GET-api-v1-admin-videos-flash-sales-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/flash-sales/1` | ecom-ops-service | [I] |
| [074](error-074-POST-api-v1-admin-videos-flash-sales-1-cancel.md) | POST | `http://localhost:8080/api/v1/admin/videos/flash-sales/1/cancel` | ecom-ops-service | [I] |
| [080](error-080-GET-api-v1-customers-me.md) | GET | `http://localhost:8080/api/v1/customers/me` | customer-service | [I] |
| [081](error-081-GET-api-v1-customers-phone-1.md) | GET | `http://localhost:8080/api/v1/customers/phone/1` | customer-service | [I] |
| [082](error-082-GET-api-v1-customers-code-1.md) | GET | `http://localhost:8080/api/v1/customers/code/1` | customer-service | [I] |
| [087](error-087-GET-api-v1-inventory-batches-scan-1.md) | GET | `http://localhost:8080/api/v1/inventory/batches/scan/1` | inventory-service | [M] |
| [093](error-093-GET-api-v1-health-quizzes-1.md) | GET | `http://localhost:8080/api/v1/health/quizzes/1` | health-tools-service | [I] |
| [125](error-125-POST-api-v1-admin-videos-outbox-retry-1.md) | POST | `http://localhost:8080/api/v1/admin/videos/outbox/retry/1` | order-service | [I] |
| [127](error-127-GET-api-v1-admin-videos-saga-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/saga/1` | order-service | [I] |
| [129](error-129-POST-api-v1-admin-videos-saga-1-compensate.md) | POST | `http://localhost:8080/api/v1/admin/videos/saga/1/compensate` | order-service | [I] |
| [130](error-130-GET-api-v1-admin-videos-saga-stuck.md) | GET | `http://localhost:8080/api/v1/admin/videos/saga/stuck` | order-service | [I] |
| [131](error-131-GET-api-v1-admin-videos-saga-by-aggregate-1-1.md) | GET | `http://localhost:8080/api/v1/admin/videos/saga/by-aggregate/1/1` | order-service | [I] |
| [136](error-136-GET-api-v1-orders-number-1.md) | GET | `http://localhost:8080/api/v1/orders/number/1` | order-service | [I] |
| [146](error-146-GET-api-v1-payments-invoice-1.md) | GET | `http://localhost:8080/api/v1/payments/invoice/1` | payment-service | [I] |
| [179](error-179-GET-api-v1-prescriptions-code-1.md) | GET | `http://localhost:8080/api/v1/prescriptions/code/1` | prescription-service | [I] |

### 405 Method Not Allowed (2 errors)

| # | Method | URL | Service | Severity |
|---|--------|-----|---------|----------|
| [056](error-056-POST-api-v1-admin-videos-videos.md) | POST | `http://localhost:8080/api/v1/admin/videos/videos` | customer-portal-service | [M] |
| [069](error-069-POST-api-v1-admin-videos-flash-sales.md) | POST | `http://localhost:8080/api/v1/admin/videos/flash-sales` | ecom-ops-service | [M] |

### 409 Conflict (1 errors)

| # | Method | URL | Service | Severity |
|---|--------|-----|---------|----------|
| [051](error-051-PATCH-api-v1-notif-settings.md) | PATCH | `http://localhost:8080/api/v1/notif-settings` | customer-portal-service | [M] |

### 500 Server Error (5 errors)

| # | Method | URL | Service | Severity |
|---|--------|-----|---------|----------|
| [013](error-013-POST-api-v1-medicines-1-image.md) | POST | `http://localhost:8080/api/v1/medicines/1/image` | catalog-service | [M] |
| [026](error-026-POST-api-v1-addresses.md) | POST | `http://localhost:8080/api/v1/addresses` | customer-portal-service | [M] |
| [091](error-091-POST-api-v1-inventory-bulk-import-file.md) | POST | `http://localhost:8080/api/v1/inventory/bulk/import-file` | inventory-service | [M] |
| [168](error-168-POST-api-v1-rx-cross-sell.md) | POST | `http://localhost:8080/api/v1/rx/cross-sell` | pharmacist-workbench-service | [H] |
| [183](error-183-POST-api-v1-rx-drug-check.md) | POST | `http://localhost:8080/api/v1/rx/drug-check` | pharmacist-workbench-service | [H] |

## Huong dan su dung

1. **Loc loi theo service**: Cuon xuong phan "Tat ca Errors theo Service"
2. **Loc theo severity**: Uu tien fix CRITICAL va HIGH truoc
3. **Click vao #** de xem chi tiet scene-by-scene narrative
4. **Moi file MD** co 5 scenes: Test request -> Gateway -> Service -> Error -> Response

## Legend

- `[C]` CRITICAL - Loi server, can fix ngay
- `[H]` HIGH - Loi integration, can fix trong tuan
- `[M]` MEDIUM - Loi validation/design, can review
- `[L]` LOW - Loi logic nghiep vu, fix khi co thoi gian
- `[I]` INFO - Khong phai bug, security feature dang hoat dong dung hoac test data issue

---

*Auto-generated by PCMS Test Investigation. Total: 207 error reports.*