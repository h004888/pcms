# Plan: Comprehensive API Testing cho toàn bộ PCMS services

> **Plan mode:** Không implement code trong turn này. Deliverable là plan markdown để sau đó execute.

**Goal:** Kiểm thử TẤT CẢ các REST API của 18 services B2B/B2C + 1 AI service trong PCMS, đảm bảo phủ: happy path, validation, auth, pagination, error cases. Output: báo cáo pass/fail với evidence cho mỗi endpoint.

**Architecture:** Kế thừa hạ tầng `scripts/uat/lib.sh` (HTTP helpers, assertion, JSON parsing) và 9 test scripts có sẵn (`test-00..test-09`). Mở rộng thành bộ test hoàn chỉnh theo 2 trục:
- **Trục 1 — Functional:** Một test script per service, gom nhóm test case theo resource + method (CRUD + validation + negative). Mục tiêu: mỗi endpoint có ≥1 happy-path test, các endpoint mutation có thêm 1 negative test.
- **Trục 2 — Non-functional:** Tích hợp vào master runner (`scripts/run-uat-full.sh`) chạy tuần tự, output JSONL + summary, exit code 0/1 cho CI.

**Tech Stack:** Bash 4.x (MSYS / git-bash), curl, jq (đã có sẵn trong hệ thống), MySQL CLI cho data setup.

---

## Bối cảnh (đã verify)

### Codebase
- **18 Java services + 1 Python AI service** (xem `docs/uat/00-MASTER-PLAN.md`)
- **262 endpoints** theo Master Plan §2 (có thể đã tăng sau Sprint 11 — saga thêm +13 endpoint mới)
- Port gateway: **8080** (api-gateway), backend ports **8081-8098** + AI **8095**
- JWT secret shared trong `app.jwt.secret` (HS256, HS256-key đã verify hoạt động)
- Auth flow: `POST /api/v1/auth/login` → `Bearer <accessToken>` cho mọi request authenticated

### Hạ tầng test hiện có (reusable)
- `scripts/uat/lib.sh` — HTTP helpers, assertion (`assert_status`, `capture`), JSON parsing (`json_get`)
- `scripts/uat/test-00-auth-setup.sh` đến `test-09-payment.sh` — pattern đã chuẩn hóa
- `docs/uat/01-AUTH-USER.md` đến `14-PHARMACIST-WORKBENCH.md` — test plan chi tiết theo service (manual checklist đã có sẵn)
- `postman/PCMS.postman_collection.json` — tham khảo request shape

### Data setup hiện có
- `scripts/init-databases.sql` — schema cho 17 DB
- `scripts/seed-admin-user.sql` — admin@pcms.vn / admin123 (đã verify hoạt động ở session trước)
- Mỗi service chạy `ddl-auto: create` → DB tự tạo + seed qua `data.sql` (nếu có)

### Scripts hỗ trợ có sẵn
- `scripts/run-local.sh` — start all 15 services (background)
- `scripts/stop-all.sh` — stop all
- `scripts/uat-test.sh` — runner đơn giản (chỉ test TP-02 đến TP-09)

---

## Scope quyết định

### IN SCOPE
1. **Tất cả 18 Java services** qua API Gateway (port 8080), path `/api/v1/**`
2. **AI engine service** (port 8095, Python FastAPI) — direct calls, không qua gateway
3. **Mỗi endpoint:** 1 happy-path + 1 negative (validation/auth/not-found) test case
4. **Auth:** verify token hợp lệ + token thiếu → 401 + token sai role → 403 (nếu có)
5. **Pagination** (nếu endpoint list có `page`/`size`)
6. **Idempotency** cho mutation endpoints tạo resource (chạy 2 lần → kiểm tra 2xx hoặc duplicate handling)
7. **End-to-end smoke flow:** order creation → payment → saga (regression cho Sprint 11)

### OUT OF SCOPE (đề xuất)
1. Performance / load testing (JMeter/k6) — sẽ là plan riêng
2. Security testing (SQL injection, XSS) — penetration test plan riêng
3. Frontend integration test
4. Mobile BFF / E2E browser test
5. Multi-tenant isolation (chưa có requirement)
6. Contract testing (Pact) — đề xuất phase sau

