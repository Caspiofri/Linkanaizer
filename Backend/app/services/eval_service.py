from datetime import datetime
from app.core.firebase_config import DB


def log_llm_call(uid: str, entry: dict) -> None:
    """Persist an LLM call record to Firestore under users/{uid}/llm_logs."""
    try:
        entry.setdefault("timestamp", datetime.now())
        DB.collection("users").document(uid).collection("llm_logs").add(entry)
    except Exception as e:
        print(f"⚠️ Failed to log LLM call: {e}")
