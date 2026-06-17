from fastapi import APIRouter, Depends, HTTPException
from app.models.user_models import RegisterRequest, GoogleAuthRequest
from app.services.user_service import register_user, google_sign_in
from app.utils.token_verifier import verify_token

router = APIRouter()

@router.post("/register")
async def register(data: RegisterRequest, user=Depends(verify_token)):
    return await register_user(data, user)

@router.post("/google_auth")
async def google_auth(data: GoogleAuthRequest):
    return await google_sign_in(data.id_token)
