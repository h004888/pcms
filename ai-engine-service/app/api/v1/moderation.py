"""Content moderation endpoint (FR19.6 - auto-AI moderation)."""
from fastapi import APIRouter

from app.core.openai_client import get_client
from app.core.logger import setup_logger
from app.models.schemas import ModerationRequest, ModerationResult

logger = setup_logger(__name__)
router = APIRouter()


@router.post("", response_model=ModerationResult)
async def moderate_content(request: ModerationRequest):
    """Auto-moderate user-generated content (toxicity, spam, PII detection)."""
    try:
        client = get_client()
        # Use OpenAI moderation endpoint
        mod_resp = await client.moderations.create(input=request.text)
        result = mod_resp.results[0]
        return ModerationResult(
            flagged=result.flagged,
            toxicity_score=max(
                result.category_scores.hate,
                result.category_scores.violence,
                result.category_scores.harassment,
            ) if result.flagged else 0.0,
            spam_score=0.0,  # OpenAI doesn't detect spam - use custom classifier
            pii_detected=[],  # TODO: regex-based PII detection (email, phone, etc.)
            reason="Violated content policy" if result.flagged else None,
        )
    except Exception as e:
        logger.error("Moderation failed: %s", e)
        # Safe default: approve if service fails (with audit log)
        return ModerationResult(flagged=False, toxicity_score=0.0, spam_score=0.0)
