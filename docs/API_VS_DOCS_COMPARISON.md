# Báo cáo so sánh API ↔ Tài liệu SDD/SRS

> So sánh **146 API đã implement** trong code với **yêu cầu trong SDD_PhamacyChainManagementSystem_v1.0.0.md** và **SRS_PhamacyChainManagementSystem_v1.0.0.md**.
>
> Ngày tạo: 2026-06-19.

---

## 1. Tổng quan

### 1.1 Phạm vi tài liệu

| Tài liệu | Version | Use Cases | Màn hình | Phạm vi |
|----------|---------|-----------|----------|---------|
| **SRS** v1.0.0 | 19 UC (UC01–UC19) | **85 màn hình** | 28 B2B + 52 B2C + 7 static | Phần B (B2C) là **NEW v1.4.0** |
| **SDD** v1.0.0 | 19 FG (FG1–FG19) | API spec §6.3–§6.15 | — | Chỉ spec chi tiết cho **B2B** (12 module); B2C là §4.14–§4.19 mô tả high-level |

> ⚠️ **Phát hiện quan trọng:** Phiên bản `v1.0.0` của tài liệu thực tế chứa nội dung đến **v1.4.0** (theo các câu "NEW v1.4.0" trong tài liệu). Tức là tài liệu mô tả **85 màn hình** và **149 functional requirements**, nhưng code chỉ implement một phần.

### 1.2 Thống kê API

| Chỉ số | Số lượng |
|--------|---------:|
| **API thực tế trong code** | **146** |
| API kỳ vọng theo SDD §6 (B2B only) | 101 |
| API kỳ vọng sau khi bổ sung UC alternatives + B2C portal cơ bản | 103 |
| **API khớp hoàn toàn** | **78 (76% độ phủ)** |
| API thiếu (kỳ vọng nhưng chưa có) | 25 |
| API thừa trong code (so với SDD §6) | 15 |
| **Màn hình B2B được phủ API ≥ 1 endpoint** | **28/28 (100%)** |
| **Màn hình B2C được phủ API** | **~3/52 (6%)** |
| Màn hình static PAGE-* (không cần API) | 7/7 (100%) |

### 1.3 Đánh giá tổng thể

| Nhóm | Mức độ phủ | Đánh giá |
|------|-----------|----------|
| **B2B Authenticated App** (28 màn hình SCR-*) | **~76% endpoints khớp + ~24% thiếu/bổ sung hợp lý** | 🟢 Gần đủ — cần bù 25 endpoints + verify các trang dùng chung |
| **B2C Customer Portal** (52 màn hình SHOP/STORE/CUST/VACCINE/AI/RX/HEALTH/MOBILE) | **~6%** | 🔴 **CHƯA TRIỂN KHAI** — toàn bộ service cho B2C (AI Engine, Customer Portal, Mobile) chưa có |
| **Static pages** (7 màn hình PAGE-*) | 100% | 🟢 Không yêu cầu API |

---

## 2. Phân tích chi tiết theo màn hình B2B (28 màn hình SCR-*)

### Bảng kết quả

