"""
Auth endpoints for users authenticated in the frontend.
"""

from typing import Dict, Any, Optional

import httpx
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.core.config import settings

router = APIRouter()
bearer_scheme = HTTPBearer(auto_error=False)


async def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
) -> Dict[str, Any]:
    """
    Validate a Google ID token sent from the frontend and return basic user info.

    Expect:
      Authorization: Bearer <google_id_token>
    """
    if credentials is None or not credentials.credentials:
        raise HTTPException(status_code=401, detail="Not authenticated")

    id_token = credentials.credentials

    async with httpx.AsyncClient(timeout=10.0) as client:
        resp = await client.get(
            "https://oauth2.googleapis.com/tokeninfo",
            params={"id_token": id_token},
        )

    if resp.status_code != 200:
        raise HTTPException(status_code=401, detail="Invalid token")

    payload = resp.json()

    # Validate audience if configured
    aud = payload.get("aud", "")
    if settings.GOOGLE_CLIENT_ID and aud != settings.GOOGLE_CLIENT_ID:
        raise HTTPException(status_code=401, detail="Token audience mismatch")

    return {
        "email": payload.get("email"),
        "name": payload.get("name"),
        "picture": payload.get("picture"),
        "sub": payload.get("sub"),
    }


@router.get("/me")
async def me(user: Dict[str, Any] = Depends(get_current_user)):
    return user

