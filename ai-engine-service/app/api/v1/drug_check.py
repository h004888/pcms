"""Drug interaction check endpoints (UC15-AI-03)."""

from fastapi import APIRouter

from app.core.logger import setup_logger
from app.models.schemas import DrugCheckRequest, DrugCheckResponse, DrugInteraction

logger = setup_logger(__name__)
router = APIRouter()


# Pre-loaded drug interaction rules (in production: from PostgreSQL drug_interaction_rules table)
KNOWN_INTERACTIONS = [
    DrugInteraction(
        drug_a="Warfarin",
        drug_b="Aspirin",
        severity="MAJOR",
        description="Tăng nguy cơ chảy máu khi kết hợp Warfarin + Aspirin",
        source="DrugBank",
    ),
    DrugInteraction(
        drug_a="Simvastatin",
        drug_b="Clarithromycin",
        severity="MAJOR",
        description="Tăng nồng độ Simvastatin, nguy cơ tiêu cơ vân",
        source="DrugBank",
    ),
    DrugInteraction(
        drug_a="Metformin",
        drug_b="Alcohol",
        severity="MODERATE",
        description="Tăng nguy cơ nhiễm axit lactic",
        source="twELVET",
    ),
]


@router.post("", response_model=DrugCheckResponse)
async def check_drug_interactions(request: DrugCheckRequest):
    """Check drug-drug interactions for a list of medicine IDs."""
    # Stub: return all known interactions as if all medicines interact
    # Real impl would query drug_interaction_rules table by medicine IDs
    return DrugCheckResponse(
        interactions=KNOWN_INTERACTIONS,
        allergy_warnings=[],
        max_dose_warnings=[],
    )