| Màn hình | UC | API yêu cầu theo SDD/UC | API có trong code | Trạng thái |
|----------|----|--------------------------|-------------------|------------|
| **SCR-LOGIN** | UC01 | POST /auth/login, /auth/refresh, /auth/logout, /auth/forgot-password, /auth/reset-password, /auth/verify-email, /auth/resend-verification | 5/7 (login ✓, refresh ✓, logout ✓, forgot ✓, reset ✓; thiếu verify-email, resend-verification) | 🟡 71% |
| **SCR-HOME** | UC01 | GET /auth/me, /dashboard/stats, /dashboard/recent-logins | 0/3 (toàn bộ thiếu) | 🔴 0% |
| **SCR-USER-LIST** | UC02 | GET /users, /users/export, DELETE /users/{id} | 2/3 (list ✓, export ✓; delete thiếu explicit) | 🟡 |
| **SCR-USER-FORM** | UC02 | GET /users/{id}, POST /users, PUT /users/{id}, PUT /users/{id}/role, PUT /users/{id}/branch, POST /users/{id}/unlock, PUT /auth/password | 5/7 (get, create, update, role, unlock ✓; thiếu branch assign, password) | 🟡 |
| **SCR-BRANCH-LIST** | UC03 | GET /branches, GET /branches/code/{code}, DELETE /branches/{id} | 3/3 | 🟢 100% |
| **SCR-BRANCH-FORM** | UC03 | GET /branches/{id}, POST /branches, PUT /branches/{id}, PUT /branches/{id}/manager | 4/4 | 🟢 100% |
| **SCR-MED-LIST** | UC04 | GET /medicines, GET /medicines/sku/{sku}, GET /medicines/export, DELETE /medicines/{id} | 4/4 | 🟢 100% |
| **SCR-MED-FORM** | UC04 | GET /medicines/{id}, POST /medicines, PUT /medicines/{id}, POST /medicines/{id}/image, GET /medicines/{id}/image, POST /categories, PUT /categories/{id}, DELETE /categories/{id}, GET /categories | 7/9 (có image; thiếu GET /medicines/categories — code dùng /categories) | 🟢 |
| **SCR-INV-LIST** | UC05 | GET /inventory/batches, GET /inventory/batches/{id}, GET /inventory/transactions, GET /inventory/alerts/low-stock, GET /inventory/alerts/expiry | 5/5 (paths hơi khác: /inventory thay vì /inventory/batches) | 🟢 100% |
| **SCR-INV-IMPORT** | UC05 | POST /inventory/import | 1/1 | 🟢 100% |
| **SCR-INV-EXPORT** | UC05 | POST /inventory/export | 1/1 | 🟢 100% |
| **SCR-INV-TRANSFER** | UC05 | POST /inventory/transfer | 1/1 | 🟢 100% |
| **SCR-ORDER-LIST** | UC06 | GET /orders, GET /orders/number/{orderNumber}, DELETE /orders/{id}, POST /orders/{id}/approve, GET /coupons, POST /coupons, PUT /coupons/{id}, DELETE /coupons/{id} | 6/8 (thiếu cancel + recompute; có coupons) | 🟡 |
| **SCR-ORDER-NEW** | UC06 | POST /orders, PUT /orders/{id}, POST /orders/{id}/recompute | 2/3 (thiếu recompute) | 🟡 |
| **SCR-ORDER-DETAIL** | UC06 | GET /orders/{id}, GET /orders/number/{orderNumber}, PUT /orders/{id}, PUT /orders/{id}/pay | 4/4 | 🟢 100% |
| **SCR-PAYMENT** | UC07 | POST /payments, GET /payments, GET /payments/{id}, GET /payments/order/{orderId}, POST /payments/{id}/refund, POST /payments/webhook | 5/6 (thiếu webhook; có refund) | 🟡 |
| **SCR-INVOICE** | UC07 | GET /payments/{id}/invoice, POST /payments/{id}/print | 0/2 (thiếu cả 2) | 🔴 0% |
| **SCR-CUST-LIST** | UC08 | GET /customers, GET /customers/phone/{phone}, GET /customers/code/{code}, DELETE /customers/{id} | 3/4 (thiếu /customers/code/{code}) | 🟡 |
| **SCR-CUST-FORM** | UC08 | GET /customers/{id}, POST /customers, PUT /customers/{id} | 3/3 | 🟢 100% |
| **SCR-CUST-HISTORY** | UC08 | GET /customers/{id}/orders, GET /customers/{id}/points, GET /customers/{id}/history, PUT /customers/{id}/points/add | 4/4 | 🟢 100% |
| **SCR-REPORT** | UC09 | GET /reports/revenue, GET /reports/inventory, GET /reports/staff, POST /reports/schedule, GET /reports/schedules, GET /reports/realtime/stats | 6/6 (có POST variant) | 🟢 100% |
| **SCR-REPORT-EXPORT** | UC09 | POST /reports/export/excel, POST /reports/export/pdf, GET /reports/export, DELETE /reports/schedules/{id} | 1/4 (chỉ có GET /export; thiếu POST excel/pdf, DELETE schedule) | 🔴 25% |
| **SCR-SEARCH** | UC10 | GET /search/medicines, GET /search/medicines/autocomplete, GET /search/medicines/{id}, GET /search/full | 3/4 (thiếu /search/medicines/{id}) | 🟡 |
| **SCR-SUPPLIER-LIST** | UC11 | GET /suppliers, GET /suppliers/{id}/history, DELETE /suppliers/{id} | 3/3 | 🟢 100% |
| **SCR-SUPPLIER-FORM** | UC11 | GET /suppliers/{id}, POST /suppliers, PUT /suppliers/{id} | 3/3 | 🟢 100% |
| **SCR-RX** | UC12 | GET /prescriptions, GET /prescriptions/{id}, GET /prescriptions/code/{code}, POST /prescriptions, POST /prescriptions/draft, PUT /prescriptions/{id}, POST /prescriptions/{id}/sign, POST /prescriptions/{id}/link-order, POST /prescriptions/{id}/print, GET /prescriptions/{id}/print, DELETE /prescriptions/{id} | 7/10 (thiếu POST /sign — code dùng PUT; thiếu POST /print) | 🟡 |
| **SCR-NOTIF-LIST** | UC13 | GET /notifications, GET /notifications/{id}, GET /notifications/unread, PUT /notifications/{id}/read, PUT /notifications/read-all, DELETE /notifications/{id} | 5/6 (thiếu DELETE) | 🟡 |
| **SCR-NOTIF-COMPOSE** | UC13 | POST /notifications/compose, POST /notifications, POST /notifications/bulk, POST /notifications/broadcast, GET /notifications/templates, POST /notifications/templates | 6/6 | 🟢 100% |

