# Phân tích lỗi api-gateway và các services — Spring Boot 4.0 Migration

**Ngày phân tích:** 2026-06-19
**Stack:** Spring Boot 4.0.7 + Spring Cloud Gateway (server-webmvc) + Spring Cloud Config
**Triệu chứng:** Tất cả services Java không start được, đặc biệt `api-gateway` fail với:
```
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
Reason: Failed to determine a suitable driver class
```

---

## 1. Tổng quan — Hai lỗi đan xen

Có **2 vấn đề riêng biệt** nhưng cùng xuất hiện khi chạy api-gateway:

| # | Vấn đề | Mức độ | Ảnh hưởng |
|---|--------|--------|-----------|
| 1 | Excludes dùng package paths **CŨ** (Spring Boot 3.x) | **CRITICAL** | Auto-config KHÔNG được loại trừ → fail khi tạo DataSource |
| 2 | api-gateway không có `spring.config.import` để kết nối config-server | **CRITICAL** | Config-server không được load → gateway chạy local-only, không có routes từ config |

---

## 2. Vấn đề #1 — Package paths cho Auto-Configuration đã đổi trong Spring Boot 4.0

### 2.1 Bằng chứng

Log của api-gateway (logs/api-gateway.log):

```
2026-06-19T15:18:29.077+07:00 WARN 30596 --- [api-gateway] [           main] 
  ConfigServletWebServerApplicationContext : Exception encountered during context 
  initialization - cancelling refresh attempt: org.springframework.beans.factory.
  BeanCreationException: Error creating bean with name 'entityManagerFactory' 
  defined in class path resource [org/springframework/boot/hibernate/autoconfigure/
  HibernateJpaConfiguration.class]: Failed to initialize dependency 
  'dataSourceScriptDatabaseInitializer' of LoadTimeWeaverAware bean 
  'entityManagerFactory'
```

Quan sát quan trọng: stack trace chỉ ra class đang được load là
`org.springframework.boot.hibernate.autoconfigure.HibernateJpaConfiguration`
— đây là package path **MỚI** của Spring Boot 4.

### 2.2 Nguyên nhân

Theo [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide), phần **"Module Dependencies"** & **"Package Organization"**:

> *"Spring Boot 4.0 has a new modular design and now ships smaller focused modules rather than several large jars."*
>
> *"The new modules and 'starter' POMs follow a convention that lets you identify where the support for a given technology comes from:*
>   - *All Spring Boot modules are named `spring-boot-<technology>`.*
>   - *The root package of each module is `org.springframework.boot.<technology>`."*

Quy ước mapping:

| Technology | Module | Root package MỚI (SB 4) |
|------------|--------|--------------------------|
| JDBC | `spring-boot-jdbc` | `org.springframework.boot.jdbc` |
| Hibernate | `spring-boot-hibernate` | `org.springframework.boot.hibernate` |
| Spring Data JPA | `spring-boot-data-jpa` | `org.springframework.boot.data.jpa` |

Mapping class cụ thể:

| Auto-configuration Class | Package CŨ (Spring Boot 3.x) | Package MỚI (Spring Boot 4.0) |
|--------------------------|-------------------------------|---------------------------------|
| `DataSourceAutoConfiguration` | `org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration` | `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration` |
| `HibernateJpaAutoConfiguration` → `HibernateJpaConfiguration` | `org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration` | `org.springframework.boot.hibernate.autoconfigure.HibernateJpaConfiguration` |
| `JpaRepositoriesAutoConfiguration` | `org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration` | `org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration` |

### 2.3 Vị trí lỗi trong code

**File 1:** `api-gateway/src/main/resources/application.yml` (lines 3–11)
```yaml
spring:
  application:
    name: api-gateway
  autoconfigure:
    exclude:                           # ← DÙNG PACKAGE CŨ, KHÔNG MATCH
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

**File 2:** `config-server/src/main/resources/config/api-gateway.yml` (lines 3–14) — CŨNG DÙNG PACKAGE CŨ
```yaml
spring:
  application:
    name: api-gateway
  config:
    import: "optional:configserver:http://localhost:8888"   # ← BUG #2 (circular)
  profiles:
    active: default
  autoconfigure:
    exclude:                           # ← DÙNG PACKAGE CŨ
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

