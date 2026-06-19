"""
PCMS AI Engine Service - FastAPI entrypoint.
UC15 - AI-Powered Assistance (12 features)
- Chatbot RAG (CHAT-AI)
- OCR Prescription (AI-RX-OCR)
- Drug Interaction (AI-DRUG-CHECK)
- Semantic Search (AI-SEMANTIC-SEARCH)
- Demand Forecast (NSF-16)
- Anomaly Detection (NSF-18)
- Medical Record Summary
- Sales Trend Analysis
- CSKH Chatbot
- Re-examination Reminder
- Dosage Check
- Auto-Moderation

Tech stack: Python 3.11+ / FastAPI / LangChain / pgvector / OpenAI
"""

from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from app.api.v1 import (
    chat,
    ocr,
    drug_check,
    semantic_search,
    forecast,
    anomaly,
    summary,
    moderation,
    chat_session,
    dosage,
)
from app.core.config import settings
from app.core.logger import setup_logger

logger = setup_logger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("ai-engine-service starting on port %d", settings.PORT)
    yield
    logger.info("ai-engine-service shutting down")


app = FastAPI(
    title="PCMS AI Engine Service",
    version="1.0.0",
    description="UC15 - AI-Powered Assistance for PCMS Pharmacy Chain",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount v1 routers
app.include_router(chat.router, prefix="/api/v1/ai/chat", tags=["UC15 - AI Chatbot"])
app.include_router(ocr.router, prefix="/api/v1/ai/ocr", tags=["UC15 - AI OCR"])
app.include_router(
    drug_check.router, prefix="/api/v1/ai/drug-check", tags=["UC15 - Drug Interaction"]
)
app.include_router(
    semantic_search.router,
    prefix="/api/v1/ai/semantic-search",
    tags=["UC15 - Semantic Search"],
)
app.include_router(
    forecast.router, prefix="/api/v1/ai/forecast", tags=["UC15 - Demand Forecast"]
)
app.include_router(
    anomaly.router, prefix="/api/v1/ai/anomaly", tags=["UC15 - Anomaly Detection"]
)
app.include_router(
    summary.router, prefix="/api/v1/ai/summary", tags=["UC15 - Medical Summary"]
)
app.include_router(
    moderation.router,
    prefix="/api/v1/ai/moderation",
    tags=["UC15 - Content Moderation"],
)
app.include_router(
    chat_session.router,
    prefix="/api/v1/ai/chat/sessions",
    tags=["UC15 - Chat Sessions"],
)
app.include_router(
    dosage.router, prefix="/api/v1/ai/dosage", tags=["UC15 - Dosage Check"]
)


@app.get("/healthz")
async def healthz():
    """Liveness probe"""
    return {"status": "UP", "service": "ai-engine", "version": "1.0.0"}


@app.get("/readyz")
async def readyz():
    """Readiness probe - check OpenAI / pgvector connectivity"""
    try:
        from app.core.openai_client import check_openai

        await check_openai()
        return {"status": "READY"}
    except Exception as e:
        raise HTTPException(status_code=503, detail=f"Not ready: {e}")


if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=settings.PORT,
        reload=settings.DEBUG,
    )
