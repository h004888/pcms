# ✅ PCMS - Verification Report

**Date:** 2026-06-15
**Environment:** Windows 11, JDK 21.0.7, Maven 3.9.16, Docker Compose v5.0.0

---

## 📊 Kết quả tổng quan

| Phase | Status | Notes |
|---|:---:|---|
| Maven validate | ✅ | Tất cả 15 POMs hợp lệ |
| Maven compile | ✅ | Tất cả 15 services compile thành công (30s) |
| Maven package | ✅ | 15 JARs tạo thành công (tổng 1.2GB) |
| Config-server start | ✅ | Port 8888, serve configs OK |
| Discovery-server start | ✅ | Port 8761, Eureka dashboard OK |

---

## 🐛 Bugs đã phát hiện & fix

### 1. XML Parse Errors (POM)
**Issue:** Ký tự `&` không hợp lệ trong XML element content
**Files:** `user-service/pom.xml`, `customer-service/pom.xml`
**Fix:** Thay `&` bằng `and`

```diff
- <description>User & Authentication Service (port 8081) - UC01, UC02</description>
+ <description>User and Authentication Service (port 8081) - UC01, UC02</description>
```

### 2. Spring Cloud 5.0.x Gateway Renamed
**Issue:** `spring-cloud-starter-gateway` đã bị đổi tên trong Spring Cloud 5.0.x
**File:** `api-gateway/pom.xml`
**Fix:** Đổi sang `spring-cloud-starter-gateway-server-webmvc`

```diff
- <artifactId>spring-cloud-starter-gateway</artifactId>
+ <artifactId>spring-cloud-starter-gateway-server-webmvc</artifactId>
```

> **Lý do:** Spring Cloud Gateway 5.0.x tách thành 2 starters riêng:
> - `spring-cloud-starter-gateway-server-webflux` (reactive)
> - `spring-cloud-starter-gateway-server-webmvc` (servlet, dùng cho MVC apps)

### 3. Spring Boot 4.0 Renamed AOP Starter
**Issue:** `spring-boot-starter-aop` đã bị đổi tên trong Spring Boot 4.0
**Files:** `inventory-service`, `order-service`, `payment-service`, `notification-service` POMs
**Fix:** Đổi sang `spring-boot-starter-aspectj`

```diff
- <artifactId>spring-boot-starter-aop</artifactId>
+ <artifactId>spring-boot-starter-aspectj</artifactId>
```

### 4. Resilience4j BOM Missing Module Version
**Issue:** `resilience4j-bom` 2.4.0 không có version cho `resilience4j-spring-boot4` (Issue #2427)
**File:** `pom.xml` (parent)
**Fix:** Thêm explicit version trong `<dependencyManagement>`

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot4</artifactId>
    <version>${resilience4j.version}</version>
</dependency>
```

### 5. Config-Server Profile Not Activated
**Issue:** `spring.profiles.active: native` trong application.yml bị ignore, default profile chạy
**Fix:** Set profile qua system property khi start
- `docker-compose.yml`: `SPRING_PROFILES_ACTIVE: native`
- `scripts/run-local.sh` và `.bat`: thêm `--spring.profiles.active=native`

### 6. Config-Server Port Conflict
**Issue:** File `config/application.yml` có `server.port: 8080` → ghi đè port config-server (8888)
**Fix:** Xóa `server.port` khỏi `config/application.yml` (vì mỗi service đã có port riêng trong file config riêng)

### 7. Repository Method Signature
**Issue:** `findByStatus(UserStatus, Pageable)` không match với call `findByStatus(UserStatus)`
**File:** `user-service/UserRepository.java`
**Fix:** Thêm method overload `List<User> findByStatus(UserStatus status)`

```java
Page<User> findByStatus(UserStatus status, Pageable pageable);
List<User> findByStatus(UserStatus status);  // mới thêm
```

### 8. Missing Import
**Issue:** `InventoryClient` trong report-service dùng `List` nhưng thiếu import
**File:** `report-service/client/InventoryClient.java`
**Fix:** Thêm `import java.util.List;`

---

## 📈 Verification log chi tiết

### Maven Compile
```
[INFO] Reactor Summary for pharmacy-chain-management 1.0.0-SNAPSHOT:
[INFO] pharmacy-chain-management .......................... SUCCESS
[INFO] config-server ...................................... SUCCESS
[INFO] discovery-server ................................... SUCCESS
[INFO] api-gateway ........................................ SUCCESS
[INFO] user-service ....................................... SUCCESS
[INFO] branch-service ..................................... SUCCESS
[INFO] catalog-service .................................... SUCCESS
[INFO] category-service ................................... SUCCESS
[INFO] supplier-service ................................... SUCCESS
[INFO] inventory-service .................................. SUCCESS
[INFO] customer-service ................................... SUCCESS
[INFO] order-service ...................................... SUCCESS
[INFO] payment-service .................................... SUCCESS
[INFO] prescription-service ............................... SUCCESS
[INFO] notification-service ............................... SUCCESS
[INFO] report-service ..................................... SUCCESS
[INFO] BUILD SUCCESS
```

### Config-Server Test
```
$ curl http://localhost:8888/actuator/health
{"status":"UP", ...}

$ curl http://localhost:8888/user-service/default
{
  "name": "user-service",
  "profiles": ["default"],
  "propertySources": [{
    "name": "classpath:/config/user-service.yml",
    "source": {
      "server.port": 8081,
      "spring.datasource.url": "jdbc:mysql://localhost:3306/pcms_user?...",
      "app.jwt.secret": "pcms-jwt-secret-key-..."
    }
  }]
}
```

### Discovery-Server Test
```
$ curl http://localhost:8761/actuator/health
{"status":"UP"}

$ curl http://localhost:8761/ | grep title
<title>Eureka</title>
```

---

## 🔜 Chưa verify (cần MySQL + Docker)

- ⏳ MySQL container start
- ⏳ Business services start với MySQL
- ⏳ Inter-service communication qua Eureka
- ⏳ API Gateway routing
- ⏳ Feign Client calls
- ⏳ Resilience4J circuit breaker
- ⏳ JPA auto-create tables

**Để test toàn bộ, cần:**
```bash
# Start MySQL
docker run -d --name pcms-mysql -e MYSQL_ROOT_PASSWORD=rootpass -e MYSQL_USER=pcms_user -e MYSQL_PASSWORD=pcms_pass -e MYSQL_DATABASE=pcms -p 3306:3306 mysql:8.0

# Then start all services via run scripts
./scripts/run-local.sh
```
