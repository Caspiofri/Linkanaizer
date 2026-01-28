"""
Main API router that includes all endpoint routers.
"""
from fastapi import APIRouter

from app.api.v1.endpoints import links, categories

api_router = APIRouter()

# Include endpoint routers
api_router.include_router(links.router, prefix="/links", tags=["links"])
api_router.include_router(categories.router, prefix="/categories", tags=["categories"])


@api_router.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy"}
