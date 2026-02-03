"""
Application configuration using Pydantic settings.
Handles environment variables for database, APIs, and other services.
"""
from typing import List, Optional
from pydantic import Field, PostgresDsn, validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    Application settings loaded from environment variables.
    """

    # Project Information
    PROJECT_NAME: str = "LinkClassify"
    VERSION: str = "0.1.0"
    DESCRIPTION: str = "Link classification API"
    API_V1_STR: str = "/api/v1"

    # CORS
    BACKEND_CORS_ORIGINS: List[str] = Field(
        default=[
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:8000",
            "http://127.0.0.1:8000",
        ],
        description="List of allowed CORS origins",
    )

    @validator("BACKEND_CORS_ORIGINS", pre=True)
    def assemble_cors_origins(cls, v):
        if isinstance(v, str) and not v.startswith("["):
            return [i.strip() for i in v.split(",")]
        elif isinstance(v, (list, str)):
            return v
        raise ValueError(v)

    # Database Configuration
    POSTGRES_SERVER: str = Field(default="localhost", description="PostgreSQL server")
    POSTGRES_USER: str = Field(default="postgres", description="PostgreSQL user")
    POSTGRES_PASSWORD: str = Field(default="postgres", description="PostgreSQL password")
    POSTGRES_DB: str = Field(default="linkclassify", description="PostgreSQL database name")
    POSTGRES_PORT: int = Field(default=5432, description="PostgreSQL port")
    DATABASE_URL: Optional[str] = Field(
        default=None,
        description="Database connection URL. Supports PostgreSQL (postgresql+asyncpg://) or SQLite (sqlite+aiosqlite://)",
    )

    @validator("DATABASE_URL", pre=True)
    def assemble_db_connection(cls, v, values):
        if isinstance(v, str):
            # If DATABASE_URL is explicitly provided, use it as-is
            return v
        # Otherwise, build PostgreSQL URL from individual components
        return PostgresDsn.build(
            scheme="postgresql+asyncpg",
            username=values.get("POSTGRES_USER"),
            password=values.get("POSTGRES_PASSWORD"),
            host=values.get("POSTGRES_SERVER"),
            port=values.get("POSTGRES_PORT"),
            path=f"/{values.get('POSTGRES_DB') or ''}",
        )

    # Scraping API Configuration
    SCRAPING_API_KEY: str = Field(..., description="API key for the scraping service")
    SCRAPING_API_URL: str = Field(
        default="https://api.scraping.com/v1",
        description="Base URL for the scraping API",
    )
    SCRAPING_API_TIMEOUT: int = Field(
        default=30,
        description="Timeout in seconds for scraping API requests",
    )

    # LLM Provider Configuration
    LLM_PROVIDER: str = Field(
        default="openai",
        description="LLM provider name (e.g., 'openai', 'anthropic', 'groq')",
    )
    LLM_API_KEY: str = Field(..., description="API key for the LLM provider")
    LLM_API_URL: Optional[str] = Field(
        default=None,
        description="Base URL for the LLM API (if different from default)",
    )
    LLM_MODEL: str = Field(
        default="gpt-4",
        description="Model name to use for LLM requests",
    )
    LLM_TEMPERATURE: float = Field(
        default=0.7,
        ge=0.0,
        le=2.0,
        description="Temperature for LLM generation",
    )
    LLM_MAX_TOKENS: int = Field(
        default=2000,
        description="Maximum tokens for LLM responses",
    )

    # Security
    SECRET_KEY: str = Field(
        ...,
        description="Secret key for JWT tokens and encryption",
        min_length=32,
    )
    ALGORITHM: str = Field(default="HS256", description="JWT algorithm")
    ACCESS_TOKEN_EXPIRE_MINUTES: int = Field(
        default=30,
        description="Access token expiration time in minutes",
    )

    # Logging
    LOG_LEVEL: str = Field(default="INFO", description="Logging level")

    # Frontend Auth (NextAuth / Google)
    GOOGLE_CLIENT_ID: str = Field(
        default="",
        description="Google OAuth client ID (used to validate id_token aud)",
    )
    GOOGLE_CLIENT_SECRET: str = Field(
        default="",
        description="Google OAuth client secret (not required for token verification)",
    )
    NEXTAUTH_SECRET: str = Field(
        default="",
        description="NextAuth secret (optional, if you later verify NextAuth JWTs server-side)",
    )

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore",
    )


settings = Settings()
