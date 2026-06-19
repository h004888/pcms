# PCMS - Danh sách API đầy đủ

Tổng quan dự án: **Pharmacy Chain Management System (PCMS)** - Hệ thống quản lý chuỗi nhà thuốc, kiến trúc Microservices (Spring Boot + Spring Cloud Gateway + Eureka).

> **Tổng số API: 146** endpoints thuộc **12 services**.
> Gateway cổng `8080` ánh xạ các path `/api/v1/<service>/**` và `StripPrefix=2` tới service nội bộ.

## Tổng hợp theo service

| # | Service                 | Số API | Cổng (mặc định) | Public base path       |
|---|-------------------------|-------:|----------------:|------------------------|
| 1 | branch-service          |      8 |            8081 | `/api/v1/branches/**`  |
| 2 | catalog-service         |     16 |            8082 | `/api/v1/medicines/**`, `/api/v1/search/**` |
| 3 | category-service        |      5 |            8083 | `/api/v1/categories/**` |
| 4 | customer-service        |     14 |            8084 | `/api/v1/customers/**` |
| 5 | inventory-service       |     21 |            8085 | `/api/v1/inventory/**` |
| 6 | notification-service    |     15 |            8086 | `/api/v1/notifications/**` |
| 7 | order-service           |     14 |            8087 | `/api/v1/orders/**`, `/api/v1/coupons/**` |
| 8 | payment-service         |      9 |            8088 | `/api/v1/payments/**`, `/api/v1/webhooks/**` |
| 9 | prescription-service    |      9 |            8089 | `/api/v1/prescriptions/**` |
|10 | report-service          |     11 |            8090 | `/api/v1/reports/**` |
|11 | supplier-service        |      6 |            8091 | `/api/v1/suppliers/**` |
|12 | user-service            |     18 |            8092 | `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/dashboard/**`, `/api/v1/audit-logs/**` |

---

## 1. branch-service (8 APIs)
*Controller: `BranchController`*

| Method | Path                       | Mô tả                        |
|--------|----------------------------|------------------------------|
| GET    | /branches                  | Danh sách chi nhánh           |
| GET    | /branches/{id}             | Chi tiết chi nhánh theo id    |
| GET    | /branches/code/{code}      | Tìm theo mã chi nhánh        |
| GET    | /branches/{id}/staff       | Danh sách nhân viên của CN   |
| POST   | /branches                  | Tạo chi nhánh mới             |
| PUT    | /branches/{id}             | Cập nhật chi nhánh           |
| PUT    | /branches/{id}/manager     | Gán quản lý cho chi nhánh    |
| DELETE | /branches/{id}             | Xoá mềm chi nhánh            |

---

## 2. catalog-service (16 APIs)

### MedicineController (`/medicines`)
| Method | Path                       | Mô tả                        |
|--------|----------------------------|------------------------------|
| GET    | /medicines                 | Danh sách thuốc              |
| GET    | /medicines/{id}            | Chi tiết thuốc               |
| GET    | /medicines/sku/{sku}       | Tìm theo SKU                 |
| GET    | /medicines/count           | Đếm theo danh mục            |
| GET    | /medicines/export          | Xuất danh sách thuốc         |
| POST   | /medicines                 | Tạo thuốc (JSON)             |
| POST   | /medicines                 | Tạo thuốc (multipart)        |
| PUT    | /medicines/{id}            | Cập nhật thuốc (JSON)        |
| PUT    | /medicines/{id}            | Cập nhật thuốc (multipart)   |
| POST   | /medicines/{id}/image      | Upload ảnh thuốc             |
| GET    | /medicines/{id}/image      | Lấy ảnh thuốc                |
| DELETE | /medicines/{id}            | Xoá mềm thuốc                |

### SearchController (`/search`)
| Method | Path                                | Mô tả                  |
|--------|-------------------------------------|------------------------|
| GET    | /search                             | Tìm kiếm tổng hợp     |
| GET    | /search/medicines/autocomplete      | Gợi ý tự động hoàn thành |
| GET    | /search/medicines                   | Tìm kiếm thuốc        |
| GET    | /search/full                        | Tìm kiếm fulltext     |

---

## 3. category-service (5 APIs)
*Controller: `CategoryController` (`/categories`)*

| Method | Path              | Mô tả                |
|--------|-------------------|----------------------|
| GET    | /categories       | Danh sách danh mục   |
| GET    | /categories/{id}  | Chi tiết danh mục    |
| POST   | /categories       | Tạo danh mục         |
| PUT    | /categories/{id}  | Cập nhật danh mục    |
| DELETE | /categories/{id}  | Xoá danh mục         |

