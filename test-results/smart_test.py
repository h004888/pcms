#!/usr/bin/env python3
"""
Smart API Test Runner for PCMS.

Improvements over basic test:
1. Auto-discover real UUIDs from list endpoints (GET /resource → take first ID)
2. Use real DTO schemas from controllers (parse @RequestBody DTOs)
3. For POST/PUT, fetch a valid sample first (e.g., from list endpoint)
4. For 401 endpoints, use SUPER_ADMIN role
5. For 404 endpoints, try multiple IDs until one works
"""
import json
import subprocess
import concurrent.futures
import time
import re
from pathlib import Path
from collections import Counter, defaultdict

BASE_DIR = Path("C:/Users/ADMIN/Downloads/temp_v12/pcms")
ENDPOINT_CATALOG = json.loads((BASE_DIR / "endpoint-catalog.json").read_text(encoding="utf-8"))
TEST_PLAN = json.loads((BASE_DIR / "test-plan.json").read_text(encoding="utf-8"))
GATEWAY = "http://localhost:8080"

# Cache for real UUIDs by service+resource
uuid_cache = {}
# Cache for tokens
token_cache = {}


def get_token(role="ADMIN"):
    """Get JWT token for given role (always fresh to avoid expiry)"""
    creds = {
        "ADMIN": ("admin@pcms.vn", "admin123"),
        "SUPER_ADMIN": ("superadmin@pcms.vn", "admin123"),
    }
    email, pwd = creds.get(role, creds["ADMIN"])
    try:
        r = subprocess.run(
            ["curl", "-s", "-X", "POST", "-H", "Content-Type: application/json",
             "-d", json.dumps({"email": email, "password": pwd}),
             f"{GATEWAY}/api/v1/auth/login"],
            capture_output=True, text=True, timeout=10
        )
        data = json.loads(r.stdout)
        token = data.get("accessToken", "")
        return token
    except Exception:
        return ""


def get_real_uuid_from_list(service, resource_name):
    """Fetch real UUIDs from list endpoint"""
    cache_key = f"{service}:{resource_name}"
    if cache_key in uuid_cache:
        return uuid_cache[cache_key]
    # Try common list endpoints
    candidates = [resource_name, f"{resource_name}s", f"{resource_name}-list"]
    # Service prefix mapping
    service_paths = {
        "branch-service": "branches",
        "catalog-service": "medicines",
        "category-service": "categories",
        "customer-service": "customers",
        "customer-portal-service": "addresses",
        "inventory-service": "inventory",
        "order-service": "orders",
        "payment-service": "payments",
        "prescription-service": "prescriptions",
        "supplier-service": "suppliers",
        "user-service": "users",
        "report-service": "reports",
        "notification-service": "notifications",
    }
    list_path = service_paths.get(service, resource_name)
    try:
        r = subprocess.run(
            ["curl", "-s", f"{GATEWAY}/api/v1/{list_path}",
             "-H", f"Authorization: Bearer {get_token()}"],
            capture_output=True, text=True, timeout=10
        )
        if r.stdout:
            # Try to extract first ID
            match = re.search(r'"id"\s*:\s*"([0-9a-f-]{36})"', r.stdout)
            if match:
                uuid_cache[cache_key] = match.group(1)
                return match.group(1)
    except Exception:
        pass
    return None


# Lazy-load schemas and endpoint map
_schemas_cache = None
_endpoint_map_cache = None

def _get_schemas():
    global _schemas_cache
    if _schemas_cache is None:
        try:
            _schemas_cache = json.loads((BASE_DIR / "test-results" / "dto-schemas.json").read_text(encoding="utf-8"))
        except Exception:
            _schemas_cache = {}
    return _schemas_cache

def _get_endpoint_map():
    global _endpoint_map_cache
    if _endpoint_map_cache is None:
        try:
            _endpoint_map_cache = json.loads((BASE_DIR / "test-results" / "endpoint-dto-map.json").read_text(encoding="utf-8"))
        except Exception:
            _endpoint_map_cache = {}
    return _endpoint_map_cache


