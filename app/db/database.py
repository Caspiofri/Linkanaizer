"""
Database connection and session management.
"""
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine, async_sessionmaker
from sqlalchemy.pool import NullPool
from sqlalchemy.orm import sessionmaker

from app.core.config import settings

# Get database URL
database_url = str(settings.DATABASE_URL)
print(f"Initializing database with URL: {database_url}")

# Determine if we're using SQLite
is_sqlite = database_url and "sqlite" in database_url.lower()

# Create async engine with SQLite-specific settings if needed
engine_kwargs = {
    "echo": settings.LOG_LEVEL == "DEBUG",
    "future": True,
}

# SQLite-specific configuration
if is_sqlite:
    # SQLite with aiosqlite works better with NullPool for async operations
    engine_kwargs["poolclass"] = NullPool
    print("Using SQLite with aiosqlite driver")

# Create async engine
try:
    engine = create_async_engine(
        database_url,
        **engine_kwargs,
    )
    print("Database engine created successfully")
except Exception as e:
    print(f"ERROR: Failed to create database engine: {e}")
    raise

# Create async session factory
AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)


async def get_db() -> AsyncSession:
    """
    Dependency for getting database session.
    Use this in FastAPI route dependencies.
    """
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
