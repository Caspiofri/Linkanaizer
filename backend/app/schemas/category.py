"""
Pydantic schemas for category management.
"""

from typing import Optional

from pydantic import BaseModel, Field


class CategoryBase(BaseModel):
    """Base fields for categories."""

    name: str = Field(
        ...,
        description="Category name (e.g., 'calisthenics', 'programming')",
        min_length=1,
        max_length=100,
    )


class CategoryCreate(CategoryBase):
    """Schema for creating a new category."""

    is_visible: bool = Field(
        default=True,
        description="Whether this category is currently visible in the UI",
    )


class CategoryRead(CategoryBase):
    """Schema returned from the API when reading a category."""

    id: int = Field(..., description="Category ID")
    emoji: Optional[str] = Field(
        default=None,
        description="Emoji associated with this category (e.g., '💪', '🎥')",
        max_length=10,
    )
    is_visible: bool = Field(
        default=True,
        description="Whether this category is currently visible in the UI",
    )
    link_count: int = Field(
        default=0,
        description="Number of links currently assigned to this category",
    )

    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "name": "calisthenics",
                "emoji": "💪",
                "is_visible": True,
                "link_count": 3,
            }
        }

