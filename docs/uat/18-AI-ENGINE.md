# UAT Test Scenario: AI Engine Service (Python FastAPI)

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `ai-engine-service` (port 8095) — Python FastAPI
**UAT Doc Reference:** `18-AI-ENGINE.md`
**Coverage:** UC15 (AI-Powered Assistance) — 13 mounted endpoints

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  AI-ENGINE-SERVICE                                                           ║
║  Tier    : AI (Python FastAPI)                                                ║
║  Port    : 8095 (direct, NOT via gateway)                                    ║
║  URL     : http://localhost:8095                                              ║
║  Auth    : permitAll in dev, JWT HS256 in prod                                ║
║  DB      : PostgreSQL 16 + pgvector                                          ║
║  Stack   : FastAPI + LangChain + OpenAI                                       ║
║  Tests   : 13 endpoints, ~40 cases, est. 90 min                              ║
║                                                                              ║
║  Note   : Service is called DIRECTLY at port 8095 (not via gateway).         ║
║           Health check: GET /healthz and /readyz.                             ║
║           requires OPENAI_API_KEY env var for chat/drug-check endpoints.     ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary (13 mounted + 2 unmounted)

| #  | Router                  | Method | Path                                | Description                  | Test Cases |
|----|-------------------------|--------|-------------------------------------|------------------------------|-----------:|
| 1  | healthz (main)          | GET    | `/healthz`                          | Liveness probe               |          2 |
| 2  | readyz (main)           | GET    | `/readyz`                           | Readiness (OpenAI + pgvector)|          2 |
| 3  | chat.router             | POST   | `/api/v1/ai/chat`                   | Chat RAG                     |          4 |
| 4  | ocr.router              | POST   | `/api/v1/ai/ocr/prescription`       | OCR scan prescription        |          3 |
| 5  | ocr.router              | GET    | `/api/v1/ai/ocr/prescription/{id}`  | Get OCR job status           |          2 |
| 6  | drug_check.router       | POST   | `/api/v1/ai/drug-check`             | Drug interaction check       |          3 |
| 7  | semantic_search.router  | GET    | `/api/v1/ai/semantic-search`        | Semantic medicine search     |          3 |
| 8  | forecast.router         | GET    | `/api/v1/ai/forecast/{medicine_id}` | Demand forecast              |          2 |
| 9  | anomaly.router          | POST   | `/api/v1/ai/anomaly/prescription`   | Prescription anomaly detect  |          2 |
| 10 | summary.router          | POST   | `/api/v1/ai/summary`                | Medical record summary       |          2 |
| 11 | moderation.router       | POST   | `/api/v1/ai/moderation`             | Content moderation           |          2 |
| 12 | chat_session.router     | GET    | `/api/v1/ai/chat/sessions/{id}`     | Get chat session             |          2 |
| 13 | chat_session.router     | POST   | `/api/v1/ai/chat/sessions/{id}/escalate` | Escalate to pharmacist   |          2 |
| 14 | chat_session.router     | POST   | `/api/v1/ai/chat/sessions/{id}/end` | End chat session             |          1 |
| 15 | dosage.router           | POST   | `/api/v1/ai/dosage/check`           | Dosage validation            |          2 |
| -- | (unmounted)             | -      | cross-sell, sales-summary           | NOT registered in main.py    |          - |
| **TOTAL**                  |        |                                     |                              |     **~34**|

---

## 2. Prerequisites

- [x] Python 3.11+ venv activated
- [x] PostgreSQL 16 + pgvector running, schema `pcms_ai_engine` migrated
- [x] `OPENAI_API_KEY` env var set
- [x] pgvector populated with medicine embeddings (seed script)
- [x] Service reachable at `http://localhost:8095`

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8095/healthz                          ║
║  → {"status":"UP","service":"ai-engine","version":"1.0.0"}    ║
║  $ curl http://localhost:8095/readyz                           ║
║  → {"status":"READY"}                                         ║
║  $ echo $OPENAI_API_KEY | head -c 7                            ║
║  → sk-... (present)                                           ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Variables to Capture