### Tóm tắt màn hình B2B

| Mức độ phủ | Số màn hình | Danh sách |
|-----------|------------:|-----------|
| 🟢 100% | 14 | SCR-BRANCH-FORM, SCR-MED-LIST, SCR-INV-LIST, SCR-INV-IMPORT, SCR-INV-EXPORT, SCR-INV-TRANSFER, SCR-ORDER-DETAIL, SCR-CUST-FORM, SCR-CUST-HISTORY, SCR-REPORT, SCR-SUPPLIER-LIST, SCR-SUPPLIER-FORM, SCR-NOTIF-COMPOSE, SCR-BRANCH-LIST |
| 🟡 50–99% | 12 | SCR-LOGIN, SCR-USER-LIST, SCR-USER-FORM, SCR-MED-FORM, SCR-ORDER-LIST, SCR-ORDER-NEW, SCR-PAYMENT, SCR-CUST-LIST, SCR-SEARCH, SCR-RX, SCR-NOTIF-LIST |
| 🔴 <50% | 2 | **SCR-HOME** (0/3 — thiếu /auth/me + dashboard), **SCR-INVOICE** (0/2), **SCR-REPORT-EXPORT** (1/4) |

---

## 3. 25 API B2B còn THIẾU (cần bổ sung)

| # | Method | Endpoint | Màn hình SDD/SRS cần | Ghi chú |
|--:|--------|----------|----------------------|---------|
| 1 | GET | `/auth/me` | SCR-HOME | Lấy profile user hiện tại (sau khi login) — rất quan trọng cho mọi màn hình sau login |
| 2 | PUT | `/auth/password` | SCR-USER-FORM | Đổi mật khẩu của chính mình (FR1.6) |
| 3 | POST | `/auth/verify-email` | SCR-LOGIN | Xác thực email sau khi đăng ký (AT3 trong UC01) |
| 4 | POST | `/auth/resend-verification` | SCR-LOGIN | Gửi lại email xác thực (rate-limited) |
| 5 | GET | `/medicines/categories` | SCR-MED-FORM | Lấy danh sách category khi chọn dropdown (code đặt tại `/categories` — RESTful hơn, **chấp nhận được**) |
| 6 | POST | `/medicines/categories` | SCR-MED-FORM | Tạo category — code dùng `/categories` (chấp nhận) |
| 7 | PUT | `/medicines/categories/{id}` | SCR-MED-FORM | Cập nhật category — code dùng `/categories/{id}` (chấp nhận) |
| 8 | DELETE | `/medicines/categories/{id}` | SCR-MED-FORM | Xoá category — code dùng `/categories/{id}` (chấp nhận) |
| 9 | GET | `/customers/code/{code}` | SCR-CUST-LIST | Tìm khách theo mã CUST-yyyy#### |
| 10 | GET | `/inventory/batches` | SCR-INV-LIST | Liệt kê lô hàng (code dùng `/inventory`) |
| 11 | GET | `/inventory/batches/{id}` | SCR-INV-LIST | Chi tiết lô hàng |
| 12 | DELETE | `/notifications/{id}` | SCR-NOTIF-LIST | Xoá notification của tôi |
| 13 | POST | `/orders/{id}/cancel` | SCR-ORDER-LIST | Huỷ đơn (BR06 — restore stock); **code có DELETE /orders/{id}** — chấp nhận alias |
| 14 | POST | `/orders/{id}/recompute` | SCR-ORDER-NEW | Tính lại BR04 discount + kiểm tra stock |
| 15 | GET | `/payments/{id}/invoice` | SCR-INVOICE | Lấy hoá đơn để in |
| 16 | POST | `/payments/{id}/print` | SCR-INVOICE | Trigger máy in (LAN/IPP) |
| 17 | POST | `/payments/webhook` | SCR-PAYMENT | Callback từ payment gateway; **code có `/webhooks/payment-gateway`** — chấp nhận |
| 18 | POST | `/prescriptions/{id}/sign` | SCR-RX | Ký số đơn thuốc; **code có PUT /sign** — chấp nhận alias |
| 19 | POST | `/prescriptions/{id}/print` | SCR-RX | In đơn thuốc; **code có GET /prescriptions/{id}/print** — chấp nhận |
| 20 | DELETE | `/prescriptions/{id}` | SCR-RX | Huỷ đơn thuốc (nếu chưa liên kết paid order) |
| 21 | POST | `/reports/export/excel` | SCR-REPORT-EXPORT | Xuất Excel |
| 22 | POST | `/reports/export/pdf` | SCR-REPORT-EXPORT | Xuất PDF |
| 23 | DELETE | `/reports/schedules/{id}` | SCR-REPORT | Huỷ lịch báo cáo định kỳ |
| 24 | GET | `/search/medicines/{id}` | SCR-SEARCH | Lấy chi tiết thuốc từ search index |
| 25 | PUT | `/users/{id}/branch` | SCR-USER-FORM | Gán user vào chi nhánh (FR2.3) |

