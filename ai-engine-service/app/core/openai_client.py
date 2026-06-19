"""OpenAI client wrapper."""
from openai import AsyncOpenAI
from app.core.config import settings
from app.core.logger import setup_logger

logger = setup_logger(__name__)

_client: AsyncOpenAI | None = None


def get_client() -> AsyncOpenAI:
    global _client
    if _client is None:
        _client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
    return _client


async def check_openai() -> bool:
    """Verify OpenAI connectivity."""
    try:
        client = get_client()
        await client.models.list()
        return True
    except Exception as e:
        logger.error("OpenAI check failed: %s", e)
        return False