### Test Runner Strategy
- **Sequential** (không parallel) để tránh race condition khi test cùng 1 resource
- Mỗi test case ~5-15 giây (timeout 30s)
- Ước lượng: ~262-300 endpoint × ~10s/test × ~2 case/endpoint = ~90-100 phút cho full run
- Cho phép chạy subset qua flag: `./run-uat-full.sh [auth|branch|catalog|...|all]`

---

## File Structure

### Mới tạo
- `scripts/uat/test-11-prescription.sh` — Prescription service (chưa có file tương ứng)
- `scripts/uat/test-12-notification.sh` — Notification service
- `scripts/uat/test-13-report.sh` — Report service
- `scripts/uat/test-14-pharmacist-workbench.sh` — Pharmacist workbench (consultations, follow-ups, VIP, rx-ai)
- `scripts/uat/test-15-customer-portal.sh` — Customer portal (cart, address, family, favorite, installment, voucher, wallet, shop, store, vaccine)
- `scripts/uat/test-16-health-tools.sh` — Health tools (quiz, articles)
- `scripts/uat/test-17-ecom-ops.sh` — Ecom ops (flash sales)
- `scripts/uat/test-18-mobile-bff.sh` — Mobile BFF (mobile + reminders)
- `scripts/uat/test-19-ai-engine.sh` — AI engine (forecast, summary, chatbot — Python direct)
- `scripts/uat/test-20-saga-flow.sh` — Saga end-to-end smoke (regression Sprint 11)

### Mở rộng / Sửa
- `scripts/uat/test-08-order.sh` — Thêm Saga admin endpoints (`/admin/saga/*`)
- `scripts/uat/test-09-payment.sh` — Verify outbox flow sau Sprint 11
- `scripts/run-uat-full.sh` — NEW: master runner, chạy tuần tự tất cả test-NN, output JSONL + summary
- `scripts/uat/lib.sh` — Thêm `assert_json_path`, `assert_array_min_length`, helper `post_form` (multipart cho image upload)

### Docs
- `docs/uat/15-CUSTOMER-PORTAL.md` — Test plan customer-portal (chưa có)
- `docs/uat/16-HEALTH-TOOLS.md`
- `docs/uat/17-ECOM-OPS.md`
- `docs/uat/18-MOBILE-BFF.md`
- `docs/uat/19-AI-ENGINE.md`
- `docs/uat/20-SAGA-FLOW.md` — Regression test cho Sprint 11
- `docs/uat/API_TESTING_RUNBOOK.md` — Hướng dẫn chạy, debug, đọc output

---

## Task 1: Khảo sát endpoint list đầy đủ (PREREQUISITE)

**Files:** None (read-only)

**Step 1:** Đọc `docs/API_LIST.md` (146 B2B APIs) + `docs/uat/00-MASTER-PLAN.md` §2 (262 endpoints) + scan tất cả `*Controller.java` để confirm endpoint hiện tại.

Run:
```bash
grep -rE "@(Get|Post|Put|Patch|Delete|Request)Mapping" \
    --include="*Controller.java" /c/Users/ADMIN/Downloads/temp_v12/pcms \
    | grep -v "/target/" > /tmp/all-endpoints.txt
wc -l /tmp/all-endpoints.txt
```

Expected: Tổng ~316-376 mapping annotations trên ~50 controllers.

**Step 2:** Cross-check với `docs/API_LIST.md` để phát hiện endpoints mới thêm sau Sprint 11 (đặc biệt SagaAdminController, OutboxConsumerController mới trong inventory/notification).

Output: bảng `endpoint -> method -> service -> test case ID` trong `docs/uat/00-MASTER-PLAN.md` (cập nhật).

---

## Task 2: Mở rộng lib.sh với helpers còn thiếu

**Files:**
- Modify: `scripts/uat/lib.sh`

**Step 1:** Thêm helper `assert_json_path`

