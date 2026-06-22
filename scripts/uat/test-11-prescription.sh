#!/usr/bin/env bash
# =====================================================
# PCMS UAT - TP-11: Prescription Service (UC12)
# =====================================================
set -uo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib.sh"
[[ -f "$SCRIPT_DIR/.env-captured" ]] && source "$SCRIPT_DIR/.env-captured"

echo "=========================================="
echo "  TP-11: Prescription Service"
echo "=========================================="

preflight || exit 1

# ============== TP-11A: Prescription CRUD ==============
echo ""
echo "--- TP-11A: Prescription CRUD ---"

# TC-01: List prescriptions
read -r code body <<< "$(http GET /api/v1/prescriptions "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-11A-01 prescriptions-list"

# TC-02: List with pagination
read -r code body <<< "$(http GET "/api/v1/prescriptions?page=0&size=10" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "200" "$code" "TP-11A-02 prescriptions-paginated"

# TC-03: Get non-existent -> 404
read -r code body <<< "$(http GET "/api/v1/prescriptions/00000000-0000-0000-0000-000000000000" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "404" "$code" "TP-11A-03 prescription-not-found"

# TC-04: Create prescription (capture ID)
TEST_CODE="TEST-RX-$(date +%s)"
PATIENT_NAME="Test Patient RX"
read -r code body <<< "$(http POST /api/v1/prescriptions \
    "{\"code\":\"$TEST_CODE\",\"patientName\":\"$PATIENT_NAME\",\"doctorId\":\"$ADMIN_ID\",\"medicines\":[]}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "201" "$code" "TP-11A-04 prescription-create"
capture PRESCRIPTION_ID "$body" ".id"
capture PRESCRIPTION_CODE "$body" ".code"

# TC-05: Get by id
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" ]]; then
    read -r code body <<< "$(http GET "/api/v1/prescriptions/$PRESCRIPTION_ID" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11A-05 prescription-get-by-id"
    assert_not_empty "$body" ".id" "TP-11A-05b prescription-id-not-empty"
fi

# TC-06: Get by code
if [[ -n "${PRESCRIPTION_CODE:-}" && "$PRESCRIPTION_CODE" != "null" ]]; then
    read -r code body <<< "$(http GET "/api/v1/prescriptions/code/$PRESCRIPTION_CODE" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11A-06 prescription-get-by-code"
fi

# TC-07: Create draft
DRAFT_CODE="TEST-DRAFT-$(date +%s)"
read -r code body <<< "$(http POST /api/v1/prescriptions/draft \
    "{\"code\":\"$DRAFT_CODE\",\"patientName\":\"Draft Patient\"}" \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "201" "$code" "TP-11A-07 prescription-create-draft"

# TC-08: Update
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" ]]; then
    read -r code body <<< "$(http PUT "/api/v1/prescriptions/$PRESCRIPTION_ID" \
        "{\"patientName\":\"Updated Patient\",\"notes\":\"Updated by UAT\"}" \
        "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11A-08 prescription-update"
fi

# TC-09: Sign (POST variant)
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" ]]; then
    read -r code body <<< "$(http POST "/api/v1/prescriptions/$PRESCRIPTION_ID/sign" \
        "{\"signature\":\"UAT_SIGNED\"}" \
        "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11A-09 prescription-sign"
fi

# TC-10: Missing required field -> 400
read -r code body <<< "$(http POST /api/v1/prescriptions \
    '{"description":"missing required fields"}' \
    "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "400" "$code" "TP-11A-10 prescription-validation-error"

# ============== TP-11B: Link/Print/Delete ==============
echo ""
echo "--- TP-11B: Link / Print / Delete ---"

# TC-11: Link to order (if ORDER_ID available)
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" && -n "${ORDER_ID:-}" && "$ORDER_ID" != "null" ]]; then
    read -r code body <<< "$(http POST "/api/v1/prescriptions/$PRESCRIPTION_ID/link-order?orderId=$ORDER_ID" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11B-11 prescription-link-order"
else
    log_warn "TP-11B-11 skipped - ORDER_ID or PRESCRIPTION_ID not captured"
fi

# TC-12: Print GET
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" ]]; then
    read -r code body <<< "$(http GET "/api/v1/prescriptions/$PRESCRIPTION_ID/print" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11B-12 prescription-print-get"
fi

# TC-13: Print POST
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" ]]; then
    read -r code body <<< "$(http POST "/api/v1/prescriptions/$PRESCRIPTION_ID/print" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "200" "$code" "TP-11B-13 prescription-print-post"
fi

# TC-14: Delete
if [[ -n "${PRESCRIPTION_ID:-}" && "$PRESCRIPTION_ID" != "null" ]]; then
    read -r code body <<< "$(http DELETE "/api/v1/prescriptions/$PRESCRIPTION_ID" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
    assert_status "204" "$code" "TP-11B-14 prescription-delete"
fi

# TC-15: Delete non-existent -> 404
read -r code body <<< "$(http DELETE "/api/v1/prescriptions/00000000-0000-0000-0000-000000000000" "" "Authorization: Bearer *** $ACCESS_TOKEN")"
assert_status "404" "$code" "TP-11B-15 prescription-delete-not-found"

log_info "TP-11 complete: $(grep -c "PASS\|FAIL" "$RAW_RESULTS" 2>/dev/null || echo 0) tests"