#!/usr/bin/env python3
"""Generate master index.md for 207 error reports."""
import json
from pathlib import Path
from datetime import datetime
from collections import Counter, defaultdict

manifest = json.loads(Path('test-results/error-reports/_manifest.json').read_text(encoding='utf-8'))
print(f'Building index for {len(manifest)} errors...')

# Stats
sev_count = Counter(m['severity'] for m in manifest)
cat_count = Counter(m['category'] for m in manifest)
status_count = Counter(m['status'] for m in manifest)
service_count = Counter(m['service'] for m in manifest)
with_evidence = sum(1 for m in manifest if m['has_log_evidence'])

# Group by status code
by_status = defaultdict(list)
for m in manifest:
    by_status[m['status']].append(m)

# Group by service
by_service = defaultdict(list)
for m in manifest:
    by_service[m['service']].append(m)

# Build markdown
md = []
md.append('# PCMS API Test - Error Reports Index')
md.append('')
md.append(f'**Generated**: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}  ')
md.append('**Test source**: `test-results.json` (268 endpoints tested, 207 errors)  ')
md.append('**Format**: Scene-by-Scene narrative theo PCMS investigation  ')
md.append('')

# Summary
md.append('## Tom tat Tong quan')
md.append('')
md.append('| Metric | Value |')
md.append('|--------|-------|')
md.append(f'| Tong errors | **{len(manifest)}** |')
md.append(f'| Co log evidence | {with_evidence} ({100*with_evidence/len(manifest):.1f}%) |')
md.append(f'| Status codes | {len(status_count)} unique |')
md.append(f'| Services affected | {len(service_count)} |')
md.append(f'| Categories | {len(cat_count)} |')
md.append('')

# Status distribution
md.append('### Phan bo theo HTTP Status')
md.append('')
md.append('| Status | Count | % | Description |')
md.append('|--------|------:|--:|-------------|')
status_labels = {
    400: 'Bad Request',
    401: 'Unauthorized',
    404: 'Not Found',
    405: 'Method Not Allowed',
    409: 'Conflict',
    500: 'Server Error'
}
for status in sorted(status_count.keys()):
    n = status_count[status]
    pct = 100 * n / len(manifest)
    label = status_labels.get(status, str(status))
    md.append(f'| {status} {label} | {n} | {pct:.1f}% | |')
md.append('')

# Severity distribution
md.append('### Phan bo theo Severity')
md.append('')
md.append('| Severity | Count |')
md.append('|----------|------:|')
sev_order = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']
for sev in sev_order:
    if sev in sev_count:
        md.append(f'| {sev} | {sev_count[sev]} |')
md.append('')

# Category distribution
md.append('### Phan bo theo Root Cause Category')
md.append('')
md.append('| Category | Count | Description |')
md.append('|----------|------:|-------------|')
cat_descriptions = {
    'Media Type': 'Endpoint yeu cau multipart/form-data nhung nhan JSON',
    'Bad Request': 'Body rong hoac path param khong hop le (test data issue)',
    'Resource Not Found': 'ID khong ton tai trong DB (chua seed data)',
    'Validation': 'Annotation validation sai (vi du: @NotBlank tren enum)',
    'API Design': 'HTTP method khong duoc ho tro (405)',
    'Authentication': 'JWT token khong hop le hoac thieu (401)',
    'Integration': 'Feign client loi, service downstream khong available',
    'Service Discovery': 'Eureka khong tim thay instance cua service',
    'Auth - Working Correctly': 'Security feature dang hoat dong dung (KHONG phai bug)',
    'Security - Working Correctly': 'Webhook verification hoat dong dung (KHONG phai bug)',
    'Data Integrity': 'Unique constraint violation, resource da ton tai',
    'Path Parameter': 'Path param can UUID nhung nhan string "1"'
}
for cat, count in cat_count.most_common():
    desc = cat_descriptions.get(cat, '')
    md.append(f'| {cat} | {count} | {desc} |')