```bash
# Assert a JSON path exists and equals expected value
# Usage: assert_json_path "$.id" "expected-uuid" "$body" "test-name"
assert_json_path() {
    local path="$1"
    local expected="$2"
    local body="$3"
    local name="$4"
    local actual
    actual=$(json_get "$body" "$path")
    if [[ "$actual" == "$expected" ]]; then
        log_pass "$name [json $path=$actual]"
    else
        log_fail "$name [json $path expected=$expected, got=$actual]"
        log_info "  body: $(echo "$body" | head -c 200)"
    fi
}
```

**Step 2:** Thêm `assert_array_min_length`

```bash
# Assert response array has at least N elements
# Usage: assert_array_min_length 3 "$body" "list-medicines"
assert_array_min_length() {
    local min="$1"
    local body="$2"
    local name="$3"
    local len
    len=$(json_get "$body" "length")
    if [[ "$len" -ge "$min" ]]; then
        log_pass "$name [array length=$len >= $min]"
    else
        log_fail "$name [array length=$len < $min]"
    fi
}
```

**Step 3:** Thêm `post_form` cho multipart upload

```bash
# POST multipart file upload
# Usage: post_form <path> <file_field> <file_path> [extra headers...]
post_form() {
    local path="$1"
    local field="$2"
    local file="$3"
    shift 3
    curl -s -o /tmp/uat-body.txt -w "%{http_code}" --max-time "$TIMEOUT" \
        -X POST "${headers[@]}" \
        -F "${field}=@${file}" \
        "$GATEWAY$path"
    cat /tmp/uat-body.txt
}
```

**Step 4:** Verify bằng cách chạy test-02-branch.sh hiện tại — phải pass như trước.

---

## Task 3: Test scripts cho B2B services CHƯA có file (TP-11 đến TP-13)

**Pattern tham chiếu:** `scripts/uat/test-02-branch.sh` đã đọc ở trên.

### Task 3.1: test-11-prescription.sh

**Files:** Create `scripts/uat/test-11-prescription.sh`

Endpoints cần test (từ `docs/uat/10-PRESCRIPTION.md`):
- `GET /api/v1/prescriptions` — list
- `GET /api/v1/prescriptions/{id}` — get by id
- `POST /api/v1/prescriptions` — create
- `PUT /api/v1/prescriptions/{id}` — update
- `DELETE /api/v1/prescriptions/{id}` — soft delete
- `GET /api/v1/prescriptions/{id}/items` — list items
- `POST /api/v1/prescriptions/{id}/items` — add item
- `PUT /api/v1/prescriptions/{id}/status` — change status (PENDING → VERIFIED)
- `GET /api/v1/prescriptions/verify/{code}` — public verify by code

Test cases:
- TC-01..TC-08: Happy path CRUD + status transition
- TC-09: Missing required field → 400
- TC-10: Non-existent UUID → 404
- TC-11: Verify public endpoint không cần auth (theo route gateway `prescriptions/**` → prescription-service chứ không public — check security config)

### Task 3.2: test-12-notification.sh

**Files:** Create `scripts/uat/test-12-notification.sh`

Endpoints:
- 15 endpoints theo Master Plan §2.1 (UC13)
- NotificationController + NotificationTemplateController + OutboxConsumerController (Sprint 11 mới)

### Task 3.3: test-13-report.sh

**Files:** Create `scripts/uat/test-13-report.sh`

Endpoints (UC09):
- 11 endpoints theo Master Plan §2.1
- ReportController: sales/dashboard/inventory reports

---

## Task 4: Test scripts cho B2C services (TP-14 đến TP-18)

### Task 4.1: test-14-pharmacist-workbench.sh
**Files:** Create `scripts/uat/test-14-pharmacist-workbench.sh`

Endpoints:
- ConsultationController, RxAiController, FollowUpController, VipMarkController
- Customer360Controller

### Task 4.2: test-15-customer-portal.sh
**Files:** Create `scripts/uat/test-15-customer-portal.sh`

