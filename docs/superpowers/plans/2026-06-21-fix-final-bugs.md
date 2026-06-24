# Final Bug Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the last 4 real bugs found in re-test (Bug C: Cart 409, Bug D: payment-service 500, Branch POST status, Branch PUT partial update) and achieve 100% pass rate on all fixed endpoints.

**Architecture:** Code fixes for entity annotations and DTOs, MySQL config increase for connection limit, simple ResponseEntity status fix for POST.

**Tech Stack:** Java 21, Spring Boot 4.0.7, Spring Data JPA, Hibernate, MySQL 9.5

---

## Background: Remaining 4 Real Bugs

After all previous fixes, re-test revealed 4 remaining real bugs:

| Bug | Endpoint(s) | Service | Root Cause |
|-----|------------|---------|------------|
| C | `DELETE /api/v1/cart` | customer-portal | Cart entity missing `@CreatedDate`/`@LastModifiedDate` (same issue as CartItem/Video) |
| D | payment-service 6 endpoints | payment-service | MySQL `max_connections=151` exhausted by 18 services × 10 Hikari connections |
| Spec-1 | `POST /api/v1/branches` | branch-service | Returns 200 instead of 201 Created |
| Spec-2 | `PUT /api/v1/branches/{id}` | branch-service | UpdateBranchRequest has all fields @NotBlank, doesn't support partial updates |

---

## File Structure

### Files to Modify
- `customer-portal-service/src/main/java/com/ppcms/customerportal/entity/Cart.java` - Add `@CreatedDate`/`@LastModifiedDate` annotations + imports
- `branch-service/src/main/java/com/pcms/branchservice/controller/BranchController.java` - Change POST to return 201 Created
- `branch-service/src/main/java/com/pcms/branchservice/dto/request/UpdateBranchRequest.java` - Make fields nullable for partial updates
- `C:/ProgramData/MySQL/MySQL Server 9.5/my.ini` - Increase `max_connections` from 151 to 500

### Files to Create
- None

### No Business Logic Changes
- Only annotation additions, DTO validation relaxation, and config tweaks
- 1-line changes to BranchController

---

## Task 1: Fix Cart entity audit annotations (Bug C)

**Files:**
- Modify: `customer-portal-service/src/main/java/com/pcms/customerportal/entity/Cart.java:1-10,70-74`

- [ ] **Step 1: Read current imports**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && head -10 customer-portal-service/src/main/java/com/pcms/customerportal/entity/Cart.java`
Expected: Shows package and imports

- [ ] **Step 2: Add new imports**

In `customer-portal-service/src/main/java/com/pcms/customerportal/entity/Cart.java`, change the existing import block. Replace:
```java
import com.pcms.customerportal.enums.CartStatus;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
```

With:
```java
import com.pcms.customerportal.enums.CartStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
```

- [ ] **Step 3: Add `@CreatedDate` to createdAt field**

In the same file, locate the `createdAt` field declaration. Replace:
```java
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;
```

With:
```java
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;
```

- [ ] **Step 4: Verify changes**

Run: `grep -E "CreatedDate|LastModifiedDate" customer-portal-service/src/main/java/com/pcms/customerportal/entity/Cart.java`
Expected: Shows two lines with `@CreatedDate` and `@LastModifiedDate`

- [ ] **Step 5: Rebuild customer-portal-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests -pl customer-portal-service 2>&1 | tail -3`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git add customer-portal-service/src/main/java/com/pcms/customerportal/entity/Cart.java
git commit -m "fix(cart): add @CreatedDate and @LastModifiedDate to Cart entity

Cart.clearCart() creates a new empty cart row. Without @CreatedDate
annotation, JPA doesn't auto-populate the created_at column, causing
MySQL to reject with 'Column created_at cannot be null' (409 error).

This is the same pattern fix applied to CartItem and Video."
```

---

## Task 2: Fix branch POST to return 201 Created

**Files:**
- Modify: `branch-service/src/main/java/com/pcms/branchservice/controller/BranchController.java:60-63`

- [ ] **Step 1: Read the current create method**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && sed -n '60,63p' branch-service/src/main/java/com/pcms/branchservice/controller/BranchController.java`
Expected: Shows the create method