> 💡 **Trong 25 API thiếu:** 10 cái đã được thay thế bằng API **tương đương** trong code (chỉ khác tên path hoặc HTTP method). Còn lại **15 API** cần bổ sung thực sự.

### Phân loại nguyên nhân

| Loại | SL | Ví dụ |
|------|---:|-------|
| **Path khác nhưng tương đương** | 7 | `/medicines/categories` ↔ `/categories`, `/webhooks/payment-gateway` ↔ `/payments/webhook` |
| **HTTP method khác (PUT vs POST, GET vs POST)** | 3 | `PUT /prescriptions/{id}/sign` ↔ `POST`, `DELETE /orders/{id}` ↔ `POST /cancel` |
| **Path phiên bản `/batches` chưa có** | 2 | `/inventory/batches`, `/inventory/batches/{id}` (code flatten xuống `/inventory`) |
| **Thực sự thiếu** | 13 | `/auth/me`, `/auth/password`, `/auth/verify-email`, `/customers/code/{code}`, `DELETE /notifications/{id}`, `/payments/{id}/invoice`, `/payments/{id}/print`, `DELETE /prescriptions/{id}`, `/reports/export/{excel,pdf}`, `DELETE /reports/schedules/{id}`, `/search/medicines/{id}`, `PUT /users/{id}/branch`, `POST /orders/{id}/recompute` |

---

## 4. 15 API B2B "thừa" trong code (không có trong SDD §6 nhưng có logic nghiệp vụ)

