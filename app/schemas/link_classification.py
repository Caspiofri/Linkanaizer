"""
Pydantic schemas for link classification.
"""
from typing import List
from pydantic import BaseModel, Field


class LinkClassification(BaseModel):
    """
    Schema for link classification results.
    """
    category: str = Field(
        ...,
        description="The category/type of the link (e.g., 'article', 'product', 'video', 'document')",
        min_length=1,
        max_length=100,
    )
    summary: str = Field(
        ...,
        description="A brief summary of the link's content",
        min_length=10,
        max_length=500,
    )
    keywords: List[str] = Field(
        ...,
        description="List of relevant keywords extracted from the content",
        min_items=1,
        max_items=20,
    )

    class Config:
        json_schema_extra = {
            "example": {
                "category": "article",
                "summary": "A comprehensive guide to FastAPI best practices",
                "keywords": ["fastapi", "python", "web development", "api", "best practices"],
            }
        }


class LinkClassificationRequest(BaseModel):
    """Request schema for link classification."""
    url: str = Field(..., description="The URL to classify")
    categories: List[str] = Field(
        default=None,
        description="Optional list of allowed categories. If not provided, the LLM will choose freely.",
    )

    class Config:
        json_schema_extra = {
            "example": {
                "url": "https://example.com/article",
                "categories": ["article", "product", "video", "document"],
            }
        }
