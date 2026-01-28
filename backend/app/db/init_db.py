"""
One-time script to create database tables (including categories) in SQLite.

Run this from the backend folder:

    cd c:\dev\linkclassify\backend
    python -m app.db.init_db
"""

from sqlalchemy import create_engine

from app.core.config import settings
from app.db.base import Base
from app.db.models import Category  # noqa: F401  (ensure model is imported)


def main() -> None:
  # For sqlite+aiosqlite, need a sync engine for create_all
  database_url = str(settings.DATABASE_URL)
  if database_url.startswith("sqlite+aiosqlite"):
    database_url = database_url.replace("sqlite+aiosqlite", "sqlite", 1)

  engine = create_engine(database_url, future=True)
  print(f"Creating tables on: {database_url}")
  try:
    Base.metadata.create_all(bind=engine)
    print("Tables created successfully.")
  except Exception as e:
    print(f"ERROR: Failed to create tables: {e}")
    import traceback
    traceback.print_exc()


if __name__ == "__main__":
  main()

