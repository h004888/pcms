---
name: api-testing-executor
description: Use when running API test cases - executes HTTP requests, captures responses, handles errors (timeout, connection), and saves raw results for Evaluator. Part of api-testing-agent workflow.
---

# API Test Executor

## Overview

Executes API test cases and returns raw results. Does NOT evaluate pass/fail—that is Evaluator's job.

## When to Use

- Part of api-testing-agent workflow
- After Test Plan is confirmed
- Need to run HTTP requests and capture responses

## Input

- `test_plan`: danh sách test cases
- `base_url`: URL gốc của API
- `auth`: thông tin xác thực
- `timeout`: số giây (default 30)

## Execution Method

**Python với `requests`** là phương pháp chính vì:
- Dễ xử lý auth, headers, body phức tạp
- Capture đầy đủ response (status, headers, body, timing)
- Error handling rõ ràng
- Chạy được trong bash

Fallback: `curl` cho request đơn giản.

## Quy trình

### 1. Chuẩn bị môi trường
```bash
pip install requests --quiet --break-system-packages 2>/dev/null || true
```

### 2. Sinh và chạy Python script

Script mẫu:
```python
import requests
import json
import time
from datetime import datetime

BASE_URL = "{base_url}"
TIMEOUT = {timeout}

HEADERS = {
    "Content-Type": "application/json",
    # "Authorization": "Bearer {token}",
}

results = []

def run_test(test_id, method, endpoint, description, body=None, extra_headers=None):
    url = BASE_URL + endpoint
    headers = {**HEADERS, **(extra_headers or {})}
    start_time = time.time()
    try:
        response = requests.request(method=method, url=url, headers=headers, json=body, timeout=TIMEOUT)
        duration_ms = round((time.time() - start_time) * 1000)
        try:
            body_parsed = response.json()
        except:
            body_parsed = response.text
        results.append({
            "test_id": test_id, "description": description, "method": method,
            "url": url, "request_body": body, "status_code": response.status_code,
            "response_headers": dict(response.headers), "response_body": body_parsed,
            "duration_ms": duration_ms, "error": None
        })
    except requests.exceptions.Timeout:
        results.append({"test_id": test_id, "description": description, "method": method,
            "url": url, "request_body": body, "status_code": None, "response_headers": None,
            "response_body": None, "duration_ms": round((time.time() - start_time) * 1000), "error": "TIMEOUT"})
    except requests.exceptions.ConnectionError as e:
        results.append({"test_id": test_id, "description": description, "method": method,
            "url": url, "request_body": body, "status_code": None, "response_headers": None,
            "response_body": None, "duration_ms": None, "error": f"CONNECTION_ERROR: {str(e)}"})

# TEST CASES (từ test_plan)
# run_test(1, "GET", "/users", "List all users")
# run_test(2, "POST", "/users", "Create user", body={"name": "Test"})

# LƯU KẾT QUẢ
os.makedirs("raw_results", exist_ok=True)
output_file = f"raw_results/execution_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
with open(output_file, "w", encoding="utf-8") as f:
    json.dump(results, f, ensure_ascii=False, indent=2)
print(f"\n✅ Đã lưu {len(results)} kết quả vào {output_file}")
```

## Error Handling

| Error | Handling |
|-------|----------|
| SSL certificate error | Thêm `verify=False`, ghi chú trong kết quả |
| Redirect loop | Thêm `allow_redirects=False` |
| Large response (>1MB) | Truncate, ghi "TRUNCATED" |
| Binary response | Ghi `"[BINARY CONTENT]"` |
| Rate limit (429) | Thêm `time.sleep(1)` giữa requests, retry 1 lần |

## Output

```json
{
  "execution_time": "2024-01-15T10:30:00",
  "total_executed": N,
  "raw_results_file": "raw_results/execution_YYYYMMDD_HHMMSS.json",
  "results": [ ...array of result objects... ]
}
```

## Nguyên tắc

- **Không bỏ qua** bất kỳ test case nào
- **Không đánh giá** kết quả—chỉ capture dữ liệu
- **Luôn chạy hết** tất cả test cases
- **Ghi lại timing** (duration_ms) cho mọi request