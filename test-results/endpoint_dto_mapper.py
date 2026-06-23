#!/usr/bin/env python3
"""Map (service, http_method, path) -> DTO class name by parsing controllers.

Improved version: handles multi-line annotations, @RequestPart multipart,
class-level @RequestMapping, Swagger @Operation, and various real-world
Spring controller patterns.
"""
import re
import json
from pathlib import Path

BASE_DIR = Path("C:/Users/ADMIN/Downloads/temp_v12/pcms")


def normalize_path(path: str) -> str:
    """Normalize path: collapse slashes, strip /api/v1 prefix."""
    if not path:
        return ''
    path = path.strip()
    # Strip /api/v1 prefix variants
    for prefix in ('/api/v1/', '/api/v1', '/api/'):
        if path.startswith(prefix):
            path = path[len(prefix):]
            break
    # Collapse repeated slashes
    path = re.sub(r'/+', '/', path)
    # Ensure starts with /
    if not path.startswith('/'):
        path = '/' + path
    return path


def join_paths(class_path: str, method_path: str) -> str:
    """Join class-level and method-level paths properly."""
    class_path = (class_path or '').strip()
    method_path = (method_path or '').strip()
    if not class_path:
        return normalize_path(method_path)
    if not method_path:
        return normalize_path(class_path)
    cp = class_path.rstrip('/')
    mp = method_path if method_path.startswith('/') else '/' + method_path
    return normalize_path(cp + mp)


def extract_class_request_mapping(content: str) -> str:
    """Find class-level @RequestMapping path."""
    # Look in first ~2KB for the class-level annotation
    head = content[:3000]
    m = re.search(r'@RequestMapping\s*\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']', head)
    if m:
        return m.group(1)
    return ''


def extract_op_path(content: str) -> str:
    """Find @Operation annotation path (Swagger, used in some controllers)."""
    m = re.search(r'@Operation\s*\([^)]*summary\s*=\s*["\'][^"\']*["\']', content)
    return ''  # @Operation doesn't carry path; skip


def parse_controller(filepath):
    """Extract endpoint -> DTO mappings from controller file."""
    content = filepath.read_text(encoding='utf-8', errors='ignore')
    mappings = []
    class_path = extract_class_request_mapping(content)

    # Pattern strategy: find each method annotation block, then walk forward
    # to find public signature and @RequestBody DTO.
    # Annotation: @PostMapping / @PutMapping / @PatchMapping (with optional path args)
    method_ann_pattern = re.compile(
        r'@(Post|Put|Patch)Mapping\s*'
        r'(\([^)]*\))?',  # optional args (may contain path or consumes only)
        re.DOTALL
    )

    # Find signature after annotation: public ... methodName( ... @RequestBody DTO name
    sig_pattern = re.compile(
        r'public\s+[\w<>,\s\[\]]+?\s+'           # return type
        r'(\w+)\s*\('                              # method name
        r'[^;{]*?'                                 # params (lazy)
        r'@RequestBody\s+'                         # @RequestBody
        r'(?:@\w+(?:\([^)]*\))?\s+)*'              # optional inline annotation
        r'(\w+)\s+'                                # DTO type
        r'\w+',                                    # param name
        re.DOTALL
    )

    # @RequestPart (multipart) - also produces a body for schema lookup
    requestpart_pattern = re.compile(
        r'@RequestPart\s*\(\s*["\']\w+["\']\s*\)\s*'
        r'(?:@\w+(?:\([^)]*\))?\s+)*'
        r'(\w+)\s+'                                # DTO type
        r'\w+',
        re.DOTALL
    )

    for ann_m in method_ann_pattern.finditer(content):
        http_method = ann_m.group(1).upper()
        ann_args = ann_m.group(2) or ''

        # Extract path from method annotation args (if present)
        method_path = ''
        # Look for value = "..." or "..." directly
        path_m = re.search(r'(?:value\s*=\s*)?["\']([^"\']+)["\']', ann_args)
        if path_m:
            method_path = path_m.group(1)
        else:
            # @PostMapping with no args - might have multiple paths
            # @PostMapping({"/a", "/b"}) - take first
            arr_m = re.search(r'\{([^}]+)\}', ann_args)
            if arr_m:
                first = re.search(r'["\']([^"\']+)["\']', arr_m.group(1))
                if first:
                    method_path = first.group(1)

        # Determine if this is multipart (consumes only, no body)
        is_multipart_only = 'consumes' in ann_args and 'MULTIPART_FORM_DATA' in ann_args and not path_m

        # Skip pure-multipart (no path, just consumes)
        if is_multipart_only and not method_path:
            continue

        # Search for signature after this annotation, up to next annotation or 1500 chars
        search_region = content[ann_m.end():ann_m.end() + 2000]
        sig_m = sig_pattern.search(search_region)
        if not sig_m:
            continue
        method_name = sig_m.group(1)
        dto_name = sig_m.group(2)

        # Skip java.lang.Map / String / wrapper types for body
        if dto_name in ('Map', 'String', 'Object', 'HttpServletRequest', 'MultipartFile'):
            # Try @RequestPart next (multipart endpoint)
            rp_m = requestpart_pattern.search(search_region)
            if rp_m and rp_m.group(1) not in ('Map', 'String', 'MultipartFile'):
                dto_name = rp_m.group(1)
            else:
                continue

        # Combine class + method paths
        full_path = join_paths(class_path, method_path)

        mappings.append({
            'http_method': http_method,
            'path': full_path,
            'method_name': method_name,
            'dto': dto_name
        })

    return mappings


# Build map
endpoint_map = {}
scanned = 0
for svc_dir in BASE_DIR.iterdir():
    if not svc_dir.is_dir() or '-' not in svc_dir.name:
        continue
    ctrl_dir = svc_dir / 'src' / 'main' / 'java'
    if not ctrl_dir.exists():
        continue
    service_name = svc_dir.name
    for ctrl_file in ctrl_dir.rglob('*Controller.java'):
        if 'target' in ctrl_file.parts:
            continue
        scanned += 1
        try:
            mappings = parse_controller(ctrl_file)
            for m in mappings:
                key = f"{service_name}:{m['http_method']}:{m['path']}"
                # Last wins (later methods override earlier)
                endpoint_map[key] = m['dto']
        except Exception as e:
            print(f"  Warning: failed to parse {ctrl_file}: {e}")

out_dir = BASE_DIR / 'test-results'
out_dir.mkdir(parents=True, exist_ok=True)
out = out_dir / 'endpoint-dto-map.json'
out.write_text(json.dumps(endpoint_map, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Mapped {len(endpoint_map)} endpoints to DTOs (scanned {scanned} controllers)')

# Show some samples
sample = list(endpoint_map.items())[:15]
print('\nSample mappings:')
for k, v in sample:
    print(f'  {k} -> {v}')

# Also show by service
from collections import Counter
svc_counts = Counter(k.split(':')[0] for k in endpoint_map)
print('\nEndpoints by service:')
for svc, cnt in svc_counts.most_common():
    print(f'  {svc}: {cnt}')
