# Code Context

## Files Retrieved

1. `docker-compose.yml` (full) - 16 services defined (1 mysql + 15 microservices)
2. `pom.xml` (lines 1-60) - parent POM, 16 modules, Spring Boot 4.0.7 + Java 21 + Spring Cloud 2025.1.2
3. `docs/PLAN_API_COMPLETION.md` (lines 1-80) - 10-sprint roadmap (Sprint 1-10) for B2B+B2C API completion
4. `docs/API_LIST.md` (lines 1-60) - 146 endpoints across 12 services
5. `docs/API_VS_DOCS_COMPARISON.md` (lines 1-50) - 76% B2B coverage, 6% B2C coverage; 15 APIs truly missing
6. `docs/uat/00-MASTER-PLAN.md` (lines 1-79) - UAT master plan
7. `README.md` (lines 1-118) - project overview, build/install instructions
8. `CODE_RULES.md` (lines 1-60) - 16-module code rules XML
9. `.gitignore` - ignores target/, .idea/, etc.

## Recon Snapshot (2026-06-19)

### 1. Build status: ⚠️ UNKNOWN (Maven không có sẵn)

- `mvn: command not found` trên PATH; **không có mvnw wrapper** trong repo
- Java 21 (Temurin `21.0.7+8-LTS-245`) ✅ có sẵn
- Tuy nhiên: **tất cả 16 module đều đã có `target/classes/`** ⇒ code đã được build trước đó (nhiều khả năng bằng IntelliJ IDEA vì có `.idea/` dir)
- Build script: `scripts/build-all.sh` / `scripts/build-all.bat` (cần Maven)
- ➡️ Trước khi code: cài Maven 3.9.16+ (theo README) HOẶC build qua IntelliJ IDEA Ultimate

### 2. Git status

- **Branch:** `main` (3 commits ahead of `origin/main`, **chưa push**)
- **Last 5 commits:**

  ```
  63b9397 merge: integrate khanh backend scope into main
  4c8c8b2 feat: update source code for branch khanh
  81653f8 feat: complete Khanh backend scope
  add0d13 Update : document
  524eb4d feat: implement 15+ critical blockers (auth, security, webhook, outbox)
  ```

- **Untracked:** `SDD_PhamacyChainManagementSystem_v1.0.0.md`, `SRS_PhamacyChainManagementSystem_v1.0.0.md`, `docs/` (whole dir)
- **No merge conflicts**: không có `.orig`/`.rej`; grep `<<<<<<<` trong `.java` → 0 hit
- **Working tree sạch cho tracked files** (chỉ có untracked + unpushed)

### 3. 12 business services — ✅ CONFIRMED

`user`, `branch`, `catalog`, `category`, `supplier`, `inventory`, `customer`, `order`, `payment`, `prescription`, `notification`, `report`
- 3 infrastructure (`config-server`, `discovery-server`, `api-gateway`) + 1 shared lib (`pcms-common`) = **16 modules** trong pom.xml

### 4. Code metrics

- **Java files: 323** (`find . -name "*.java" -not -path "*/target/*"`)
- **Controllers: 23** (rải đều 12 services, từ 1–4 mỗi service)
- 16 `application.yml` (mỗi service 1 cái), 0 `.properties`
- 15 `Dockerfile` (mỗi service + common, không có ở `pcms-common`)
- 6 `.sql` data files (5 service-level + `scripts/init-databases.sql` + `scripts/setup-mysql.sql`)

### 5. Postman artifacts

- **1 file:** `postman/PCMS.postman_collection.json` (209 dòng)

### 6. docker-compose services

**16 services** (matches 12 business + 3 infra + 1 mysql). Header: "15 microservices + MySQL 8.0". Port mapping 8080 (gateway) → 8081–8092 (business).

### 7. UAT scripts

**Chỉ 1 file:** `docs/uat/00-MASTER-PLAN.md` (master plan tham chiảo tới 10 file con `01-AUTH-USER.md`, `02-BRANCH.md`, `03-CATALOG.md`, `04-INVENTORY.md`, `05-ORDER.md`, `06-PAYMENT.md`, `07-CUSTOMER.md`, `08-REPORT.md`, `09-PRESCRIPTION.md`, `10-NOTIFICATION.md`, `11-E2E-FLOWS.md` — **chưa tồn tại**).

## Plan Scope (`docs/PLAN_API_COMPLETION.md`)

- **Sprint 1-3** (~3 tuần): Hoàn thiện **15 B2B API còn thiếu** (theo `API_VS_DOCS_COMPARISON.md` §7: `/auth/me`, `/payments/{id}/invoice`+`/print`, `/reports/export/{excel,pdf}`, `/auth/password`, `/auth/verify-email`, `/auth/resend-verification`, `/customers/code/{code}`, `/search/medicines/{id}`, `DELETE /notifications/{id}`, `DELETE /prescriptions/{id}`, `/orders/{id}/recompute`, `PUT /users/{id}/branch`, `DELETE /reports/schedules/{id}`)
- **Sprint 4-10** (~10-14 tuần): 6 service mới cho B2C: `customer-portal-service`, `ai-engine-service` (Python/FastAPI), `pharmacist-workbench-service`, `mobile-bff`, `health-tools-service`, `ecom-ops-service`

## Architecture

- **Pattern:** Microservices, Spring Cloud (Gateway + Eureka + Config Server), database-per-service
- **Stack:** Spring Boot 4.0.7, Java 21, Spring Cloud 2025.1.2 (Oakwood), MySQL 8.0, Resilience4j 2.4.0, JJWT 0.12.6, springdoc 2.6.0, Caffeine 3.1.8
- **Shared concerns:** `pcms-common` chứa `BaseEntity`, `ErrorResponse` (RFC 7807), `EventPublisher` (Outbox), idempotency helper
- **Auth:** JWT HS256 (đang permitAll trong dev profile — UAT note)
- **API style:** thin controller → service → repository, Outbox pattern cho events, `Idempotency-Key` header cho mutating endpoints (CR-05)

## Start Here

1. **`docs/PLAN_API_COMPLETION.md`** — đọc §3-§7 để nắm 10 sprint + convention
2. **`docs/API_VS_DOCS_COMPARISON.md`** §3 — bảng 25 API thiếu (15 cần code thật + 10 alias/path khác)
3. **`docs/API_LIST.md`** — danh sách 146 endpoint hiện hữu (chống trùng lặp)
4. **`docs/uat/00-MASTER-PLAN.md`** — nắm test plan (sẽ reference cho verification)

## Issues cần fix TRƯỚC khi bắt đầu code

1. ⚠️ **Maven chưa cài** trên máy này → cài Maven 3.9.16+ theo README §3.2 (hoặc dùng IntelliJ) trước khi `mvn clean compile`
2. ⚠️ **3 commits chưa push** lên `origin/main` + 3 untracked dirs (`docs/`, 2 file `.md`) → recommend `git add -A && git commit` hoặc stash trước khi bắt đầu sprint, tránh mất work hoặc conflict
3. ⚠️ **UAT scripts per-UC chưa có** (chỉ có master plan) → nếu plan cần test thì phải tạo 10 file con trong `docs/uat/`
4. ℹ️ **B2C (Sprint 4-10) là greenfield** — cần 6 service mới + dependency mới (Python/FastAPI, pgvector, React Native BFF…) → tốn effort lớn
5. ℹ️ `docs/api-list.json` + `docs/api-comparison.json` có sẵn — dùng để tooling check API parity tự động

## Supervisor coordination

Không cần liên hệ supervisor — đây là recon task thuần túy, không có blocker.