Endpoints (16 controllers — đã list ở trên):
- AddressController, CartController, FamilyController, FavoriteController, HealthContentController, InstallmentController, NotificationSettingsController, OrderTrackingController, PrescriptionHistoryController, ShopController, StoreController, VaccineController, VideoAdminController, VoucherController, WalletController

### Task 4.3: test-16-health-tools.sh
**Files:** Create `scripts/uat/test-16-health-tools.sh`

Endpoints:
- HealthQuizController

### Task 4.4: test-17-ecom-ops.sh
**Files:** Create `scripts/uat/test-17-ecom-ops.sh`

Endpoints:
- EcomFlashSaleController, FlashSaleController

### Task 4.5: test-18-mobile-bff.sh
**Files:** Create `scripts/uat/test-18-mobile-bff.sh`

Endpoints:
- MobileController, ReminderController

---

## Task 5: Test AI engine (Python FastAPI, port 8095 — direct, không qua gateway)

**Files:** Create `scripts/uat/test-19-ai-engine.sh`

Endpoints cần khảo sát (xem `ai-engine-service/app/api/v1/`):
- Forecast (time-series prediction)
- Summary (text summarization)
- Chatbot (conversational)
- Recommendation

Lưu ý: AI service KHÔNG đi qua gateway, dùng `http://localhost:8095/api/v1/ai/**`. Cần update `lib.sh` để hỗ trợ target URL khác gateway (đã có `AI_ENGINE` var nhưng `http()` helper chưa dùng).

**Step 1:** Update `lib.sh`:
```bash
# Modify http() to accept optional base URL
http() {
    local method="$1"
    local path="$2"
    local data="$3"
    shift 3
    local base="${HTTP_BASE:-$GATEWAY}"  # new
    local url="$base$path"
    ...
}
```

Cho phép: `HTTP_BASE=http://localhost:8095/api/v1/ai http POST /forecast ...`

---

## Task 6: Saga end-to-end smoke test (regression Sprint 11)

**Files:** Create `scripts/uat/test-20-saga-flow.sh`

Flow:
1. Login admin → get ACCESS_TOKEN
2. Tạo customer test
3. Tạo order PENDING_PAYMENT (medicine non-prescription, qty=1)
4. POST payment → verify 201
5. Đợi 35s cho PaymentOutboxPublisher → check `outbox_events.status = SENT`
6. Query `pcms_order.saga_instances` qua direct MySQL — verify có saga với `aggregate_id = orderId`
7. Query `pcms_order.saga_steps` — verify có STOCK_CONSUMED step
8. Verify order status = `PAID`
9. Cleanup: optional rollback DB

Helpers mới cần:
- `mysql_query()` — wrapper cho mysql CLI (đã có ở session trước)
- `wait_for_outbox_sent()` — poll DB mỗi 5s, timeout 60s
- `verify_saga_exists()` — query + assert

---

## Task 7: Master runner

**Files:** Create `scripts/run-uat-full.sh`

```bash
#!/usr/bin/env bash
# Run all UAT tests sequentially, capture JSONL output
set -uo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Source lib for global config
source scripts/uat/lib.sh

# Filter to run subset if provided
FILTER="${1:-all}"
RESULTS_DIR="${RESULTS_DIR:-./test-results}"
mkdir -p "$RESULTS_DIR"
RUN_ID="$(date +%Y%m%d-%H%M%S)"
RAW="$RESULTS_DIR/run-${RUN_ID}.jsonl"
SUMMARY="$RESULTS_DIR/summary-${RUN_ID}.txt"

declare -a TESTS=(
    "00-auth-setup"
    "02-branch"
    "03-catalog"
    "04-category"
    "05-supplier"
    "06-inventory"
    "07-customer"
    "08-order"
    "09-payment"
    "11-prescription"
    "12-notification"
    "13-report"
    "14-pharmacist-workbench"
    "15-customer-portal"
    "16-health-tools"
    "17-ecom-ops"
    "18-mobile-bff"
    "20-saga-flow"
    "19-ai-engine"
)

run_one() {
    local script="$1"
    local path="scripts/uat/test-${script}.sh"
    if [[ ! -f "$path" ]]; then
        log_warn "Skipping $script (not found)"
        return
    fi
    echo "=========================================="
    echo "  RUN: $script"
    echo "=========================================="
    bash "$path" 2>&1 | tee -a "$RAW"
    echo
}

# Filter
if [[ "$FILTER" != "all" ]]; then
    TESTS=("$FILTER")
fi

TOTAL_PASS=0
TOTAL_FAIL=0
for t in "${TESTS[@]}"; do
    run_one "$t"
done

# Summary
cat > "$SUMMARY" <<EOF
PCMS UAT Run ${RUN_ID}
Tests run: ${#TESTS[@]}
Pass: $TOTAL_PASS
Fail: $TOTAL_FAIL
EOF
cat "$SUMMARY"
exit $([[ $TOTAL_FAIL -eq 0 ]] && echo 0 || echo 1)
```

