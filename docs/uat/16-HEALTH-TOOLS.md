# UAT Test Scenario: Health Tools Service

**Version:** 1.0
**Date:** 2026-06-19
**Service:** `health-tools-service` (port 8097)
**UAT Doc Reference:** `16-HEALTH-TOOLS.md`
**Coverage:** UC18 (Health Tools - HEALTH-QUIZ, HEALTH-QUIZ-LIST, HEALTH-QUIZ-RESULT)

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  HEALTH-TOOLS-SERVICE                                                       ║
║  Tier    : B2C                                                                ║
║  Port    : 8097 (internal)                                                    ║
║  Gateway : http://localhost:8080/api/v1/health/**                            ║
║  Auth    : JWT HS256 - permitAll in dev, customer-only in prod               ║
║  DB      : MySQL 8 (schema = health_tools_db)                                ║
║  Tests   : 4 endpoints, ~15 cases, est. 30 min                               ║
║                                                                              ║
║  Note   : Single controller HealthQuizController under /health.               ║
║           Customer is identified via customerId query param                   ║
║           (no X-User-Id header required at controller level - dev profile).  ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 1. Endpoint Summary

| #  | Controller             | Method | Path                          | Description                  | Test Cases |
|----|------------------------|--------|-------------------------------|------------------------------|-----------:|
| 1  | HealthQuizController   | GET    | `/health/quizzes`             | List all 8 quizzes           |          3 |
| 2  | HealthQuizController   | GET    | `/health/quizzes/{slug}`      | Get quiz detail by slug      |          3 |
| 3  | HealthQuizController   | POST   | `/health/quizzes/{slug}/submit` | Submit answers & get result |          4 |
| 4  | HealthQuizController   | GET    | `/health/quiz-results/me`     | My quiz result history       |          3 |
| **TOTAL**                 |        |                               |                              |     **~13**|

### 1.1 Available Quizzes (seeded)

| Slug                       | Title                       | Risk Levels                |
|----------------------------|-----------------------------|----------------------------|
| `bmicalc`                  | BMI Calculator              | UNDER/NORMAL/OVER/OBESE    |
| `sleepquality`             | Sleep Quality Assessment    | GOOD/FAIR/POOR             |
| `stresslevel`              | Stress Level (PSS-10)       | LOW/MODERATE/HIGH          |
| `cardiorisk`               | Cardiovascular Risk (10y)   | LOW/MEDIUM/HIGH/VERY_HIGH  |
| `diabetesrisk`             | Diabetes Risk (FINDRISC)    | LOW/MODERATE/HIGH/VERY_HIGH|
| `nutritionhabit`           | Nutrition Habits            | GOOD/FAIR/POOR             |
| `depressionscreen`         | PHQ-9 Depression Screen     | MINIMAL/MILD/MOD/SEVERE    |
| `allergyscreen`            | Allergy Screening           | LOW/MODERATE/HIGH          |

---

## 2. Prerequisites

- [x] MySQL 8 running, schema `health_tools_db` migrated
- [x] Eureka: `HEALTH-TOOLS-SERVICE` registered
- [x] Gateway route `/api/v1/health/**` → health-tools-service
- [x] At least 1 B2C customer exists (`{{customerId}}` from Master Plan §7 Step 2)
- [x] 8 quizzes seeded in DB

```
╔════════════════════════════════════════════════════════════════╗
║  Pre-flight check                                              ║
║  $ curl http://localhost:8761/eureka/apps/HEALTH-TOOLS-SERVICE  ║
║  → 1+ instance                                                ║
║  $ curl http://localhost:8080/api/v1/health/quizzes | jq length ║
║  → 8                                                         ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 3. Test Data Seeding (this service)

```
╔══════════════════════════════════════════════════════════════╗
║  Quizzes seeded by SQL (see scripts/seed-test-data.sql):    ║
║                                                              ║
║  - 8 quizzes (bmicalc, sleepquality, stresslevel, cardiorisk,║
║    diabetesrisk, nutritionhabit, depressionscreen,           ║
║    allergyscreen)                                            ║
║  - Each has 5-15 questions with scoring rules               ║
║                                                              ║
║  Required customer:                                          ║
║    customer@pcms.vn / customer123                            ║
║    → {{customerId}} (UUID)                                   ║
╚══════════════════════════════════════════════════════════════╝
```

If quizzes missing, run `scripts/seed-quizzes.sql` or trigger Master Plan §7.9 verification.

---

## 4. Authorization Matrix (B2C)

| Endpoint                          | Required role (prod)              | Dev behavior |
|-----------------------------------|-----------------------------------|--------------|
| GET /health/quizzes               | Customer (or guest)               | permitAll    |
| GET /health/quizzes/{slug}        | Customer (or guest)               | permitAll    |
| POST /health/quizzes/{slug}/submit| Customer (own customerId)         | permitAll    |
| GET /health/quiz-results/me       | Customer (own customerId only)    | permitAll    |

---

## 5. Variables to Capture

| Variable             | Example                                | Captured from            |
|----------------------|----------------------------------------|--------------------------|
| `{{gateway}}`        | `http://localhost:8080`                | (static)                 |
| `{{customerId}}`     | `uuid`                                 | Master Plan §7 Step 2    |
| `{{quizSlug}}`       | `cardiorisk`                           | (static - one of 8)      |
| `{{quizResultId}}`   | `uuid`                                 | TC-03 below              |
| `{{riskLevel}}`      | `LOW` / `MEDIUM` / `HIGH` / `VERY_HIGH`| TC-03 response           |

