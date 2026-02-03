# LinkClassify

A full-stack application for classifying and analyzing web links using AI.

## Project Structure

```
linkclassify/
├── backend/          # FastAPI backend application
│   ├── app/         # FastAPI application code
│   ├── alembic/     # Database migrations
│   ├── .env         # Backend environment variables
│   └── requirements.txt
└── frontend/        # Next.js frontend application
    ├── public/
    │   └── assets/
    │       └── images/  # Place your Figma SVGs here
    └── ...
```

## Quick Start

### Backend Setup

```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Backend runs on: `http://localhost:8000`
API Docs: `http://localhost:8000/docs`

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:3000`

## Assets (Figma SVGs)

Place your exported Figma SVGs in: **`frontend/public/assets/images/`**

## Detailed Instructions

See [SETUP_INSTRUCTIONS.md](./SETUP_INSTRUCTIONS.md) for comprehensive setup and usage instructions.
