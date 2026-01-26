"""
Pydantic schemas for request/response validation.
"""
from app.schemas.link_classification import (
    LinkClassification,
    LinkClassificationRequest,
)

__all__ = ["LinkClassification", "LinkClassificationRequest"]
