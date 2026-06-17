import os
from app.core.firebase_config import DB
from fastapi import HTTPException
from datetime import datetime
from firebase_admin import auth as firebase_auth
from google.oauth2 import id_token as google_id_token
from google.auth.transport import requests as google_requests

async def register_user(data, user):
    if user["uid"] != data.uid:
        raise HTTPException(status_code=403, detail="Token UID mismatch")

    try:
        doc_ref = DB.collection('users_info').document(data.uid)
        doc_ref.set({
            'uid': data.uid,
            'name': data.name,
            'email': data.email,
            'timestamp': datetime.now()
        })
        return {"message": "User registered successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

async def google_sign_in(token: str) -> dict:
    try:
        google_client_id = os.getenv("GOOGLE_CLIENT_ID")
        idinfo = google_id_token.verify_oauth2_token(
            token,
            google_requests.Request(),
            audience=google_client_id,
        )

        email = idinfo["email"]
        name = idinfo.get("name", "")

    except ValueError as e:
        raise HTTPException(status_code=401, detail=f"Invalid Google ID token: {e}")

    # Find or create Firebase user
    try:
        firebase_user = firebase_auth.get_user_by_email(email)
    except firebase_auth.UserNotFoundError:
        firebase_user = firebase_auth.create_user(email=email, display_name=name)

    uid = firebase_user.uid

    # Store/update user info in Firestore
    DB.collection("users_info").document(uid).set(
        {"uid": uid, "name": name, "email": email, "timestamp": datetime.now()},
        merge=True,
    )

    # Create custom token for the client
    custom_token = firebase_auth.create_custom_token(uid)
    # create_custom_token returns bytes in some firebase-admin versions
    if isinstance(custom_token, bytes):
        custom_token = custom_token.decode("utf-8")

    return {"custom_token": custom_token, "uid": uid, "email": email, "name": name}