**Step verify:**
```bash
chmod +x scripts/run-uat-full.sh
bash scripts/run-uat-full.sh auth   # chỉ chạy TP-00
bash scripts/run-uat-full.sh branch
bash scripts/run-uat-full.sh all
```

Expected: Mỗi test script output `[PASS]/[FAIL]` markers. Master script tổng hợp summary.

---

## Task 8: Update docs/uat cho services mới

**Files:**
- Create `docs/uat/15-CUSTOMER-PORTAL.md`
- Create `docs/uat/16-HEALTH-TOOLS.md`
- Create `docs/uat/17-ECOM-OPS.md`
- Create `docs/uat/18-MOBILE-BFF.md`
- Create `docs/uat/19-AI-ENGINE.md`
- Create `docs/uat/20-SAGA-FLOW.md`
- Modify `docs/uat/00-MASTER-PLAN.md` (update endpoint count, add tests reference)
- Create `docs/uat/API_TESTING_RUNBOOK.md` (hướng dẫn chạy)

Mỗi file test plan: chỉ liệt kê endpoint + test case ID + expected result. Không cần code.

---

## Task 9: Verification & baseline run

**Step 1:** Pre-flight — đảm bảo tất cả services chạy
```bash
bash scripts/run-local.sh
sleep 60  # đợi Eureka + Gateway ready
curl -s http://localhost:8080/api/v1/branches | head -c 100  # smoke
```

**Step 2:** Run auth setup
```bash
bash scripts/run-uat-full.sh 00-auth-setup
```
Expected: ADMIN_TOKEN + CUSTOMER_TOKEN captured, 3 branches + 5 categories created.

**Step 3:** Run full suite
```bash
bash scripts/run-uat-full.sh all 2>&1 | tee /tmp/uat-run.log
```
Expected: ~250-300 test cases, pass rate >95% (một số negative test có thể fail do security config chưa đúng).

**Step 4:** Generate report
```bash
ls -la test-results/run-*.txt
cat test-results/summary-*.txt
```

---

## Task 10: Commit

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add \
    scripts/uat/ scripts/run-uat-full.sh \
    docs/uat/15-CUSTOMER-PORTAL.md docs/uat/16-HEALTH-TOOLS.md \
    docs/uat/17-ECOM-OPS.md docs/uat/18-MOBILE-BFF.md \
    docs/uat/19-AI-ENGINE.md docs/uat/20-SAGA-FLOW.md \
    docs/uat/API_TESTING_RUNBOOK.md docs/uat/00-MASTER-PLAN.md

git -c user.email="huybeoku@gmail.com" -c user.name="h004888" commit -m "test(uat): comprehensive API coverage for all 18 services + AI

- Extend scripts/uat/lib.sh with assert_json_path, assert_array_min_length, post_form
- Add HTTP_BASE override for AI engine (port 8095, not via gateway)
- New test scripts: test-11..13 (B2B services), test-14..18 (B2C services),
  test-19-ai-engine.sh (Python FastAPI direct), test-20-saga-flow.sh (Sprint 11 regression)
