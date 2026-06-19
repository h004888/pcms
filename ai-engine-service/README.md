# AI Engine Service (UC15)

Python FastAPI microservice for AI-powered features.

## Architecture

- **Language:** Python 3.11+
- **Framework:** FastAPI
- **AI Stack:** LangChain + OpenAI GPT-4o-mini + pgvector
- **Database:** PostgreSQL 14+ with pgvector extension
- **Background jobs:** Celery + Redis
- **OCR:** Google Cloud Vision API

## Port: 8094

## Endpoints (18 total)

| Endpoint | Method | UC | Description |
|----------|--------|-----|-------------|
| /healthz | GET | - | Liveness probe |
| /readyz | GET | - | Readiness probe |
| /api/v1/ai/chat | POST | AI-01 | AI Pharmacist chatbot (RAG) |
| /api/v1/ai/chat/sessions/{id} | GET | AI-01 | Get chat session |
| /api/v1/ai/chat/sessions/{id}/escalate | POST | AI-01 | Escalate to human |
| /api/v1/ai/chat/sessions/{id}/end | POST | AI-01 | End session |
| /api/v1/ai/ocr/prescription | POST | AI-02 | OCR prescription image |
| /api/v1/ai/ocr/prescription/{jobId} | GET | AI-02 | Get OCR status |
| /api/v1/ai/drug-check | POST | AI-03 | Drug interaction check |
| /api/v1/ai/semantic-search | GET | AI-05 | Symptom-based search |
| /api/v1/ai/forecast/{medicineId} | GET | NSF-16 | Demand forecast |
| /api/v1/ai/anomaly/prescription | POST | NSF-18 | Prescription anomaly check |
| /api/v1/ai/summary | POST | AI-08 | Medical history summary |
| /api/v1/ai/moderation | POST | FR19.6 | Content moderation |
| /api/v1/ai/dosage/check | POST | AI-12 | Dosage check |
| /api/v1/ai/cross-sell | POST | RX-04 | Cross-sell suggestions |
| /api/v1/ai/summary | POST | AI-08 | (alias) |

## Running locally

```bash
# Install deps
pip install -r requirements.txt

# Start PostgreSQL with pgvector
docker run -d --name pcms-postgres -e POSTGRES_PASSWORD=pcms_pass -p 5432:5432 pgvector/pgvector:pg16

# Start Redis (for Celery)
docker run -d --name pcms-redis -p 6379:6379 redis:7

# Run server
uvicorn app.main:app --reload --port 8094

# Run Celery worker (for background OCR)
celery -A app.workers.celery_app worker --loglevel=info
```

## Environment Variables

Create `.env` file:

```
OPENAI_API_KEY=sk-...
GOOGLE_VISION_CREDENTIALS=/path/to/credentials.json
POSTGRES_HOST=localhost
POSTGRES_USER=pcms_user
POSTGRES_PASSWORD=pcms_pass
POSTGRES_DB=pcms_ai_engine
REDIS_URL=redis://localhost:6379/0
```
