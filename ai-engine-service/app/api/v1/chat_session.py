"""Chat session endpoints (escalation to human pharmacist)."""
import uuid
from datetime import datetime, timezone
from fastapi import APIRouter, HTTPException

from app.core.logger import setup_logger
from app.models.schemas import ChatSessionResponse

logger = setup_logger(__name__)
router = APIRouter()


# In-memory store (production: PostgreSQL ai_chat_sessions table)
_SESSIONS: dict[uuid.UUID, ChatSessionResponse] = {}


@router.get("/{session_id}", response_model=ChatSessionResponse)
async def get_session(session_id: uuid.UUID):
    """Get chat session history."""
    session = _SESSIONS.get(session_id)
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    return session


@router.post("/{session_id}/escalate", response_model=ChatSessionResponse)
async def escalate_to_pharmacist(session_id: uuid.UUID):
    """Escalate AI chat session to a human pharmacist."""
    session = _SESSIONS.get(session_id)
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    session.status = "ESCALATED"
    session.updated_at = datetime.now(timezone.utc)
    logger.info("Session %s escalated to human pharmacist", session_id)
    return session


@router.post("/{session_id}/end", response_model=ChatSessionResponse)
async def end_session(session_id: uuid.UUID):
    """End the chat session."""
    session = _SESSIONS.get(session_id)
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    session.status = "ENDED"
    session.updated_at = datetime.now(timezone.utc)
    return session