| Variable             | Example                                | Captured from            |
|----------------------|----------------------------------------|--------------------------|
| `{{aiEngine}}`       | `http://localhost:8095`                | (static)                 |
| `{{customerId}}`     | `uuid`                                 | Master Plan §7 Step 2    |
| `{{med1}}`           | `uuid` (Paracetamol 500mg)             | Master Plan §7 Step 6    |
| `{{med2}}`           | `uuid` (Vitamin C 1000mg)              | Master Plan §7 Step 6    |
| `{{sessionId}}`      | `uuid`                                 | TC-03 below              |
| `{{ocrJobId}}`       | `uuid`                                 | TC-04 below              |

---

## TC-01: GET /healthz (Liveness probe)

### TC-01a: Liveness - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /healthz                                 ║
║  EXPECTED: 200 OK                                            ║
║  { "status":"UP", "service":"ai-engine", "version":"1.0.0" } ║
║  ← VERIFY: status === "UP"                                   ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01b: Liveness - Always Returns 200 (no auth needed)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /healthz                                 ║
║  HEADER: (no auth)                                           ║
║  EXPECTED: 200 OK (liveness không cần auth)                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /readyz (Readiness probe)

### TC-02a: Readiness - OpenAI + pgvector UP

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /readyz                                  ║
║  PREREQ: OPENAI_API_KEY set + pgvector populated             ║
║  EXPECTED: 200 OK → {"status":"READY"}                       ║
║  ← VERIFY: status === "READY"                                ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02b: Readiness - OpenAI Down → 503

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /readyz                                  ║
║  PREREQ: Temporarily unset OPENAI_API_KEY                    ║
║  EXPECTED: 503 Service Unavailable                           ║
║  { "detail":"Not ready: OpenAI ping failed" }                ║
║  ← VERIFY: 503 not 500 (graceful degradation)                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /api/v1/ai/chat (Chat RAG)

