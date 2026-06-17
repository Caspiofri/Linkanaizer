"""
Quick smoke test for the LLM eval pipeline.
Run from Backend/ with: python test_eval.py
"""
import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from dotenv import load_dotenv
load_dotenv()

from app.services.model_api import judge_classification
from app.services.eval_service import log_llm_call
from datetime import datetime

TEST_UID = "test_eval_user"

SAMPLE_METADATA = (
    "How to build a chest workout at home with no equipment. "
    "15-minute routine targeting pecs, shoulders, and triceps."
)

SAMPLE_LLM_RESULT = {
    "short_name": "Home Chest Workout",
    "summary": "15-minute chest and shoulders routine requiring no gym equipment.",
    "category": "fitness",
    "tags": ["chest", "home workout"],
}

print("--- Testing judge_classification ---")
judge = judge_classification(SAMPLE_METADATA, SAMPLE_LLM_RESULT)
print(f"Verdict  : {judge['verdict']}")
print(f"Reasoning: {judge['reasoning']}")
print(f"Latency  : {judge['latency_ms']} ms")

print("\n--- Testing log_llm_call (classify) ---")
log_llm_call(TEST_UID, {
    "type": "classify",
    "model": "google/gemma-3-4b-it",
    "url": "https://example.com/home-chest-workout",
    "latency_ms": 1234,
    "category": SAMPLE_LLM_RESULT["category"],
    "tags": SAMPLE_LLM_RESULT["tags"],
    "summary": SAMPLE_LLM_RESULT["summary"],
    "timestamp": datetime.now(),
})
print("Classify log written to Firestore.")

print("\n--- Testing log_llm_call (judge) ---")
log_llm_call(TEST_UID, {
    "type": "judge",
    "model": "google/gemma-3-4b-it",
    "url": "https://example.com/home-chest-workout",
    "latency_ms": judge["latency_ms"],
    "verdict": judge["verdict"],
    "reasoning": judge["reasoning"],
    "timestamp": datetime.now(),
})
print("Judge log written to Firestore.")

print("\nDone. Check Firestore under users/test_eval_user/llm_logs/")
