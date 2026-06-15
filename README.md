# 🏥 PCMS — Pharmacy Chain Management System

> **Hệ thống quản lý chuỗi nhà thuốc** theo kiến trúc Microservice
> **Stack:** Spring Boot **4.0.7** + Spring Cloud **2025.1.2** (Oakwood) + JDK **21** + Maven 3.9.16 + MySQL 8.0
> **Resilience4J:** 2.4.0
> **SRS Document:** [SRS_PhamacyChainManagementSystem_v1.0.0.md](../SRS_PhamacyChainManagementSystem_v1.0.0.md) (v1.3.0)

---

## 📋 Mục lục

1. [Tổng quan](#-tổng-quan)
2. [Yêu cầu môi trường](#-yêu-cầu-môi-trường)
3. [Cài đặt](#-cài-đặt)
   - [Cài JDK 21](#1-cài-jdk-21)
   - [Cài Maven 3.9.16](#2-cài-maven-3916)
   - [Cài MySQL 8.0](#3-cài-mysql-80)
   - [Cài IntelliJ IDEA (tùy chọn)](#4-cài-intellij-idea-tùy-chọn)
4. [Setup dự án](#-setup-dự-án)
   - [Clone/Tải source code](#1-clonetải-source-code)
   - [Setup MySQL database](#2-setup-mysql-database)
   - [Build project](#3-build-project)
5. [Chạy dự án](#-chạy-dự-án)
   - [Cách 1: Chạy thủ công (khuyến nghị cho dev)](#cách-1-chạy-thủ-công-khuyến-nghị-cho-dev)
   - [Cách 2: Dùng script tự động](#cách-2-dùng-script-tự-động)
   - [Cách 3: Docker Compose (cho production)](#cách-3-docker-compose-cho-production)
6. [Verify hệ thống](#-verify-hệ-thống)
7. [Sử dụng API](#-sử-dụng-api)
   - [Test với cURL](#test-với-curl)
   - [Test với Postman](#test-với-postman)
   - [Flow nghiệp vụ hoàn chỉnh](#flow-nghiệp-vụ-hoàn-chỉnh)
8. [Cấu trúc dự án](#-cấu-trúc-dự-án)
9. [Các lỗi thường gặp & cách fix](#-các-lỗi-thường-gặp--cách-fix)
10. [Các service quan trọng](#-các-service-quan-trọng)
11. [Bước tiếp theo](#-bước-tiếp-theo)

---

## 🎯 Tổng quan

PCMS (Pharmacy Chain Management System) là hệ thống quản lý chuỗi nhà thuốc được thiết kế theo **kiến trúc Microservice** dựa trên SRS v1.3.0 với:

- ✅ **15 microservices** (3 infrastructure + 12 business)
- ✅ **5 actors nội bộ:** Admin, CEO, Branch Manager, Pharmacist, Customer
- ✅ **4 actors ngoại vi:** Payment Gateway, SMS Provider, Email Provider, Printer
- ✅ **13 Use Cases** (UC01-UC13) theo SRS §2.2
- ✅ **12 entities** phân tách theo bounded context
- ✅ **7 business rules** (BR01-BR07) + **12 non-screen functions** (NSF-01-NSF-12)
- ✅ **MySQL 8.0** với 12 database riêng (database-per-service)
- ✅ **Eureka Service Discovery** + **Spring Cloud Gateway** routing
- ✅ **Resilience4J** Circuit Breaker + Retry cho inter-service calls

### 🏗 Sơ đồ kiến trúc

```
                         ┌─────────────────────┐
                         │      CLIENT         │
                         │   (Web Browser)     │
                         └──────────┬──────────┘
                                    │ HTTPS / REST
                                    ▼
                         ┌─────────────────────┐
                         │    API GATEWAY      │ ← port 8080
                         │   (Spring Cloud     │   Routing, Load balance
                         │     Gateway MVC)    │
                         └──────────┬──────────┘
                                    │
        ┌─────────────┬─────────────┼─────────────┬──────────────┐
        │             │             │             │              │
        ▼             ▼             ▼             ▼              ▼
  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐
  │ USER    │  │ BRANCH   │  │ CATALOG  │  │INVENTORY │  │   ORDER    │
  │ 8081    │  │ 8082     │  │ 8083     │  │ 8086     │  │   8088     │
  │UC01+UC02│  │UC03      │  │UC04+UC10 │  │UC05      │  │  UC06      │
  └─────────┘  └──────────┘  └──────────┘  └──────────┘  └────────────┘
                                                                   │
   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────────────┐
   │ CUSTOMER │  │ PAYMENT  │  │ NOTIFY   │  │ CATEGORY 8084      │
   │ 8087     │  │ 8089     │  │ 8091     │  │ SUPPLIER 8085      │
   │UC08      │  │UC07      │  │UC13      │  │ PRESCRIPTION 8090  │
   └──────────┘  └──────────┘  └──────────┘  │ REPORT 8092        │
                                             └────────────────────┘

   ┌──────────────────────────────────────────────────────────────────┐
   │ Infrastructure                                                   │
   │  CONFIG-SERVER 8888  →  DISCOVERY-SERVER (Eureka) 8761         │
   │  MySQL 8.0  (12 databases: pcms_user, pcms_branch, ...pcms_report)│
   └──────────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Yêu cầu môi trường

| Tool | Version bắt buộc | Verify command |
|---|---|---|
| **JDK** | 21 (Temurin 21.0.11+10 hoặc cao hơn) | `java -version` |
| **Maven** | 3.9.16+ | `mvn -version` |
| **MySQL** | 8.0+ | `mysql --version` |
| **Git** | 2.x+ | `git --version` |
| **Docker** (tùy chọn) | 24+ | `docker --version` |
| **IntelliJ IDEA** (tùy chọn) | Ultimate 2024.3+ | - |

### 💾 Dung lượng cần

- Source code: ~50 MB
- Sau khi build: ~1.2 GB (15 file JAR)
- RAM khi chạy: ~4-6 GB (15 services)
- Disk cho MySQL: ~500 MB (12 databases)

---

## 🔧 Cài đặt

### 1. Cài JDK 21

**Windows:**
```bash
# Cách 1: Tải từ Adoptium (khuyến nghị)
# Link: https://adoptium.net/temurin/releases/?version=21
# Tải file .msi cho Windows, cài đặt bình thường

# Cách 2: Dùng Chocolatey
choco install temurin21

# Verify
java -version
# Kết quả mong đợi: openjdk version "21.0.x" 2025-xx-xx LTS
```

**macOS:**
```bash
brew install openjdk@21

# Verify
java -version
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version
```

> ⚠️ **Lưu ý:** Phải set `JAVA_HOME` environment variable:
> - Windows: `System Properties → Environment Variables → JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-21.0.x`
> - macOS/Linux: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` (thêm vào `~/.zshrc` hoặc `~/.bashrc`)

### 2. Cài Maven 3.9.16

**Windows:**
```bash
# Cách 1: Tải từ Apache
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip"
# Giải nén vào D:\tools\apache-maven-3.9.16
# Thêm D:\tools\apache-maven-3.9.16\bin vào PATH

# Cách 2: Chocolatey
choco install maven

# Verify
mvn -version
# Kết quả mong đợi: Apache Maven 3.9.16 ...
#                    Java version: 21.0.x
```

**macOS:**
```bash
brew install maven
mvn -version
```

**Linux:**
```bash
sudo apt install maven
mvn -version
```

### 3. Cài MySQL 8.0

**Windows:**
```bash
# Cách 1: MySQL Installer
# Tải từ: https://dev.mysql.com/downloads/installer/
# Chọn "Server only" hoặc "Custom" (bao gồm MySQL Server + MySQL Workbench)
# Nhớ mật khẩu root bạn đặt!

# Cách 2: Chocolatey
choco install mysql

# Verify
mysql --version
```

**macOS:**
```bash
brew install mysql
brew services start mysql

# Đặt mật khẩu root
mysql_secure_installation
```

**Linux:**
```bash
sudo apt install mysql-server-8.0
sudo systemctl start mysql
sudo mysql_secure_installation
```

> ⚠️ **QUAN TRỌNG:** Nhớ mật khẩu root MySQL! Mặc định dự án dùng user `pcms_user` với password `pcms_pass`.

### 4. Cài IntelliJ IDEA (tùy chọn)

- Tải **IntelliJ IDEA Ultimate** (Community thiếu nhiều tính năng Spring): https://www.jetbrains.com/idea/download/
- Cài đặt plugin **Spring Boot** và **Lombok** (nếu dùng)

---

## 🚀 Setup dự án

### 1. Clone/Tải source code

```bash
# Nếu dùng Git
cd C:/Users/ADMIN/Downloads
git clone <repository-url> pcms
cd pcms

# Hoặc tải file ZIP và giải nén vào D:\pcms
```

**Cấu trúc thư mục sau khi giải nén:**
```
pcms/
├── pom.xml                          # Parent POM
├── docker-compose.yml               # 15 services + MySQL
├── README.md                        # File này
├── VERIFICATION_REPORT.md           # Báo cáo verification
├── .gitignore
├── config-server/                   # port 8888
├── discovery-server/                # port 8761
├── api-gateway/                     # port 8080
├── user-service/                    # port 8081
├── branch-service/                  # port 8082
├── catalog-service/                 # port 8083
├── category-service/                # port 8084
├── supplier-service/                # port 8085
├── inventory-service/               # port 8086
├── customer-service/                # port 8087
├── order-service/                   # port 8088
├── payment-service/                 # port 8089
├── prescription-service/            # port 8090
├── notification-service/            # port 8091
├── report-service/                  # port 8092
├── postman/PCMS.postman_collection.json
└── scripts/
    ├── build-all.sh / build-all.bat
    ├── run-local.sh / run-local.bat
    ├── stop-all.sh / stop-all.bat
    ├── setup-mysql.sh / setup-mysql.bat
    └── setup-mysql.sql
```

### 2. Setup MySQL database

**Windows:**
```bash
# Mở Command Prompt với quyền Administrator
cd C:\Users\ADMIN\Downloads\pcms\scripts

REM Chạy setup (sẽ hỏi password root MySQL)
setup-mysql.bat
```

**macOS/Linux:**
```bash
cd /path/to/pcms/scripts
chmod +x setup-mysql.sh
./setup-mysql.sh
```

**Script sẽ tự động:**
1. Tạo user `pcms_user` với password `pcms_pass`
2. Tạo 12 databases: `pcms_user`, `pcms_branch`, `pcms_catalog`, `pcms_category`, `pcms_supplier`, `pcms_inventory`, `pcms_customer`, `pcms_order`, `pcms_payment`, `pcms_prescription`, `pcms_notification`, `pcms_report`
3. Cấp quyền cho user

**Hoặc chạy thủ công:**
```bash
# Đăng nhập MySQL với quyền root
mysql -u root -p

# Trong MySQL shell, chạy:
source /path/to/pcms/scripts/setup-mysql.sql;
exit;
```

**Verify MySQL setup:**
```bash
mysql -u pcms_user -ppcms_pass -e "SHOW DATABASES LIKE 'pcms_%';"
```
Kết quả mong đợi: 12 databases hiện ra.

### 3. Build project

**Windows:**
```bash
cd C:\Users\ADMIN\Downloads\pcms
scripts\build-all.bat
```

**macOS/Linux:**
```bash
cd /path/to/pcms
chmod +x scripts/build-all.sh
./scripts/build-all.sh
```

**Quá trình build sẽ:**
- Tải về ~500MB dependencies Maven (lần đầu)
- Compile 81 Java files
- Tạo 15 file JAR (~80-100MB mỗi cái)
- Tổng thời gian: 5-10 phút (lần đầu), 30 giây (lần sau)

**Verify build thành công:**
```bash
# Windows
dir *\target\*.jar
# Linux/macOS
ls */target/*.jar
```
Kết quả mong đợi: 15 file JAR (`config-server-1.0.0-SNAPSHOT.jar`, ...)

---

## ▶️ Chạy dự án

### Cách 1: Chạy thủ công (khuyến nghị cho dev)

Mở **3 terminal/cmd** riêng biệt:

**Terminal 1 — Config Server (BẮT BUỘC chạy đầu tiên):**
```bash
cd C:\Users\ADMIN\Downloads\pcms
java -jar config-server\target\config-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=native
```
Đợi thấy: `Started ConfigServerApplication in 6 seconds`

**Terminal 2 — Discovery Server (Eureka):**
```bash
cd C:\Users\ADMIN\Downloads\pcms
java -jar discovery-server\target\discovery-server-1.0.0-SNAPSHOT.jar
```
Đợi thấy: `Started DiscoveryServerApplication` và check http://localhost:8761

**Terminal 3 — API Gateway:**
```bash
cd C:\Users\ADMIN\Downloads\pcms
java -jar api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar
```
Đợi thấy: `Started ApiGatewayApplication`

**Các terminal 4-15 — 12 business services (mỗi service 1 terminal, hoặc chạy nền):**
```bash
# Mỗi service một lệnh riêng:
java -jar user-service\target\user-service-1.0.0-SNAPSHOT.jar
java -jar branch-service\target\branch-service-1.0.0-SNAPSHOT.jar
java -jar catalog-service\target\catalog-service-1.0.0-SNAPSHOT.jar
# ... và 9 services còn lại
```

### Cách 2: Dùng script tự động

**Windows:**
```bash
cd C:\Users\ADMIN\Downloads\pcms
scripts\run-local.bat
```

**macOS/Linux:**
```bash
cd /path/to/pcms
chmod +x scripts/run-local.sh
./scripts/run-local.sh
```

Script sẽ tự động:
1. Start config-server (đợi 15s)
2. Start discovery-server (đợi 10s)
3. Start api-gateway
4. Start 12 business services song song
5. Hiển thị URLs quan trọng

**Dừng tất cả services:**
```bash
# Windows
scripts\stop-all.bat

# macOS/Linux
./scripts/stop-all.sh
```

### Cách 3: Docker Compose (cho production)

```bash
cd /path/to/pcms
docker-compose up -d --build
```

Lệnh này sẽ:
1. Build 15 Docker images
2. Start 1 MySQL container + 15 service containers
3. Tự động tạo networks và health checks

**Verify Docker:**
```bash
docker-compose ps
docker-compose logs -f
```

**Dừng Docker:**
```bash
docker-compose down
```

---

## ✅ Verify hệ thống

Sau khi start xong (đợi ~60 giây cho tất cả services), kiểm tra:

### 1. Health check tất cả 15 services

Tạo file `verify.bat` hoặc copy lệnh sau:

**Windows (PowerShell):**
```powershell
1..15 | ForEach-Object {
    $svc = @(
        "config-server:8888", "discovery-server:8761", "api-gateway:8080",
        "user-service:8081", "branch-service:8082", "catalog-service:8083",
        "category-service:8084", "supplier-service:8085", "inventory-service:8086",
        "customer-service:8087", "order-service:8088", "payment-service:8089",
        "prescription-service:8090", "notification-service:8091", "report-service:8092"
    )[$_ - 1]
    $name, $port = $svc.Split(':')
    $health = try { (Invoke-WebRequest "http://localhost:$port/actuator/health" -UseBasicParsing -TimeoutSec 2).Content } catch { "DOWN" }
    Write-Host "$name ($port)`: $health"
}
```

**macOS/Linux (bash):**
```bash
for svc in "config-server:8888" "discovery-server:8761" "api-gateway:8080" \
           "user-service:8081" "branch-service:8082" "catalog-service:8083" \
           "category-service:8084" "supplier-service:8085" "inventory-service:8086" \
           "customer-service:8087" "order-service:8088" "payment-service:8089" \
           "prescription-service:8090" "notification-service:8091" "report-service:8092"; do
    name=${svc%:*}; port=${svc#*:}
    status=$(curl -s --max-time 2 "http://localhost:$port/actuator/health" 2>&1 | grep -oE '"status":"[^"]*"' | head -1)
    [ -n "$status" ] && echo "✅ $name ($port) → $status" || echo "❌ $name ($port) → DOWN"
done
```

### 2. Kiểm tra Eureka

Mở browser: **http://localhost:8761**

Bạn sẽ thấy:
- Status: `UP`
- Instances currently registered: **13 services** (trừ config-server và discovery-server không tự đăng ký)

### 3. Kiểm tra Database

```bash
mysql -u pcms_user -ppcms_pass -e "SELECT TABLE_SCHEMA, TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA LIKE 'pcms_%' AND TABLE_TYPE='BASE TABLE';"
```

Kết quả: 13 tables trong 12 databases (database `pcms_inventory` có 2 tables: `inventory_batches` + `inventory_transactions`).

---

## 🌐 Sử dụng API

Tất cả API đều truy cập qua **API Gateway** ở `http://localhost:8080` với prefix `/api/v1/`.

### Test với cURL

#### 1. Tạo chi nhánh (UC03)
```bash
curl -X POST http://localhost:8080/api/v1/branches \
  -H "Content-Type: application/json" \
  -d '{
    "code": "HQ",
    "name": "Headquarter",
    "address": "12 Le Loi, District 1, HCMC",
    "phone": "0281234567"
  }'
```

**Response:**
```json
{
  "id": "cefc2026-bf86-4dcd-8e17-69e749f13404",
  "code": "HQ",
  "name": "Headquarter",
  "address": "12 Le Loi, District 1, HCMC",
  "phone": "0281234567",
  "status": "ACTIVE",
  "createdAt": "2026-06-15T11:59:29.110Z",
  "updatedAt": "2026-06-15T11:59:29.110Z"
}
```

#### 2. List chi nhánh
```bash
curl http://localhost:8080/api/v1/branches
```

**Response:**
```json
{
  "data": [...],
  "page": 0,
  "size": 20,
  "total": 2,
  "totalPages": 1
}
```

#### 3. Tạo khách hàng (UC08 - auto-generate code CUST-yyyy####)
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nguyen Van A",
    "phone": "0901234567",
    "email": "a@pcms.vn"
  }'
```

**Response:**
```json
{
  "id": "4074f318-c1f0-4974-9d93-7e792a24a3af",
  "code": "CUST-20260001",
  "name": "Nguyen Van A",
  "phone": "0901234567",
  "email": "a@pcms.vn",
  "points": 0,
  ...
}
```

#### 4. Tạo thuốc (UC04)
```bash
curl -X POST http://localhost:8080/api/v1/medicines \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "MD001",
    "name": "Paracetamol 500mg",
    "categoryId": "00000000-0000-0000-0000-000000000001",
    "price": 5000,
    "unit": "box",
    "prescriptionRequired": false
  }'
```

#### 5. Tìm kiếm thuốc (UC10 - autocomplete)
```bash
curl "http://localhost:8080/api/v1/search?q=Para"
```

### Test với Postman

1. Mở Postman → **Import** → chọn file `postman/PCMS.postman_collection.json`
2. Collection sẽ có 13 folders (UC01-UC13) với 50+ test requests
3. Set environment variable `gateway` = `http://localhost:8080`
4. Chạy từng request theo thứ tự UC01 → UC13

### Flow nghiệp vụ hoàn chỉnh

Dưới đây là flow thực tế: **Nhập kho → Tạo đơn → Thanh toán → Trừ tích điểm**

```bash
# Bước 1: Lấy IDs (giả sử đã có branch + customer + medicine)
BRANCH_ID=$(curl -s http://localhost:8080/api/v1/branches | python -c "import sys,json; print(json.load(sys.stdin)['data'][0]['id'])")
MED_ID=$(curl -s http://localhost:8080/api/v1/medicines | python -c "import sys,json; print(json.load(sys.stdin)['data'][0]['id'])")
CUST_ID=$(curl -s http://localhost:8080/api/v1/customers | python -c "import sys,json; print(json.load(sys.stdin)['data'][0]['id'])")
USER_ID="00000000-0000-0000-0000-000000000001"

# Bước 2: Nhập kho 100 viên Paracetamol (UC05)
curl -X POST "http://localhost:8080/api/v1/inventory/import" \
  -H "Content-Type: application/json" \
  -d "{
    \"medicineId\": \"$MED_ID\",
    \"branchId\": \"$BRANCH_ID\",
    \"batchNo\": \"BTH-001\",
    \"qty\": 100,
    \"expiryDate\": \"2027-12-31\",
    \"actorId\": \"$USER_ID\"
  }"

# Bước 3: Tạo đơn 12 viên (BR04: 5% discount cho qty ≥ 10)
ORDER_RESP=$(curl -s -X POST "http://localhost:8080/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"$CUST_ID\",
    \"branchId\": \"$BRANCH_ID\",
    \"items\": [{\"medicineId\": \"$MED_ID\", \"quantity\": 12}]
  }")
echo "$ORDER_RESP"
# Kết quả: subtotal=60000, discount=3000 (5%), total=57000

ORDER_ID=$(echo "$ORDER_RESP" | python -c "import sys,json; print(json.load(sys.stdin)['id'])")

# Bước 4: Thanh toán tiền mặt (UC07)
curl -X POST "http://localhost:8080/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"paymentMethod\": \"CASH\",
    \"amount\": 57000,
    \"tenderedAmount\": 60000,
    \"staffId\": \"$USER_ID\"
  }"

# Bước 5: Mark order as paid (sẽ trigger trừ stock FIFO + cộng điểm BR07)
curl -X PUT "http://localhost:8080/api/v1/orders/$ORDER_ID/pay"

# Bước 6: Kiểm tra điểm loyalty (BR07: 57000/1000 = 57 điểm)
curl http://localhost:8080/api/v1/customers
# Kết quả: customer có 57 points
```

---

## 📂 Cấu trúc dự án

```
pcms/
├── pom.xml                          ← Parent POM (Spring Boot 4.0.7, Cloud 2025.1.2)
├── docker-compose.yml               ← 15 services + MySQL
├── README.md                        ← File này
├── VERIFICATION_REPORT.md           ← Báo cáo verification
│
├── config-server/                   ← port 8888
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/pcms/configserver/
│       │   └── ConfigServerApplication.java
│       └── resources/
│           ├── application.yml
│           └── config/              ← 13 file yml cho mỗi service
│
├── discovery-server/                ← port 8761 (Eureka)
├── api-gateway/                     ← port 8080 (12 routes)
│
├── user-service/                    ← port 8081 (UC01+UC02)
│   ├── entity/User.java             ← 12 attributes
│   ├── enums/{Role,UserStatus}.java
│   ├── repository/UserRepository.java
│   ├── controller/{Auth,User}Controller.java
│   ├── security/JwtService.java
│   ├── config/SecurityConfig.java
│   └── scheduler/AccountUnlockScheduler.java  ← NSF-10
│
├── branch-service/                  ← port 8082 (UC03)
├── catalog-service/                 ← port 8083 (UC04+UC10)
├── category-service/                ← port 8084
├── supplier-service/                ← port 8085 (UC11)
├── inventory-service/               ← port 8086 (UC05)
│   ├── entity/{InventoryBatch,InventoryTransaction}.java
│   ├── repository/...
│   ├── controller/InventoryController.java
│   └── scheduler/ExpiryCheckScheduler.java  ← NSF-03
│
├── customer-service/                ← port 8087 (UC08)
├── order-service/                   ← port 8088 (UC06)
│   ├── client/{Catalog,Inventory,Customer}Client.java
│   ├── service/OrderService.java
│   ├── scheduler/OrderAutoCancelScheduler.java  ← NSF-01, BR01
│   └── entity/{Order,OrderItem}.java
│
├── payment-service/                 ← port 8089 (UC07)
├── prescription-service/            ← port 8090 (UC12)
├── notification-service/            ← port 8091 (UC13)
│   └── service/NotificationSenderService.java  ← NSF-09 retry
│
├── report-service/                  ← port 8092 (UC09)
│
├── postman/PCMS.postman_collection.json
│
└── scripts/
    ├── build-all.sh / build-all.bat     ← Build tất cả JARs
    ├── run-local.sh / run-local.bat     ← Start tất cả services
    ├── stop-all.sh / stop-all.bat       ← Stop tất cả services
    ├── setup-mysql.sh / setup-mysql.bat ← Setup MySQL (1 lần)
    └── setup-mysql.sql
```

### Bảng ánh xạ UC ↔ Service

| UC | Use Case | Primary Service | Feign Clients Used |
|:---:|---|---|---|
| UC01 | Login | **user-service** | - |
| UC02 | Manage Users | **user-service** | - |
| UC03 | Manage Branches | **branch-service** | - |
| UC04 | Manage Medicines | **catalog-service** | - |
| UC05 | Manage Inventory | **inventory-service** | notification (BR02) |
| UC06 | Manage Orders | **order-service** | catalog, inventory, customer, notification |
| UC07 | Process Payment | **payment-service** | order (mark paid), customer (points) |
| UC08 | Customer & Loyalty | **customer-service** | - |
| UC09 | View Reports | **report-service** | order, inventory |
| UC10 | Search Medicines | **catalog-service** | - |
| UC11 | Manage Suppliers | **supplier-service** | - |
| UC12 | Issue Prescription | **prescription-service** | customer (validate) |
| UC13 | Notifications | **notification-service** | email/sms providers |

---

## 🐛 Các lỗi thường gặp & cách fix

### ❌ Lỗi 1: "Failed to configure a DataSource"

**Nguyên nhân:** MySQL chưa chạy, hoặc user `pcms_user` chưa được tạo.

**Fix:**
```bash
# 1. Kiểm tra MySQL đang chạy
netstat -an | grep ":3306"
# Phải thấy: 0.0.0.0:3306 LISTENING

# 2. Chạy setup script
./scripts/setup-mysql.sh

# 3. Verify user tồn tại
mysql -u pcms_user -ppcms_pass -e "SHOW DATABASES LIKE 'pcms_%';"
```

### ❌ Lỗi 2: "Build failure - Unable to rename JAR"

**Nguyên nhân:** JAR file đang bị lock bởi process Java đang chạy.

**Fix:**
```bash
# Windows
taskkill //F //IM java.exe

# macOS/Linux
pkill -9 java

# Sau đó build lại
mvn clean package -DskipTests
```

### ❌ Lỗi 3: "Connection refused localhost:8761"

**Nguyên nhân:** Discovery server chưa start hoặc Eureka chưa ready.

**Fix:**
- Đợi 15-30 giây sau khi start discovery-server
- Kiểm tra log: `Started DiscoveryServerApplication`
- Verify: `curl http://localhost:8761/actuator/health`

### ❌ Lỗi 4: "Port already in use"

**Fix:**
```bash
# Tìm process chiếm port (vd: 8080)
# Windows
netstat -ano | findstr :8080
taskkill //F //PID <pid>

# macOS/Linux
lsof -i :8080
kill -9 <pid>
```

### ❌ Lỗi 5: "Spring Cloud Gateway 404"

**Nguyên nhân:** Routes config sai format cho Spring Cloud Gateway 5.0.x webmvc.

**Fix:** Đảm bảo routes config dùng đúng key:
```yaml
spring:
  cloud:
    gateway:
      server:
        webmvc:
          routes:    # ← Phải là 'server.webmvc.routes', không phải 'routes'
            - id: branch-service
              ...
```

### ❌ Lỗi 6: "XML parse error - entity reference names"

**Nguyên nhân:** Ký tự `&` trong XML description.

**Fix:** Thay `&` bằng `and` trong các file pom.xml (đã fix sẵn trong project này).

### ❌ Lỗi 7: "Cannot find symbol List"

**Fix:** Thêm import:
```java
import java.util.List;
```

### ❌ Lỗi 8: "Spring Cloud starter not found"

**Fix:** Đảm bảo parent POM có:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2025.1.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### ❌ Lỗi 9: "Mail authentication failed (Notification service)"

**Fix:** Đã xử lý trong project - notification-service không cấu hình SMTP mặc định. Để enable email, set env vars:
```bash
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

### ❌ Lỗi 10: "Foreign key constraint fails" (khi xóa record)

**Fix:** Project dùng **soft-delete** (status=INACTIVE), KHÔNG dùng DELETE. Nếu cần force delete, phải xóa dependent records trước.

---

## 📊 Các service quan trọng

| Service | Port | Chức năng chính | Database |
|---|---:|---|---|
| **api-gateway** | 8080 | Entry point, routing, load balancing | - |
| **config-server** | 8888 | Centralized config (native profile) | - |
| **discovery-server** | 8761 | Eureka service discovery | - |
| **user-service** | 8081 | Auth (JWT) + User CRUD | pcms_user |
| **branch-service** | 8082 | Quản lý chi nhánh | pcms_branch |
| **catalog-service** | 8083 | Medicine CRUD + Search (UC10) | pcms_catalog |
| **category-service** | 8084 | Medicine categories | pcms_category |
| **supplier-service** | 8085 | Supplier CRUD | pcms_supplier |
| **inventory-service** | 8086 | Stock + FIFO + Expiry check | pcms_inventory |
| **customer-service** | 8087 | Customer + Loyalty points | pcms_customer |
| **order-service** | 8088 | Order + Auto-cancel + BR04 discount | pcms_order |
| **payment-service** | 8089 | Payment + Invoice + BR07 points | pcms_payment |
| **prescription-service** | 8090 | Prescription với digital signature | pcms_prescription |
| **notification-service** | 8091 | In-app/Email/SMS + Retry | pcms_notification |
| **report-service** | 8092 | Revenue/Inventory/Staff reports | pcms_report |

### Business Rules đã implement

| Rule | Description | Implementing Service | Status |
|:---:|---|---|:---:|
| **BR01** | Auto-cancel unpaid orders (>24h) | order-service (NSF-01 scheduler) | ✅ |
| **BR02** | Alert low stock | inventory-service + notification-service | ✅ |
| **BR03** | Batch expiry check (30 days) | inventory-service (NSF-03 scheduler) | ✅ |
| **BR04** | 5% discount for qty≥10 | order-service | ✅ |
| **BR05** | Lock account after 5 failed logins | user-service | ✅ |
| **BR06** | Restore stock on cancel | order-service + inventory-service | ✅ |
| **BR07** | Loyalty points (1pt/1000 VND) | payment-service + customer-service | ✅ |

### Non-Screen Functions (NSF) Status

| ID | Trigger | Service | Status |
|:---:|---|---|:---:|
| NSF-01 | Cron 15min | order-service | ✅ |
| NSF-02 | Inventory commit | inventory-service | ✅ |
| NSF-03 | Cron daily 00:00 | inventory-service | ✅ |
| NSF-04 | Order paid | payment-service → customer-service | ✅ |
| NSF-05 | Stock FIFO | inventory-service | ✅ |
| NSF-06 | Cron daily 01:00 | report-service | ✅ (scaffold) |
| NSF-07 | Logout | user-service | ⏳ (TODO) |
| NSF-09 | Channel failure | notification-service | ✅ (retry 3x) |
| NSF-10 | Cron 5min | user-service | ✅ |
| NSF-11 | Payment success | payment-service | ⏳ (TODO) |
| NSF-12 | Order created | order-service | ✅ |

---

## 🎓 Bước tiếp theo

Dự án hiện tại đã có **full skeleton với 15 microservices hoạt động được**. Những phần cần bổ sung:

- [ ] **Seed data tự động** - admin user + sample medicines khi start
- [ ] **JWT security filter** - bảo vệ API endpoints (hiện tại public)
- [ ] **Refresh token rotation** - UC01 AT
- [ ] **Payment gateway thật** - VNPay/MoMo webhook
- [ ] **File upload** - Medicine image (FR4.5) - MinIO/S3
- [ ] **Export Excel/PDF** - Apache POI + iText (FR9.3, FR9.4)
- [ ] **Distributed tracing** - Zipkin/Micrometer
- [ ] **Unit + Integration tests** - Testcontainers
- [ ] **Frontend SPA** - React/Vue cho SCR-* screens
- [ ] **Kubernetes manifests** - Helm charts
- [ ] **API documentation** - SpringDoc OpenAPI (Swagger UI)
- [ ] **Rate limiting** - Gateway filter (FR §4.5)
- [ ] **Audit log** - Aspect-based logging (FR §4.5)
- [ ] **i18n** - vi-VN + en-US bundles (CR-01)

---

## 📚 Tham khảo

- **SRS Document:** `../SRS_PhamacyChainManagementSystem_v1.0.0.md`
- **Tutorial gốc (E-Commerce example):** `../step-by-step-tutorial.md`
- **Spring Boot 4.0 Docs:** https://docs.spring.io/spring-boot/docs/4.0.7/
- **Spring Cloud 2025.1:** https://spring.io/projects/spring-cloud
- **Resilience4J:** https://resilience4j.readme.io/
- **MySQL 8.0:** https://dev.mysql.com/doc/refman/8.0/en/

---

> **Nếu gặp lỗi ở bất kỳ bước nào, paste error message lên đây để tôi debug giúp nhé!** 🚀
