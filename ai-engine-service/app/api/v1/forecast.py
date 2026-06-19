"""Demand forecast endpoints (NSF-16)."""

import uuid
from datetime import datetime, timedelta
from fastapi import APIRouter

from app.core.logger import setup_logger
from app.models.schemas import ForecastResponse, ForecastPoint

logger = setup_logger(__name__)
router = APIRouter()


@router.get("/{medicine_id}", response_model=ForecastResponse)
async def forecast_demand(
    medicine_id: uuid.UUID, days: int = 30, branch_id: uuid.UUID = None
):
    """Predict next N-day demand for a medicine (using Prophet/ARIMA in production)."""
    # Stub: return simple moving average as baseline forecast
    today = datetime.now()
    forecast = []
    base_qty = 100  # placeholder
    for i in range(1, days + 1):
        date = (today + timedelta(days=i)).strftime("%Y-%m-%d")
        # Add some random variation - in production, this is Prophet output
        predicted = base_qty + (i % 7) * 5
        forecast.append(
            ForecastPoint(
                date=date,
                predicted_qty=predicted,
                confidence=0.7 + (0.2 if i <= 7 else 0.0),
            )
        )

    return ForecastResponse(
        medicine_id=medicine_id,
        branch_id=branch_id,
        days=days,
        forecast=forecast,
        model_version="stub-v1",
    )
