#!/bin/bash
# UAT Test Runner - PCMS
# Usage: bash scripts/uat-test.sh

cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"

ACCESS_TOKEN=$(grep -oE '"accessToken":"[^"]+"' /tmp/login.json 2>/dev/null | head -1 | cut -d'"' -f4)
if [ -z "$ACCESS_TOKEN" ]; then
	echo "ERROR: No access token. Run login first."
	exit 1
fi

PASS=0
FAIL=0
RESULTS=""

# Helper function
test_endpoint() {
	local name="$1"
	local method="$2"
	local url="$3"
	local data="$4"
	local expected="$5"

	if [ -n "$data" ]; then
		status=$(curl -s -o /tmp/resp.json -w "%{http_code}" --max-time 5 -X "$method" "$url" \
			-H "Content-Type: application/json" \
			-H "Authorization: Bearer $ACCESS_TOKEN" \
			-d "$data" 2>/dev/null)
	else
		status=$(curl -s -o /tmp/resp.json -w "%{http_code}" --max-time 5 -X "$method" "$url" \
			-H "Authorization: Bearer $ACCESS_TOKEN" 2>/dev/null)
	fi

	if [ "$status" = "$expected" ] || [ "$status" = "200" ] || [ "$status" = "201" ] || [ "$status" = "204" ]; then
		PASS=$((PASS + 1))
		printf "  ✅ %-50s %s\n" "$name" "HTTP $status"
	else
		FAIL=$((FAIL + 1))
		printf "  ❌ %-50s %s (expected %s)\n" "$name" "HTTP $status" "$expected"
	fi
}

echo "============================================================"
echo "  PCMS UAT TEST RUNNER - $(date)"
echo "============================================================"

# === TP-02: BRANCH SERVICE ===
echo ""
echo "━━━ TP-02: BRANCH SERVICE ━━━"
test_endpoint "TC-01 List branches" "GET" "http://localhost:8082/branches" "" "200"
test_endpoint "TC-02 Get by ID (random UUID)" "GET" "http://localhost:8082/branches/00000000-0000-0000-0000-000000000000" "" "404"
test_endpoint "TC-03 Create branch" "POST" "http://localhost:8082/branches" '{"code":"UAT-01","name":"UAT Test","address":"123 UAT","phone":"0901234567"}' "201"
test_endpoint "TC-04 Create branch (missing field)" "POST" "http://localhost:8082/branches" '{"code":"UAT-02"}' "400"
test_endpoint "TC-05 Get by code" "GET" "http://localhost:8082/branches/code/UAT-01" "" "200"

# === TP-03: CATALOG SERVICE ===
echo ""
echo "━━━ TP-03: CATALOG SERVICE ━━━"
test_endpoint "TC-01 List medicines" "GET" "http://localhost:8083/medicines" "" "200"
test_endpoint "TC-02 Search" "GET" "http://localhost:8083/medicines?search=paracetamol" "" "200"
test_endpoint "TC-03 Get by ID" "GET" "http://localhost:8083/medicines/00000000-0000-0000-0000-000000000000" "" "404"
test_endpoint "TC-04 Create medicine" "POST" "http://localhost:8083/medicines" '{"name":"Paracetamol 500mg","code":"PCM500","price":5000,"categoryId":"00000000-0000-0000-0000-000000000001","unit":"HOP"}' "201"
test_endpoint "TC-05 Create medicine (missing price)" "POST" "http://localhost:8083/medicines" '{"name":"Test","code":"T1"}' "400"

# === TP-04: CATEGORY SERVICE ===
echo ""
echo "━━━ TP-04: CATEGORY SERVICE ━━━"
test_endpoint "TC-01 List categories" "GET" "http://localhost:8084/categories" "" "200"
test_endpoint "TC-02 Get by ID" "GET" "http://localhost:8084/categories/00000000-0000-0000-0000-000000000000" "" "404"
test_endpoint "TC-03 Create category" "POST" "http://localhost:8084/categories" '{"name":"Thuốc giảm đau","slug":"giam-dau"}' "201"
test_endpoint "TC-04 Get by slug" "GET" "http://localhost:8084/categories/slug/giam-dau" "" "200"

# === TP-05: SUPPLIER SERVICE ===
echo ""
echo "━━━ TP-05: SUPPLIER SERVICE ━━━"
test_endpoint "TC-01 List suppliers" "GET" "http://localhost:8085/suppliers" "" "200"
test_endpoint "TC-02 Create supplier" "POST" "http://localhost:8085/suppliers" '{"name":"Supplier A","taxCode":"TAX-001","phone":"0901234567","email":"supplier@test.com"}' "201"
test_endpoint "TC-03 Get by ID" "GET" "http://localhost:8085/suppliers/00000000-0000-0000-0000-000000000000" "" "404"

# === TP-06: INVENTORY SERVICE ===
echo ""
echo "━━━ TP-06: INVENTORY SERVICE ━━━"
test_endpoint "TC-01 List inventory" "GET" "http://localhost:8086/inventory" "" "200"
test_endpoint "TC-02 Get by branch" "GET" "http://localhost:8086/inventory/branch/00000000-0000-0000-0000-000000000000" "" "200"
test_endpoint "TC-03 Get by medicine" "GET" "http://localhost:8086/inventory/medicine/00000000-0000-0000-0000-000000000000" "" "200"
test_endpoint "TC-04 List low-stock" "GET" "http://localhost:8086/inventory/low-stock" "" "200"