- Update test-08-order.sh + test-09-payment.sh for saga endpoints
- New master runner scripts/run-uat-full.sh with subset filter + JSONL output
- New docs/uat/{15..20} test plans + API_TESTING_RUNBOOK.md
- Update docs/uat/00-MASTER-PLAN.md endpoint counts and references"
```

---

## Ước lượng Effort

| Task | Loại | Dòng / Effort |
|------|------|---------------|
| 1. Khảo sát endpoint | Read-only | 30 min |
| 2. lib.sh extensions | Modify | ~60 dòng |
| 3. B2B tests (TP-11..13) | New | ~600 dòng / 3 files |
| 4. B2C tests (TP-14..18) | New | ~1500 dòng / 5 files |
| 5. AI engine test | New + lib edit | ~150 dòng |
| 6. Saga smoke test | New | ~120 dòng |
| 7. Master runner | New | ~80 dòng |
| 8. Docs (6 files) | New | ~500 dòng |
| 9. Verify + baseline | Test | ~30-90 phút runtime |
| 10. Commit | Git | 1 step |
| **TOTAL** | | **~3000 dòng + 90 phút runtime** |

---

## Risks & Trade-offs

1. **Runtime ~90 phút** cho full suite — chấp nhận được. Subset filter giúp chạy nhanh khi debug.
2. **Test data pollution** giữa các run — cần cleanup script riêng (`scripts/uat/cleanup.sh`) xóa các resource tên `TEST-*` trước khi chạy. Task bổ sung: thêm cleanup script.
3. **AI engine có thể chậm** (10-30s/request) — timeout 60s cho AI test.
4. **Saga test cần MySQL CLI** — đã verify available tại `C:\Program Files\MySQL\MySQL Server 9.5\bin\mysql.exe`.
5. **Một số endpoint có thể fail do security config** (route gateway chưa allow). Kết quả fail sẽ là bug report, không phải test bug.
6. **Async tests** (saga, outbox) cần sleep → test chậm hơn test sync.

## Open Questions (cần user xác nhận trước khi execute)

1. **Q1:** Có muốn tôi implement hết 10 tasks (ước lượng ~3000 dòng code + 90 phút runtime) trong 1 session? Hay chia thành 2-3 phase (Phase 1: B2B tests TP-11..13 + lib.sh, Phase 2: B2C + AI, Phase 3: saga + docs)?
2. **Q2:** Có cần thêm cleanup script (xóa `TEST-*` resources) trước run không? Y/N?
3. **Q3:** Format output: JSONL only (machine-readable cho CI) hay cả human-readable summary file? Mặc định: cả hai.
4. **Q4:** AI engine test cần API key không (OpenAI/Anthropic)? Nếu không có key, chỉ test 200 response với mock payload?

---

## Verification Checklist (sau khi execute)

- [ ] Tất cả 19 test scripts tồn tại và chmod +x
- [ ] `bash scripts/run-uat-full.sh 00-auth-setup` pass
- [ ] `bash scripts/run-uat-full.sh 02-branch` pass
- [ ] `bash scripts/run-uat-full.sh all` chạy đến hết, exit code 0 hoặc summary rõ ràng
- [ ] Test results JSONL file được tạo tại `test-results/run-*.jsonl`
- [ ] Summary file tại `test-results/summary-*.txt`
- [ ] Saga smoke test (test-20) verify được saga instance trong DB
- [ ] AI engine test (test-19) nhận response 200 (hoặc skip gracefully nếu thiếu key)

---

## Lưu ý quan trọng

- **Secret redaction**: khi viết test scripts, KHÔNG inline JWT token vào file source. Dùng pattern `source scripts/uat/.env-captured` (đã có sẵn).
- **MSYS bash vs PowerShell**: tất cả script dùng bash syntax (đã verify hoạt động).
- **ddl-auto=create**: mỗi lần restart service, data bị mất. Cần re-seed sau khi restart (xem `scripts/init-databases.sql` + `seed-admin-user.sql`).
- **MySQL port**: 3306 (đã verify), KHÔNG phải 3307 mặc định trong config. Khi start services, cần `MYSQL_PORT=3306` env.