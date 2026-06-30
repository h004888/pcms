# BACKEND API GAPS — PCMS

**Ngày kiểm:** 2026-06-24
**Phạm vi:** So sánh API backend (`pcms/`) với nhu cầu tích hợp của frontend (`pcms-frontend/`)
**Tổng quan:** 19 microservices (18 Spring Boot + 1 Python FastAPI), ~250+ endpoints
**Mức độ sẵn sàng tích hợp:** **~62%** (68/110 endpoints yêu cầu)

---

## Tổng quan Backend

| Service | Tech | Routes | Public path prefix |
|---|---|---|---|
| `user-service` | Spring Boot | 27 | `/auth`, `/users`, `/dashboard`, `/audit-logs` |
| `branch-service` | Spring Boot | 9 | `/branches` |
| `catalog-service` | Spring Boot | 17 | `/medicines`, `/search` |
| `category-service` | Spring Boot | 5 | `/categories` |
| `inventory-service` | Spring Boot | 28 | `/inventory` |
| `order-service` | Spring Boot | 16 | `/orders`, `/coupons` |
| `payment-service` | Spring Boot | 13 | `/payments`, `/webhooks` |
| `customer-service` | Spring Boot | 13 | `/customers` |
| `supplier-service` | Spring Boot | 6 | `/suppliers` |
| `prescription-service` | Spring Boot | 13 | `/prescriptions` |
| `notification-service` | Spring Boot | 16 | `/notifications`, `/notifications/templates` |
| `report-service` | Spring Boot | 14 | `/reports` |
| `customer-portal-service` | Spring Boot | ~50 | `/shop`, `/store`, `/vaccines`, `/addresses`, `/cart`, `/wallet`, `/vouchers`, ... |
| `pharmacist-workbench-service` | Spring Boot | 17 | `/consultations`, `/rx`, `/follow-ups`, `/vip-marks` |
| `mobile-bff` | Spring Boot | 7 | `/mobile`, `/mobile/medication-reminders` |
| `health-tools-service` | Spring Boot | 5 | `/health` |
| `ecom-ops-service` | Spring Boot | 9 | `/ecom-ops/flash-sales`, `/admin/flash-sales`, `/admin/vouchers`, `/admin/reviews` |
| `ai-engine-service` | **Python FastAPI** | ~20 | `/ai/chat`, `/ai/ocr`, `/ai/drug-check`, `/ai/semantic-search`, `/ai/cross-sell`, ... |

**API Gateway routing** đã được cấu hình đầy đủ trong `pcms/api-gateway/src/main/resources/application.yml` với `StripPrefix=2` cho mọi service.

---

## ✅ PHẦN 1: ĐÃ CÓ SẴN & KHỚP (kết nối được luôn — 68 endpoints)

### 1.1 Auth & Dashboard
- `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me`, `POST /auth/logout`, `POST /auth/forgot-password`, `POST /auth/reset-password` (user-service)
- `GET /dashboard/stats`, `GET /dashboard/recent-logins` (user-service)
- `GET/POST/PUT/DELETE /audit-logs` (user-service)

### 1.2 Admin modules (đầy đủ CRUD)
- **Branches:** `GET/POST/PUT/DELETE /branches`, `GET /branches/{id}`, `/code/{code}`, `/{id}/staff`, `/{id}/manager`
- **Categories:** `GET/POST/PUT/DELETE /categories`
- **Medicines:** `GET/POST/PUT/DELETE /medicines`, `/sku/{sku}`, `/count`, `/export`, `/{id}/image`
- **Inventory:** `GET /inventory`, `/batches`, `/low-stock`, `/alerts/low-stock`, `/expiring`, `/alerts/expiry`, `/report/stock-level`, `/report/movement`, `/transactions`; `POST /inventory/import`, `/export`, `/consume`, `/transfer`, `/bulk/import`, `/bulk/import-file`
- **Orders:** `GET/POST/PUT/DELETE /orders`, `POST /orders/{id}/approve`, `/reject`, `/cancel`, `/pay`, `/recompute`
- **Payments:** `GET/POST /payments`, `POST /payments/{id}/refund`, `GET /payments/{id}/refund-history`, `/{id}/invoice`, `/{id}/print`
- **Customers (admin):** `GET/POST/PUT/DELETE /customers`, `/phone/{phone}`, `/code/{code}`, `/{id}/tier`, `/{id}/orders`, `/{id}/points`, `/{id}/history`, `PUT /customers/{id}/points/add`
- **Suppliers:** `GET/POST/PUT/DELETE /suppliers`, `/{id}/history`
- **Prescriptions:** `GET/POST/PUT/DELETE /prescriptions`, `/code/{code}`, `POST /prescriptions/draft`, `PUT/POST /{id}/sign`, `POST /{id}/link-order`, `GET /{id}/print`
- **Notifications:** `GET/POST /notifications`, `/unread`, `/{id}`, `POST /bulk`, `/broadcast`, `/compose`, `/{id}/retry`, `PUT /{id}/read`, `/read-all`
- **Reports:** `GET/POST /reports/revenue`, `/inventory`, `/staff`, `/realtime/stats`, `/realtime/recent-orders`, `/export`, `POST /schedule`, `GET /schedules`, `POST /export/excel`, `/export/pdf`
- **Coupon admin:** `GET/POST/PUT/DELETE /coupons` (order-service)
- **Saga admin:** `GET /admin/saga/{sagaId}`, `/by-aggregate/{type}/{id}`, `/stuck`; `POST /admin/saga/{sagaId}/compensate`
- **Outbox admin:** `POST /admin/outbox/retry/{id}`