---

## TC-01: GET /health/quizzes (List all quizzes)

### TC-01a: List - Happy Path

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: List all available health quizzes                    ║
║  METHOD: GET  PATH: /api/v1/health/quizzes                  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    (none required in dev - permitAll)                       ║
║    Authorization : Bearer {{customerToken}}  ← prod         ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "slug":"bmicalc", "title":"BMI Calculator",            ║
║      "category":"Fitness","questionCount":5,                ║
║      "estimatedMinutes":1 },                                ║
║    { "slug":"sleepquality", ... },                          ║
║    { "slug":"stresslevel", ... },                           ║
║    { "slug":"cardiorisk", ... },                            ║
║    { "slug":"diabetesrisk", ... },                          ║
║    { "slug":"nutritionhabit", ... },                        ║
║    { "slug":"depressionscreen", ... },                      ║
║    { "slug":"allergyscreen", ... }                          ║
║  ]                                                          ║
║  ← VERIFY: length === 8                                     ║
║  ← VERIFY: each has slug, title, category, questionCount    ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01b: List - Verify Seed Quiz Slugs

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Verify quiz slugs match seed                         ║
║  METHOD: GET  PATH: /api/v1/health/quizzes                  ║
╠══════════════════════════════════════════════════════════════╣
║  ← VERIFY: response contains slugs:                        ║
║    bmicalc, sleepquality, stresslevel, cardiorisk,         ║
║    diabetesrisk, nutritionhabit, depressionscreen,         ║
║    allergyscreen                                           ║
║  ← VERIFY: no duplicate slugs                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-01c: List - Negative Path (no auth, prod-only check)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call without JWT (prod profile only)                 ║
║  METHOD: GET  PATH: /api/v1/health/quizzes                  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    (no Authorization)                                       ║
║                                                              ║
║  EXPECTED RESPONSE (prod):                                 ║
║    401 Unauthorized                                         ║
║  { "code":"AUTH_001",                                       ║
║    "message":"Missing or invalid JWT token" }                ║
║                                                              ║
║  NOTE: In dev profile (permitAll) this returns 200.         ║
║        Set spring.profiles.active=prod to verify 401.       ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-02: GET /health/quizzes/{slug} (Get quiz detail)

