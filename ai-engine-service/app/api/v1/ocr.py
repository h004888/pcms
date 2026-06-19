"""OCR Prescription endpoints (UC15-AI-02)."""
import uuid
from fastapi import APIRouter, HTTPException

from app.core.logger import setup_logger
from app.models.schemas import OcrRequest, OcrResponse, ExtractedMedicine

logger = setup_logger(__name__)
router = APIRouter()


@router.post("/prescription", response_model=OcrResponse)
async def ocr_prescription(request: OcrRequest):
    """Process prescription image via OCR (Google Vision)."""
    if not request.image_url and not request.image_base64:
        raise HTTPException(status_code=400, detail="image_url or image_base64 required")

    job_id = uuid.uuid4()
    image_url = request.image_url or f"inline://ocr/{job_id}"

    # TODO: integrate Google Vision API
    # For now, return a stub response with mock extracted data
    try:
        # Placeholder for real OCR call
        extracted = [
            ExtractedMedicine(
                name="Paracetamol 500mg",
                dosage="1 viên",
                frequency="3 lần/ngày",
                duration="5 ngày",
                notes="Sau ăn"
            ),
            ExtractedMedicine(
                name="Vitamin C 1000mg",
                dosage="1 viên",
                frequency="1 lần/ngày",
                duration="30 ngày",
            ),
        ]
        return OcrResponse(
            job_id=job_id,
            image_url=image_url,
            status="COMPLETED",
            extracted_medicines=extracted,
            raw_text="Paracetamol 500mg 1v x 3l/ngay, Vitamin C 1000mg 1v/ngay",
            confidence=0.92,
            pharmacist_status="PENDING_REVIEW",
        )
    except Exception as e:
        logger.error("OCR failed: %s", e)
        raise HTTPException(status_code=500, detail=f"OCR error: {str(e)}")


@router.get("/prescription/{job_id}", response_model=OcrResponse)
async def get_ocr_status(job_id: uuid.UUID):
    """Get OCR job status."""
    # Stub - real impl would query ai_ocr_jobs table
    return OcrResponse(
        job_id=job_id,
        image_url="",
        status="COMPLETED",
        pharmacist_status="APPROVED",
    )
