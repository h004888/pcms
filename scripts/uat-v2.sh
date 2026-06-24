#!/bin/bash
# UAT Test Runner v2 - PCMS (corrected paths)
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"

ACCESS_TOKEN=$(grep -oE '"accessToken":"[^"]+"' /tmp/login.json 2>/dev/null | head -1 | cut -d'"' -f4)
[ -z "$ACCESS_TOKEN" ] && {
	echo "No token. Run login first."
	exit 1
}

PASS=0
FAIL=0
test() {
	local name="$1" method="$2" url="$3" data="$4"
	if [ -n "$data" ]; then
		st=$(curl -s -o /tmp/r.json -w "%{http_code}" --max-time 5 -X "$method" "$url" \
			-H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -d "$data" 2>/dev/null)
	else
		st=$(curl -s -o /tmp/r.json -w "%{http_code}" --max-time 5 -X "$method" "$url" \
			-H "Authorization: Bearer $ACCESS_TOKEN" 2>/dev/null)
	fi
	if [ "$st" = "200" ] || [ "$st" = "201" ] || [ "$st" = "204" ]; then
		PASS=$((PASS + 1))
		printf "  ✅ %-50s %s\n" "$name" "$st"
	else
		FAIL=$((FAIL + 1))
		printf "  ❌ %-50s %s\n" "$name" "$st"
	fi
}

echo "━━━ TP-12: REPORT (corrected paths) ━━━"
test "GET /reports/revenue" GET "http://localhost:8092/reports/revenue?from=2025-01-01&to=2025-12-31"
test "GET /reports/inventory" GET "http://localhost:8092/reports/inventory"
test "GET /reports/staff" GET "http://localhost:8092/reports/staff?fromDate=2025-01-01&toDate=2025-12-31"

echo ""
echo "━━━ TP-13: CUSTOMER-PORTAL (corrected) ━━━"
test "GET /shop" GET "http://localhost:8093/shop"
test "GET /addresses" GET "http://localhost:8093/addresses"
test "GET /favorites" GET "http://localhost:8093/favorites"
test "GET /family" GET "http://localhost:8093/family"

echo ""
echo "━━━ TP-08: ORDER (corrected) ━━━"
test "GET /admin/outbox?status=PENDING" GET "http://localhost:8088/admin/outbox?status=PENDING"
test "GET /coupons?status=ACTIVE" GET "http://localhost:8088/coupons?status=ACTIVE"
test "GET /orders?status=PENDING" GET "http://localhost:8088/orders?status=PENDING"

echo ""
echo "━━━ TP-11: NOTIFICATION (corrected) ━━━"
test "GET /notifications?userId=test" GET "http://localhost:8091/notifications?userId=test"
test "GET /notifications/templates" GET "http://localhost:8091/notifications/templates"

echo ""
echo "━━━ TP-06: INVENTORY (corrected) ━━━"
test "GET /inventory?branchId=UUID" GET "http://localhost:8086/inventory?branchId=00000000-0000-0000-0000-000000000000"
test "GET /inventory/low-stock" GET "http://localhost:8086/inventory/low-stock"

echo ""
echo "============================================================"
echo "  ADDITIONAL TESTS: PASS=$PASS, FAIL=$FAIL"
echo "============================================================"
