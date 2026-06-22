#!/usr/bin/env bash
# =====================================================
# PCMS UAT - Shared Library for Test Scripts
# Provides: HTTP helpers, assertion, JSON parsing, env vars
# =====================================================

# ----------- Configuration -----------
export GATEWAY="${GATEWAY:-http://localhost:8080}"
export AI_ENGINE="${AI_ENGINE:-http://localhost:8095}"
export EUREKA="${EUREKA:-http://localhost:8761}"
export TIMEOUT="${TIMEOUT:-30}"

# Test data (override via env or seed from Master Plan §7)
export ADMIN_EMAIL="${ADMIN_EMAIL:-admin@pcms.vn}"
export ADMIN_PASS="${ADMIN_PASS:-admin123}"
export CUSTOMER_EMAIL="${CUSTOMER_EMAIL:-customer@pcms.vn}"
export CUSTOMER_PASS="${CUSTOMER_PASS:-customer123}"

# Captured variables (initialized empty)
export ACCESS_TOKEN=""
export CUSTOMER_TOKEN=""
export PHARMACIST_TOKEN=""
export MANAGER_TOKEN=""
export CUSTOMER_ID=""
export ADMIN_ID=""
export BRANCH_HQ=""
export MED1=""
export MED2=""
export MED3=""
export CART_ID=""
export ORDER_ID=""
export PAYMENT_ID=""

# Result tracking
RESULTS_DIR="${RESULTS_DIR:-./test-results}"
mkdir -p "$RESULTS_DIR"
RAW_RESULTS="$RESULTS_DIR/raw-results-$(date +%Y%m%d-%H%M%S).jsonl"
SUMMARY="$RESULTS_DIR/summary-$(date +%Y%m%d-%H%M%S).txt"
PASS_COUNT=0
FAIL_COUNT=0
TOTAL_COUNT=0

# ----------- Color codes -----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ----------- Logging -----------
log_info() {
    echo -e "${BLUE}[INFO]${NC} $*" >&2
}

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $*" >&2
    PASS_COUNT=$((PASS_COUNT + 1))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $*" >&2
    FAIL_COUNT=$((FAIL_COUNT + 1))
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $*" >&2
}

# ----------- HTTP Helper -----------
# Usage: http <METHOD> <PATH> [DATA] [HEADERS...]
# Echoes: HTTP_STATUS BODY
http() {
    local method="$1"
    local path="$2"
    local data="$3"
    shift 3
    local headers=("$@")

    local url="$path"
    if [[ "$path" != http* ]]; then
        url="$GATEWAY$path"
    fi

    local args=(-s -o /tmp/uat-body.txt -w "%{http_code}" --max-time "$TIMEOUT" -X "$method")
    for h in "${headers[@]}"; do
        args+=(-H "$h")
    done
    if [[ -n "$data" ]]; then
        args+=(-H "Content-Type: application/json" -d "$data")
    fi

    local code
    code=$(curl "${args[@]}" "$url" 2>/dev/null)
    local body
    body=$(cat /tmp/uat-body.txt 2>/dev/null)
    echo "$code"
    echo "$body"
}

# Same as http() but for AI engine direct calls
http_ai() {
    local method="$1"
    local path="$2"
    local data="$3"
    shift 3
    local headers=("$@")

    local url="$AI_ENGINE$path"

    local args=(-s -o /tmp/uat-body.txt -w "%{http_code}" --max-time "$TIMEOUT" -X "$method")
    for h in "${headers[@]}"; do
        args+=(-H "$h")
    done
    if [[ -n "$data" ]]; then
        args+=(-H "Content-Type: application/json" -d "$data")
    fi

    local code
    code=$(curl "${args[@]}" "$url" 2>/dev/null)
    local body
    body=$(cat /tmp/uat-body.txt 2>/dev/null)
    echo "$code"
    echo "$body"
}

# ----------- Assertion -----------
# Usage: assert_status <expected_code> <actual_code> <test_name>
assert_status() {
    local expected="$1"
    local actual="$2"
    local name="$3"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    if [[ "$actual" == "$expected" ]]; then
        log_pass "$name (HTTP $actual)"
        echo "{\"test\":\"$name\",\"status\":\"PASS\",\"http\":$actual}" >> "$RAW_RESULTS"
        return 0
    else
        log_fail "$name (expected $expected, got $actual)"
        echo "{\"test\":\"$name\",\"status\":\"FAIL\",\"expected\":$expected,\"actual\":$actual}" >> "$RAW_RESULTS"
        return 1
    fi
}

# Usage: assert_contains <haystack> <needle> <test_name>
assert_contains() {
    local haystack="$1"
    local needle="$2"
    local name="$3"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    if [[ "$haystack" == *"$needle"* ]]; then
        log_pass "$name (contains '$needle')"
        echo "{\"test\":\"$name\",\"status\":\"PASS\",\"check\":\"contains:$needle\"}" >> "$RAW_RESULTS"
        return 0
    else
        log_fail "$name (missing '$needle')"
        echo "{\"test\":\"$name\",\"status\":\"FAIL\",\"check\":\"contains:$needle\"}" >> "$RAW_RESULTS"
        return 1
    fi
}