- [ ] **Step 2: Modify the create method**

In `branch-service/src/main/java/com/pcms/branchservice/controller/BranchController.java`, change the `@PostMapping` method. Replace:
```java
    @PostMapping
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.ok(branchService.create(request));
    }
```

With:
```java
    @PostMapping
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }
```

- [ ] **Step 3: Verify change**

Run: `grep -A2 "PostMapping" branch-service/src/main/java/com/pcms/branchservice/controller/BranchController.java | head -5`
Expected: Shows `status(HttpStatus.CREATED).body`

- [ ] **Step 4: Rebuild branch-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests -pl branch-service 2>&1 | tail -3`
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git add branch-service/src/main/java/com/pcms/branchservice/controller/BranchController.java
git commit -m "fix(branch): POST /branches returns 201 Created instead of 200

RESTful convention: POST endpoints that create resources should return
201 Created with the new resource body. Currently returns 200 OK."
```

---

## Task 3: Fix UpdateBranchRequest to support partial updates

**Files:**
- Modify: `branch-service/src/main/java/com/pcms/branchservice/dto/request/UpdateBranchRequest.java`

- [ ] **Step 1: Read current DTO**

Run: `cat branch-service/src/main/java/com/pcms/branchservice/dto/request/UpdateBranchRequest.java`
Expected: Shows current content

- [ ] **Step 2: Replace DTO with partial-update version**

Write to `branch-service/src/main/java/com/pcms/branchservice/dto/request/UpdateBranchRequest.java`:

```java
package com.pcms.branchservice.dto.request;

import com.pcms.branchservice.enums.BranchStatus;
import jakarta.validation.constraints.Size;

/**
 * Partial update DTO for branches. All fields are nullable - only non-null
 * fields will be updated. This allows clients to PATCH a single field
 * (e.g. just the name) without sending the entire branch.
 */
public record UpdateBranchRequest(
        @Size(max = 100) String name,
        @Size(max = 255) String address,
        @Size(max = 20) String phone,
        BranchStatus status
) {
}
```

- [ ] **Step 3: Verify the DTO change**

Run: `cat branch-service/src/main/java/com/pcms/branchservice/dto/request/UpdateBranchRequest.java`
Expected: Shows new partial update DTO without @NotBlank/@NotNull

- [ ] **Step 4: Check if service uses these fields safely**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && grep -A10 "UpdateBranchRequest" branch-service/src/main/java/com/pcms/branchservice/service/impl/BranchServiceImpl.java | head -20`
Expected: Shows how the service handles null fields

- [ ] **Step 5: Update BranchServiceImpl to skip null fields**

If the service implementation currently overwrites null fields with null, we need to add null checks. In `branch-service/src/main/java/com/pcms/branchservice/service/impl/BranchServiceImpl.java`, find the update method (around `update(...)` taking `UpdateBranchRequest`). 

For each field in UpdateBranchRequest, wrap in null check. Example transformation:
```java
// Before (overwrites with null):
branch.setName(request.name());
branch.setAddress(request.address());
// ... etc

// After (only updates if not null):
if (request.name() != null) branch.setName(request.name());
if (request.address() != null) branch.setAddress(request.address());
if (request.phone() != null) branch.setPhone(request.phone());
if (request.status() != null) branch.setStatus(request.status());
```

If the service already has these null checks, skip this step.

- [ ] **Step 6: Rebuild branch-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests -pl branch-service 2>&1 | tail -3`
Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git add branch-service/src/main/java/com/pcms/branchservice/dto/request/UpdateBranchRequest.java
git add branch-service/src/main/java/com/pcms/branchservice/service/impl/BranchServiceImpl.java
git commit -m "fix(branch): allow partial updates via nullable UpdateBranchRequest

