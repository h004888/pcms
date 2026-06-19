"""Dosage check endpoint (AI-12, FR15.18)."""

from fastapi import APIRouter

from app.core.logger import setup_logger
from app.models.schemas import DosageCheckRequest, DosageCheckResponse

logger = setup_logger(__name__)
router = APIRouter()


@router.post("/check", response_model=DosageCheckResponse)
async def check_dosage(request: DosageCheckRequest):
    """Check if prescribed dosage is safe for patient age/weight."""
    warnings = []
    safe = True

    # Stub: simple rules
    if request.patient_age < 6 and "aspirin" in request.mprescribed_dose.lower():
        warnings.append("Không dùng Aspirin cho trẻ dưới 6 tuổi (hội chứng Reye)")
        safe = False

    if request.patient_age > 65:
        warnings.append("Người cao tuổi: cần giảm liều, theo dõi chức năng thận")

    return DosageCheckResponse(
        safe=safe,
        recommended_dose=request.mprescribed_dose,
        max_daily_dose=None,
        warnings=warnings,
    )