### TC-02a: Get by slug - Happy Path (cardiorisk)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get cardiovascular risk quiz detail                  ║
║  METHOD: GET  PATH: /api/v1/health/quizzes/cardiorisk       ║
╠══════════════════════════════════════════════════════════════╣
║  PATH PARAM:                                                ║
║    slug=cardiorisk                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "slug":"cardiorisk",                                     ║
║    "title":"Cardiovascular Risk Assessment (10-year)",       ║
║    "category":"Heart Health",                               ║
║    "description":"Estimate your 10-year cardiovascular ...",║
║    "questions":[                                            ║
║      { "id":"q1", "text":"Age (years)",                      ║
║        "type":"NUMBER", "min":18, "max":100 },               ║
║      { "id":"q2", "text":"Gender",                          ║
║        "type":"CHOICE",                                     ║
║        "options":["male","female"] },                        ║
║      { "id":"q3", "text":"Smoker?",                         ║
║        "type":"CHOICE",                                     ║
║        "options":["no","yes"] },                            ║
║      { "id":"q4", "text":"Systolic BP (mmHg)",              ║
║        "type":"NUMBER", "min":80, "max":250 },               ║
║      { "id":"q5", "text":"Total cholesterol (mg/dL)",       ║
║        "type":"NUMBER", "min":100, "max":500 }               ║
║    ],                                                       ║
║    "questionCount":5,                                       ║
║    "estimatedMinutes":2                                     ║
║  }                                                          ║
║  ← VERIFY: questions array has 5 entries                   ║
║  ← VERIFY: each question has id, text, type, options        ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02b: Get by slug - Happy Path (bmicalc)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get BMI calculator quiz detail                      ║
║  METHOD: GET  PATH: /api/v1/health/quizzes/bmicalc          ║
╠══════════════════════════════════════════════════════════════╣
║  PATH PARAM:                                                ║
║    slug=bmicalc                                             ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "slug":"bmicalc",                                        ║
║    "title":"BMI Calculator",                                ║
║    "category":"Fitness",                                    ║
║    "questions":[                                            ║
║      { "id":"height", "type":"NUMBER", "unit":"cm" },       ║
║      { "id":"weight", "type":"NUMBER", "unit":"kg" }        ║
║    ],                                                       ║
║    "questionCount":2                                        ║
║  }                                                          ║
║  ← VERIFY: questionCount === 2                             ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-02c: Get by slug - Negative Path (non-existent slug)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get quiz with non-existent slug                      ║
║  METHOD: GET  PATH: /api/v1/health/quizzes/nonexistent-quiz ║
╠══════════════════════════════════════════════════════════════╣
║  PATH PARAM:                                                ║
║    slug=nonexistent-quiz                                    ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  {                                                          ║
║    "code":"MSG31",                                          ║
║    "message":"Quiz not found: nonexistent-quiz"              ║
║  }                                                          ║
║  ← VERIFY: error code MSG31                                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-03: POST /health/quizzes/{slug}/submit (Submit answers)

