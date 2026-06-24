"""Medical record summary endpoint (AI-08)."""

from datetime import datetime, timezone

from fastapi import APIRouter

from app.core.config import settings
from app.core.logger import setup_logger
from app.core.openai_client import get_client
from app.models.schemas import SummaryRequest, SummaryResponse

logger = setup_logger(__name__)
router = APIRouter()


@router.post("", response_model=SummaryResponse)
async def summarize_customer_history(request: SummaryRequest):
    """Generate AI summary of customer medical history for pharmacist review."""
    try:
        client = get_client()
        # TODO: gather customer data from customer-service, prescription-service, order-service
        # For now, generate stub summary
        prompt = (
            f"Tóm tắt lịch sử y tế của khách hàng (ID: {request.customer_id}) trong {request.max_words} từ. "
            "Bao gồm: thuốc đã dùng, dị ứng (nếu có), bệnh nền (nếu có), xu hướng sức khỏe."
        )
        resp = await client.chat.completions.create(
            model=settings.OPENAI_MODEL,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=request.max_words * 2,
            temperature=0.3,
        )
        _raw = resp.choices[0].message.content
        summary: str = _raw if _raw is not None else ""
        return SummaryResponse(
            customer_id=request.customer_id,
            summary=summary,
            key_findings=[],
            generated_at=datetime.now(timezone.utc),
        )
    except Exception as e:
        logger.error("Summary failed: %s", e)
        return SummaryResponse(
            customer_id=request.customer_id,
            summary="Không thể tạo tóm tắt lúc này. Vui lòng thử lại sau.",
            key_findings=[],
            generated_at=datetime.now(timezone.utc),
        )
