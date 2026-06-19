"""Cross-sell endpoint (called by pharmacist-workbench)."""
from fastapi import APIRouter
from typing import List, Dict, Any

from app.core.openai_client import get_client
from app.core.logger import setup_logger

logger = setup_logger(__name__)
router = APIRouter()


@router.post("/cross-sell", response_model=List[Dict[str, Any]])
async def cross_sell_suggestions(request: Dict[str, Any]):
    """Get AI cross-sell suggestions for an order context."""
    try:
        client = get_client()
        # TODO: query medicine catalog + customer history via Feign-like client
        # For MVP, return stub suggestions
        medicine_ids = request.get("medicineIds", [])
        return [
            {"medicineId": "stub-1", "name": "Vitamin C 1000mg", "confidence": 0.85, "reason": "Bổ sung khi dùng kháng sinh"},
            {"medicineId": "stub-2", "name": "Probiotic", "confidence": 0.78, "reason": "Cân bằng hệ vi sinh đường ruột"},
        ]
    except Exception as e:
        logger.error("Cross-sell failed: %s", e)
        return []


@router.post("/summary", response_model=Dict[str, Any])
async def medical_summary(request: Dict[str, Any]):
    """Alias for /api/v1/ai/summary - used by pharmacist-workbench."""
    customer_id = request.get("customerId")
    return {
        "summary": f"AI tóm tắt cho khách hàng {customer_id}: chưa có dữ liệu",
        "fallback": True,
    }
