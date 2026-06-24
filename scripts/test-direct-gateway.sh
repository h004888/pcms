#!/bin/bash
# Direct gateway integration test (no Next.js, no mock)
# Verifies 12 endpoints with REAL backend (Spring Cloud Gateway -> Eureka -> MySQL)

set -e

# Step 1: Login via gateway directly
echo "=== Direct gateway integration test ==="
echo ""
echo "1. Login via gateway (port 8080)..."
LOGIN_JSON='{"email":"admin@pcms.vn","password":"admin123"}'
LOGIN_RES=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "$LOGIN_JSON")
echo "  Login response: ${LOGIN_RES:0:120}..."

# Extract accessToken using grep + cut (avoid node -e with parens)
TOKEN=$(echo "$LOGIN_RES" | grep -oE '"accessToken":"[^"]+"' | head -1 | sed 's/"accessToken":"//;s/"$//')
echo "  Token (first 50): ${TOKEN:0:50}..."
echo ""

if [ -z "$TOKEN" ]; then
  echo "  ERROR: No token in response"
  exit 1
fi

# Step 2: Test 12 endpoints
echo "2. Test 12 endpoints via DIRECT gateway (bypass Next.js)..."
for ep in users branches medicines customers categories suppliers inventory orders payments prescriptions notifications reports; do
  HTTP=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/${ep}?size=1")
  BODY=$(curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/${ep}?size=1")
  TOTAL=$(echo "$BODY" | grep -oE '"total":[0-9]+' | head -1 | sed 's/"total"://')
  echo "  $ep: HTTP $HTTP, total=${TOTAL:-?}"
done

# Step 3: Verify customer ID matches MySQL (not mock)
echo ""
echo "3. Verify customer UUID is from MySQL (not mock)..."
CUST_RES=$(curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/customers?size=1")
echo "  Raw response: ${CUST_RES:0:300}"
echo ""
echo "  Mock seed UUID was: 021fa652-c819-4fc4-95e2-93146d39e3c4"
echo "  Expected real UUID: ee392a88-2c16-4688-82fc-b0c9881454d4"
echo "  Actual UUID:"
echo "$CUST_RES" | grep -oE '"id":"[^"]+"' | head -1
echo ""

# Step 4: Verify orders (should have data)
echo "4. Verify orders (PAID status should have data)..."
ORD_RES=$(curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/orders?size=2")
echo "  ${ORD_RES:0:300}"
echo ""
