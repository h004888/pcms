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


# Service prefix mapping
SERVICE_PATHS = {
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
    "ecom-service": "products",
    "health-service": "records",
    "mobile-service": "devices",
    "pharmacist-mobile-service": "tasks",
    "pharmacist-workbench-service": "consultations",
}


def get_all_uuids_from_list(service, resource_name, limit=10):
    """Fetch multiple UUIDs from list endpoint. Returns list of UUIDs (may be empty)."""
    cache_key = f"{service}:{resource_name}:all"
    if cache_key in uuid_cache:
        return uuid_cache[cache_key]
    list_path = SERVICE_PATHS.get(service, resource_name)
    candidate_paths = [list_path, resource_name, f"{resource_name}s"]
    seen = set()
    for lp in candidate_paths:
        if lp in seen or not lp:
            continue
        seen.add(lp)
        try:
            r = subprocess.run(
                ["curl", "-s", f"{GATEWAY}/api/v1/{lp}",
                 "-H", f"Authorization: Bearer {get_token()}"],
                capture_output=True, text=True, timeout=10,
                encoding="utf-8", errors="replace"
            )
            if r.stdout and len(r.stdout) > 20:
                ids = re.findall(
                    r'"id"\s*:\s*"([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"',
                    r.stdout,
                )
                if ids:
                    # Deduplicate while preserving order
                    seen_ids = []
                    seen_set = set()
                    for i in ids:
                        if i not in seen_set:
                            seen_set.add(i)
                            seen_ids.append(i)
                    result = seen_ids[:limit]
                    uuid_cache[cache_key] = result
                    return result
        except Exception:
            pass
    uuid_cache[cache_key] = []
    return []


def get_real_uuid_from_list(service, resource_name):
    """Fetch a real UUID from list endpoint (back-compat). Returns one UUID or None."""
    cache_key = f"{service}:{resource_name}"
    if cache_key in uuid_cache:
        return uuid_cache[cache_key]
    ids = get_all_uuids_from_list(service, resource_name)
    if ids:
        # Pick first for back-compat path
        uuid_cache[cache_key] = ids[0]
        return ids[0]
    uuid_cache[cache_key] = None
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


def _normalize_endpoint_key(url, service, method):
    """Build all candidate key formats for endpoint-dto-map lookup.

    The endpoint-dto-map.json stores keys in the form:
        service:METHOD:<path>
    where <path> is what comes AFTER /api/v1/<resource> segment, e.g.:
        POST /api/v1/branches         -> "branch-service:POST:"           (empty)
        PUT /api/v1/branches/{id}     -> "branch-service:PUT:/{id}"
        PUT /api/v1/branches/{id}/mgr -> "branch-service:PUT:/{id}/manager"  (approx)
    """
    # Strip host and query
    path = url.replace("http://localhost:8080", "").split("?")[0].rstrip("/")
    # Strip /api/v1/ prefix
    if path.startswith("/api/v1/"):
        path = path[7:]
    elif path.startswith("api/v1/"):
        path = path[6:]
    if path.startswith("/"):
        path = path[1:]

    # Replace numeric IDs and UUID-like tokens with {id}
    path = re.sub(r'/\d+(?=/|$)', '/{id}', path)
    path = re.sub(r'/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(?=/|$)',
                  '/{id}', path)

    # Split into segments and build candidates that mirror the map's format
    segments = path.split("/") if path else []
    candidates = []
    # Variant A: drop the first segment (resource name) — keys store what's after it
    if len(segments) >= 2:
        tail = "/".join(segments[1:])
        candidates.append(f"/{tail}")
        if len(segments) >= 3:
            # deeper: e.g., "{id}/manager"
            candidates.append(f"/{tail}")
    # Variant B: full normalized path with leading slash
    if path:
        candidates.append(f"/{path}")
    # Variant C: just the full path (no leading slash)
    if path:
        candidates.append(path)
    # Variant D: empty string (collection root: POST /api/v1/branches -> "")
    candidates.append("")
    # De-dupe while preserving order
    seen = set()
    out = []
    for c in candidates:
        if c not in seen:
            seen.add(c)
            out.append(c)
    return out