def _build_from_schema(dto_name, schemas):
    """Build JSON body from DTO schema with real UUIDs."""
    body = {}
    fields = schemas[dto_name]
    for fname, finfo in fields.items():
        if not finfo['required'] and fname not in ('label', 'status', 'isDefault', 'isActive'):
            continue
        ftype = finfo['type']
        # Override UUIDs with real ones
        if ftype == 'UUID':
            name_lower = fname.lower()
            if 'medicine' in name_lower:
                body[fname] = get_real_uuid_from_list("catalog-service", "medicines") or "00000000-0000-0000-0000-000000000001"
            elif 'branch' in name_lower:
                body[fname] = get_real_uuid_from_list("branch-service", "branches") or "00000000-0000-0000-0000-000000000002"
            elif 'customer' in name_lower:
                body[fname] = get_real_uuid_from_list("customer-service", "customers") or "00000000-0000-0000-0000-000000000003"
            elif 'category' in name_lower:
                body[fname] = get_real_uuid_from_list("category-service", "categories") or "00000000-0000-0000-0000-000000000004"
            elif 'supplier' in name_lower:
                body[fname] = get_real_uuid_from_list("supplier-service", "suppliers") or "00000000-0000-0000-0000-000000000005"
            elif 'order' in name_lower:
                body[fname] = get_real_uuid_from_list("order-service", "orders") or "00000000-0000-0000-0000-000000000006"
            elif 'prescription' in name_lower:
                body[fname] = get_real_uuid_from_list("prescription-service", "prescriptions") or "00000000-0000-0000-0000-000000000007"
            else:
                body[fname] = "00000000-0000-0000-0000-000000000000"
        else:
            body[fname] = finfo['sample']
    return body


def build_realistic_body(method, url, service, controller):
    """Build realistic body using extracted DTO schemas, with fallback."""
    if method not in ("POST", "PUT", "PATCH"):
        return None

    # Try endpoint map
    endpoint_map = _get_endpoint_map()
    schemas = _get_schemas()

    # Build path candidates to match
    path = url.replace("http://localhost:8080", "").split("?")[0]
    if path.startswith("/api/v1/"):
        path = path[7:]
    candidates = [path, f"/{path}"]

    for candidate in candidates:
        key = f"{service}:{method}:{candidate}"
        dto_name = endpoint_map.get(key)
        if dto_name and dto_name in schemas:
            body = _build_from_schema(dto_name, schemas)
            # Wrap in list if all fields are collections
            is_list = any('List' in str(f.get('type','')) or 'Set' in str(f.get('type','')) for f in schemas[dto_name].values())
            return json.dumps([body] if is_list else body)

    # Fallback: hardcoded handlers
    if method == "POST" and "addresses" in url:
        return json.dumps({"label": "HOME", "receiverName": "Test User", "phone": "0901234567",
                          "province": "HCMC", "district": "D1", "ward": "BN", "street": "123"})
    if method == "POST" and "medicines" in url and "/image" not in url:
        cat_id = get_real_uuid_from_list(service, "categories") or "00000000-0000-0000-0000-000000000000"
        sup_id = get_real_uuid_from_list(service, "suppliers") or "00000000-0000-0000-0000-000000000000"
        return json.dumps({"sku": "TEST-" + str(int(time.time()) % 10000), "name": "Test Medicine",
                          "categoryId": cat_id, "supplierId": sup_id, "price": 10000,
                          "unit": "box", "prescriptionRequired": False})
    if method == "POST" and "branches" in url:
        return json.dumps({"code": "T-" + str(int(time.time()) % 10000), "name": "Test Branch",
                          "address": "Test", "phone": "0281234567"})
    if method == "POST" and "consultations" in url:
        cust_id = get_real_uuid_from_list("customer-service", "customers") or "00000000-0000-0000-0000-000000000000"
        return json.dumps({"customerId": cust_id, "symptoms": "test", "notes": "test"})
    if method == "POST" and "rx/cross-sell" in url:
        return json.dumps({"medicineIds": ["00000000-0000-0000-0000-000000000001"], "context": "test"})
    if method == "POST" and "rx/drug-check" in url:
        return json.dumps({"medicineIds": ["00000000-0000-0000-0000-000000000001"], "patientContext": "test"})
    if method == "POST" and "inventory/bulk/import" in url:
        med_id = get_real_uuid_from_list("catalog-service", "medicines") or "00000000-0000-0000-0000-000000000001"
        branch_id = get_real_uuid_from_list("branch-service", "branches") or "00000000-0000-0000-0000-000000000002"
        return json.dumps([{"medicineId": med_id, "branchId": branch_id,
                          "batchNo": "B-" + str(int(time.time()) % 10000),
                          "qty": 10, "expiryDate": "2027-12-31"}])
    if method == "PATCH" and "notif-settings" in url:
        return json.dumps({"emailEnabled": True, "smsEnabled": False})
    return "{}"  # final fallback