### 1.3 B2C — Customer Portal (đầy đủ)
- **Shop:** `GET /shop/home`, `/shop/pdp/{id}`, `/shop/search`
- **Lookup:** `GET /lookup/drug`, `/lookup/ingredient`, `/lookup/herb`
- **Store locator:** `GET /store/locator`, `/store/locator/{branchId}`
- **Vaccines:** `GET /vaccines`, `GET /vaccines/{id}/slots`, `POST /vaccine-bookings`, `GET /vaccine-bookings/me`, `DELETE /vaccine-bookings/{id}`, `GET /vaccination-ledger/me`
- **Health articles:** `GET /health-articles`, `GET /health-articles/{slug}`
- **Diseases (list):** `GET /diseases`
- **Verify origin:** `POST /verify-origin/scan`
- **Addresses:** `GET/POST/PUT/DELETE /addresses`, `PUT /addresses/{id}/default`
- **Favorites:** `GET/POST/DELETE /favorites`, `GET /favorites/{medicineId}/check`
- **Family:** `GET/POST/PUT/DELETE /family`
- **Cart:** `GET /cart`, `POST /cart/items`, `PUT /cart/items/{itemId}`, `DELETE /cart/items/{itemId}`, `DELETE /cart`, `POST /cart/checkout/preview`, `POST /cart/checkout/confirm`
- **Wallet:** `GET /wallet`, `GET /wallet/transactions`, `POST /wallet/redeem`
- **Vouchers:** `GET /vouchers`, `POST /vouchers/apply`, `GET /vouchers/history`
- **Installment:** `POST /installment/quote`, `POST /installment/confirm`
- **Notif settings:** `GET/PUT/PATCH /notif-settings`
- **Order tracking:** `GET /orders/{id}/track`, `GET /orders/history`
- **Profile (B2C):** `POST /customers/register`, `GET /customers/me`, `PUT /customers/me`

### 1.4 Pharmacist Workbench (UC16)
- **Consultations:** `POST /consultations`, `GET /consultations/{id}`, `POST /consultations/{id}/end`, `POST /consultations/{id}/messages`, `GET /consultations/by-customer/{customerId}`, `/by-pharmacist/{pharmacistId}`
- **Customer 360:** `GET /rx/customers/{id}/profile-360`
- **Cross-sell:** `POST /rx/cross-sell`, `POST /ai/cross-sell`
- **Drug-check:** `POST /rx/drug-check`, `POST /ai/drug-check/check`
- **Follow-ups:** `POST /follow-ups`, `GET /follow-ups/by-customer/{customerId}`, `POST /follow-ups/{id}/response`, `DELETE /follow-ups/{id}`
- **VIP marks:** `POST /vip-marks`, `GET /vip-marks/by-customer/{customerId}`, `/by-tier/{tier}`, `GET /vip-marks`, `DELETE /vip-marks/{customerId}`

### 1.5 Mobile BFF (UC17)
- **Home:** `GET /mobile/home`, `GET /mobile/nearby-pharmacies`
- **Medication reminders:** `POST/GET /mobile/medication-reminders`, `PUT /mobile/medication-reminders/{id}/deactivate`, `DELETE /mobile/medication-reminders/{id}`