Theo [Spring Boot docs §Auto-configuration](https://docs.spring.io/spring-boot/4.0/reference/using/auto-configuration.html):

> *"If the class is not on the classpath, you can use the `excludeName` attribute of the annotation and specify the fully qualified name instead... Finally, you can also control the list of auto-configuration classes to exclude by using the `spring.autoconfigure.exclude` property."*

→ Khi một FQCN **không tồn tại** trên classpath, Spring Boot 4 **vẫn khởi tạo bean `entityManagerFactory`** vì auto-config match không thành công nhưng không throw exception. Kết quả: exclude bị ignore → DataSource/Hibernate JPA tự động được khởi tạo → fail vì thiếu `spring.datasource.url`.

Ví dụ chính thức từ docs SB 4.0:
```java
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
```
→ Xác nhận package MỚI: `org.springframework.boot.jdbc.autoconfigure`.

---

## 3. Vấn đề #2 — api-gateway không kết nối Spring Cloud Config Server

### 3.1 Bằng chứng

Log api-gateway:
```
No active profile set, falling back to 1 default profile: "default"
```

Nếu api-gateway đã kết nối config-server, profile phải được load từ response của config-server. So sánh:

| Service | Local `application.yml` | Có `spring.config.import`? |
|---------|-------------------------|---------------------------|
| `user-service` | có đầy đủ | ✅ |
| `branch-service` | (kiểm tra dưới) | (kiểm tra dưới) |
| ... (các business services khác) | ... | ... |
| `api-gateway` | **THIẾU** `spring.config.import` | ❌ |

### 3.2 Nguyên nhân

Theo [Spring Cloud Config Client docs](https://docs.spring.io/spring-cloud-config/reference/client.html), phần **"Spring Boot Config Data Import"**:

> *"Spring Boot 2.4 introduced a new way to import configuration data via the `spring.config.import` property. This is now the default way to bind to Config Server."*
>
> *"A `bootstrap` file (properties or yaml) is **not** needed for the Spring Boot Config Data method of import via `spring.config.import`."*

Recommended pattern (Spring Boot 4 + Spring Cloud Config):
```yaml
spring:
  application:
    name: my-service
  config:
    import: "optional:configserver:http://localhost:8888"
  profiles:
    active: default
```

So sánh với `user-service/src/main/resources/application.yml` (hoạt động đúng):
```yaml
spring:
  application:
    name: user-service
  config:
    import: "optional:configserver:http://localhost:8888"   # ← ĐÚNG
  profiles:
    active: default
```

`api-gateway/src/main/resources/application.yml` hiện tại (SAI):
```yaml
spring:
  application:
    name: api-gateway
  # ← THIẾU spring.config.import
  autoconfigure:
    exclude: ...
```

→ Không có `spring.config.import` ⇒ api-gateway chỉ đọc local config, không bao giờ tải routes từ config-server. Ngược lại, business services như `user-service` có import nên load đúng `user-service.yml` từ config-server (có datasource config).

### 3.3 Bug phụ — circular reference trong config-server

`config-server/src/main/resources/config/api-gateway.yml` chứa:
```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"   # ← circular!
```

Đây là **circular reference**: config của api-gateway lại bảo api-gateway import từ config-server. Đúng pattern là chỉ đặt `spring.config.import` ở **local** `application.yml` của từng service, không đặt trong file trên config-server (vì file đó LÀ cái được serve về).

---

## 4. Cách fix

### Fix 1 — `api-gateway/src/main/resources/application.yml` (LOCAL)

**Trước** (SAI):
```yaml
spring:
  application:
    name: api-gateway
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

**Sau** (ĐÚNG):
```yaml
spring:
  application:
    name: api-gateway
  config:
    import: "optional:configserver:http://localhost:8888"
  profiles:
    active: default
  autoconfigure:
    exclude:
      - org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
      - org.springframework.boot.hibernate.autoconfigure.HibernateJpaConfiguration
      - org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration
```

### Fix 2 — `config-server/src/main/resources/config/api-gateway.yml` (SERVER)

**Trước** (SAI):
```yaml
spring:
  application:
    name: api-gateway
  config:
    import: "optional:configserver:http://localhost:8888"   # ← circular
  profiles:
    active: default
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration       # ← OLD
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration # ← OLD
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration # ← OLD

app:
  jwt:
    secret: "<JWT_SECRET_FROM_USER_SERVICE_CONFIG>"  # phải match với user-service
```

### Fix 3 (optional) — Audit các business services

Các business services (user-service, branch-service, ...) **CẦN** DataSource, nên KHÔNG exclude JPA. Đã verify `user-service` local `application.yml` có `spring.config.import` đúng. Có thể các service khác tương tự.

---

## 5. Verification sau khi fix

```bash
# 1. Kill process java cũ đã start nhưng fail
powershell -Command "Get-Process | Where-Object { \$_.ProcessName -like '*java*' } | ForEach-Object { \$cmdline = (Get-WmiObject Win32_Process -Filter \"ProcessId = \$(\$_.Id)\").CommandLine; if (\$cmdline -match 'api-gateway') { Stop-Process -Id \$_.Id -Force } }"

# 2. Rebuild config-server và api-gateway (vì application.yml đã thay đổi)
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
./apache-maven-3.9.9/bin/mvn -pl config-server,api-gateway -am clean package -DskipTests

# 3. Restart config-server (vì file served đã đổi)
powershell -Command "Get-Process | Where-Object { \$_.ProcessName -like '*java*' } | ForEach-Object { \$cmdline = (Get-WmiObject Win32_Process -Filter \"ProcessId = \$(\$_.Id)\").CommandLine; if (\$cmdline -match 'config-server') { Stop-Process -Id \$_.Id -Force } }"
sleep 2
nohup "C:\Program Files\Java\jdk-21\bin\java.exe" -jar config-server/target/config-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=native > logs/config-server.log 2>&1 &
echo "config-server started, waiting 20s..."
sleep 20

# 4. Verify config-server serves đúng content
curl -s "http://localhost:8888/api-gateway/default" | head -20

# 5. Start api-gateway
nohup "C:\Program Files\Java\jdk-21\bin\java.exe" -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar > logs/api-gateway.log 2>&1 &
echo "api-gateway started, waiting 25s..."
sleep 25

# 6. Verify
curl -s -o /dev/null -w "Gateway: %{http_code}\n" http://localhost:8080/actuator/health
tail -10 logs/api-gateway.log
```

Nếu fix đúng, output sẽ là:
- Gateway UP (200)
- Log không còn `BeanCreationException` 
- Log hiện `Started ApiGatewayApplication in X.XXX seconds`

---

## 6. Tham khảo (sources đã fetch)

| # | URL | Mục đích |
|---|-----|---------|
| 1 | https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide | Migration guide — đổi package paths & modular design |
| 2 | https://docs.spring.io/spring-boot/4.0/reference/using/auto-configuration.html | Auto-configuration reference — exclude pattern & class names |
| 3 | https://docs.spring.io/spring-cloud-config/reference/client.html | Spring Cloud Config Client — `spring.config.import` usage |

---

**Tóm tắt 1 câu:** Spring Boot 4.0 đã modularize package structure (`org.springframework.boot.<technology>` thay vì `org.springframework.boot.autoconfigure.<technology>`), nên mọi `spring.autoconfigure.exclude` đang dùng package cũ đều bị ignore, kết hợp với việc thiếu `spring.config.import` ở api-gateway khiến config-server không được load ⇒ DataSource tự động được khởi tạo nhưng không có URL ⇒ fail to start.
