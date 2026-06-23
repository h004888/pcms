#!/usr/bin/env python3
"""Mark 4xx error MD files as RESOLVED if they now return 2xx in test results."""
import json
from pathlib import Path
from datetime import datetime

manifest = json.loads(Path("test-results/error-reports/_manifest.json").read_text(encoding="utf-8"))
test_results = json.loads(Path("test-results/smart-test-results.json").read_text(encoding="utf-8"))

# Build url -> status map
url_to_status = {r["url"]: r["new_status"] for r in test_results}

marked = 0
for m in manifest:
    if m["index"] in (13, 26, 91, 168, 183):
        continue  # Already marked FIXED in earlier task
    status = url_to_status.get(m["url"])
    if status and 200 <= status < 300:
        filepath = Path(f"test-results/error-reports/{m['filename']}")
        if filepath.exists():
            content = filepath.read_text(encoding="utf-8")
            if "**Status**: RESOLVED" not in content:
                content += f"\n\n---\n\n## Status: RESOLVED on {datetime.now().strftime('%Y-%m-%d')}\n"
                filepath.write_text(content, encoding="utf-8")
                marked += 1

print(f"Marked {marked} additional MD files as RESOLVED")