---

## 4. customer-service (14 APIs)

### CustomerController (`/customers`)
| Method | Path                              | Mô tả                       |
|--------|-----------------------------------|-----------------------------|
| GET    | /customers                        | Danh sách khách hàng        |
| GET    | /customers/{id}                   | Chi tiết khách hàng         |
| GET    | /customers/phone/{phone}          | Tìm theo SĐT                |
| GET    | /customers/{id}/tier              | Hạng thành viên             |
| GET    | /customers/{id}/orders            | Lịch sử đơn hàng           |
| GET    | /customers/{id}/points            | Điểm thưởng hiện tại       |
| GET    | /customers/{id}/history           | Lịch sử điểm                |
| POST   | /customers                        | Tạo khách hàng              |
| PUT    | /customers/{id}                   | Cập nhật khách hàng        |
| DELETE | /customers/{id}                   | Xoá khách hàng              |
| PUT    | /customers/{id}/points/add        | Cộng điểm thưởng            |

### CustomerPortalController (`/customers`) — Self-service portal
| Method | Path                  | Mô tả                            |
|--------|-----------------------|----------------------------------|
| POST   | /customers/register   | Khách tự đăng ký                 |
| GET    | /customers/me         | Lấy thông tin tài khoản của tôi  |
| PUT    | /customers/me         | Cập nhật thông tin của tôi       |

---

## 5. inventory-service (21 APIs)

### InventoryController (`/inventory`)
| Method | Path                                              | Mô tả                        |
|--------|---------------------------------------------------|------------------------------|
| GET    | /inventory                                       | Danh sách tồn kho            |
| GET    | /inventory/{id}                                  | Chi tiết bản ghi tồn         |
| GET    | /inventory/batches/scan/{code}                   | Quét mã lô hàng              |
| GET    | /inventory/branch/{branchId}/medicine/{medicineId}| Tra tồn theo CN + thuốc      |
| POST   | /inventory/import                                | Nhập kho                     |
| POST   | /inventory/export                                | Xuất kho                     |
| POST   | /inventory/consume                               | Tiêu hao                     |
| POST   | /inventory/transfer                              | Chuyển kho nội bộ            |
| POST   | /inventory/bulk/import (JSON)                    | Nhập kho hàng loạt (JSON)    |
| POST   | /inventory/bulk/import (multipart)               | Nhập kho hàng loạt (file)    |
| POST   | /inventory/bulk/import-file                      | Nhập kho hàng loạt (file)    |
| GET    | /inventory/bulk/export                           | Xuất danh sách tồn kho       |
| GET    | /inventory/transactions                          | Lịch sử giao dịch kho        |
| GET    | /inventory/low-stock                             | Cảnh báo tồn thấp            |
| GET    | /inventory/alerts/low-stock                      | Cảnh báo tồn thấp (alias)    |
| GET    | /inventory/expiring                              | Cảnh báo sắp hết hạn         |
| GET    | /inventory/alerts/expiry                         | Cảnh báo hết hạn             |
| GET    | /inventory/report/stock-level                    | Báo cáo mức tồn kho          |
| GET    | /inventory/report/movement                       | Báo cáo biến động kho        |

### OutboxConsumerController (`/inventory/orders`)
| Method | Path                                      | Mô tả                              |
|--------|-------------------------------------------|------------------------------------|
| POST   | /inventory/orders/{orderId}/paid          | Outbox: đơn đã thanh toán         |
| POST   | /inventory/orders/{orderId}/cancelled     | Outbox: đơn bị huỷ                |

---

## 6. notification-service (15 APIs)

### NotificationController (`/notifications`)
| Method | Path                              | Mô tả                          |
|--------|-----------------------------------|--------------------------------|
| GET    | /notifications                    | Danh sách thông báo            |
| GET    | /notifications/unread             | Thông báo chưa đọc             |
| GET    | /notifications/{id}               | Chi tiết thông báo             |
| POST   | /notifications                    | Gửi thông báo                  |
| POST   | /notifications/bulk               | Gửi nhiều thông báo            |
| POST   | /notifications/broadcast          | Broadcast thông báo            |
| POST   | /notifications/compose            | Soạn thông báo                 |
| POST   | /notifications/{id}/retry         | Gửi lại thông báo thất bại     |
| PUT    | /notifications/{id}/read          | Đánh dấu đã đọc                |
| PUT    | /notifications/read-all           | Đánh dấu tất cả đã đọc         |

