"""
Category database model.
"""

from sqlalchemy import Column, Integer, String, Boolean

from app.db.base import Base


class Category(Base):
    """
    Category model representing a logical link category with an optional emoji.
    """

    __tablename__ = "categories"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), unique=True, index=True, nullable=False)
    emoji = Column(String(10), nullable=True)
    is_visible = Column(Boolean, nullable=False, default=True)

