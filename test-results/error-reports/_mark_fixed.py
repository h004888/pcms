#!/usr/bin/env python3
"""Mark 5 fixed 500-error MD files with FIXED status."""
import json
from pathlib import Path
from datetime import datetime

manifest = json.loads(Path("test-results/error-reports/_manifest.json").read_text(encoding="utf-8"))
fixed_ids = [13, 26, 91, 168, 183]

for m in manifest:
    if m["index"] in fixed_ids:
        filepath = Path(f"test-results/error-reports/{m['filename']}")
        if not filepath.exists():
            print(f"  SKIP (not found): {m['filename']}")
            continue
        content = filepath.read_text(encoding="utf-8")
        if "**Status**: FIXED" not in content:
            fixed_header = f"\n\n---\n\n## Status: FIXED on {datetime.now().strftime('%Y-%m-%d')}\n\n"
            content = content + fixed_header
            filepath.write_text(content, encoding="utf-8")
            print(f"  Updated: {m['filename']}")
        else:
            print(f"  Already marked: {m['filename']}")