### 1.6 Health Tools (UC18)
- **Quizzes:** `GET /health/quizzes`, `GET /health/quizzes/{slug}`, `POST /health/quizzes/{slug}/submit`, `GET /health/quiz-results/me`

### 1.7 E-com Ops (UC19)
- **Flash-sale B2C:** `GET /ecom-ops/flash-sales/active`, `GET /ecom-ops/flash-sales/{id}`
- **Flash-sale admin:** `POST /admin/flash-sales`, `GET /admin/flash-sales/active`, `GET /admin/flash-sales`, `GET /admin/flash-sales/{id}`, `POST /admin/flash-sales/{id}/cancel`
- **Admin videos:** `GET/POST/PUT/DELETE /admin/videos`
- **Admin vouchers / reviews:** routes đã khai báo trong gateway (chưa thấy controller)

### 1.8 AI Engine (UC15 — Python FastAPI)
- **Chat:** `POST /api/v1/ai/chat/...`, `GET /api/v1/ai/chat/sessions/{session_id}`, `POST /api/v1/ai/chat/sessions/{session_id}/escalate`, `/end`
- **OCR toa thuốc:** `POST /api/v1/ai/ocr/prescription`, `GET /api/v1/ai/ocr/prescription/{job_id}`
- **Drug-check:** `POST /api/v1/ai/drug-check/check`
- **Semantic search:** `POST /api/v1/ai/semantic-search`
- **Dosage check:** `POST /api/v1/ai/dosage/check`
- **Forecast:** `GET /api/v1/ai/forecast/{medicine_id}`
- **Anomaly:** `POST /api/v1/ai/anomaly/prescription`
- **Summary:** `POST /api/v1/ai/summary`
- **Moderation:** (route đã khai báo trong main.py)
- **Cross-sell:** `POST /api/v1/ai/cross-sell`

### 1.9 Search (B2C/B2B)
- `GET /search?q=`, `/search/medicines/autocomplete`, `/search/medicines`, `/search/full`

---

## ⚠️ PHẦN 2: CÓ MỘT PHẦN — Cần bổ sung / điều chỉnh path

| # | Vấn đề | Backend hiện tại | FE yêu cầu | Hướng xử lý |
|---|---|---|---|---|
| 1 | Diseases detail page | `GET /diseases` (list only) | `GET /diseases/{slug}` (detail) | **Thêm `GET /diseases/{slug}` vào `HealthContentController`** (customer-portal-service) |
| 2 | Vouchers redeem | `POST /vouchers/apply` (validate) | `POST /vouchers/redeem` (lưu vào ví KH) | **Đổi tên hoặc bổ sung `POST /vouchers/redeem`** (customer-portal-service) |
| 3 | Wallet topup | `POST /wallet/redeem` (rút điểm) | `POST /customers/me/wallet/topup` (nạp tiền) | **Thêm `POST /wallet/topup`** (customer-portal-service) |
| 4 | Upload toa thuốc (B2C) | `POST /prescriptions` (admin, kê đơn) | `POST /prescriptions/upload` (KH upload ảnh) | **Tạo mới `POST /prescriptions/upload` cho B2C** (prescription-service) |
| 5 | Bài viết path slug | `/health-articles/{slug}` | FE hardcode `/bai-viet/[slug]` | **FE rewrite path sang `/health-articles`** (frontend fix, không cần BE) |
| 6 | Favorites path | `/favorites/...` (flat) | FE dùng `/customers/me/favorites/...` | **Cập nhật FE services sang flat path `/favorites`** (frontend fix) |
| 7 | Notif settings path | `/notif-settings` | FE dùng `/customers/me/notif-settings` | **Cập nhật FE path sang `/notif-settings`** (frontend fix) |
| 8 | Customer points (B2C) | `GET /customers/{id}/points` (admin) | `GET /customers/me/points` (B2C) | **Thêm `GET /customers/me/points` + `GET /customers/me/points/history`** (customer-service) |
| 9 | Cancers (`/chuyen-trang-ung-thu`) | Không có endpoint riêng | FE cần list + detail | **Thêm `GET /cancers`, `GET /cancers/{slug}`** (customer-portal-service) |
| 10 | Public videos | Chỉ `/admin/videos` CRUD | FE cần `GET /videos` cho B2C | **Thêm `GET /videos` + `GET /videos/{id}` public** (customer-portal-service — controller mới) |
| 11 | Live chat (real-time) | Không có | FE cần WebSocket/SSE | **Thêm module chat** (ai-engine-service hoặc tách chat-service) |
| 12 | Policies (CMS) | Không có | FE cần `GET /policies/{slug}` | **Tạo CMS service mới** hoặc thêm vào customer-portal-service |
| 13 | Drug-check path qua gateway | `/api/v1/ai/drug-check/check` (Python) | FE cần `/api/v1/ai/drug-check` (proxy) | **Cập nhật api-gateway route** — đổi path filter `/api/v1/ai/drug-check/**` thay vì `/api/v1/ai/**` rồi rewrite |
| 14 | Vouchers history filter | `GET /vouchers/history` | FE cần `?status=available\|used\|expired` | **Thêm query param `status`** (customer-portal-service) |
| 15 | Bài viết `bai-viet` URL | `/health-articles` (BE) | FE URL: `/bai-viet` | **Giữ URL FE, đổi path trong service mapping** |
| 16 | Chat session escalate/end | `POST /api/v1/ai/chat/sessions/{session_id}/escalate\|/end` | FE cần `POST /ai/chat/{sessionId}/escalate` | **api-gateway route `/ai/chat/sessions/**` đã đúng, kiểm tra FE path builder** |
| 17 | Sổ tiêm cá nhân path | `GET /vaccination-ledger/me` (đúng) | FE cần `GET /customers/me/vaccination-ledger` | **FE rewrite path** |
| 18 | Family/favorites/wallet path scoping | `/family`, `/favorites`, `/wallet` (flat) | FE dùng `/customers/me/...` | **Cập nhật FE service mapping** |
| 19 | Diseases (bệnh) URL slug | `/diseases/{slug}` cần thêm (chưa có) | FE URL: `/benh-thuong-gap/[slug]` | **Thêm endpoint** |
| 20 | Lookup dược liệu/hoạt chất chỉ search | `/lookup/herb`, `/lookup/ingredient` (chỉ search) | FE cần list + filter + detail | **Thêm `GET /lookup/drug?category=&q=` + `GET /lookup/herb/{slug}` + `GET /lookup/ingredient/{slug}`** |

