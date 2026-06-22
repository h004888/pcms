#!/usr/bin/env bash
# =====================================================
# PCMS UAT - TP-13: Report Service
# Covers UC09 - View Reports, UC10 - Search/Filter
# =====================================================

set -uo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib.sh"
[[ -f "$SCRIPT_DIR/.env-captured" ]] && source "$SCRIPT_DIR/.env-captured"

echo "=========================================="
echo "  TP-13: Report Service"
echo "=========================================="

preflight || exit 1

# Test resources
TEST_RPT_CODE="TEST-RPT-$(date +%s)"
BRANCH_ID="${BRANCH_HQ:-00000000-0000-0000-0000-000000000003}"
CREATED_BY="${ADMIN_ID:-00000000-0000-0000-0000-000000000001}"
SCHEDULE_ID=""

# Date range: fixed June 2026 window
FROM_DATE="${FROM_DATE:-2026-06-01}"
TO_DATE="${TO_DATE:-2026-06-30}"

# Auth header (matches existing tests)
AUTH="Authorization: Bearer *** $ACCESS_TOKEN"
BRANCH_HDR="X-Branch-Id: $BRANCH_ID"
USER_HDR="X-User-Id: $CREATED_BY"

# ============== TP-13A: Reports (revenue / inventory / staff) ==============
echo ""
echo "--- TP-13A: Reports (revenue / inventory / staff) ---"

# TC-01: GET revenue with date range
read -r code body <<< "$(http GET "/api/v1/reports/revenue?from=$FROM_DATE&to=$TO_DATE" "" "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13A-01 revenue-get"
assert_not_empty "$body" "." "TP-13A-01b revenue-body-not-empty"