def _fuzzy_lookup_endpoint(endpoint_map, service, method, candidates):
    """Try exact match, then fuzzy by service+method, then path-suffix."""
    # 1. Exact candidate match
    for c in candidates:
        key = f"{service}:{method}:{c}"
        if key in endpoint_map:
            return endpoint_map[key]
    # 2. Fuzzy: same service+method, longest path-suffix match
    prefix = f"{service}:{method}:"
    best_key = None
    best_len = -1
    for k, v in endpoint_map.items():
        if not k.startswith(prefix):
            continue
        kpath = k[len(prefix):]
        for c in candidates:
            if not c:
                continue
            if c.endswith(kpath) or kpath.endswith(c):
                if len(kpath) > best_len:
                    best_len = len(kpath)
                    best_key = v
                    break
    if best_key:
        return best_key
    # 3. Fallback: empty-path key for this service+method (collection endpoint)
    empty_key = f"{service}:{method}:"
    if empty_key in endpoint_map:
        return endpoint_map[empty_key]
    return None


def _cross_service_lookup(endpoint_map, service, method, url):
    """Last-resort: try to find a similar endpoint from any service.

    Used when the target service has no entry in the map. E.g., 'addresses'
    (customer-portal-service) POST can borrow from 'customers' (customer-service).
    """
    path = url.replace("http://localhost:8080", "").split("?")[0].rstrip("/")
    if path.startswith("/api/v1/"):
        path = path[7:]
    elif path.startswith("api/v1/"):
        path = path[6:]
    if path.startswith("/"):
        path = path[1:]
    # Take last meaningful token (the resource/action name)
    parts = path.split("/")
    leaf = parts[-1] if parts else ""
    if not leaf:
        return None
    # Search across all services for an endpoint with matching leaf
    for k, v in endpoint_map.items():
        if not k.endswith(":" + leaf) and not k.endswith("/" + leaf):
            continue
        # Match by method
        parts2 = k.split(":", 2)
        if len(parts2) >= 2 and parts2[1] == method:
            return v
    return None


def build_realistic_body(method, url, service, controller):
    """Build realistic body using extracted DTO schemas, with fallback."""
    if method not in ("POST", "PUT", "PATCH"):
        return None

    endpoint_map = _get_endpoint_map()
    schemas = _get_schemas()

    candidates = _normalize_endpoint_key(url, service, method)
    dto_name = _fuzzy_lookup_endpoint(endpoint_map, service, method, candidates)
    if not dto_name:
        # Last-resort: cross-service leaf match
        dto_name = _cross_service_lookup(endpoint_map, service, method, url)

    if dto_name and dto_name in schemas:
        body = _build_from_schema(dto_name, schemas)
        is_list = any('List' in str(f.get('type', '')) or 'Set' in str(f.get('type', ''))
                      for f in schemas[dto_name].values())
        return json.dumps([body] if is_list else body)

    # String raw body endpoints (e.g., POST /{id}/messages)
    if dto_name == "String":
        return json.dumps("test message")

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
    """Replace /1, /2 etc. and any existing placeholder with real UUIDs.

    Fixes the previous bug where the regex concatenated prefix+id (e.g.,
    'medicines/1' became 'medicines11f16edf...'). Now we replace the numeric
    segment with a real UUID fetched from the matching service's list endpoint.

    Uses the multi-UUID cache and rotates through IDs so callers that retry
    get a different ID on each attempt.
    """
    # Identify the service by inspecting the URL path
    service_for_url = None
    path = url.replace("http://localhost:8080", "").split("?")[0].lstrip("/")
    if path.startswith("api/v1/"):
        path = path[7:]
    seg0 = path.split("/")[0] if path else ""

    # Map URL resource segment -> service name
    RESOURCE_TO_SERVICE = {
        "branches": "branch-service",
        "medicines": "catalog-service",
        "categories": "category-service",
        "customers": "customer-service",
        "suppliers": "supplier-service",
        "users": "user-service",
        "orders": "order-service",
        "payments": "payment-service",
        "prescriptions": "prescription-service",
        "notifications": "notification-service",
        "addresses": "customer-portal-service",
        "cart": "customer-portal-service",
        "family": "customer-portal-service",
        "favorites": "customer-portal-service",
        "inventory": "inventory-service",
        "consultations": "pharmacist-workbench-service",
    }
    # Suffix-based fallback for nested-resource URLs (e.g., /{id}/manager, /{id}/staff)
    SVC_HINT_SUFFIX = {
        "manager": "branch-service",
        "staff": "branch-service",
        "image": "catalog-service",
        "default": "customer-portal-service",
        "messages": "pharmacist-workbench-service",
        "role": "user-service",
        "status": "user-service",
        "branch": "user-service",
    }
    service_for_url = RESOURCE_TO_SERVICE.get(seg0)
    if not service_for_url:
        # Try to detect service from second segment
        parts = path.split("/")
        if len(parts) >= 2:
            service_for_url = SVC_HINT_SUFFIX.get(parts[1])

    def repl(m):
        prefix = m.group(1)  # path prefix WITHOUT the digit
        if service_for_url:
            # Resource name = first segment of the URL
            resource = seg0 or "resource"
            ids = get_all_uuids_from_list(service_for_url, resource)
            if ids:
                # Round-robin through available IDs
                idx_key = f"{service_for_url}:{resource}:rr"
                idx = uuid_cache.get(idx_key, 0)
                uuid = ids[idx % len(ids)]
                uuid_cache[idx_key] = idx + 1
                return f"{prefix}{uuid}"
        # Fallback: replace digit with a fixed UUID
        return f"{prefix}00000000-0000-0000-0000-000000000000"

    # Replace /<digits> at end or before another /
    url = re.sub(r'(/)(\d+)(?=/|$)', repl, url)
    return url


