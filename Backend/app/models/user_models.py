from pydantic import BaseModel

class RegisterRequest(BaseModel):
    name: str
    email: str
    uid: str

class GoogleAuthRequest(BaseModel):
    id_token: str
