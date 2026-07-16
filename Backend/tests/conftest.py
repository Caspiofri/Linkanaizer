"""
Test setup: make the app importable without live credentials.

- Adds Backend/ to sys.path so `app.*` imports resolve.
- Stubs app.core.firebase_config so unit tests never touch Firestore.
- Sets a dummy OPENROUTER_API_KEY so model_api imports cleanly.
"""
import os
import sys
from unittest.mock import MagicMock

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

os.environ.setdefault("OPENROUTER_API_KEY", "test-key-not-real")

# Stub firebase before any app module imports it
fake_firebase = MagicMock()
sys.modules.setdefault("app.core.firebase_config", fake_firebase)
