"""Chat endpoints (UC15-AI-01)."""

import uuid
from datetime import datetime, timezone
from fastapi import APIRouter, HTTPException

from app.core.openai_client import get_client
from app.core.config import settings
from app.core.logger import setup_logger
from app.models.schemas import ChatRequest, ChatResponse

logger = setup_logger(__name__)
router = APIRouter()


@router.post("", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """Send a chat message to AI Pharmacist (RAG)."""
    if not request.consent_ai:
        raise HTTPException(
            status_code=403, detail="User has not consented to AI processing"
        )

    session_id = request.session_id or uuid.uuid4()

    try:
        client = get_client()
        # Build prompt with RAG context (placeholder - in production, query pgvector)
        system_prompt = (
            "Bạn là dược sĩ ảo của PCMS. Trả lời các câu hỏi về thuốc, triệu chứng, "
            "tương tác thuốc một cách chính xác và an toàn. Nếu câu hỏi vượt quá khả năng, "
            "khuyến nghị người dùng liên hệ dược sĩ thật."
        )
        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": request.message},
        ]
        resp = await client.chat.completions.create(
            model=settings.OPENAI_MODEL,
            messages=messages,
            temperature=0.3,
            max_tokens=500,
        )
        response_text = resp.choices[0].message.content
        return ChatResponse(
            session_id=session_id,
            response=response_text,
            citations=[],  # populated by RAG in production
            confidence=0.85,
            escalated=False,
            timestamp=datetime.now(timezone.utc),
        )
    except Exception as e:
        logger.error("Chat failed: %s", e)
        raise HTTPException(status_code=500, detail=f"AI service error: {str(e)}")
