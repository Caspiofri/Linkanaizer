# LinkClassify

A scalable FastAPI application for link classification using PostgreSQL, SQLAlchemy, Scraping API, and LLM providers.

## Project Structure

```
app/
├── __init__.py
├── main.py                 # FastAPI application entry point
├── api/                    # API routes
│   ├── __init__.py
│   └── v1/
│       ├── __init__.py
│       ├── router.py       # Main API router
│       └── endpoints/      # Individual endpoint modules
│           └── __init__.py
├── core/                   # Core configuration
│   ├── __init__.py
│   ├── config.py          # Settings and environment variables
│   └── security.py        # Authentication/authorization utilities
├── db/                     # Database configuration
│   ├── __init__.py
│   ├── base.py            # SQLAlchemy base
│   ├── database.py        # Database connection and sessions
│   └── models/            # Database models
│       └── __init__.py
├── schemas/                # Pydantic schemas
│   └── __init__.py
├── services/               # Business logic services
│   ├── __init__.py
│   ├── scraping_service.py # Scraping API integration
│   └── llm_service.py     # LLM provider integration
└── utils/                  # Utility functions
    ├── __init__.py
    └── logging.py         # Logging configuration
```

## Setup

1. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   Then edit `.env` with your actual values.

3. **Run database migrations (when using Alembic):**
   ```bash
   alembic upgrade head
   ```

4. **Start the server:**
   ```bash
   uvicorn app.main:app --reload
   ```

## Environment Variables

All environment variables are configured in `.env` and loaded via `pydantic-settings`. See `.env.example` for all available options.

### Required Variables:
- `SCRAPING_API_KEY`: Your scraping API key
- `LLM_API_KEY`: Your LLM provider API key
- `SECRET_KEY`: Secret key for JWT tokens (minimum 32 characters)
- `POSTGRES_*`: Database connection details

### Optional Variables:
- `LLM_PROVIDER`: LLM provider name (default: "openai")
- `LLM_MODEL`: Model name (default: "gpt-4")
- `SCRAPING_API_URL`: Custom scraping API URL
- `LLM_API_URL`: Custom LLM API URL (if different from default)

## Features

- ✅ Scalable folder structure
- ✅ Async PostgreSQL with SQLAlchemy
- ✅ Environment variable management with Pydantic
- ✅ Scraping API service integration
- ✅ LLM provider service integration (OpenAI, Anthropic, Groq)
- ✅ Security utilities (JWT, password hashing)
- ✅ CORS configuration
- ✅ Structured logging

## Development

The project uses:
- **FastAPI** for the web framework
- **SQLAlchemy 2.0** with async support
- **Pydantic Settings** for configuration management
- **Alembic** for database migrations (setup needed)
