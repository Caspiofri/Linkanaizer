# LinkClassify – Production Deployment

This document covers deploying the **Smart Link Categorizer** backend and frontend for production (including mobile).

---

## 1. Environment configuration (backend)

All API keys and secrets are loaded via **Pydantic Settings** from `.env` (see `backend/app/core/config.py`).

| Variable | Required | Description |
|----------|----------|-------------|
| `DATABASE_URL` or `POSTGRES_*` | Yes | PostgreSQL connection (e.g. `postgresql+asyncpg://user:pass@host:5432/db`) |
| `SCRAPING_API_KEY` | Yes | ScrapingBee (or scraping service) API key |
| `LLM_API_KEY` | Yes | LLM provider API key (OpenAI, Anthropic, Groq, etc.) |
| `LLM_PROVIDER` | No | Default: `openai` |
| `SECRET_KEY` | Yes | JWT/encryption secret (min 32 chars) |
| `BACKEND_CORS_ORIGINS` | Yes for prod | Comma-separated allowed origins (e.g. `https://your-app.vercel.app`) |

**Production CORS:** Set `BACKEND_CORS_ORIGINS` to your frontend URL(s), e.g.:

```bash
BACKEND_CORS_ORIGINS=https://linkclassify.vercel.app,https://www.linkclassify.app
```

Copy `backend/.env.example` to `backend/.env` and fill in values. Never commit `.env` or real credentials.

---

## 2. Backend (FastAPI) – Docker

### Build and run with Docker

```bash
cd backend
docker build -t linkclassify-backend .
docker run -p 8000:8000 --env-file .env linkclassify-backend
```

### Production compose (backend + optional local Postgres)

From repo root:

```bash
# Backend only (use external DATABASE_URL)
docker compose -f docker-compose.prod.yml up -d

# Backend + local Postgres (staging / dev)
docker compose -f docker-compose.prod.yml --profile with-db up -d
```

Ensure `backend/.env` (or env vars) includes `DATABASE_URL`, `SCRAPING_API_KEY`, `LLM_API_KEY`, `SECRET_KEY`, and `BACKEND_CORS_ORIGINS` for production.

---

## 3. Backend on Railway / Render

### Railway

1. New project → Deploy from GitHub (backend folder or monorepo with root).
2. Set **Root Directory** to `backend` if repo root is the monorepo.
3. **Variables:** Add all vars from `.env.example` (especially `DATABASE_URL`, `SCRAPING_API_KEY`, `LLM_API_KEY`, `SECRET_KEY`, `BACKEND_CORS_ORIGINS`).
4. **Build:** Dockerfile in `backend/` (Railway detects it if root is `backend`) or use Nixpacks; for Docker explicitly set **Dockerfile path** to `backend/Dockerfile`.
5. **Start command:** Override with `uvicorn app.main:app --host 0.0.0.0 --port $PORT` (Railway sets `PORT`; default Dockerfile uses 8000).
6. Add **Postgres** from Railway dashboard and copy `DATABASE_URL` into variables (use `postgresql+asyncpg://...` if your driver expects async).

### Render

1. New **Web Service** → Connect repo.
2. **Root Directory:** `backend`.
3. **Environment:** Docker (use `backend/Dockerfile`).
4. **Environment variables:** Same as above; add production frontend URL to `BACKEND_CORS_ORIGINS`.
5. **Database:** Create PostgreSQL instance; set `DATABASE_URL` in service env (use internal URL for same region).

---

## 4. Frontend (Next.js) – Vercel / static export

### Vercel (recommended for Next.js)

1. Import Git repo → set **Root Directory** to `frontend`.
2. **Build command:** `npm run build` (or `pnpm build` / `yarn build`).
3. **Output:** Next.js default (no extra config unless you use static export).
4. **Environment variables:** e.g. `NEXT_PUBLIC_API_URL=https://your-backend.railway.app` (or Render URL) so the frontend calls the correct API.
5. Deploy; then add the Vercel URL to the backend’s `BACKEND_CORS_ORIGINS`.

### Build script (local / CI)

```bash
cd frontend
npm ci
npm run build
npm run start   # or use output with a static server if you use static export
```

---

## 5. Mobile UI and viewport

- **Touch targets:** Navigation and search use at least 44×44px tap areas and `touch-manipulation` where relevant.
- **Viewport:** `layout.tsx` sets `width: device-width`, `initialScale: 1`, `viewportFit: cover` for mobile.
- **Bottom nav:** Fixed bottom bar with large touch-friendly icons; main content uses `pb-20`/`pb-28` so it’s not hidden behind the nav.

---

## 6. Search and database indexes (future)

- **Current behavior:** Category list and link list are loaded from the API/store; search/filter is done **in memory** on the client (e.g. by title/summary/keywords).
- **When links are stored in PostgreSQL:** Add a `links` (or similar) table with columns such as `title`, `summary`, `url`, `keywords` (or JSONB). For scalable search:
  - Add **indexes** on columns used in `WHERE`/search (e.g. `title`, `summary`, `category_id`, `user_id`).
  - For full-text search across title/summary/keywords, consider **PostgreSQL full-text search** (GIN index on `to_tsvector(...)`).
- See `docs/SEARCH_INDEX_RECOMMENDATION.md` for a short index checklist when you add DB-backed links.

---

## 7. Checklist before go-live

- [ ] Backend: All secrets in env (no keys in code); `BACKEND_CORS_ORIGINS` includes production frontend URL.
- [ ] Frontend: `NEXT_PUBLIC_API_URL` (or equivalent) points to production backend.
- [ ] Database: Migrations applied (e.g. Alembic); connection uses TLS in production when available.
- [ ] Mobile: Test on real devices; tap targets and keyboard behavior are acceptable.
- [ ] Rate limiting / auth: Configure as needed for your LLM and scraping provider limits.
