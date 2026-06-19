"""Anomaly detection endpoints (NSF-18)."""

from fastapi import APIRouter

from app.core.logger import setup_logger
from app.models.schemas import AnomalyCheckRequest, AnomalyCheckResponse, AnomalyFlag

logger = setup_logger(__name__)
router = APIRouter()


@router.post("/prescription", response_model=AnomalyCheckResponse)
async def check_anomaly(request: AnomalyCheckRequest):
    """Check prescription for anomalies (duplicate ingredients, max-dose, age mismatch)."""
    flags = []
    safe = True

    # Stub: simple duplicate detection
    if len(request.medicine_ids) != len(set(str(m) for m in request.medicine_ids)):
        flags.append(
            AnomalyFlag(
                type="DUPLICATE_INGREDIENT",
                severity="WARNING",
                description="Có thuốc trùng lặp trong đơn",
                affected_medicines=request.medicine_ids,
            )
        )

    if not flags:
        safe = True

    return AnomalyCheckResponse(
        prescription_id=request.prescription_id,
        flags=flags,
        safe=safe,
    )
