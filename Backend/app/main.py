import os
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import users, categories, links, files

load_dotenv()

app = FastAPI()

# CORS policy — origins loaded from .env
cors_origins = os.getenv("CORS_ALLOWED_ORIGINS", "").split(",")
cors_origins = [o.strip() for o in cors_origins if o.strip()]

app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["Content-Type", "Authorization"],
)

# Register routes

app.include_router(users.router)
app.include_router(categories.router)
app.include_router(links.router)
app.include_router(files.router)


@app.get("/ping")
def ping():
    return {"status": "ok"}
