# Load Testing Reference

Dùng tài liệu này khi người dùng yêu cầu load test hoặc performance test API.

## Công cụ được hỗ trợ

### 1. Python `requests` + `threading` (đơn giản, không cần cài thêm)
Dùng khi: cần test nhanh với <100 concurrent users

```python
import threading
import requests
import time
from collections import defaultdict

results = defaultdict(list)
lock = threading.Lock()

def send_request(url, headers, body=None):
    start = time.time()
    try:
        r = requests.get(url, headers=headers, timeout=30)
        duration = (time.time() - start) * 1000
        with lock:
            results["status_codes"].append(r.status_code)
            results["durations"].append(duration)
    except Exception as e:
        with lock:
            results["errors"].append(str(e))

# Chạy 50 concurrent requests
threads = [threading.Thread(target=send_request, args=(URL, HEADERS)) for _ in range(50)]
[t.start() for t in threads]
[t.join() for t in threads]

# Phân tích
durations = results["durations"]
print(f"Total: {len(durations)}")
print(f"Avg: {sum(durations)/len(durations):.0f}ms")
print(f"P95: {sorted(durations)[int(len(durations)*0.95)]:.0f}ms")
print(f"Errors: {len(results['errors'])}")
```

### 2. `locust` (nâng cao, có UI)
```bash
pip install locust --break-system-packages
```

### 3. `ab` (Apache Bench — có sẵn trên Linux)
```bash
ab -n 1000 -c 50 https://api.example.com/endpoint
```

## Metrics cần capture
- Requests/second (throughput)
- Average response time
- P95, P99 response time
- Error rate
- Status code distribution