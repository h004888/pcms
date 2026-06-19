"""Application configuration."""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    PORT: int = 8094
    DEBUG: bool = True

    # OpenAI
    OPENAI_API_KEY: str = ""
    OPENAI_MODEL: str = "gpt-4o-mini"
    OPENAI_EMBEDDING_MODEL: str = "text-embedding-3-small"

    # PostgreSQL + pgvector
    POSTGRES_HOST: str = "localhost"
    POSTGRES_PORT: int = 5432
    POSTGRES_USER: str = "pcms_user"
    POSTGRES_PASSWORD: str = "pcms_pass"
    POSTGRES_DB: str = "pcms_ai_engine"

    # Redis (for Celery)
    REDIS_URL: str = "redis://localhost:6379/0"

    # Eureka
    EUREKA_URL: str = "http://localhost:8761/eureka/"

    # OCR
    GOOGLE_VISION_CREDENTIALS: str = ""

    @property
    def database_url(self) -> str:
        return f"postgresql://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"


settings = Settings()
