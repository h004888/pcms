#!/usr/bin/env python3
"""Extract JSON schema from Java DTO files using regex-based parsing.

Scans all *Request.java files in services' dto/request/ packages,
parses @NotNull/@NotBlank/@Size constraints, generates minimal
valid JSON schema for each DTO class.
"""
import re
import json
from pathlib import Path

BASE_DIR = Path("C:/Users/ADMIN/Downloads/temp_v12/pcms")


def get_sample_value(ftype, fname):
    """Generate sample value based on type and field name heuristics."""
    name_lower = fname.lower()
    if 'email' in name_lower:
        return 'test@pcms.vn'
    if 'phone' in name_lower:
        return '0901234567'
    if 'name' in name_lower and 'user' in name_lower:
        return 'Test User'
    if ftype == 'UUID':
        return '00000000-0000-0000-0000-000000000001'
    if ftype in ('String',):
        return f'test-{fname}'
    if ftype in ('int', 'Integer', 'Long', 'long'):
        return 1
    if ftype in ('double', 'Double', 'float', 'Float', 'BigDecimal'):
        return 100.0
    if ftype in ('boolean', 'Boolean'):
        return False
    if ftype in ('LocalDate',):
        return '2027-12-31'
    if ftype in ('LocalDateTime',):
        return '2027-12-31T00:00:00'
    if 'List' in ftype or 'Set' in ftype or 'Collection' in ftype:
        return []
    if 'Map' in ftype:
        return {}
    return None


def parse_dto(filepath):
    """Parse a single DTO file, return dict of field_name -> {type, required, sample}"""
    content = filepath.read_text(encoding='utf-8', errors='ignore')
    fields = {}
    # Strip line comments
    content = re.sub(r'//.*$', '', content, flags=re.MULTILINE)
    # Match: optional @Annotation(...) \n @Annotation2(...) \n Type fieldName;
    # Handle multi-line annotations
    pattern = r'(@\w+(?:\([^)]*\))?\s*)?(\w+(?:<[^>]+>)?)\s+(\w+)\s*[;,]'
    for m in re.finditer(pattern, content):
        annotation, ftype, fname = m.groups()
        # Skip Java keywords
        if fname in ('class', 'record', 'public', 'private', 'static', 'final', 'return', 'if', 'for', 'while'):
            continue
        if ftype in ('class', 'interface', 'enum', 'record'):
            continue
        # Skip methods (have parentheses)
        required = annotation and ('NotNull' in annotation or 'NotBlank' in annotation or 'NotEmpty' in annotation)
        # Skip if it's actually a method declaration (parentheses missing)
        fields[fname] = {
            'type': ftype,
            'required': bool(required),
            'sample': get_sample_value(ftype, fname)
        }
    return fields


# Scan all DTOs
schemas = {}
scanned_files = 0
for svc_dir in BASE_DIR.iterdir():
    if not svc_dir.is_dir() or '-' not in svc_dir.name:
        continue
    dto_dir = svc_dir / 'src' / 'main' / 'java'
    if not dto_dir.exists():
        continue
    for dto_file in dto_dir.rglob('*Request.java'):
        if 'target' in dto_file.parts:
            continue
        class_name = dto_file.stem
        try:
            fields = parse_dto(dto_file)
            if fields:
                schemas[class_name] = fields
                scanned_files += 1
        except Exception as e:
            print(f"  Warning: failed to parse {dto_file}: {e}")

# Save
out_dir = BASE_DIR / 'test-results'
out_dir.mkdir(parents=True, exist_ok=True)
out = out_dir / 'dto-schemas.json'
out.write_text(json.dumps(schemas, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Extracted {len(schemas)} DTO schemas from {scanned_files} files to {out}')