---

## ❌ PHẦN 3: CHƯA CÓ — Cần tạo mới (ưu tiên cao)

### 3.1 Catalog B2C (P0 — blocker)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 1 | `GET /products/{slug}` (B2C PDP chi tiết) | customer-portal-service (mở rộng `ShopController`) | `/tra-cuu-thuoc/[slug]`, `/[slug]/[subSlug]/[productSlug]` |
| 2 | `GET /categories/tree` (L1→L2 cây phân cấp) | category-service | `/[slug]`, `/[slug]/[subSlug]` |
| 3 | `GET /provinces` (danh sách tỉnh/thành) | customer-portal-service (mở rộng `StoreController`) | `/he-thong-cua-hang` filter |

### 3.2 Reviews module (P0 — blocker)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 4 | `GET /products/{slug}/reviews?page=&rating=` | customer-portal-service (controller mới `ReviewController`) | `/reviews` |
| 5 | `POST /products/{slug}/reviews` (body: rating, title, content, images[]) | customer-portal-service | `/reviews/new/[productSlug]` |
| 6 | `GET /admin/reviews` (admin quản lý) | ecom-ops-service (đã khai báo route) | dashboard |

### 3.3 Nội dung y khoa (P1)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 7 | `GET /diseases/{slug}` (detail) | customer-portal-service (mở rộng `HealthContentController`) | `/benh-thuong-gap/[slug]` |
| 8 | `GET /cancers?category=` | customer-portal-service (mở rộng `HealthContentController`) | `/chuyen-trang-ung-thu` |
| 9 | `GET /cancers/{slug}` | customer-portal-service | `/chuyen-trang-ung-thu` detail |
| 10 | `GET /videos?page=&category=&source=` (public) | customer-portal-service (controller mới `VideoController`) | `/video` |
| 11 | `GET /videos/{id}` (public) | customer-portal-service | `/video` detail |
| 12 | `GET /lookup/herb/{slug}` (detail dược liệu) | customer-portal-service (mở rộng `ShopController`) | `/tra-cuu-duoc-lieu/[slug]` |
| 13 | `GET /lookup/ingredient/{slug}` (detail hoạt chất) | customer-portal-service | `/tra-cuu-duoc-chat/[slug]` |
| 14 | `GET /lookup/drug?category=&q=` (list có filter) | customer-portal-service | `/tra-cuu-thuoc` |

