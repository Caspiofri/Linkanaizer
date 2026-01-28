# LinkClassify - Setup Instructions

## Project Structure

```
linkclassify/
├── backend/          # FastAPI backend application
│   ├── app/         # FastAPI application code
│   ├── alembic/     # Database migrations
│   ├── .env         # Backend environment variables
│   └── requirements.txt
├── frontend/        # Next.js frontend application
│   ├── public/
│   │   └── assets/
│   │       └── images/  # Place your Figma SVGs here
│   └── ...
└── SETUP_INSTRUCTIONS.md (this file)
```

## Placing Figma SVGs

**Location:** `frontend/public/assets/images/`

Place all your exported Figma SVGs directly in this directory:
- `frontend/public/assets/images/logo.svg`
- `frontend/public/assets/images/icon.svg`
- `frontend/public/assets/images/any-other-asset.svg`

**Usage in Next.js:**
You can reference these images in your React components like this:

```tsx
import Image from 'next/image';

// For regular images
<Image src="/assets/images/logo.svg" alt="Logo" width={100} height={100} />

// For SVGs that need to be inline
<img src="/assets/images/icon.svg" alt="Icon" />
```

## Running the Application

### Option 1: Run Both Services Separately (Recommended for Development)

**Terminal 1 - Backend:**
```bash
cd backend
# Install dependencies (first time only)
pip install -r requirements.txt

# Run the FastAPI server
uvicorn app.main:app --reload
```
Backend will run on: `http://localhost:8000`

**Terminal 2 - Frontend:**
```bash
cd frontend
# Install dependencies (first time only)
npm install

# Run the Next.js development server
npm run dev
```
Frontend will run on: `http://localhost:3000`

### Option 2: Using a Process Manager (Advanced)

You can use tools like `concurrently` or `npm-run-all` to run both services with a single command. Add this to `frontend/package.json`:

```json
{
  "scripts": {
    "dev:all": "concurrently \"npm run dev\" \"cd ../backend && uvicorn app.main:app --reload\""
  }
}
```

Then install and run:
```bash
cd frontend
npm install --save-dev concurrently
npm run dev:all
```

## Environment Variables

### Backend (.env)
The backend `.env` file is located at: `backend/.env`

Make sure it contains:
- `DATABASE_URL` (SQLite path is relative to backend folder: `./test.db`)
- `SCRAPING_API_KEY`
- `LLM_API_KEY`
- Other required variables

### Frontend (.env.local)
Create `frontend/.env.local` if you need frontend-specific environment variables:

```env
NEXT_PUBLIC_API_URL=http://localhost:8000
```

## Database Migrations

When working with the database, run migrations from the backend directory:

```bash
cd backend
alembic upgrade head
```

## API Endpoints

The backend API is available at:
- Base URL: `http://localhost:8000`
- API Docs: `http://localhost:8000/docs` (Swagger UI)
- API v1: `http://localhost:8000/api/v1`

## Troubleshooting

### Backend Issues
- Make sure you're in the `backend/` directory when running uvicorn
- Check that `backend/.env` exists and has all required variables
- Database file (`test.db`) will be created in `backend/` directory

### Frontend Issues
- Make sure you're in the `frontend/` directory when running npm commands
- Clear `.next` cache if you encounter build issues: `rm -rf .next` (or `Remove-Item -Recurse -Force .next` on Windows)

### CORS Issues
- Backend CORS is configured to allow `http://localhost:3000` and `http://127.0.0.1:3000`
- If you change the frontend port, update `BACKEND_CORS_ORIGINS` in `backend/.env`