Previously UpdateBranchRequest had @NotBlank/@NotNull on all fields,
requiring clients to send the entire branch object for any update.
Now all fields are optional - only non-null fields are applied.
This enables PATCH-like semantics on PUT /branches/{id}."
```

---

## Task 4: Increase MySQL max_connections (Bug D)

**Files:**
- Modify: `C:/ProgramData/MySQL/MySQL Server 9.5/my.ini`

- [ ] **Step 1: Read current MySQL config**

Run: `grep -i "max_connections" "C:/ProgramData/MySQL/MySQL Server 9.5/my.ini"`
Expected: Shows `max_connections=151`

- [ ] **Step 2: Backup original config**

Run: `cp "C:/ProgramData/MySQL/MySQL Server 9.5/my.ini" "C:/ProgramData/MySQL/MySQL Server 9.5/my.ini.bak"`
Expected: Backup created

- [ ] **Step 3: Update max_connections in my.ini**

Use a sed command or manual edit. The line `max_connections=151` should become `max_connections=500`:

```bash
sed -i 's/^max_connections=151$/max_connections=500/' "C:/ProgramData/MySQL/MySQL Server 9.5/my.ini"
```

- [ ] **Step 4: Verify change**

Run: `grep -i "max_connections" "C:/ProgramData/MySQL/MySQL Server 9.5/my.ini"`
Expected: Shows `max_connections=500`

- [ ] **Step 5: Restart MySQL service**

Run:
```bash
cmd //c "net stop MySQL80"
sleep 3
cmd //c "net start MySQL80"
```
Note: Service name may be different. Check with `netstat -ano | grep ":3306"` first to see if MySQL is running.

If `net stop MySQL80` fails, the MySQL service might have a different name. Find it:
```bash
cmd //c "sc query | findstr /I MySQL"
```

- [ ] **Step 6: Verify MySQL restarted with new max_connections**

Wait 10 seconds, then:
```bash
sleep 10
mysql -u pcms_user -p"pcms_pass" -e "SHOW VARIABLES LIKE 'max_connections';"
```
Expected: `max_connections` = 500

- [ ] **Step 7: Commit config change**

If the my.ini change is tracked in version control:
```bash
cd "C:/ProgramData/MySQL/MySQL Server 9.5"
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add my.ini 2>&1 | head -2
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" commit -m "chore(mysql): increase max_connections from 151 to 500

18 PCMS services × Hikari default pool size 10 = 180 connections
required, exceeding MySQL's default max_connections=151. This caused
payment-service and other downstream services to get 500 errors when
the connection pool was exhausted.

Increased to 500 to safely accommodate all services with room to spare."
```

If my.ini is not in any git repo (it usually isn't - it's a system file), skip this commit. The change is just a system-level config tweak.

---

## Task 5: Restart payment-service and customer-portal-service

**Files:** None (operations)

- [ ] **Step 1: Find the PIDs for payment-service and customer-portal-service**

Run:
```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
netstat -ano | grep ":8089" | awk '{print $5}'  # payment-service
netstat -ano | grep ":8093" | awk '{print $5}'  # customer-portal-service
```
Expected: Shows PID numbers

- [ ] **Step 2: Kill the old Java processes for these services**

Run:
```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
netstat -ano | grep ":8089" | awk '{print $5}' | xargs -I {} cmd //c "taskkill /F /PID {}" 2>&1 | head -1
netstat -ano | grep ":8093" | awk '{print $5}' | xargs -I {} cmd //c "taskkill /F /PID {}" 2>&1 | head -1
```
Expected: "SUCCESS" messages

- [ ] **Step 3: Wait for ports to be released**

Run: `sleep 8 && netstat -an | grep -E "8089|8093" | grep LISTENING | wc -l`
Expected: 0

- [ ] **Step 4: Start payment-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar > logs/payment-service.log 2>&1 &`
Expected: Process started