### NotificationTemplateController (`/notifications/templates`)
| Method | Path                              | Mô tả                          |
|--------|-----------------------------------|--------------------------------|
| GET    | /notifications/templates          | Danh sách mẫu thông báo        |
| POST   | /notifications/templates          | Tạo mẫu thông báo              |

### OutboxConsumerController (`/notifications`)
| Method | Path                                      | Mô tả                          |
|--------|-------------------------------------------|--------------------------------|
| POST   | /notifications/orders/paid                | Outbox: đơn đã thanh toán      |
| POST   | /notifications/inventory/low-stock        | Outbox: cảnh báo tồn thấp      |
| POST   | /notifications/inventory/expiry           | Outbox: cảnh báo hết hạn       |

---

## 7. order-service (14 APIs)

### CouponController (`/coupons`)
| Method | Path                | Mô tả                  |
|--------|---------------------|------------------------|
| GET    | /coupons            | Danh sách mã giảm giá  |
| POST   | /coupons            | Tạo mã giảm giá        |
| PUT    | /coupons/{id}       | Cập nhật mã            |
| DELETE | /coupons/{id}       | Vô hiệu hoá mã         |

### OrderController (`/orders`)
| Method | Path                              | Mô tả                          |
|--------|-----------------------------------|--------------------------------|
| GET    | /orders                           | Danh sách đơn hàng             |
| GET    | /orders/{id}                      | Chi tiết đơn hàng              |
| GET    | /orders/number/{orderNumber}      | Tìm theo mã đơn                |
| POST   | /orders                           | Tạo đơn hàng                   |
| PUT    | /orders/{id}                      | Cập nhật đơn hàng              |
| PUT    | /orders/{id}/pay                  | Đánh dấu đã thanh toán         |
| POST   | /orders/{id}/approve              | Duyệt đơn (theo toa)           |
| POST   | /orders/{id}/reject               | Từ chối đơn                   |
| DELETE | /orders/{id}                      | Huỷ đơn                       |

### OutboxAdminController (`/admin/outbox`)
| Method | Path                              | Mô tả                          |
|--------|-----------------------------------|--------------------------------|
| POST   | /admin/outbox/retry/{id}          | Retry outbox event             |

---

## 8. payment-service (9 APIs)

### PaymentController (`/payments`)
| Method | Path                                       | Mô tả                       |
|--------|--------------------------------------------|-----------------------------|
| GET    | /payments                                  | Danh sách thanh toán        |
| GET    | /payments/{id}                             | Chi tiết thanh toán         |
| GET    | /payments/invoice/{invoiceNumber}          | Tra theo mã hoá đơn         |
| GET    | /payments/order/{orderId}                  | Tra theo đơn hàng           |
| POST   | /payments                                  | Xử lý thanh toán            |
| POST   | /payments/{id}/refund                      | Hoàn tiền                   |
| PUT    | /payments/{id}/refund                      | Hoàn tiền (legacy)          |
| GET    | /payments/{id}/refund-history              | Lịch sử hoàn tiền           |

### WebhookController (`/webhooks`)
| Method | Path                              | Mô tả                                  |
|--------|-----------------------------------|----------------------------------------|
| POST   | /webhooks/payment-gateway         | Nhận callback từ payment gateway       |

---

## 9. prescription-service (9 APIs)
*Controller: `PrescriptionController` (`/prescriptions`)*

| Method | Path                                  | Mô tả                          |
|--------|---------------------------------------|--------------------------------|
| GET    | /prescriptions                        | Danh sách đơn thuốc           |
| GET    | /prescriptions/{id}                   | Chi tiết đơn thuốc            |
| GET    | /prescriptions/code/{code}            | Tra theo mã đơn thuốc         |
| POST   | /prescriptions                        | Tạo đơn thuốc                 |
| POST   | /prescriptions/draft                  | Lưu bản nháp                  |
| PUT    | /prescriptions/{id}                   | Cập nhật đơn thuốc            |
| PUT    | /prescriptions/{id}/sign              | Ký số đơn thuốc               |
| POST   | /prescriptions/{id}/link-order        | Liên kết với đơn hàng         |
| GET    | /prescriptions/{id}/print             | In đơn thuốc                  |

---

## 10. report-service (11 APIs)
*Controller: `ReportController` (`/reports`)*