# Usage: assert_json_field <body> <jq_path> <expected> <test_name>
assert_json_field() {
    local body="$1"
    local path="$2"
    local expected="$3"
    local name="$4"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    local actual
    actual=$(echo "$body" | jq -r "$path" 2>/dev/null)
    if [[ "$actual" == "$expected" ]]; then
        log_pass "$name ($path = '$expected')"
        echo "{\"test\":\"$name\",\"status\":\"PASS\",\"check\":\"$path=$expected\"}" >> "$RAW_RESULTS"
        return 0
    else
        log_fail "$name ($path expected '$expected', got '$actual')"
        echo "{\"test\":\"$name\",\"status\":\"FAIL\",\"check\":\"$path=$expected\",\"actual\":\"$actual\"}" >> "$RAW_RESULTS"
        return 1
    fi
}

# Usage: assert_array_min_length <body> <jq_path_to_array> <min_length> <test_name>
# Example: assert_array_min_length "$body" ".data" 1 "list-has-results"
assert_array_min_length() {
    local body="$1"
    local path="$2"
    local min="$3"
    local name="$4"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    local len
    len=$(echo "$body" | jq -r "$path | length" 2>/dev/null)
    if [[ -z "$len" || "$len" == "null" ]]; then
        log_fail "$name (path $path not an array or missing)"
        echo "{\"test\":\"$name\",\"status\":\"FAIL\",\"check\":\"array-min:$min\",\"reason\":\"path-missing\"}" >> "$RAW_RESULTS"
        return 1
    fi
    if [[ "$len" -ge "$min" ]]; then
        log_pass "$name (array length=$len >= $min)"
        echo "{\"test\":\"$name\",\"status\":\"PASS\",\"check\":\"array-min:$min\",\"actual\":$len}" >> "$RAW_RESULTS"
        return 0
    else
        log_fail "$name (array length=$len < $min)"
        echo "{\"test\":\"$name\",\"status\":\"FAIL\",\"check\":\"array-min:$min\",\"actual\":$len}" >> "$RAW_RESULTS"
        return 1
    fi
}

# Usage: assert_not_empty <body> <jq_path> <test_name>
# Asserts a field exists and is non-null/non-empty
assert_not_empty() {
    local body="$1"
    local path="$2"
    local name="$3"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    local actual
    actual=$(echo "$body" | jq -r "$path" 2>/dev/null)
    if [[ -n "$actual" && "$actual" != "null" && "$actual" != "" ]]; then
        log_pass "$name ($path = '$actual')"
        echo "{\"test\":\"$name\",\"status\":\"PASS\",\"check\":\"not-empty:$path\"}" >> "$RAW_RESULTS"
        return 0
    else
        log_fail "$name ($path empty or null)"
        echo "{\"test\":\"$name\",\"status\":\"FAIL\",\"check\":\"not-empty:$path\"}" >> "$RAW_RESULTS"
        return 1
    fi
}

# ----------- JSON Extract Helper -----------
# Usage: json_get <body> <jq_path>
json_get() {
    echo "$1" | jq -r "$2" 2>/dev/null
}

# ----------- Capture Helper -----------
# Usage: capture <var_name> <body> <jq_path>
capture() {
    local var="$1"
    local body="$2"
    local path="$3"
    local value
    value=$(json_get "$body" "$path")
    if [[ -n "$value" && "$value" != "null" ]]; then
        export "$var"="$value"
        log_info "Captured $var=$value"
    else
        log_warn "Failed to capture $var (path=$path)"
    fi
}

# ----------- Pre-flight -----------
preflight() {
    log_info "Pre-flight check: $GATEWAY"
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$GATEWAY/actuator/health" 2>/dev/null || echo "000")
    if [[ "$code" != "200" ]]; then
        log_fail "Gateway not reachable: HTTP $code"
        log_warn "Run: scripts/run-local.bat (or docker compose up)"
        return 1
    fi
    log_pass "Gateway UP"
    return 0
}

# ----------- Summary -----------
print_summary() {
    echo ""
    echo "=========================================="
    echo "  TEST SUMMARY"
    echo "=========================================="
    echo -e "Total:  $TOTAL_COUNT"
    echo -e "Passed: ${GREEN}$PASS_COUNT${NC}"
    echo -e "Failed: ${RED}$FAIL_COUNT${NC}"
    if [[ $TOTAL_COUNT -gt 0 ]]; then
        local pct=$((PASS_COUNT * 100 / TOTAL_COUNT))
        echo "Pass rate: ${pct}%"
    fi
    echo "Results: $RAW_RESULTS"
    echo "=========================================="
}

# Trap to ensure summary is printed
trap print_summary EXIT