- [ ] **Step 5: Start customer-portal-service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar customer-portal-service/target/customer-portal-service-1.0.0-SNAPSHOT.jar > logs/customer-portal-service.log 2>&1 &`
Expected: Process started

- [ ] **Step 6: Wait for services to be ready**

Run: `sleep 40 && netstat -an | grep -E "8089|8093" | grep LISTENING`
Expected: Both ports listening

---

## Task 6: Verify all 4 fixes

**Files:** None (testing)

- [ ] **Step 1: Get JWT token**

Run:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@pcms.vn","password":"admin123"}' | grep -oP '"accessToken":"[^"]+' | cut -d'"' -f4)
echo "Token: ${TOKEN:0:30}..."
```

- [ ] **Step 2: Verify Bug C fix - DELETE /cart**

Run:
```bash
# First add an item
curl -s -X POST http://localhost:8080/api/v1/cart/items -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"medicineId":"00000000-0000-0000-0000-000000000000","quantity":1}' -o /dev/null
sleep 2
# Then clear cart
echo "DELETE /cart: $(curl -s -o /dev/null -w '%{http_code}' -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/cart)"
```
Expected: `204` (NOT 409)

- [ ] **Step 3: Verify Bug D fix - payment-service**

Run:
```bash
echo "GET /payments: $(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/payments)"
echo "GET /payments/{id}: $(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/payments/00000000-0000-0000-0000-000000000000)"
```
Expected: `200` and `404` (NOT 500)

- [ ] **Step 4: Verify Spec-1 fix - POST /branches returns 201**

Run:
```bash
UNIQUE_CODE="BR-RT-$(date +%s)"
echo "POST /branches: $(curl -s -o /dev/null -w '%{http_code}' -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d "{\"code\":\"$UNIQUE_CODE\",\"name\":\"Test Branch\",\"address\":\"Test Address\",\"phone\":\"0901234567\"}" http://localhost:8080/api/v1/branches)"
```
Expected: `201` (NOT 200)

- [ ] **Step 5: Verify Spec-2 fix - PUT /branches partial update**

Run:
```bash
BRANCH_ID="00000000-0000-0000-0000-000000000000"
echo "PUT /branches/{id} (partial name only): $(curl -s -o /dev/null -w '%{http_code}' -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"name":"Updated Name Only"}' http://localhost:8080/api/v1/branches/$BRANCH_ID)"
```
Expected: `200` or `404` (NOT 400)

- [ ] **Step 6: Confirm all 4 fixes work**

If all 5 verifications pass expected codes, all 4 bugs are fixed.

---

## Task 7: Run final regression check

**Files:** None (testing)

- [ ] **Step 1: Re-run targeted test groups**

Re-run the 7 parallel subagents from the previous test run to verify no regressions and confirm 100% pass rate on the previously-failed endpoints.

- [ ] **Step 2: Verify pass rate improvement**

Expected final pass rate: 95-100% on all 103 targeted tests (was 86.4% before this fix round)

- [ ] **Step 3: Final commit and summary**

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git add test-results/
git commit -m "test: final regression check - all 4 remaining bugs fixed - 100% pass rate"
```

---

## Summary of Changes

| # | File | Change | Impact |
|---|------|--------|--------|
| 1 | `customer-portal/.../entity/Cart.java` | +2 annotations, +2 imports | Fix Bug C - DELETE /cart 409 |
| 2 | `branch-service/.../BranchController.java` | POST returns 201 | Spec-1 - POST /branches |
| 3 | `branch-service/.../UpdateBranchRequest.java` | Make fields nullable | Spec-2 - PUT /branches partial |
| 4 | `my.ini` | max_connections 151→500 | Fix Bug D - payment 500 |
| 5 | Operations only | Restart services | Reload code + DB config |

## Expected Outcome

After all 7 tasks completed:
- **Bug C fixed**: `DELETE /api/v1/cart` returns 204 (not 409)
- **Bug D fixed**: payment-service endpoints return 200/404/400 (not 500)
- **Spec-1 fixed**: `POST /api/v1/branches` returns 201 (not 200)
- **Spec-2 fixed**: `PUT /api/v1/branches/{id}` accepts partial body
- **Total**: 0 real bugs remaining
- **Test pass rate**: 95-100% (was 86.4%)