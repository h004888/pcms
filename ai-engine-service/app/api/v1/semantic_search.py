"""Semantic search endpoints (UC15-AI-05)."""

from fastapi import APIRouter

from app.core.openai_client import get_client
from app.core.config import settings
from app.core.logger import setup_logger
from app.models.schemas import SemanticSearchResponse

logger = setup_logger(__name__)
router = APIRouter()


@router.get("", response_model=SemanticSearchResponse)
async def semantic_search(query: str, top_k: int = 10):
    """Symptom-based semantic search (e.g., 'đau đầu' -> relevant medicines)."""
    try:
        client = get_client()
        # Generate embedding for query
        embedding_resp = await client.embeddings.create(
            model=settings.OPENAI_EMBEDDING_MODEL,
            input=query,
        )
        # query_embedding = embedding_resp.data[0].embedding

        # TODO: query pgvector for top-K nearest neighbors
        # Stub: return empty for now (in production, this returns matching medicines)
        return SemanticSearchResponse(query=query, hits=[])
    except Exception as e:
        logger.error("Semantic search failed: %s", e)
        return SemanticSearchResponse(query=query, hits=[])