### TC-03a: Submit BMI - Happy Path (normal range)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit BMI quiz with healthy values                  ║
║  METHOD: POST  PATH: /api/v1/health/quizzes/bmicalc/submit  ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
║    Content-Type  : application/json                         ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "answers": {                                             ║
║      "height": 170,                                         ║
║      "weight": 65                                           ║
║    }                                                        ║
║  }                                                          ║
║                                                              ║
║  ← VERIFY: BMI = 65 / (1.70^2) = 22.49                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "id": "{{quizResultId}}",    ← CAPTURE                  ║
║    "customerId": "{{customerId}}",                          ║
║    "quizSlug": "bmicalc",                                   ║
║    "score": 22,                                             ║
║    "riskLevel": "NORMAL",         ← VERIFY                  ║
║    "advice": "Your BMI is in the healthy range. Maintain...",║
║    "completedAt": "2026-06-19T10:30:00Z"                    ║
║  }                                                          ║
║  ← CAPTURE: id as {{quizResultId}}                         ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03b: Submit BMI - Obese Range

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit BMI with obese values                         ║
║  METHOD: POST  PATH: /api/v1/health/quizzes/bmicalc/submit  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "answers": { "height": 165, "weight": 90 }                ║
║  }                                                          ║
║                                                              ║
║  ← VERIFY: BMI = 90 / (1.65^2) = 33.06                      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "quizSlug":"bmicalc",                                    ║
║    "score": 33,                                             ║
║    "riskLevel": "OBESE",           ← VERIFY (≥30)           ║
║    "advice": "Your BMI indicates obesity. Consult a doctor..."║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03c: Submit Cardio Risk - Happy Path (low risk)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit cardio risk quiz - low risk profile           ║
║  METHOD: POST  PATH: /api/v1/health/quizzes/cardiorisk/submit║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "answers": {                                             ║
║      "q1": 35,                                              ║
║      "q2": "female",                                        ║
║      "q3": "no",                                            ║
║      "q4": 115,                                             ║
║      "q5": 180                                              ║
║    }                                                        ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  {                                                          ║
║    "quizSlug":"cardiorisk",                                 ║
║    "score": 1,                                              ║
║    "riskLevel": "LOW",                                      ║
║    "advice": "Your 10-year cardiovascular risk is low...",  ║
║    "completedAt": "..."                                     ║
║  }                                                          ║
║  ← VERIFY: riskLevel === "LOW"                              ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03d: Submit Quiz - Negative Path (missing customerId)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit without customerId (validation error)          ║
║  METHOD: POST  PATH: /api/v1/health/quizzes/bmicalc/submit  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT (missing field)          ║
║  {                                                          ║
║    "answers": { "height": 170, "weight": 65 }                ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  {                                                          ║
║    "code":"VAL_001",                                        ║
║    "message":"Validation failed",                           ║
║    "errors":[                                               ║
║      { "field":"customerId",                                ║
║        "message":"customerId is required" }                  ║
║    ]                                                        ║
║  }                                                          ║
║  ← VERIFY: validation error for customerId field           ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-03e: Submit Quiz - Negative Path (invalid slug)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Submit answers for non-existent quiz                  ║
║  METHOD: POST  PATH: /api/v1/health/quizzes/unknown/submit  ║
╠══════════════════════════════════════════════════════════════╣
║  REQUEST BODY:              ← INPUT                         ║
║  {                                                          ║
║    "customerId": "{{customerId}}",                          ║
║    "answers": { "q1": "test" }                              ║
║  }                                                          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    404 Not Found                                            ║
║  { "code":"MSG31", "message":"Quiz not found: unknown" }    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## TC-04: GET /health/quiz-results/me (My results)

### TC-04a: List My Results - Happy Path (after submissions)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get my quiz result history                           ║
║  METHOD: GET  PATH: /api/v1/health/quiz-results/me          ║
╠══════════════════════════════════════════════════════════════╣
║  HEADER:                                                   ║
║    Authorization : Bearer {{customerToken}}                 ║
║  QUERY PARAMS:                                             ║
║    customerId={{customerId}}    ← required even in dev      ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  [                                                          ║
║    { "id":"{{quizResultId}}", "quizSlug":"bmicalc",         ║
║      "score":22, "riskLevel":"NORMAL",                      ║
║      "advice":"Your BMI is in the healthy range...",        ║
║      "completedAt":"2026-06-19T10:30:00Z" },                ║
║    { "id":"...","quizSlug":"cardiorisk",                    ║
║      "score":1, "riskLevel":"LOW", ... },                   ║
║    ...                                                      ║
║  ]                                                          ║
║  ← VERIFY: includes results from TC-03a, TC-03b, TC-03c    ║
║  ← VERIFY: sorted by completedAt DESC                       ║
║  ← VERIFY: only results for {{customerId}}                  ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04b: List My Results - Negative Path (no customerId)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Call without customerId query param                  ║
║  METHOD: GET  PATH: /api/v1/health/quiz-results/me          ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    (none)                                                   ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    400 Bad Request                                          ║
║  {                                                          ║
║    "code":"VAL_002",                                        ║
║    "message":"customerId query parameter is required"       ║
║  }                                                          ║
╚══════════════════════════════════════════════════════════════╝
```

### TC-04c: List My Results - Negative Path (customerId with no submissions)

```
╔══════════════════════════════════════════════════════════════╗
║  STEP: Get results for a customer who never submitted        ║
║  METHOD: GET  PATH: /api/v1/health/quiz-results/me          ║
╠══════════════════════════════════════════════════════════════╣
║  QUERY PARAMS:                                             ║
║    customerId=00000000-0000-0000-0000-000000000000          ║
╠══════════════════════════════════════════════════════════════╣
║  EXPECTED RESPONSE:                                        ║
║    200 OK                                                   ║
║  []                                                          ║
║  ← VERIFY: empty array (NOT 404)                            ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Edge Cases & Notes

```
╔══════════════════════════════════════════════════════════════╗
║  EDGE-01: Submit quiz for another customer                  ║
║    → Prod: ownership check should return 403                 ║
║    → Dev (permitAll): service trusts customerId param,      ║
║      no ownership check (so customer A can submit for B).   ║
║      This is a known security gap tracked as ticket P2.     ║
║                                                              ║
║  EDGE-02: Out-of-range numeric answer                        ║
║    → BMI with height=300cm → score may be 0 or error       ║
║    → No hard validation on input ranges in current code     ║
║                                                              ║
║  EDGE-03: Missing required answer                            ║
║    → cardiorisk with only q1+q2 → score calculated on      ║
║      partial input (no 400). Document as gap.                ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Pass / Fail Summary

```
╔══════════════════════════════════════════════════════════════╗
║  Total test cases: 13                                        ║
║  Expected pass: ≥11 (≥85%)                                   ║
║  Known gaps   : EDGE-01, EDGE-03 (partial-input scoring)     ║
║                                                              ║
║  Sign-off: ___________________  Date: ___________             ║
╚══════════════════════════════════════════════════════════════╝
```

**End of `16-HEALTH-TOOLS.md`**