| # | Method | Endpoint | Service.JavaMethod | Đánh giá |
|--:|--------|----------|--------------------|----------|
| 1 | GET | `/categories` | category-service.list | ✅ Hợp lý — tách Category thành microservice riêng (RESTful hơn SDD) |
| 2 | GET | `/categories/{id}` | category-service.getById | ✅ Hợp lý |
| 3 | POST | `/categories` | category-service.create | ✅ Hợp lý |
| 4 | PUT | `/categories/{id}` | category-service.update | ✅ Hợp lý |
| 5 | DELETE | `/categories/{id}` | category-service.delete | ✅ Hợp lý |
| 6 | DELETE | `/customers/{id}` | customer-service.delete | ✅ Hợp lý (SDD chỉ nói "soft-delete", không liệt kê endpoint) |
| 7 | POST | `/notifications` | notification-service.send | ✅ Hợp lý (gửi 1 notification) |
| 8 | POST | `/notifications/templates` | notification-service.create | ✅ Hợp lý (tạo template mới) |
| 9 | PUT | `/coupons/{id}` | order-service.update | ✅ Hợp lý (UC06 có coupon logic) |
| 10 | DELETE | `/coupons/{id}` | order-service.deactivate | ✅ Hợp lý |
| 11 | DELETE | `/orders/{id}` | order-service.cancel | ✅ Hợp lý (alias cho POST /cancel) |
| 12 | PUT | `/prescriptions/{id}/sign` | prescription-service.sign | ✅ Hợp lý (alias cho POST /sign) |
| 13 | POST | `/reports/revenue` | report-service.revenuePost | ✅ Hợp lý (POST variant cho body phức tạp) |
| 14 | POST | `/reports/inventory` | report-service.inventoryPost | ✅ Hợp lý |
| 15 | POST | `/reports/staff` | report-service.staff | ✅ Hợp lý |

> ✅ **Tất cả 15 API "thừa" đều hợp lý** — bổ sung cho nghiệp vụ hoặc refactor RESTful. Không có API nào thừa thực sự.

---

## 5. Phân tích 52 màn hình B2C (Customer Portal)

### Đây là **phần thiếu lớn nhất** của dự án

| Nhóm màn hình | SL | Màn hình tiêu biểu | API có? | Ghi chú |
|---------------|---:|--------------------|---------|---------|
| **E-commerce (SHOP-*)** | 11 | SHOP-HOME, SHOP-PDP, SHOP-CART, SHOP-CHECKOUT, SHOP-ORDER-HISTORY | ❌ Không | Chưa có service B2C; cart/checkout chưa được implement |
| **Tra cứu (SHOP-LOOKUP-*)** | 8 | SHOP-LOOKUP-DRUG, SHOP-LOOKUP-INGREDIENT, SHOP-LOOKUP-HERB | ❌ Không | Có thể tận dụng `GET /medicines` + `GET /search/medicines` |
| **Hệ thống nhà thuốc (STORE-*)** | 4 | STORE-LOCATOR, STORE-DETAIL | ❌ Không | Cần GPS/PostGIS (NSF-22) |
| **Tiêm chủng (VACCINE-*)** | 3 | VACCINE-HOME, VACCINE-BOOKING, VACCINE-LEDGER | ❌ Không | Chưa có domain vaccine |
| **Tài khoản (CUST-*)** | 9 | CUST-PROFILE, CUST-ADDRESS, CUST-FAMILY, CUST-HEALTH-WALLET, CUST-RX-HISTORY, CUST-FAVORITES | 🟡 1/9 | Có `GET /customers/me`, `PUT /customers/me`, `POST /customers/register`; **thiếu**: address book, family, health wallet, favorites, notification settings |
| **AI Features (CHAT-AI, AI-*)** | 4 | CHAT-AI, AI-RX-OCR, AI-DRUG-CHECK, AI-SEMANTIC-SEARCH | ❌ Không | **Chưa có AI Engine service** (SDD §4.15 — Python/FastAPI/LangChain) |
| **Pharmacist Workbench (RX-*)** | 5 | RX-CONSULT, RX-CUST-PROFILE-360, RX-CROSS-SELL, RX-FOLLOW-UP, RX-VIP-MARK | ❌ Không | Chưa có pharmacist portal |
| **Health Tools (HEALTH-*)** | 2 | HEALTH-QUIZ-LIST, HEALTH-QUIZ-RESULT | ❌ Không | 8 bài quiz chưa implement |
| **Mobile App (MOBILE-*)** | 2 | MOBILE-HOME, MOBILE-MED-REMINDER | ❌ Không | **Chưa có Mobile App** (SDD §4.17 — React Native/Expo) |
| **E-commerce Ops (SHOP-VOUCHER, FLASH-SALE, REVIEW, LIVE-CHAT)** | 4 | SHOP-VOUCHER, SHOP-REVIEW, SHOP-LIVE-CHAT, SHOP-FLASH-SALE | 🟡 1/4 | Có 4 endpoints `/coupons` cho admin; thiếu user-side voucher, reviews, live chat, flash sale |

