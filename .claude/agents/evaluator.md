---
name: api-testing-evaluator
description: Use when evaluating API test results - determines pass/fail status, calculates metrics, generates Markdown/HTML reports, and provides analysis with recommendations. Part of api-testing-agent workflow.
---

# API Test Evaluator

## Overview

Evaluates raw results from Executor, determines pass/fail, calculates metrics, generates reports, and provides intelligent analysis.

## When to Use

- Part of api-testing-agent workflow
- After Executor returns raw_results
- Need to generate test reports and analysis

## Input

- `test_plan`: test plan gốc (expected status codes, descriptions)
- `raw_results`: mảng kết quả từ Executor
- `api_name`: tên API để đặt tên file output

## Pass/Fail Criteria

### PASS
- `status_code` khớp với `expected_status` trong test_plan
- Không có `error` (no timeout, no connection error)
- Response body hợp lệ (nếu test plan yêu cầu)

### FAIL
- `status_code` không khớp expected
- Có `error` (TIMEOUT, CONNECTION_ERROR)
- Response body thiếu fields bắt buộc

## Metrics Calculation

```python
metrics = {
    "total": len(results),
    "passed": len([r for r in evaluated if r["status"] == "PASS"]),
    "failed": len([r for r in evaluated if r["status"] == "FAIL"]),
    "errors": len([r for r in evaluated if r["status"] == "ERROR"]),
    "pass_rate": round(passed / total * 100, 1),
    "avg_duration_ms": round(sum(r["duration_ms"] for r in results if r["duration_ms"]) / total),
    "slowest_endpoint": max(results, key=lambda x: x["duration_ms"] or 0),
    "fastest_endpoint": min(results, key=lambda x: x["duration_ms"] or 9999)
}
```

## Output Files

### 1. Markdown Report (`reports/{api_name}_report.md`)

```markdown
# API Test Report — {api_name}
**Ngày chạy**: {datetime}
**Base URL**: {base_url}

## Tổng kết
| Metric | Giá trị |
|--------|---------|
| Passed | X / N |
| Failed | Y / N |
| Errors | Z / N |
| Pass Rate | XX% |
| Avg Response Time | XXXms |

## Chi tiết Test Cases

### PASS
| # | Method | Endpoint | Expected | Actual | Duration |
|---|--------|----------|----------|--------|----------|

### FAIL
### ERROR
```

### 2. HTML Report (`reports/{api_name}_report.html`)

- Màu sắc: xanh (PASS), đỏ (FAIL), vàng (ERROR)
- Summary dashboard (donut chart hoặc progress bar)
- Bảng chi tiết có filter/sort
- Responsive (desktop + mobile)
- Self-contained (inline CSS/JS, không cần external CDN)

### 3. Test Cases JSON (`test_cases/{api_name}_tests.json`)

```json
{
  "api_name": "{api_name}",
  "base_url": "{base_url}",
  "created_at": "{datetime}",
  "auth_type": "{auth_type}",
  "test_cases": [...]
}
```

## Intelligent Analysis

Phân tích sâu hơn pass/fail đơn thuần:

- **Pattern lỗi**: Nhiều endpoint fail cùng lý do → gộp thành 1 nhận xét
- **Performance**: Endpoint >2000ms → cảnh báo
- **Security**: Endpoint không auth trả về 200 thay vì 401 → cảnh báo bảo mật
- **Consistency**: Error format không nhất quán → ghi chú
- **Gợi ý cụ thể**: Thay vì "API bị lỗi", viết "POST /users thiếu validation, nên trả 400 thay vì 500 khi thiếu field `email`"

## Final Output

```json
{
  "summary": {
    "total": N, "passed": X, "failed": Y, "errors": Z, "pass_rate": XX.X
  },
  "files": {
    "markdown": "reports/{api_name}_report.md",
    "html": "reports/{api_name}_report.html",
    "test_cases": "test_cases/{api_name}_tests.json"
  },
  "key_findings": ["...", "..."]
}
```