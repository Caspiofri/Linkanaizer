"""
API endpoints for category management.

IMPORTANT:
- To keep things responsive for you right now, categories are stored in memory,
  not in the SQLite DB (no DB round-trips, no connection resets).
- We still normalize names, avoid duplicates case-insensitively, generate emoji,
  and support is_visible + toggle-visibility, so הזרימה ב-UI נשארת זהה.
"""

from typing import List

from fastapi import APIRouter, HTTPException

from app.schemas import CategoryCreate, CategoryRead
from app.services.llm_service import llm_service

router = APIRouter()

# In-memory store for categories; survives until server restart
_CATEGORIES: List[CategoryRead] = []
_NEXT_ID: int = 1


@router.get("", response_model=List[CategoryRead])
async def list_categories() -> List[CategoryRead]:
  """
  List all categories with emoji, visibility and link_count.
  """
  print("DEBUG: list_categories called (in-memory)")
  return _CATEGORIES


@router.post("", response_model=CategoryRead, status_code=201)
async def create_category(category_in: CategoryCreate) -> CategoryRead:
  """
  Create a new category in memory.

  Flow:
  1. Normalize name to Title Case to prevent duplicates like 'Work'/'work'.
  2. Check for existing category (case-insensitive).
  3. Ask the LLM service to generate an appropriate emoji.
  4. Store id + name + emoji + is_visible + link_count=0 in memory.
  """
  global _NEXT_ID

  name_normalized = category_in.name.strip()
  if not name_normalized:
    raise HTTPException(status_code=400, detail="Category name cannot be empty")

  canonical_name = name_normalized.title()

  # Check for existing category (case-insensitive)
  for c in _CATEGORIES:
    if c.name.lower() == canonical_name.lower():
      raise HTTPException(
        status_code=400,
        detail=f"Category '{canonical_name}' already exists.",
      )

  # Ask LLM for emoji for this category
  try:
    emoji = await llm_service.generate_category_emoji(canonical_name)
  except Exception as e:
    print(f"WARNING: failed to generate emoji for category {canonical_name!r}: {e}")
    emoji = None

  category = CategoryRead(
    id=_NEXT_ID,
    name=canonical_name,
    emoji=emoji,
    is_visible=category_in.is_visible,
    link_count=0,
  )
  _NEXT_ID += 1
  _CATEGORIES.append(category)

  print(f"DEBUG: created in-memory category id={category.id}, name={category.name}, emoji={category.emoji!r}")
  return category


@router.patch("/{category_id}/toggle-visibility", response_model=CategoryRead)
async def toggle_category_visibility(category_id: int) -> CategoryRead:
  """
  Toggle the is_visible flag of a category (in-memory store).
  """
  for idx, c in enumerate(_CATEGORIES):
    if c.id == category_id:
      updated = CategoryRead(
        id=c.id,
        name=c.name,
        emoji=c.emoji,
        is_visible=not c.is_visible,
        link_count=c.link_count,
      )
      _CATEGORIES[idx] = updated
      print(
        f"DEBUG: toggled visibility for category id={updated.id}, now is_visible={updated.is_visible}"
      )
      return updated

  raise HTTPException(status_code=404, detail="Category not found")