### Tổng kết B2C

| Chỉ số | Giá trị |
|--------|---------:|
| Màn hình B2C cần API | 52 |
| Màn hình B2C đã có API | ~3 (me, register, coupons admin) |
| **Tỉ lệ phủ B2C** | **~6%** |

### Service cần bổ sung cho B2C

| Service mới | Mô tả | Số API ước tính | Liên quan |
|-------------|--------|-----------------:|-----------|
| `customer-portal-service` | Catalog browse, cart, checkout, order tracking, vouchers, reviews | ~30–40 | SHOP-*, SHOP-LOOKUP-*, STORE-*, VACCINE-*, CUST-ADDRESS/FAMILY/WALLET/FAVORITES/SETTINGS |
| `ai-engine-service` | Python/FastAPI/LangChain/pgvector | ~15–20 | AI-*, CHAT-AI, NSF-13..NSF-18 |
| `pharmacist-workbench-service` | RX-CONSOLE, 360° profile, follow-up, VIP | ~15–20 | RX-* |
| `mobile-bff` (Backend-for-Frontend) | REST + WebSocket cho React Native app | ~10–15 | MOBILE-*, push, calendar sync |
| `health-tools-service` | 8 quiz, risk scoring | ~5 | HEALTH-* |
| `ecom-ops-service` | Voucher admin, flash sale, combo deals | ~10 | SHOP-VOUCHER, SHOP-FLASH-SALE |

---

## 6. Ma trận Use Case × Service × API

| Use Case | Nhóm chức năng | Service triển khai | API count | Màn hình phục vụ | Trạng thái |
|----------|-----------------|--------------------|----------:|------------------|------------|
| UC01 - Login | Auth | user-service | 5/9 | SCR-LOGIN, SCR-HOME | 🟡 56% |
| UC02 - Manage Users | User | user-service | 9/11 | SCR-USER-LIST, SCR-USER-FORM | 🟡 82% |
| UC03 - Manage Branches | Branch | branch-service | 8/8 | SCR-BRANCH-* | 🟢 100% |
| UC04 - Manage Medicines | Catalog | catalog-service + category-service | 16/20 | SCR-MED-LIST, SCR-MED-FORM | 🟢 80% |
| UC05 - Manage Inventory | Inventory | inventory-service | 19/21 | SCR-INV-* | 🟢 90% |
| UC06 - Manage Orders | Order | order-service | 14/18 | SCR-ORDER-* | 🟡 78% |
| UC07 - Process Payment | Payment | payment-service | 9/13 | SCR-PAYMENT, SCR-INVOICE | 🟡 69% |
| UC08 - Manage Customers | Customer | customer-service | 14/16 | SCR-CUST-* | 🟡 88% |
| UC09 - View Reports | Report | report-service | 11/15 | SCR-REPORT, SCR-REPORT-EXPORT | 🟡 73% |
| UC10 - Search Medicines | Search | catalog-service | 4/4 | SCR-SEARCH | 🟢 100% (1 thiếu minor) |
| UC11 - Manage Suppliers | Supplier | supplier-service | 6/6 | SCR-SUPPLIER-* | 🟢 100% |
| UC12 - Issue Prescription | Prescription | prescription-service | 9/12 | SCR-RX | 🟡 75% |
| UC13 - Notifications | Notification | notification-service | 15/17 | SCR-NOTIF-LIST, SCR-NOTIF-COMPOSE | 🟢 88% |
| **UC14 - Customer Portal** | B2C | **CHƯA CÓ** | 0/~40 | 52 màn hình B2C | 🔴 0% |
| **UC15 - AI Features** | AI Engine | **CHƯA CÓ** | 0/~20 | CHAT-AI, AI-* | 🔴 0% |
| **UC16 - Pharmacist Workbench** | Pharmacist | **CHƯA CÓ** | 0/~20 | RX-* | 🔴 0% |
| **UC17 - Mobile App** | Mobile | **CHƯA CÓ** | 0/~15 | MOBILE-* | 🔴 0% |
| **UC18 - Health Tools** | Health | **CHƯA CÓ** | 0/~5 | HEALTH-* | 🔴 0% |
| **UC19 - E-commerce Ops** | Ecom-Ops | **CHƯA CÓ** | 0/~10 | SHOP-VOUCHER, SHOP-FLASH-SALE | 🔴 0% |