### TC-03a: Chat - Happy Path (new session)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat                         ║
║  HEADER: X-Customer-Id: {{customerId}}                       ║
║  REQUEST:                                                    ║
║  { "customerId":"{{customerId}}",                            ║
║    "message":"Paracetamol 500mg có tác dụng gì?",            ║
║    "sessionId":null }                                        ║
║  EXPECTED: 200 OK                                            ║
║  { "sessionId":"{{sessionId}}", ← CAPTURE                    ║
║    "answer":"Paracetamol là thuốc giảm đau, hạ sốt...",     ║
║    "sources":[{"medicineId":"{{med1}}","name":"Paracetamol",║
║               "score":0.92}],                                ║
║    "escalate":false }                                        ║
║  ← ASYNC: 1-3s (OpenAI call)                                ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03b: Chat - Multi-drug escalation

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat                         ║
║  REQUEST:                                                    ║
║  { "sessionId":"{{sessionId}}", "customerId":"{{customerId}}",║
║    "message":"Tôi đang dùng Aspirin, Metformin, Atorvastatin,║
║              Lisinopril và Metoprolol. Có xung đột không?" } ║
║  EXPECTED: 200 OK → escalate=true, reason=multi-drug         ║
║  ← VERIFY: escalate=true                                    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03c: Chat - Continue existing session

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat                         ║
║  REQUEST:                                                    ║
║  { "sessionId":"{{sessionId}}", "customerId":"{{customerId}}",║
║    "message":"Liều dùng cho người lớn?" }                    ║
║  EXPECTED: 200 OK → answer, same sessionId (continue context)║
║  ← VERIFY: sessionId unchanged                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03d: Chat - Empty message (validation)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat                         ║
║  REQUEST: { "customerId":"{{customerId}}","message":"" }     ║
║  EXPECTED: 422 Unprocessable Entity                          ║
║  { "detail":[{"loc":["body","message"],                     ║
║              "msg":"message cannot be empty"}] }             ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: POST /api/v1/ai/ocr/prescription (OCR scan)

### TC-04a: OCR - Submit image (URL)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/ocr/prescription             ║
║  HEADER: Content-Type: application/json                      ║
║  REQUEST:                                                    ║
║  { "imageUrl":"https://example.com/rx.jpg",                  ║
║    "customerId":"{{customerId}}" }                           ║
║  EXPECTED: 202 Accepted (async job)                          ║
║  { "jobId":"{{ocrJobId}}", ← CAPTURE                        ║
║    "status":"PROCESSING",                                    ║
║    "estimatedSeconds":5 }                                    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04b: OCR - Submit image (base64)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/ocr/prescription             ║
║  REQUEST: { "imageBase64":"<base64-encoded-jpeg>",           ║
║    "customerId":"{{customerId}}" }                           ║
║  EXPECTED: 202 Accepted → jobId, status=PROCESSING           ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04c: OCR - Negative (missing image)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/ocr/prescription             ║
║  REQUEST: { "customerId":"{{customerId}}" }                  ║
║  EXPECTED: 422 Unprocessable Entity                          ║
║  ← VERIFY: imageUrl OR imageBase64 required                  ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-05: GET /api/v1/ai/ocr/prescription/{jobId}

### TC-05a: OCR Job Status - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/ocr/prescription/{{ocrJobId}} ║
║  ← ASYNC: poll tối đa 10s                                    ║
║  EXPECTED: 200 OK → status=COMPLETED                         ║
║  { "jobId":"{{ocrJobId}}","status":"COMPLETED",              ║
║    "extractedText":"Paracetamol 500mg, 3 lần/ngày...",       ║
║    "medicines":[{"name":"Paracetamol","dosage":"500mg",     ║
║                 "frequency":"3 lần/ngày"}] }                 ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-05b: OCR Job Status - Non-existent Job

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/ocr/prescription/00000000-... ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-06: POST /api/v1/ai/drug-check

### TC-06a: Drug Check - Safe Combination

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/drug-check                   ║
║  REQUEST: { "medicineIds":["{{med1}}"],                      ║
║    "customerId":"{{customerId}}" }                           ║
║  EXPECTED: 200 OK → interactions=[], overallRisk=LOW         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-06b: Drug Check - Multiple Drugs (potential interaction)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/drug-check                   ║
║  REQUEST: { "medicineIds":["{{med1}}","{{med2}}","{{med3}}"],║
║    "customerId":"{{customerId}}" }                           ║
║  EXPECTED: 200 OK → interactions[], overallRisk=MODERATE     ║
║  ← VERIFY: overallRisk ∈ {LOW,MODERATE,HIGH,SEVERE}         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-06c: Drug Check - Empty list (validation)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/drug-check                   ║
║  REQUEST: { "medicineIds":[],"customerId":"{{customerId}}" }  ║
║  EXPECTED: 422 Unprocessable Entity                          ║
║  ← VERIFY: medicineIds must be non-empty                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-07: GET /api/v1/ai/semantic-search

### TC-07a: Semantic Search - Query

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/semantic-search?q=thuốc giảm đau ║
║  EXPECTED: 200 OK                                            ║
║  { "results":[{"medicineId":"{{med1}}","name":"Paracetamol",║
║                "score":0.91}, ...] }                         ║
║  ← VERIFY: at least 1 result with score > 0.7               ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-07b: Semantic Search - Empty query

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/semantic-search?q=            ║
║  EXPECTED: 422 (missing query) or 200 with empty results     ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-07c: Semantic Search - Top-K limit

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/semantic-search?q=vitamin&k=3 ║
║  EXPECTED: 200 OK → max 3 results                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-08: GET /api/v1/ai/forecast/{medicineId}

### TC-08a: Forecast - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/forecast/{{med1}}             ║
║           ?days=30                                           ║
║  EXPECTED: 200 OK                                            ║
║  { "medicineId":"{{med1}}","horizonDays":30,                 ║
║    "predictedDemand":[120,135,140,128,...],                 ║
║    "confidence":0.85,                                        ║
║    "model":"prophet" }                                      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-08b: Forecast - Non-existent medicine

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/forecast/00000000-...        ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-09: POST /api/v1/ai/anomaly/prescription

### TC-09a: Anomaly Detect - Normal Rx

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/anomaly/prescription         ║
║  REQUEST: { "patientId":"{{customerId}}",                    ║
║    "medicines":[{"medicineId":"{{med1}}","qty":21}],        ║
║    "prescriberId":"DR-001" }                                 ║
║  EXPECTED: 200 OK → anomalies=[], riskScore=0.05            ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-09b: Anomaly Detect - High qty (anomaly)

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/anomaly/prescription         ║
║  REQUEST: { "patientId":"{{customerId}}",                    ║
║    "medicines":[{"medicineId":"{{med1}}","qty":500}],       ║
║    "prescriberId":"DR-001" }                                 ║
║  EXPECTED: 200 OK → anomalies[≥1], riskScore>0.7             ║
║  ← VERIFY: HIGH_RISK or similar flag in response             ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-10: POST /api/v1/ai/summary

### TC-10a: Summary - Generate from prescription

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/summary                      ║
║  REQUEST: { "prescriptionId":"uuid",                         ║
║    "language":"vi" }                                         ║
║  EXPECTED: 200 OK                                            ║
║  { "summary":"Bệnh nhân được kê đơn Paracetamol 500mg...",  ║
║    "language":"vi","tokensUsed":150 }                        ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-10b: Summary - Missing prescriptionId

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/summary                      ║
║  REQUEST: { "language":"vi" }                                ║
║  EXPECTED: 422 Unprocessable Entity                          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-11: POST /api/v1/ai/moderation

### TC-11a: Moderation - Clean content

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/moderation                   ║
║  REQUEST: { "content":"Paracetamol giảm đau tốt",            ║
║    "type":"COMMENT" }                                        ║
║  EXPECTED: 200 OK → safe=true, flags=[]                      ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-11b: Moderation - Toxic content

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/moderation                   ║
║  REQUEST: { "content":"<toxic text>", "type":"COMMENT" }     ║
║  EXPECTED: 200 OK → safe=false, flags=[toxic,profanity,...] ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-12-14: Chat Sessions (3 endpoints)

### TC-12a: GET /chat/sessions/{id} - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/chat/sessions/{{sessionId}}   ║
║  EXPECTED: 200 OK → sessionId, customerId, messages[], status║
╚══════════════════════════════════════════════════════════════╝
```

### TC-12b: GET /chat/sessions/{id} - Not Found

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: GET  PATH: /api/v1/ai/chat/sessions/00000000-...   ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-13a: POST /chat/sessions/{id}/escalate

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat/sessions/{{sessionId}}/escalate ║
║  REQUEST: { "reason":"complex drug interaction" }            ║
║  EXPECTED: 200 OK → escalatedTo=pharmacist-workbench,        ║
║    consultationId (linked)                                   ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-13b: Escalate - Session Not Found

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat/sessions/00000000-.../escalate ║
║  EXPECTED: 404 Not Found                                     ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-14a: POST /chat/sessions/{id}/end

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/chat/sessions/{{sessionId}}/end ║
║  EXPECTED: 200 OK → status=ENDED                             ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-15: POST /api/v1/ai/dosage/check

### TC-15a: Dosage - Valid Range

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/dosage/check                 ║
║  REQUEST: { "medicineId":"{{med1}}", "doseMg":500,           ║
║    "patientWeightKg":70, "patientAge":30 }                   ║
║  EXPECTED: 200 OK → safe=true, recommendedDoseMg=500         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-15b: Dosage - Overdose

```
╔══════════════════════════════════════════════════════════════╗
║  METHOD: POST  PATH: /api/v1/ai/dosage/check                 ║
║  REQUEST: { "medicineId":"{{med1}}", "doseMg":4000,          ║
║    "patientWeightKg":70, "patientAge":30 }                   ║
║  EXPECTED: 200 OK → safe=false, warning="exceeds max safe dose"║
╚══════════════════════════════════════════════════════════════╝
```

---

## 4. Edge Cases & Notes

```
╔══════════════════════════════════════════════════════════════╗
║  EDGE-01: AI engine DOWN → chat returns 503 graceful         ║
║    Tested in E2E-05 rollback path                            ║
║                                                              ║
║  EDGE-02: OPENAI_API_KEY invalid → 503 from /readyz         ║
║    Chat endpoints may return 503 or fallback to keyword      ║
║                                                              ║
║  EDGE-03: pgvector not migrated → 500 from semantic-search  ║
║    Should return 503 with helpful message (not raw 500)     ║
║                                                              ║
║  EDGE-04: cross-sell router NOT mounted in main.py           ║
║    /api/v1/ai/cross-sell → 404                              ║
║    /api/v1/ai/sales-summary → 404                           ║
║    Documented as gap (master plan shows 18, actual 13)       ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Pass / Fail Summary

```
╔══════════════════════════════════════════════════════════════╗
║  Total test cases: ~34                                       ║
║  Expected pass: ≥30 (≥88%)                                   ║
║  Known gaps   : EDGE-04 (cross-sell unmounted)               ║
║                                                              ║
║  Sign-off: ___________________  Date: ___________             ║
╚══════════════════════════════════════════════════════════════╝
```

**End of `18-AI-ENGINE.md`**