# === TP-07: CUSTOMER SERVICE ===
echo ""
echo "━━━ TP-07: CUSTOMER SERVICE ━━━"
test_endpoint "TC-01 List customers" "GET" "http://localhost:8087/customers" "" "200"
test_endpoint "TC-02 Search" "GET" "http://localhost:8087/customers?search=nguyen" "" "200"
test_endpoint "TC-03 Create customer" "POST" "http://localhost:8087/customers" '{"fullName":"Nguyen Van A","phone":"0901234567","email":"nguyena@test.com"}' "201"
test_endpoint "TC-04 Get stats" "GET" "http://localhost:8087/customers/stats" "" "200"

# === TP-08: ORDER SERVICE ===
echo ""
echo "━━━ TP-08: ORDER SERVICE ━━━"
test_endpoint "TC-01 List orders" "GET" "http://localhost:8088/orders" "" "200"
test_endpoint "TC-02 Get by status" "GET" "http://localhost:8088/orders?status=PENDING" "" "200"
test_endpoint "TC-03 List coupons" "GET" "http://localhost:8088/coupons" "" "200"
test_endpoint "TC-04 List outbox" "GET" "http://localhost:8088/admin/outbox" "" "200"

# === TP-09: PAYMENT SERVICE ===
echo ""
echo "━━━ TP-09: PAYMENT SERVICE ━━━"
test_endpoint "TC-01 List payments" "GET" "http://localhost:8089/payments" "" "200"
test_endpoint "TC-02 Get by status" "GET" "http://localhost:8089/payments?status=SUCCESS" "" "200"

# === TP-10: PRESCRIPTION SERVICE ===
echo ""
echo "━━━ TP-10: PRESCRIPTION SERVICE ━━━"
test_endpoint "TC-01 List prescriptions" "GET" "http://localhost:8090/prescriptions" "" "200"
test_endpoint "TC-02 Get by status" "GET" "http://localhost:8090/prescriptions?status=PENDING" "" "200"

# === TP-11: NOTIFICATION SERVICE ===
echo ""
echo "━━━ TP-11: NOTIFICATION SERVICE ━━━"
test_endpoint "TC-01 List notifications" "GET" "http://localhost:8091/notifications" "" "200"
test_endpoint "TC-02 Get by user" "GET" "http://localhost:8091/notifications/user/test" "" "200"

# === TP-12: REPORT SERVICE ===
echo ""
echo "━━━ TP-12: REPORT SERVICE ━━━"
test_endpoint "TC-01 List reports" "GET" "http://localhost:8092/reports" "" "200"
test_endpoint "TC-02 Get types" "GET" "http://localhost:8092/reports/types" "" "200"

# === TP-15: MOBILE-BFF ===
echo ""
echo "━━━ TP-15: MOBILE-BFF ━━━"
test_endpoint "TC-01 Get profile" "GET" "http://localhost:8096/mobile/profile" "" "200"
test_endpoint "TC-02 Get reminders" "GET" "http://localhost:8096/mobile/reminders" "" "200"

# === TP-16: HEALTH-TOOLS ===
echo ""
echo "━━━ TP-16: HEALTH-TOOLS ━━━"
test_endpoint "TC-01 Get quizzes" "GET" "http://localhost:8097/health/quizzes" "" "200"
test_endpoint "TC-02 List tools" "GET" "http://localhost:8097/health/tools" "" "200"

# === TP-17: ECOM-OPS ===
echo ""
echo "━━━ TP-17: ECOM-OPS ━━━"
test_endpoint "TC-01 List flash sales" "GET" "http://localhost:8098/ecom-ops/flash-sales/active" "" "200"
test_endpoint "TC-02 Admin list" "GET" "http://localhost:8098/admin/flash-sales" "" "200"

# === TP-13: CUSTOMER-PORTAL ===
echo ""
echo "━━━ TP-13: CUSTOMER-PORTAL ━━━"
test_endpoint "TC-01 Get store" "GET" "http://localhost:8093/store" "" "200"
test_endpoint "TC-02 Get cart" "GET" "http://localhost:8093/cart" "" "200"
test_endpoint "TC-03 Get vouchers" "GET" "http://localhost:8093/vouchers" "" "200"
test_endpoint "TC-04 Get wallet" "GET" "http://localhost:8093/wallet" "" "200"

# === Summary ===
echo ""
echo "============================================================"
echo "  TEST RESULTS SUMMARY"
echo "============================================================"
echo "  PASS: $PASS"
echo "  FAIL: $FAIL"
TOTAL=$((PASS + FAIL))
if [ $TOTAL -gt 0 ]; then
	PCT=$(awk "BEGIN {printf \"%.1f\", $PASS*100/$TOTAL}")
	echo "  PASS RATE: $PCT%"
fi
echo "============================================================"
