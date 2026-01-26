"""
API endpoints for link classification.
"""
from fastapi import APIRouter, HTTPException
from typing import List, Optional
import httpx

from app.schemas.link_classification import (
    LinkClassification,
    LinkClassificationRequest,
)
from app.services.link_classification_service import link_classification_service

router = APIRouter()


@router.post("/classify", response_model=LinkClassification)
async def classify_link(request: LinkClassificationRequest) -> LinkClassification:
    """
    Classify a link by scraping its HTML and parsing it with LLM.

    This endpoint:
    1. Scrapes the URL to get raw HTML
    2. Sends HTML to LLM for structured parsing into JSON schema
    3. Returns validated LinkClassification (category, summary, keywords)

    Args:
        request: LinkClassificationRequest with URL and optional categories

    Returns:
        LinkClassification object with validated JSON schema

    Raises:
        HTTPException: 400 for client errors (invalid URL, parsing failures)
                      500 for server errors (API failures)
    """
    try:
        classification = await link_classification_service.classify_link(
            url=request.url,
            categories=request.categories,
        )
        return classification
    except ValueError as e:
        error_msg = str(e)
        # Check if it's an AI service error
        if "Gemini API" in error_msg or "Google API" in error_msg or "AI" in error_msg:
            # AI service failures should return 502 Bad Gateway
            raise HTTPException(
                status_code=502,
                detail=f"AI Classification service failed: {error_msg}",
            ) from e
        # Other client errors: invalid input, parsing failures, etc.
        raise HTTPException(
            status_code=400,
            detail=f"Bad request: {error_msg}",
        ) from e
    except httpx.HTTPError as e:
        # Network/API errors - check if it's AI service related
        error_msg = str(e)
        if "generativelanguage" in error_msg or "openai" in error_msg or "anthropic" in error_msg:
            raise HTTPException(
                status_code=502,
                detail=f"AI Classification service failed: {error_msg}",
            ) from e
        # Other external API errors
        raise HTTPException(
            status_code=500,
            detail=f"External API error: {error_msg}",
        ) from e
    except Exception as e:
        # Unexpected server errors
        error_msg = str(e)
        if "AI" in error_msg or "classification" in error_msg.lower():
            raise HTTPException(
                status_code=502,
                detail=f"AI Classification service failed: {error_msg}",
            ) from e
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error: {error_msg}",
        ) from e
