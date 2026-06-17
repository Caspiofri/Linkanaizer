"""
Test error handling and fallback behavior in process_url.
Run from Backend/ with: python test_errors.py
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from dotenv import load_dotenv
load_dotenv()

import requests as http
from app.services.url_service import process_url

TEST_UID = "test_eval_user"

def section(title):
    print(f"\n{'='*50}")
    print(f"  {title}")
    print('='*50)

# 1. Ping
section("1. Server health check")
try:
    r = http.get("http://127.0.0.1:8000/ping", timeout=3)
    print(f"GET /ping -> {r.status_code} {r.json()}")
except Exception as e:
    print(f"Server not reachable: {e}")

# 2. Unscrapable URL
section("2. Unscrapable URL (login-gated page)")
result = process_url("https://example-private-dashboard.internal/secret", TEST_UID)
print(f"success     : {result.get('success')}")
print(f"status_code : {result.get('status_code')}")
print(f"message     : {result.get('message')}")

# 3. Real URL — happy path with judge + logging
section("3. Real public URL (happy path)")
result = process_url("https://www.bbc.com/sport/football", TEST_UID)
print(f"success     : {result.get('success')}")
print(f"name        : {result.get('name')}")
print(f"category    : {result.get('category')}")
print(f"warning     : {result.get('warning', 'none')}")

# 4. Duplicate URL (via process_and_store_url)
section("4. Duplicate URL detection")
import asyncio
from app.services.url_service import process_and_store_url
from fastapi import HTTPException

async def test_duplicate():
    try:
        await process_and_store_url(TEST_UID, "https://www.bbc.com/sport/football")
    except HTTPException as e:
        print(f"HTTPException {e.status_code}: {e.detail}")

asyncio.run(test_duplicate())
