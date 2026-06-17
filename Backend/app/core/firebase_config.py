import firebase_admin
from firebase_admin import credentials, firestore, storage
import os
import json

# Support two modes:
# 1. Local dev: serviceAccountKey.json file on disk
# 2. Cloud Run: FIREBASE_SERVICE_ACCOUNT env var with JSON content

service_account_json = os.getenv("FIREBASE_SERVICE_ACCOUNT")

if service_account_json:
    cred = credentials.Certificate(json.loads(service_account_json))
else:
    base_dir = os.path.abspath(os.path.join(os.path.abspath(__file__), "..", "..", ".."))
    key_path = os.path.join(base_dir, "serviceAccountKey.json")
    cred = credentials.Certificate(key_path)

if not firebase_admin._apps:
    firebase_admin.initialize_app(
        cred,
        {
            "storageBucket": os.getenv("FIREBASE_STORAGE_BUCKET", "linkanaizer.firebasestorage.app"),
        },
    )

DB = firestore.client()
bucket = storage.bucket()