# TC-02: POST revenue with body
read -r code body <<< "$(http POST /api/v1/reports/revenue \
    "{\"fromDate\":\"$FROM_DATE\",\"toDate\":\"$TO_DATE\",\"branchId\":\"$BRANCH_ID\",\"groupBy\":\"day\"}" \
    "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13A-02 revenue-post"

# TC-03: GET inventory report
read -r code body <<< "$(http GET "/api/v1/reports/inventory?branchId=$BRANCH_ID" "" "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13A-03 inventory-get"

# TC-04: GET staff report
read -r code body <<< "$(http GET "/api/v1/reports/staff?fromDate=$FROM_DATE&toDate=$TO_DATE&branchId=$BRANCH_ID" "" "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13A-04 staff-get"

# TC-05: POST staff report (body variant)
read -r code body <<< "$(http POST /api/v1/reports/staff \
    "{\"fromDate\":\"$FROM_DATE\",\"toDate\":\"$TO_DATE\",\"branchId\":\"$BRANCH_ID\"}" \
    "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13A-05 staff-post"

# TC-06: POST inventory report (body variant)
read -r code body <<< "$(http POST /api/v1/reports/inventory \
    "{\"branchId\":\"$BRANCH_ID\",\"fromDate\":\"$FROM_DATE\",\"toDate\":\"$TO_DATE\"}" \
    "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13A-06 inventory-post"

# ============== TP-13B: Realtime + Export ==============
echo ""
echo "--- TP-13B: Realtime + Export ---"

# TC-07: realtime stats
read -r code body <<< "$(http GET "/api/v1/reports/realtime/stats?branchId=$BRANCH_ID" "" "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13B-07 realtime-stats"
assert_not_empty "$body" "." "TP-13B-07b realtime-stats-body"

# TC-08: realtime recent-orders
read -r code body <<< "$(http GET "/api/v1/reports/realtime/recent-orders?branchId=$BRANCH_ID&limit=10" "" "$AUTH" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13B-08 realtime-recent-orders"
assert_array_min_length "$body" "." 0 "TP-13B-08b recent-orders-array"

# TC-09: export (GET variant). Returns binary - accept 2xx.
read -r code body <<< "$(http GET "/api/v1/reports/export?type=revenue&format=excel&from=$FROM_DATE&to=$TO_DATE" "" "$AUTH")"
if [[ "$code" =~ ^2..$ ]]; then
    log_pass "TP-13B-09 report-export (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    echo "{\"test\":\"TP-13B-09 report-export\",\"status\":\"PASS\",\"http\":$code}" >> "$RAW_RESULTS"
else
    log_fail "TP-13B-09 report-export (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    FAIL_COUNT=$((FAIL_COUNT + 1))
    echo "{\"test\":\"TP-13B-09 report-export\",\"status\":\"FAIL\",\"http\":$code}" >> "$RAW_RESULTS"
fi

# ============== TP-13C: Schedules ==============
echo ""
echo "--- TP-13C: Schedules ---"

# TC-10: Schedule a report (capture SCHEDULE_ID)
read -r code body <<< "$(http POST /api/v1/reports/schedule \
    "{\"type\":\"REVENUE\",\"format\":\"PDF\",\"branchId\":\"$BRANCH_ID\",\"cronExpression\":\"0 0 8 * * *\",\"recipientEmail\":\"uat-$TEST_RPT_CODE@pcms.vn\",\"createdBy\":\"$CREATED_BY\"}" \
    "$AUTH" "$USER_HDR" "$BRANCH_HDR")"
assert_status "200" "$code" "TP-13C-10 schedule-create"
capture SCHEDULE_ID "$body" ".id"

# TC-11: List schedules
read -r code body <<< "$(http GET /api/v1/reports/schedules "" "$AUTH")"
assert_status "200" "$code" "TP-13C-11 schedules-list"

# TC-12: Delete the schedule (idempotent → 200 expected)
if [[ -n "$SCHEDULE_ID" && "$SCHEDULE_ID" != "null" ]]; then
    read -r code body <<< "$(http DELETE "/api/v1/reports/schedules/$SCHEDULE_ID" "" "$AUTH")"
    assert_status "200" "$code" "TP-13C-12 schedule-delete"
fi

# TC-13: Schedule missing required field (no type / no cron) → 400
read -r code body <<< "$(http POST /api/v1/reports/schedule \
    "{\"format\":\"PDF\",\"recipientEmail\":\"missing-fields@pcms.vn\"}" \
    "$AUTH" "$USER_HDR" "$BRANCH_HDR")"
assert_status "400" "$code" "TP-13C-13 schedule-missing-fields"

# TC-14: Delete non-existent schedule → 404
read -r code body <<< "$(http DELETE "/api/v1/reports/schedules/00000000-0000-0000-0000-000000000000" "" "$AUTH")"
assert_status "404" "$code" "TP-13C-14 schedule-delete-not-found"

# ============== TP-13D: Async Export Jobs ==============
echo ""
echo "--- TP-13D: Async Export Jobs (excel/pdf) ---"

# TC-15: Export excel (POST async; returns 202 Accepted with job descriptor)
read -r code body <<< "$(http POST /api/v1/reports/export/excel \
    "{\"type\":\"REVENUE\",\"fromDate\":\"$FROM_DATE\",\"toDate\":\"$TO_DATE\",\"branchId\":\"$BRANCH_ID\"}" \
    "$AUTH")"
if [[ "$code" =~ ^2..$ ]] && [[ "$code" != "500" ]]; then
    log_pass "TP-13D-15 export-excel (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    echo "{\"test\":\"TP-13D-15 export-excel\",\"status\":\"PASS\",\"http\":$code}" >> "$RAW_RESULTS"
else
    log_fail "TP-13D-15 export-excel (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    FAIL_COUNT=$((FAIL_COUNT + 1))
    echo "{\"test\":\"TP-13D-15 export-excel\",\"status\":\"FAIL\",\"http\":$code}" >> "$RAW_RESULTS"
fi

# TC-16: Export pdf (POST async; returns 202 Accepted)
read -r code body <<< "$(http POST /api/v1/reports/export/pdf \
    "{\"type\":\"REVENUE\",\"fromDate\":\"$FROM_DATE\",\"toDate\":\"$TO_DATE\",\"branchId\":\"$BRANCH_ID\"}" \
    "$AUTH")"
if [[ "$code" =~ ^2..$ ]] && [[ "$code" != "500" ]]; then
    log_pass "TP-13D-16 export-pdf (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    echo "{\"test\":\"TP-13D-16 export-pdf\",\"status\":\"PASS\",\"http\":$code}" >> "$RAW_RESULTS"
else
    log_fail "TP-13D-16 export-pdf (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    FAIL_COUNT=$((FAIL_COUNT + 1))
    echo "{\"test\":\"TP-13D-16 export-pdf\",\"status\":\"FAIL\",\"http\":$code}" >> "$RAW_RESULTS"
fi

# TC-17: Invalid date range (from > to). Service may either reject with 400 or accept (200).
# Assert it is not 500.
read -r code body <<< "$(http GET "/api/v1/reports/revenue?from=2026-12-31&to=2026-01-01" "" "$AUTH" "$BRANCH_HDR")"
if [[ "$code" == "500" ]]; then
    log_fail "TP-13D-17 invalid-date-range (HTTP $code - server error)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    FAIL_COUNT=$((FAIL_COUNT + 1))
    echo "{\"test\":\"TP-13D-17 invalid-date-range\",\"status\":\"FAIL\",\"http\":$code}" >> "$RAW_RESULTS"
else
    log_pass "TP-13D-17 invalid-date-range (HTTP $code)"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    echo "{\"test\":\"TP-13D-17 invalid-date-range\",\"status\":\"PASS\",\"http\":$code}" >> "$RAW_RESULTS"
fi

log_info "TP-13 complete"