def run_smart_test(t):
    """Run test with smart data. If first attempt returns 404 due to stale UUID,
    retry with a different UUID from the cached pool (up to MAX_404_RETRIES)."""
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

    def _exec(u):
        args = ["curl", "-s", "-o", "/dev/null", "-w", "%{http_code}|%{time_total}",
                "-H", f"Authorization: Bearer {token}",
                "-X", method, "--max-time", "15"]
        if body:
            args += ["-H", "Content-Type: application/json", "-d", body]
        args.append(u)
        try:
            out = subprocess.run(args, capture_output=True, text=True, timeout=18).stdout.strip()
            parts = out.split("|")
            ns = int(parts[0]) if parts[0].isdigit() else 0
            el = float(parts[1]) if len(parts) > 1 else 0
            return ns, el
        except Exception:
            return 0, 0

    new_status, elapsed = _exec(url)
    # On 404, retry up to N times with regenerated UUIDs (skip write methods)
    MAX_404_RETRIES = 3
    attempts = 0
    while new_status == 404 and attempts < MAX_404_RETRIES and method in ("GET", "DELETE"):
        attempts += 1
        url = replace_path_uuid(t["url"])  # rotates to next UUID via round-robin
        new_status, elapsed = _exec(url)

    return {
        **t,
        "url": url,
        "old_status": status,
        "new_status": new_status,
        "time": round(elapsed, 3),
        "role": role,
        "uuid_retries": attempts,
    }


# Run
print("Loading UUIDs from list endpoints...")
WARMUP_SERVICES = [
    ("branch-service", "branches"),
    ("catalog-service", "medicines"),
    ("category-service", "categories"),
    ("customer-service", "customers"),
    ("customer-portal-service", "addresses"),
    ("inventory-service", "inventory"),
    ("order-service", "orders"),
    ("payment-service", "payments"),
    ("prescription-service", "prescriptions"),
    ("supplier-service", "suppliers"),
    ("user-service", "users"),
    ("report-service", "reports"),
    ("notification-service", "notifications"),
    ("pharmacist-workbench-service", "consultations"),
]
# Sequential warmup — avoids auth race conditions
total_uuids = 0
for svc, res in WARMUP_SERVICES:
    ids = get_all_uuids_from_list(svc, res)
    if ids:
        # Back-compat: also populate single-uuid cache
        uuid_cache[f"{svc}:{res}"] = ids[0]
        total_uuids += len(ids)
print(f"  Got {total_uuids} UUIDs across {len(WARMUP_SERVICES)} services")

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