md.append('')

# CRITICAL/HIGH issues
md.append('## CRITICAL & HIGH Severity Errors (uu tien fix)')
md.append('')
md.append('| # | Method | URL | Status | Service | Category |')
md.append('|---|--------|-----|--------|---------|----------|')
priority_errors = [m for m in manifest if m['severity'] in ('CRITICAL', 'HIGH')]
priority_errors.sort(key=lambda x: (x['sever'], -x['index']) if hasattr(x, 'sever') else (x['severity'], -x['index']))
for m in sorted(priority_errors, key=lambda x: (x['severity'], x['index'])):
    sev_marker = '[CRIT]' if m['severity'] == 'CRITICAL' else '[HIGH]'
    md.append(f"| [{m['index']:03d}]({m['filename']}) | {m['method']} | `{m['url']}` | {m['status']} | {m['service']} | {sev_marker} {m['category']} |")
md.append('')

# By service
md.append('## Tat ca Errors theo Service')
md.append('')
for service in sorted(by_service.keys()):
    errs = sorted(by_service[service], key=lambda x: x['index'])
    md.append(f'### {service} ({len(errs)} errors)')
    md.append('')
    md.append('| # | Method | URL | Status | Severity | Category |')
    md.append('|---|--------|-----|--------|----------|----------|')
    for m in errs:
        sev_marker = {'CRITICAL': '[C]', 'HIGH': '[H]', 'MEDIUM': '[M]', 'LOW': '[L]', 'INFO': '[I]'}.get(m['severity'], '')
        md.append(f"| [{m['index']:03d}]({m['filename']}) | {m['method']} | `{m['url']}` | {m['status']} | {sev_marker} | {m['category']} |")
    md.append('')

# By status code
md.append('## Tat ca Errors theo HTTP Status')
md.append('')
for status in sorted(by_status.keys()):
    errs = sorted(by_status[status], key=lambda x: x['index'])
    label = status_labels.get(status, str(status))
    md.append(f'### {status} {label} ({len(errs)} errors)')
    md.append('')
    md.append('| # | Method | URL | Service | Severity |')
    md.append('|---|--------|-----|---------|----------|')
    for m in errs:
        sev_marker = {'CRITICAL': '[C]', 'HIGH': '[H]', 'MEDIUM': '[M]', 'LOW': '[L]', 'INFO': '[I]'}.get(m['severity'], '')
        md.append(f"| [{m['index']:03d}]({m['filename']}) | {m['method']} | `{m['url']}` | {m['service']} | {sev_marker} |")
    md.append('')

# Footer
md.append('## Huong dan su dung')
md.append('')
md.append('1. **Loc loi theo service**: Cuon xuong phan "Tat ca Errors theo Service"')
md.append('2. **Loc theo severity**: Uu tien fix CRITICAL va HIGH truoc')
md.append('3. **Click vao #** de xem chi tiet scene-by-scene narrative')
md.append('4. **Moi file MD** co 5 scenes: Test request -> Gateway -> Service -> Error -> Response')
md.append('')

md.append('## Legend')
md.append('')
md.append('- `[C]` CRITICAL - Loi server, can fix ngay')
md.append('- `[H]` HIGH - Loi integration, can fix trong tuan')
md.append('- `[M]` MEDIUM - Loi validation/design, can review')
md.append('- `[L]` LOW - Loi logic nghiep vu, fix khi co thoi gian')
md.append('- `[I]` INFO - Khong phai bug, security feature dang hoat dong dung hoac test data issue')
md.append('')

md.append('---')
md.append('')
md.append(f'*Auto-generated by PCMS Test Investigation. Total: {len(manifest)} error reports.*')

# Write
Path('test-results/error-reports/index.md').write_text('\n'.join(md), encoding='utf-8')
print(f'Saved: test-results/error-reports/index.md')
print(f'Total files: {len(manifest)} + 1 index = {len(manifest) + 1} files')
