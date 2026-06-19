"""Pydantic models / schemas for AI Engine API."""
from datetime import datetime
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from uuid import UUID


# ============================================================
# Chat (UC15-AI-01)
# ============================================================
class ChatMessage(BaseModel):
    role: str = Field(..., description="user|assistant|system")
    content: str


class ChatRequest(BaseModel):
    customer_id: Optional[UUID] = None
    message: str
    context_type: Optional[str] = "GENERAL"  # SYMPTOM|DRUG_INFO|INTERACTION|GENERAL
    session_id: Optional[UUID] = None
    consent_ai: bool = True


class Citation(BaseModel):
    source: str
    title: str
    snippet: str
    confidence: float


class ChatResponse(BaseModel):
    session_id: UUID
    response: str
    citations: List[Citation] = []
    confidence: float = 0.0
    escalated: bool = False
    timestamp: datetime


# ============================================================
# OCR Prescription (UC15-AI-02)
# ============================================================
class OcrRequest(BaseModel):
    image_url: Optional[str] = None
    image_base64: Optional[str] = None


class ExtractedMedicine(BaseModel):
    name: str
    dosage: Optional[str] = None
    frequency: Optional[str] = None
    duration: Optional[str] = None
    notes: Optional[str] = None


class OcrResponse(BaseModel):
    job_id: UUID
    image_url: str
    status: str  # PENDING|PROCESSING|COMPLETED|FAILED
    extracted_medicines: List[ExtractedMedicine] = []
    raw_text: Optional[str] = None
    confidence: float = 0.0
    pharmacist_status: str = "PENDING_REVIEW"  # PENDING_REVIEW|APPROVED|REJECTED


# ============================================================
# Drug Interaction (UC15-AI-03)
# ============================================================
class DrugCheckRequest(BaseModel):
    medicine_ids: List[UUID]
    customer_id: Optional[UUID] = None  # For allergy check


class DrugInteraction(BaseModel):
    drug_a: str
    drug_b: str
    severity: str  # MINOR|MODERATE|MAJOR|CONTRAINDICATED
    description: str
    source: str  # DrugBank|twELVET|local


class DrugCheckResponse(BaseModel):
    interactions: List[DrugInteraction] = []
    allergy_warnings: List[str] = []
    max_dose_warnings: List[Dict[str, Any]] = []


# ============================================================
# Semantic Search (UC15-AI-05)
# ============================================================
class SemanticSearchRequest(BaseModel):
    query: str
    top_k: int = 10


class SearchHit(BaseModel):
    medicine_id: UUID
    name: str
    score: float
    snippet: str


class SemanticSearchResponse(BaseModel):
    query: str
    hits: List[SearchHit] = []


# ============================================================
# Demand Forecast (NSF-16)
# ============================================================
class ForecastRequest(BaseModel):
    medicine_id: UUID
    branch_id: Optional[UUID] = None
    days: int = 30  # 30/60/90


class ForecastPoint(BaseModel):
    date: str
    predicted_qty: int
    confidence: float


class ForecastResponse(BaseModel):
    medicine_id: UUID
    branch_id: Optional[UUID]
    days: int
    forecast: List[ForecastPoint] = []
    model_version: str


# ============================================================
# Anomaly Detection (NSF-18)
# ============================================================
class AnomalyCheckRequest(BaseModel):
    prescription_id: Optional[UUID] = None
    medicine_ids: List[UUID]
    customer_id: Optional[UUID] = None
    diagnosis: Optional[str] = None


class AnomalyFlag(BaseModel):
    type: str  # DUPLICATE_INGREDIENT|MAX_DOSE|AGE_MISMATCH|CONTRAINDICATION
    severity: str  # INFO|WARNING|CRITICAL
    description: str
    affected_medicines: List[UUID] = []


class AnomalyCheckResponse(BaseModel):
    prescription_id: Optional[UUID]
    flags: List[AnomalyFlag] = []
    safe: bool = True


# ============================================================
# Medical Record Summary (AI-08)
# ============================================================
class SummaryRequest(BaseModel):
    customer_id: UUID
    include_prescriptions: bool = True
    include_orders: bool = True
    include_allergies: bool = True
    max_words: int = 200


class SummaryResponse(BaseModel):
    customer_id: UUID
    summary: str
    key_findings: List[str] = []
    generated_at: datetime


# ============================================================
# Content Moderation (FR19.6)
# ============================================================
class ModerationRequest(BaseModel):
    text: str
    content_type: str  # REVIEW|COMMENT|POST


class ModerationResult(BaseModel):
    flagged: bool
    toxicity_score: float
    spam_score: float
    pii_detected: List[str] = []
    reason: Optional[str] = None


# ============================================================
# Chat Sessions
# ============================================================
class ChatSessionResponse(BaseModel):
    id: UUID
    customer_id: Optional[UUID]
    pharmacist_id: Optional[UUID]
    messages: List[ChatMessage] = []
    status: str  # ACTIVE|ESCALATED|ENDED
    created_at: datetime
    updated_at: datetime


# ============================================================
# Dosage Check (AI-12, FR15.18)
# ============================================================
class DosageCheckRequest(BaseModel):
    medicine_id: UUID
    patient_age: int
    patient_weight_kg: Optional[float] = None
    prescribed_dose: str
    frequency: str


class DosageCheckResponse(BaseModel):
    safe: bool
    recommended_dose: str
    max_daily_dose: Optional[str] = None
    warnings: List[str] = []