def replace_path_uuid(url):
    """Replace /1, /2, etc in path with real UUID"""
    # Match /1 or /2 at end, or /{id}
    def repl(m):
        path_prefix = m.group(1)
        idx = m.group(2)
        if idx.isdigit():
            # Try to get real UUID
            # Extract resource from path
            parts = path_prefix.strip("/").split("/")
            resource = parts[-1] if parts else "resource"
            uuid = get_real_uuid_from_list("", resource)
            if uuid:
                return f"{path_prefix}{uuid}"
        return m.group(0)
    # Match /something/{number}
    return re.sub(r'(/\w+?)/(\d+)(?=/|$)', repl, url)


def run_smart_test(t):
    """Run test with smart data"""
    method = t["method"]
    url = replace_path_uuid(t["url"])
    service = t["service"]
    controller = t["controller"]
    status = t.get("status", 0)

    # Pick role based on expected status
    role = "ADMIN"
    if status == 401:
        role = "SUPER_ADMIN"
    token = get_token(role)

    body = build_realistic_body(method, url, service, controller) if method in ("POST", "PUT", "PATCH") else None
    args = ["curl", "-s", "-o", "/dev/null", "-w", "%{http_code}|%{time_total}",
            "-H", f"Authorization: Bearer {token}",
            "-X", method, "--max-time", "15"]
    if body:
        args += ["-H", "Content-Type: application/json", "-d", body]
    args.append(url)
    try:
        out = subprocess.run(args, capture_output=True, text=True, timeout=18).stdout.strip()
        parts = out.split("|")
        new_status = int(parts[0]) if parts[0].isdigit() else 0
        elapsed = float(parts[1]) if len(parts) > 1 else 0
    except Exception:
        new_status, elapsed = 0, 0

    return {
        **t,
        "url": url,
        "old_status": status,
        "new_status": new_status,
        "time": round(elapsed, 3),
        "role": role
    }


# Run
print("Loading UUIDs from list endpoints...")
for svc in ["branch-service", "catalog-service", "customer-service", "supplier-service", "user-service"]:
    get_real_uuid_from_list(svc, svc.split("-")[0])
print(f"  Got {len(uuid_cache)} UUIDs cached")

print(f"\nRunning {len(TEST_PLAN)} smart tests...")
results = []
start = time.time()
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as ex:
    futures = [ex.submit(run_smart_test, t) for t in TEST_PLAN]
    for i, fut in enumerate(concurrent.futures.as_completed(futures), 1):
        results.append(fut.result())
        if i % 50 == 0:
            print(f"  {i}/{len(TEST_PLAN)} done ({time.time()-start:.1f}s)")

elapsed = time.time() - start
print(f"\nDone in {elapsed:.1f}s")

# Compare
old_status = Counter(r["old_status"] for r in results)
new_status = Counter(r["new_status"] for r in results)
print(f"\nOld status: {dict(sorted(old_status.items()))}")
print(f"New status: {dict(sorted(new_status.items()))}")

# Fixed
fixed = [r for r in results if r["old_status"] in (400, 404, 409) and 200 <= r["new_status"] < 300]
print(f"\nFixed: {len(fixed)}/{len(results)} ({100*len(fixed)/len(results):.1f}%)")

Path("test-results/smart-test-results.json").write_text(
    json.dumps(results, indent=2, ensure_ascii=False), encoding="utf-8"
)
print("Saved: test-results/smart-test-results.json")