| Method | Path                                  | Mô tả                          |
|--------|---------------------------------------|--------------------------------|
| GET    | /reports/revenue                      | Báo cáo doanh thu (GET)        |
| POST   | /reports/revenue                      | Báo cáo doanh thu (POST, body)|
| GET    | /reports/inventory                    | Báo cáo kho (GET)              |
| POST   | /reports/inventory                    | Báo cáo kho (POST, body)       |
| GET    | /reports/staff                        | Báo cáo nhân sự (GET)          |
| POST   | /reports/staff                        | Báo cáo nhân sự (POST, body)   |
| GET    | /reports/realtime/stats               | Thống kê realtime             |
| GET    | /reports/realtime/recent-orders       | Đơn hàng gần đây              |
| GET    | /reports/export                       | Xuất báo cáo                  |
| POST   | /reports/schedule                     | Lên lịch báo cáo              |
| GET    | /reports/schedules                    | Danh sách lịch báo cáo       |

---

## 11. supplier-service (6 APIs)
*Controller: `SupplierController` (`/suppliers`)*

| Method | Path                          | Mô tả                       |
|--------|-------------------------------|-----------------------------|
| GET    | /suppliers                    | Danh sách nhà cung cấp     |
| GET    | /suppliers/{id}               | Chi tiết NCC                |
| POST   | /suppliers                    | Tạo NCC                     |
| PUT    | /suppliers/{id}               | Cập nhật NCC                |
| GET    | /suppliers/{id}/history       | Lịch sử giao dịch NCC      |
| DELETE | /suppliers/{id}               | Xoá mềm NCC                 |

---

## 12. user-service (18 APIs)

### AuthController (`/auth`)
| Method | Path                              | Mô tả                          |
|--------|-----------------------------------|--------------------------------|
| POST   | /auth/login                       | Đăng nhập                      |
| POST   | /auth/forgot-password             | Quên mật khẩu                 |
| POST   | /auth/reset-password              | Đặt lại mật khẩu              |
| POST   | /auth/refresh                     | Refresh token                  |
| POST   | /auth/logout                      | Đăng xuất                      |

### UserController (`/users`)
| Method | Path                          | Mô tả                          |
|--------|-------------------------------|--------------------------------|
| GET    | /users                        | Danh sách người dùng          |
| GET    | /users/export                 | Xuất danh sách user           |
| GET    | /users/{id}                   | Chi tiết user                  |
| POST   | /users                        | Tạo user                       |
| PUT    | /users/{id}                   | Cập nhật user                  |
| PUT    | /users/{id}/role              | Đổi role                       |
| PUT    | /users/{id}/status            | Đổi trạng thái                 |
| POST   | /users/{id}/unlock            | Mở khoá tài khoản             |
| DELETE | /users/{id}                   | Xoá mềm user                   |
| GET    | /users/role/{role}            | Tìm user theo role             |

### DashboardController (`/dashboard`)
| Method | Path                              | Mô tả                          |
|--------|-----------------------------------|--------------------------------|
| GET    | /dashboard/stats                  | Thống kê tổng quan             |
| GET    | /dashboard/recent-logins          | Lịch sử đăng nhập gần đây     |

### AuditLogController (`/audit-logs`)
| Method | Path           | Mô tả                          |
|--------|----------------|--------------------------------|
| GET    | /audit-logs    | Lịch sử hoạt động             |

---

## Tổng kết theo HTTP Method

| Method  | Số lượng |
|---------|---------:|
| GET     |       79 |
| POST    |       48 |
| PUT     |       14 |
| DELETE  |        9 |
| **Tổng**| **146** |

## Quy ước gọi qua API Gateway

Tất cả endpoint phía trên được gọi qua Gateway ở cổng **8080**, prefix `/api/v1`:

```
GET  http://localhost:8080/api/v1/medicines                  -> catalog-service
POST http://localhost:8080/api/v1/orders                    -> order-service
GET  http://localhost:8080/api/v1/inventory/low-stock        -> inventory-service
POST http://localhost:8080/api/v1/auth/login                -> user-service
...
```

> **Lưu ý**: Ngoài 12 service REST trên, hệ thống còn 3 service hỗ trợ:
> - **config-server** (port 8888) - Cấu hình tập trung
> - **discovery-server** (port 8761) - Eureka Service Registry
> - **api-gateway** (port 8080) - Định tuyến & bảo mật
>
> Các service này không phải REST business API, chỉ dùng nội bộ hạ tầng.
