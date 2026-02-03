"""
Main FastAPI application entry point.
"""
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.api.v1.router import api_router
from app.db.database import engine
from app.db.base import Base


logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Lifespan context manager for application startup and shutdown.
    Handles database connection cleanup.
    """
    logger.debug("Application lifespan started")
    yield  # Application runs here
    
    # Shutdown: Cleanup database connections
    try:
        await engine.dispose()
        logger.info("Database connection closed")
    except Exception:
        logger.exception("Failed to close database connection during shutdown")


def create_application() -> FastAPI:
    """
    Create and configure FastAPI application instance.
    """
    app = FastAPI(
        title=settings.PROJECT_NAME,
        version=settings.VERSION,
        description=settings.DESCRIPTION,
        openapi_url=f"{settings.API_V1_STR}/openapi.json",
        lifespan=lifespan,
    )

    # CORS middleware
    if settings.BACKEND_CORS_ORIGINS:
        app.add_middleware(
            CORSMiddleware,
            allow_origins=[str(origin) for origin in settings.BACKEND_CORS_ORIGINS],
            allow_credentials=True,
            allow_methods=["*"],
            allow_headers=["*"],
        )

    # Include routers
    app.include_router(api_router, prefix=settings.API_V1_STR)

    return app


app = create_application()
