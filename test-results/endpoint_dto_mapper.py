#!/usr/bin/env python3
"""Map (service, http_method, path) → DTO class name by parsing controllers."""
import re
import json
from pathlib import Path

BASE_DIR = Path("C:/Users/ADMIN/Downloads/temp_v12/pcms")


def parse_controller(filepath):
    """Extract endpoint → DTO mappings from controller file.

    Returns list of dicts: {http_method, path, method_name, dto}
    """
    content = filepath.read_text(encoding='utf-8', errors='ignore')
    mappings = []

    # Find each method that has @RequestBody
    # Pattern: @PostMapping("path") public X method(@RequestBody DtoName req, ...)
    # Or: @PostMapping public X method(@RequestBody DtoName req, ...)
    # Use DOTALL to match across newlines
    method_pattern = re.compile(
        r'@(Post|Put|Patch)Mapping\s*'
        r'(?:\(\s*(?:value\s*=\s*)?"([^"]*)"[^)]*\))?\s*'
        r'(?:\@\w+(?:\([^)]*\))?\s+)*'  # Other annotations
        r'public\s+[\w<>,\s]+?\s+'  # Return type
        r'(\w+)\s*\('  # Method name
        r'[^)]*?'  # Params start
        r'@RequestBody\s+(?:@\w+(?:\([^)]*\))?\s+)*(\w+)\s+\w+',  # DTO type and param name
        re.DOTALL
    )

    for m in method_pattern.finditer(content):
        http_method, path, method_name, dto_name = m.groups()
        # Normalize method
        method = http_method.upper()
        if not path:
            path = ''  # empty path = use class-level @RequestMapping
        mappings.append({
            'http_method': method,
            'path': path,
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
                endpoint_map[key] = m['dto']
        except Exception as e:
            print(f"  Warning: failed to parse {ctrl_file}: {e}")

out_dir = BASE_DIR / 'test-results'
out = out_dir / 'endpoint-dto-map.json'
out.write_text(json.dumps(endpoint_map, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Mapped {len(endpoint_map)} endpoints to DTOs (scanned {scanned} controllers)')

# Show some samples
sample = list(endpoint_map.items())[:10]
print('\nSample mappings:')
for k, v in sample:
    print(f'  {k} -> {v}')
