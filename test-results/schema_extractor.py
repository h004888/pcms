#!/usr/bin/env python3
"""Extract JSON schema from Java DTO files using regex-based parsing.

Improved version: handles records with multi-line annotations, complex
generic types (Map, List, nested), inline validation annotations,
and class-based DTOs.
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
    # Generic types
    if ftype.startswith('List<') or ftype.startswith('Set<') or ftype.startswith('Collection<'):
        return []
    if ftype.startswith('Map<'):
        return {}
    if 'Page<' in ftype or 'Wrapper<' in ftype:
        return {}
    return None


def is_required_annotation(ann_text):
    """Check if annotation text indicates a required field."""
    if not ann_text:
        return False
    required_markers = ('NotNull', 'NotBlank', 'NotEmpty')
    return any(m in ann_text for m in required_markers)


def parse_record(content: str):
    """Parse a record body (inside record(...))."""
    fields = {}
    m = re.search(r'public\s+record\s+\w+(?:<[^>]+>)?\s*\((.+?)\)\s*(?:implements[^{]*)?\{',
                  content, re.DOTALL)
    if not m:
        return fields
    body = m.group(1)
    # Split body by commas that are not inside <...> or (...)
    depth = 0
    paren = 0
    parts = []
    cur = []
    for ch in body:
        if ch == '<':
            depth += 1
        elif ch == '>':
            depth -= 1
        elif ch == '(':
            paren += 1
        elif ch == ')':
            paren -= 1
        if ch == ',' and depth == 0 and paren == 0:
            parts.append(''.join(cur).strip())
            cur = []
        else:
            cur.append(ch)
    last = ''.join(cur).strip()
    if last:
        parts.append(last)

    for part in parts:
        if not part:
            continue
        # Strip trailing comma if any
        part = part.rstrip(',').strip()
        if not part:
            continue
        # Extract @Annotations (may span multiple lines, joined)
        annotations = re.findall(r'@\w+(?:\([^)]*\))?', part, re.DOTALL)
        ann_text = ' '.join(annotations)
        # Remove annotations from the part
        remainder = re.sub(r'@\w+(?:\([^)]*\))?', '', part, flags=re.DOTALL).strip()
        # Field pattern: Type fieldName (for records: no trailing ;)
        # Allow multi-word types and generic types
        fm = re.search(r'([A-Za-z_][\w<>,\s\[\].]*?)\s+(\w+)\s*$', remainder, re.DOTALL)
        if not fm:
            continue
        ftype = fm.group(1).strip()
        fname = fm.group(2).strip()
        if fname in ('class', 'record', 'public', 'private', 'static', 'final'):
            continue
        if ftype in ('class', 'interface', 'enum', 'record', 'implements'):
            continue
        fields[fname] = {
            'type': ftype,
            'required': is_required_annotation(ann_text),
            'sample': get_sample_value(ftype, fname),
        }
    return fields


def parse_class_fields(content: str):
    """Parse traditional class with field declarations."""
    fields = {}
    # Strip line comments
    stripped = re.sub(r'//.*$', '', content, flags=re.MULTILINE)
    # Match field declarations with multi-line annotations
    # Allow optional @Annotations (one or more) followed by Type and field name
    pattern = re.compile(
        r'((?:@\w+(?:\([^)]*\))?\s+)+)?'  # one or more annotations
        r'(?:public|private|protected)?\s*'
        r'(?:static\s+)?(?:final\s+)?'
        r'([\w<>,\s\[\]]+?)'              # type
        r'\s+(\w+)\s*[;=]',                # field name
        re.DOTALL
    )
    for m in pattern.finditer(stripped):
        ann_block = m.group(1) or ''
        ftype = m.group(2).strip()
        fname = m.group(3).strip()
        # Skip methods (look back for closing paren)
        # Skip keywords
        if fname in ('class', 'record', 'public', 'private', 'static', 'final', 'return', 'if', 'for', 'while'):
            continue
        if ftype in ('class', 'interface', 'enum', 'record'):
            continue
        # Skip if it looks like a method (has parens before ;)
        # Check the context before
        start = m.start()
        prev_chunk = stripped[max(0, start-200):start]
        if '(' in prev_chunk and ')' not in prev_chunk[prev_chunk.rfind('('):]:
            continue
        # Skip if no annotation block AND no semicolon-only assignment
        # Simple heuristic: must have at least one annotation or be a plain field
        if not ann_block and '=' not in m.group(0):
            # Could still be a plain field like: private String name;
            pass
        fields[fname] = {
            'type': ftype,
            'required': is_required_annotation(ann_block),
            'sample': get_sample_value(ftype, fname),
        }
    return fields


def parse_dto(filepath):
    """Parse a single DTO file, return dict of field_name -> {type, required, sample}."""
    content = filepath.read_text(encoding='utf-8', errors='ignore')
    # Detect record vs class
    if re.search(r'public\s+record\s+', content):
        return parse_record(content)
    return parse_class_fields(content)


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

# Show some samples
sample = list(schemas.items())[:5]
print('\nSample schemas:')
for k, v in sample:
    print(f'  {k}: {list(v.keys())}')
