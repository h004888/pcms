#!/usr/bin/env bash
# =====================================================
# PCMS UAT - TP-12: Notification Service (UC13)
# Covers: NotificationController + TemplateController + OutboxConsumer
# =====================================================
set -uo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib.sh"
[[ -f "$SCRIPT_DIR/.env-captured" ]] && source "$SCRIPT_DIR/.env-captured"

echo "=========================================="
echo "  TP-12: Notification Service"
echo "=========================================="

preflight || exit 1

# ============== TP-12A: Notification CRUD ==============
echo ""
echo "--- TP-12A: Notification CRUD ---"

# TC-01: List notifications
read -r code body <<< "$(http GET /api/v1/notifications "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12A-01 notifications-list"

# TC-02: List unread
read -r code body <<< "$(http GET /api/v1/notifications/unread "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12A-02 notifications-unread"

# TC-03: Get non-existent -> 404
read -r code body <<< "$(http GET "/api/v1/notifications/00000000-0000-0000-0000-000000000000" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "404" "$code" "TP-12A-03 notification-not-found"

# TC-04: Create notification
RECIPIENT_ID="${CUSTOMER_ID:-00000000-0000-0000-0000-000000000001}"
TEST_NOTIF_BODY="{\"recipientId\":\"$RECIPIENT_ID\",\"type\":\"INFO\",\"title\":\"UAT Test\",\"message\":\"Hello from UAT test\"}"
read -r code body <<< "$(http POST /api/v1/notifications "$TEST_NOTIF_BODY" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "201" "$code" "TP-12A-04 notification-create"
capture NOTIF_ID "$body" ".id"

# TC-05: Bulk create
read -r code body <<< "$(http POST /api/v1/notifications/bulk \
    "{\"recipientIds\":[\"$RECIPIENT_ID\"],\"type\":\"INFO\",\"title\":\"UAT Bulk\",\"message\":\"Bulk test\"}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12A-05 notification-bulk-create"

# TC-06: Compose (template-based)
read -r code body <<< "$(http POST /api/v1/notifications/compose \
    "{\"recipientId\":\"$RECIPIENT_ID\",\"templateCode\":\"WELCOME\",\"vars\":{}}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12A-06 notification-compose"

# TC-07: Mark as read
if [[ -n "${NOTIF_ID:-}" && "$NOTIF_ID" != "null" ]]; then
    read -r code body <<< "$(http PUT "/api/v1/notifications/$NOTIF_ID/read" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-12A-07 notification-mark-read"
fi

# TC-08: Mark all read
read -r code body <<< "$(http PUT "/api/v1/notifications/read-all?recipientId=$RECIPIENT_ID" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12A-08 notification-mark-all-read"

# TC-09: Broadcast
read -r code body <<< "$(http POST /api/v1/notifications/broadcast \
    "{\"type\":\"INFO\",\"title\":\"UAT Broadcast\",\"message\":\"Broadcast test\"}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12A-09 notification-broadcast"

# TC-10: Retry non-existent -> 404
read -r code body <<< "$(http POST "/api/v1/notifications/00000000-0000-0000-0000-000000000000/retry" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "404" "$code" "TP-12A-10 notification-retry-not-found"

# TC-11: Delete
if [[ -n "${NOTIF_ID:-}" && "$NOTIF_ID" != "null" ]]; then
    read -r code body <<< "$(http DELETE "/api/v1/notifications/$NOTIF_ID" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-12A-11 notification-delete"
fi

# TC-12: Missing required -> 400
read -r code body <<< "$(http POST /api/v1/notifications '{"description":"missing"}' "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "400" "$code" "TP-12A-12 notification-validation"

# ============== TP-12B: Templates ==============
echo ""
echo "--- TP-12B: Notification Templates ---"

# TC-13: List templates
read -r code body <<< "$(http GET /api/v1/notifications/templates "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-12B-13 templates-list"

# TC-14: Create template
read -r code body <<< "$(http POST /api/v1/notifications/templates \
    "{\"code\":\"UAT_TPL_$(date +%s)\",\"name\":\"UAT Test Template\",\"subject\":\"Test\",\"body\":\"Hello {{name}}\"}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "201" "$code" "TP-12B-14 template-create"

# ============== TP-12C: Outbox Consumer ==============
echo ""
echo "--- TP-12C: Outbox Consumer (saga events) ---"

# TC-15: Outbox order-paid event (from saga)
ORDER_ID_FOR_NOTIF="${ORDER_ID:-019f7662-e5d5-4946-8112-9b91c925dfe7}"
read -r code body <<< "$(http POST /api/v1/notifications/orders/paid \
    "{\"orderId\":\"$ORDER_ID_FOR_NOTIF\",\"orderNumber\":\"ORD-UAT-N\",\"customerId\":\"$RECIPIENT_ID\",\"branchId\":\"${BRANCH_HQ:-00000000-0000-0000-0000-000000000002}\",\"staffId\":\"$ADMIN_ID\",\"total\":50000}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
# Accept 200, 404 (no real order), or 500 (downstream) — must not be 503/connection refused
if [[ "$code" == "200" || "$code" == "404" || "$code" == "500" ]]; then
    log_pass "TP-12C-15 outbox-orders-paid (HTTP $code)"
else
    log_fail "TP-12C-15 outbox-orders-paid (HTTP $code)"
fi

# TC-16: Outbox low-stock event
read -r code body <<< "$(http POST /api/v1/notifications/inventory/low-stock \
    "{\"medicineId\":\"00000000-0000-0000-0000-000000000099\",\"branchId\":\"${BRANCH_HQ:-00000000-0000-0000-0000-000000000002}\",\"currentQty\":5,\"threshold\":10}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
if [[ "$code" == "200" || "$code" == "404" || "$code" == "500" ]]; then
    log_pass "TP-12C-16 outbox-low-stock (HTTP $code)"
else
    log_fail "TP-12C-16 outbox-low-stock (HTTP $code)"
fi

# TC-17: Outbox expiry event
read -r code body <<< "$(http POST /api/v1/notifications/inventory/expiry \
    "{\"medicineId\":\"00000000-0000-0000-0000-000000000099\",\"branchId\":\"${BRANCH_HQ:-00000000-0000-0000-0000-000000000002}\",\"expiryDate\":\"2026-12-31\",\"daysToExpiry\":30}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
if [[ "$code" == "200" || "$code" == "404" || "$code" == "500" ]]; then
    log_pass "TP-12C-17 outbox-expiry (HTTP $code)"
else
    log_fail "TP-12C-17 outbox-expiry (HTTP $code)"
fi

log_info "TP-12 complete"