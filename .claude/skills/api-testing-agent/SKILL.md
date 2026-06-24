---
name: api-testing-agent
description: Use when user wants to test API endpoints, check REST/GraphQL/WebSocket/gRPC responses, validate API behavior, debug API issues, run load tests, or create API test reports. Also triggers for "test api", "gọi api", "kiểm tra endpoint", "api spec", "swagger", "postman".
---

# API Testing Agent

## Overview

Orchestrates comprehensive API testing by coordinating two specialized subagents: Executor (runs tests) and Evaluator (analyzes results).

## When to Use

- User asks to test API endpoints
- User wants to validate REST/GraphQL/WebSocket/gRPC behavior
- User needs API test reports (Markdown/HTML)
- User provides OpenAPI/Swagger spec or Postman collection
- User mentions "test api", "gọi api", "kiểm tra endpoint"

## Workflow

```
Nhận yêu cầu
    ↓
Thu thập thông tin (Base URL, endpoints, auth)
    ↓
Tạo Test Plan → Xác nhận với user
    ↓
Executor Agent (agents/executor.md)
    ↓
Evaluator Agent (agents/evaluator.md)
    ↓
Tổng hợp & báo cáo
```

## Quick Reference

| Step | Action | Output |
|------|--------|--------|
| 1 | Thu thập inputs (base_url, endpoints, auth) | Confirmed test scope |
| 2 | Tạo Test Plan (max 20 cases) | Markdown table |
| 3 | Executor chạy tests | raw_results JSON |
| 4 | Evaluator phân tích | pass/fail + reports |
| 5 | Tổng hợp & hiển thị | Final summary |

## Test Plan Template

```markdown
## Test Plan: [API Name]

**Base URL**: ...
**Auth**: ...
**Tổng số test cases**: N

### Test Cases
| # | Method | Endpoint | Scenario | Expected Status |
|---|--------|----------|----------|-----------------|
| 1 | GET    | /users   | Happy path | 200 |
| 2 | POST   | /users   | Create valid user | 201 |
...
```

## Required Inputs

**Bắt buộc:**
- Base URL
- Danh sách endpoints hoặc file spec (OpenAPI/Postman)
- Authentication method (api key, Bearer, Basic, none)

**Tùy chọn:**
- Environment (dev/staging/production)
- Timeout (default: 30s)
- Test scenario type (happy path, edge cases, load test)

## Special Cases

| Situation | Handling |
|-----------|----------|
| Chỉ test 1 endpoint nhanh | Bỏ qua bước hiển thị Test Plan, chạy thẳng |
| OAuth2/session cookie auth | Yêu cầu user cung cấp token/cookie có sẵn |
| Non-JSON response (XML/text/binary) | Executor capture response, Evaluator đánh giá theo status code |
| Timeout/connection error | Executor báo lỗi, Evaluator đánh dấu FAILED |
| Load testing | Xem `references/load-testing.md` |

## Subagents

**Executor** (`agents/executor.md`): Thực thi tất cả test cases, trả về raw_results

**Evaluator** (`agents/evaluator.md`): Đánh giá pass/fail, sinh báo cáo Markdown/HTML, lưu test cases JSON