### 3.4 B2C Auth bổ sung (P1)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 15 | `GET /customers/me/points` | customer-service | `/points` |
| 16 | `GET /customers/me/points/history?page=` | customer-service | `/points` |
| 17 | `POST /wallet/topup` | customer-portal-service (mở rộng `WalletController`) | `/wallet` |
| 18 | `POST /vouchers/redeem` | customer-portal-service (mở rộng `VoucherController`) | `/voucher` |
| 19 | `GET /vouchers?status=available\|used\|expired` | customer-portal-service | `/voucher` |
| 20 | `POST /customers/me/avatar` (upload ảnh đại diện) | customer-service | `/profile` |

### 3.5 B2C Upload & Prescription (P1)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 21 | `POST /prescriptions/upload` (B2C, multipart: file) → trả về `{prescriptionId, parsedMedicines[]}` | prescription-service (controller mới) | `/upload-toa` |
| 22 | `GET /prescriptions/me` (sửa thành filter user hiện tại) | prescription-service | `/rx-console/...` |

### 3.6 Dashboard admin list (P2)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 23 | `GET /payments?page=&status=&from=&to=` | payment-service (mở rộng `PaymentController`) | `/payments` (dashboard) |
| 24 | `GET /consultations?status=&assignedTo=` | pharmacist-workbench-service | `/rx-console/consult` |
| 25 | `PUT /consultations/{id}/respond` | pharmacist-workbench-service | `/rx-console/consult` |
| 26 | `GET /follow-ups?status=&page=` | pharmacist-workbench-service | `/rx-console/follow-up` |

### 3.7 Real-time & CMS (P2 — lớn)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 27 | `WebSocket /ws/chat` + `POST /chat/messages` | chat-service mới HOẶC mở rộng mobile-bff | `/live-chat` |
| 28 | `GET /policies/{slug}` | cms-service mới HOẶC customer-portal-service | `/chinh-sach/[slug]` |
| 29 | `GET /pages/about` | cms-service mới | `/gioi-thieu` |
| 30 | `GET /news?page=&category=` | cms-service mới | `/tin-tuc-su-kien` |
| 31 | `GET /careers?page=&location=` | cms-service mới | `/tuyen-dung` |
| 32 | `POST /careers/apply` (multipart) | cms-service mới | `/tuyen-dung` (form apply) |

### 3.8 Flash-sale nâng cấp (P3)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 33 | `GET /ecom-ops/flash-sales?status=active\|upcoming\|ended` | ecom-ops-service (mở rộng `EcomFlashSaleController`) | `/flash-sale` (list) |
| 34 | `GET /ecom-ops/flash-sales/{id}/products` | ecom-ops-service | `/flash-sale/[id]` |

### 3.9 Order B2C nâng cấp (P3)

| # | Endpoint cần tạo | Service đề xuất | Cho trang FE |
|---|---|---|---|
| 35 | `POST /customer/orders` (B2C tạo đơn, body: items[], address, paymentMethod, shippingMethod, voucherCode) | order-service hoặc customer-portal-service | `/checkout` |
| 36 | `POST /customer/orders/{id}/cancel` | order-service | `/don-hang/[id]` |

---

## 📊 TỔNG KẾT MỨC ĐỘ SẴN SÀNG

| Nhóm FE | Số trang | % đáp ứng | Hành động |
|---|---|---|---|
| A. Shop catalog (L1/L2/PDP) | 10 | **50%** | Tạo `/products/{slug}`, `/categories/tree`, `/provinces` |
| B. Nội dung y khoa | 8 | **43%** | Tạo `/diseases/{slug}`, `/cancers`, public `/videos`, lookup details |
| C. Store locator | 3 | **33%** | Tạo `/provinces`, filter province |
| D. Đơn hàng B2C | 3 | **75%** | Tạo `POST /customer/orders`, `/cancel` |
| E. Cart & Voucher & Installment | 3 | **86%** | Tạo `POST /vouchers/redeem` |
| F. Tư vấn & Tiêm chủng | 5 | **88%** | Tạo `POST /prescriptions/upload` (B2C) |
| G. Reviews | 2 | **0%** | Tạo controller mới trong customer-portal |
| H. AI | 4 | **100%** | ✅ Đã có (chỉ cần chỉnh path gateway) |
| I. Auth profile/family | 7 | **88%** | Tạo `/customers/me/points`, `/wallet/topup`, `/vouchers/redeem` |
| J. Flash-sale | 2 | **67%** | Tạo list với status filter |
| K. Reminders | 3 | **67%** | Đổi path FE sang flat `/mobile/medication-reminders` |
| L. Live chat / verify / CMS | 3 | **20%** | Tạo WebSocket chat, policies CMS |
| M. Dashboard shell | 8 | **50%** | Tạo `/payments?`, `/consultations?`, `/follow-ups?` list |
| N. Shop static (CMS) | 4 | **0%** | Tạo cms-service mới |
| **TỔNG** | **65** | **~62%** | **36 endpoint cần tạo + 1 CMS service** |