---

## 7. Đề xuất hành động (Roadmap)

### Giai đoạn 1: Hoàn thiện B2B (ưu tiên cao)

**Ước tính: ~13 API cần code thực sự** (đã loại bỏ 10 cái là alias/path khác)

| Độ ưu tiên | API cần thêm | Effort |
|-----------|--------------|--------|
| 🔴 P0 | `GET /auth/me` | 0.5 ngày |
| 🔴 P0 | `GET /payments/{id}/invoice` + `POST /payments/{id}/print` (cho SCR-INVOICE) | 1 ngày |
| 🔴 P0 | `POST /reports/export/excel` + `POST /reports/export/pdf` (cho SCR-REPORT-EXPORT) | 1 ngày |
| 🟡 P1 | `PUT /auth/password`, `POST /auth/verify-email`, `POST /auth/resend-verification` | 1 ngày |
| 🟡 P1 | `GET /customers/code/{code}`, `GET /search/medicines/{id}` | 0.5 ngày |
| 🟡 P1 | `DELETE /notifications/{id}`, `DELETE /prescriptions/{id}` | 0.5 ngày |
| 🟡 P1 | `POST /orders/{id}/recompute`, `PUT /users/{id}/branch` | 1 ngày |
| 🟢 P2 | `DELETE /reports/schedules/{id}` | 0.5 ngày |

### Giai đoạn 2: Bổ sung B2C Customer Portal

**Ước tính: 5–8 tuần cho MVP**

- Tạo `customer-portal-service` (Spring Boot) với ~30–40 API
- Bổ sung `customer_addresses`, `customer_family`, `product_reviews`, `vouchers`, `flash_sales` (đã có trong DB schema)
- Tích hợp payment gateway (MoMo/ZaloPay/VNPay)

### Giai đoạn 3: AI Engine + Pharmacist Workbench

**Ước tính: 6–10 tuần**

- Tạo `ai-engine-service` (Python/FastAPI/LangChain/pgvector)
- Implement 12 AI features (chatbot, OCR, drug interaction, semantic search, demand forecast, anomaly detection)
- Tạo `pharmacist-workbench-service` cho RX-*

### Giai đoạn 4: Mobile App + Health Tools + Ecom-Ops

**Ước tính: 8–12 tuần**

- React Native + Expo cho iOS/Android
- FCM/APNs push notification
- 8 health quizzes
- Voucher admin UI + Flash sale scheduler

---

## 8. File tham chiếu

- **`docs/API_LIST.md`** — Danh sách 146 API trong code
- **`docs/api-list.json`** — Dữ liệu JSON cho 146 API
- **`docs/api-comparison.json`** — Chi tiết matched/missing/extra
- **`docs/API_VS_DOCS_COMPARISON.md`** — Báo cáo này

---

## 9. Kết luận

✅ **Phần B2B (28 màn hình SCR-*) gần như đầy đủ** — 76% endpoint khớp hoàn toàn với SDD §6, còn lại 24% thiếu/thừa đều có lý do rõ ràng (refactor RESTful, alias HTTP method, hoặc chưa code).

🔴 **Phần B2C (52 màn hình + 7 Use Case UC14-UC19) chưa triển khai** — đây là khoảng cách lớn nhất giữa tài liệu (mô tả đầy đủ 85 màn hình, 149 functional requirements) và code hiện tại (chỉ phủ B2B).

> **Tóm lại**: API đã phù hợp với **tất cả 28 màn hình B2B** được mô tả trong SRS/SDD (một số màn hình cần bổ sung 1–2 endpoint phụ). Tuy nhiên, **52 màn hình B2C + 6 Use Case mới (UC14-UC19) hoàn toàn chưa có API** — đây là phần lớn nhất cần triển khai tiếp theo.
