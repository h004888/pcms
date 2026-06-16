# 🗄️ PCMS — Database-per-Service Strategy

> **Version:** 1.0
> **Ngày:** 2026-06-16
> **Mục đích:** Document strategy database isolation cho cả team (CR-09 / Cross-Team #9)

---

## 🎯 Nguyên tắc

PCMS sử dụng pattern **Database-per-Service** (theo [Chris Richardson's Microservices Patterns](https://microservices.io/patterns/data/database-per-service.html)):

- **Mỗi service có database riêng** — không share schema, không cross-database JOIN
- **Cross-service reference chỉ lưu UUID**, query thông qua Feign client
- **Mỗi service có user riêng** với quyền chỉ trên database đó
- **Connection string qua Config Server**, không hardcode

## 📦 Danh sách 12 databases

| # | Service | Database name | User | Tables chính |
|---|---------|---------------|------|--------------|
| 1 | `user-service` | `pcms_user` | `pcms_user` | `users` |
| 2 | `branch-service` | `pcms_branch` | `pcms_branch` | `branches` |
| 3 | `catalog-service` | `pcms_catalog` | `pcms_catalog` | `medicines` |
| 4 | `category-service` | `pcms_category` | `pcms_category` | `categories` |
| 5 | `supplier-service` | `pcms_supplier` | `pcms_supplier` | `suppliers` |
| 6 | `inventory-service` | `pcms_inventory` | `pcms_inventory` | `inventory_batches`, `inventory_transactions` |
| 7 | `customer-service` | `pcms_customer` | `pcms_customer` | `customers`, `loyalty_transactions` |
| 8 | `order-service` | `pcms_order` | `pcms_order` | `orders`, `order_items` |
| 9 | `payment-service` | `pcms_payment` | `pcms_payment` | `payments` |
| 10 | `prescription-service` | `pcms_prescription` | `pcms_prescription` | `prescriptions` |
| 11 | `notification-service` | `pcms_notification` | `pcms_notification` | `notifications` |
| 12 | `report-service` | `pcms_report` | `pcms_report` | `report_schedules` |

**Infra services** (không có DB):
- `discovery-server` (Eureka) — in-memory registry
- `config-server` — file-based config
- `api-gateway` — stateless

## 🚀 Cách setup

### Cách 1: Docker Compose (khuyến nghị)

```bash
cd pcms
docker-compose up -d mysql
```

Script `scripts/init-databases.sql` sẽ tự động:
1. Tạo 12 databases với charset `utf8mb4_unicode_ci`
2. Tạo user `pcms_user` với password `pcms_pass`
3. Grant quyền trên tất cả `pcms_*` databases

### Cách 2: Manual (MySQL local)

```bash
mysql -u root -p < scripts/init-databases.sql
```

Hoặc dùng script wrapper:
- **Windows:** `scripts\setup-mysql.bat`
- **Linux/Mac:** `bash scripts/setup-mysql.sh`

## ⚠️ Quy tắc cho developer

### ✅ ĐƯỢC phép

- Truy vấn bảng trong database của service mình (qua JPA Repository)
- Tạo thêm bảng mới trong database của service mình
- Thêm index, constraint trong database của service mình
- Lưu UUID tham chiếu entity của service khác (vd: `Order.customerId` lưu UUID, không JOIN)

### ❌ CẤM

- ❌ JOIN cross-database (vd: `SELECT ... FROM pcms_order.orders JOIN pcms_user.users`)
- ❌ Truy cập trực tiếp database của service khác
- ❌ Hardcode connection string trong code (phải qua Config Server)
- ❌ Tạo user MySQL với quyền `*.*` (chỉ `pcms_<service>.*`)
- ❌ Đặt tên database không theo pattern `pcms_<service>`
- ❌ Đặt tên bảng dùng chung (vd: cả `order-service` và `customer-service` đều có bảng `transactions`)

## 🔧 Connection string template

```yaml
# config-server/src/main/resources/config/<service-name>.yml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/pcms_<service>?useSSL=false&serverTimezone=Asia/Ho_Chi_Min&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: ${MYSQL_USER:pcms_<service>}
    password: ${MYSQL_PASSWORD:pcms_pass}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

> **Lưu ý:** `createDatabaseIfNotExist=true` chỉ work khi user có quyền CREATE. Trong production, nên disable param này và tạo DB thủ công qua DBA.

## 🧪 Test & Dev

### Testcontainers (đề xuất cho Integration Test)

```java
@Container
static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withDatabaseName("pcms_user_test")
    .withUsername("test")
    .withPassword("test");
```

Mỗi test class có thể tạo database riêng hoặc dùng schema-per-test.

### Local Development với 1 MySQL instance

Đơn giản chỉ cần 1 MySQL container, 12 databases bên trong. Đỡ tốn resource hơn 12 MySQL containers.

### Production

- Tùy business scale, có thể tách thành nhiều MySQL instance (vd: read replica cho `report-service`)
- Sử dụng managed database (AWS RDS, Azure Database) cho HA
- Backup tự động theo từng database (RPO < 1h)

## 🚫 Cross-service data — pattern xử lý

### Cách 1: Feign client (sync) — dùng cho read

```java
// Trong order-service, muốn biết thông tin customer
@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/customers/{id}")
    CustomerResponse getById(@PathVariable UUID id);
}
```

### Cách 2: Event bus (async) — dùng cho state change

```java
// Trong order-service, khi order PAID → publish event
eventPublisher.publish("pcms.order.paid", DomainEvents.orderPaid(...));

// Trong notification-service, subscribe event → gửi notification
```

### Cách 3: Saga / Compensation — dùng cho transaction đa service

Vd: `payment-service` tạo payment → gọi `order-service.markAsPaid` → consume stock qua `inventory-service` → nếu fail phải rollback từng bước.

Đã implement: xem `OrderServiceImpl.markAsPaid` (try-catch + log warning + eventual consistency).

## 📋 Checklist khi tạo service mới

- [ ] Tạo module trong `pcms/` + `pom.xml`
- [ ] Thêm vào `<modules>` của parent pom
- [ ] Tạo config file `config-server/src/main/resources/config/<service-name>.yml`
- [ ] Database: thêm `CREATE DATABASE pcms_<service>` vào `scripts/init-databases.sql`
- [ ] User: thêm `CREATE USER 'pcms_<service>'@'%'` và GRANT
- [ ] Update API Gateway routing trong `api-gateway/application.yml`
- [ ] Test: `mvn -pl <service> -am clean package -DskipTests`

---

> **Xem thêm:**
> - `STANDARDS.md` §7 (Database & JPA rules)
> - `docker-compose.yml` (full setup)
> - `scripts/init-databases.sql` (DB init script)