---

## 🎯 Đề xuất thứ tự ưu tiên

| Ưu tiên | Hạng mục | Số endpoint | Effort | Phụ thuộc |
|---|---|---|---|---|
| **P0** | Catalog B2C: `/products/{slug}`, `/categories/tree`, `/provinces` | 3 | 2-3 ngày | customer-portal, category |
| **P0** | Reviews module (3 endpoints + admin) | 4 | 1 ngày | customer-portal, ecom-ops |
| **P0** | AI path alignment (api-gateway route fix) | 1 | 0.5 ngày | api-gateway |
| **P1** | Diseases detail, cancers, public videos, lookup detail | 8 | 3-4 ngày | customer-portal |
| **P1** | B2C auth: `/customers/me/points`, `/wallet/topup`, `/vouchers/redeem` | 5 | 1-2 ngày | customer, customer-portal |
| **P1** | Upload toa B2C: `POST /prescriptions/upload` | 1 | 1 ngày | prescription |
| **P2** | Dashboard admin list: `/payments?`, `/consultations?`, `/follow-ups?` | 3 | 1-2 ngày | payment, pharmacist |
| **P2** | Flash-sale list với status, Order B2C `POST /customer/orders` | 4 | 1-2 ngày | ecom-ops, order |
| **P3** | CMS service (news, careers, about, policies) | 5 | 1 tuần | service mới |
| **P3** | WebSocket live-chat | 2 | 1 tuần | service mới hoặc mở rộng mobile-bff |

---

## Ghi chú kỹ thuật

### Routing đặc biệt cần lưu ý trong `pcms/api-gateway/src/main/resources/application.yml`:

- **AI Engine:** route `/api/v1/ai/**` → `lb://ai-engine-service`, `StripPrefix=2` — nhưng nhiều route con có path lồng nhau (vd `/api/v1/ai/chat/sessions/{id}/escalate`). Nếu frontend gọi `/ai/chat/sessions/123/escalate` thì sau StripPrefix=2 sẽ thành `/chat/sessions/123/escalate` — cần kiểm tra service thực tế có route này không.
- **Customer Portal:** route rất dài `/api/v1/shop/**,/api/v1/store/**,/api/v1/vaccines/**,/api/v1/vaccine-bookings/**,/api/v1/vaccination-ledger/**,/api/v1/health-articles/**,/api/v1/diseases/**,/api/v1/videos/**,/api/v1/lookup/**,/api/v1/verify-origin/**,/api/v1/addresses/**,/api/v1/favorites/**,/api/v1/family/**,/api/v1/cart/**,/api/v1/notif-settings/**,/api/v1/installment/**,/api/v1/wallet/**,/api/v1/vouchers/**,/api/v1/admin/videos/**` → `lb://customer-portal-service`.
- **E-com Ops:** route `/api/v1/ecom-ops/**,/api/v1/admin/vouchers/**,/api/v1/admin/flash-sales/**,/api/v1/admin/reviews/**` — chưa thấy controller `/admin/vouchers` và `/admin/reviews` trong code.
- **Auth:** route `/api/v1/auth/**,/api/v1/users/**,/api/v1/dashboard/**,/api/v1/audit-logs/**` → `lb://user-service`.

### Frontend cần điều chỉnh path (không cần BE):
- `src/features/favorites/services/favoriteService.ts` → đổi từ `/customers/me/favorites` sang `/favorites`
- `src/features/addresses/services/addressService.ts` → đổi sang `/addresses`
- `src/features/family/services/familyService.ts` → đổi sang `/family`
- `src/features/wallet/services/walletService.ts` → đổi sang `/wallet`
- `src/features/articles/` (FE) → đổi path từ `/articles` sang `/health-articles`
- `src/features/notifications/` → `notif-settings` đã đúng, kiểm tra
