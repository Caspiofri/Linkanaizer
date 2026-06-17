import os
import re
import tempfile
import requests
from datetime import datetime
from app.services.url_service import process_url
from app.services.extract_service import _is_safe_url
from app.core.firebase_config import DB, bucket
from app.utils.url_utils import url_exists

URL_REGEX = re.compile(r"(\d{1,2}\.\d{1,2}\.\d{4}), (\d{1,2}:\d{2}) - .*?(http[s]?://\S+)")

# ── In-memory import state (per user) ──
_import_progress = {}  # uid -> {"processed": N, "skipped": N, "errors": N, "total": N, "status": "..."}
_cancel_flags = {}     # uid -> True/False


def get_import_progress(uid: str) -> dict:
    """Return current import progress for a user."""
    return _import_progress.get(uid, {"processed": 0, "total": 0, "status": "idle"})


def cancel_import(uid: str):
    """Set the cancel flag so the background loop stops."""
    _cancel_flags[uid] = True
    print(f"[file_service] Cancel requested for {uid}")


def clear_import_state(uid: str):
    """Clean up import state after completion or cancellation."""
    _import_progress.pop(uid, None)
    _cancel_flags.pop(uid, None)


def upload_to_firebase_storage(local_path, storage_path):
    print("[file_service] uploading to Firebase:")
    blob = bucket.blob(storage_path)
    blob.upload_from_filename(local_path)
    blob.make_public()
    print("[file_service] Uploaded to Firebase:", blob.public_url)
    return blob.public_url


async def import_from_file_url(file_url: str, uid: str) -> dict:
    if not _is_safe_url(file_url):
        raise ValueError("Blocked unsafe file URL")
    response = requests.get(file_url)
    response.raise_for_status()

    suffix = os.path.splitext(file_url.split("?")[0])[-1] or ".txt"
    tmp = tempfile.NamedTemporaryFile(delete=False, suffix=suffix, dir=tempfile.gettempdir())
    try:
        tmp.write(response.content)
        tmp.close()

        firebase_url = upload_to_firebase_storage(tmp.name, f"{uid}/{os.path.basename(tmp.name)}")

        with open(tmp.name, "r", encoding="utf-8") as f:
            content = f.read()
        _process_links_from_text(content, uid)

        return {"message": "File imported successfully", "firebase_url": firebase_url}
    finally:
        if os.path.exists(tmp.name):
            os.unlink(tmp.name)


def count_links_in_text(content: str) -> int:
    """Count how many links are found in a WhatsApp-style text export."""
    return len(URL_REGEX.findall(content))


def process_links_background(content: str, uid: str):
    """Background task: process all links found in text. Called from FastAPI BackgroundTasks."""
    print(f"[file_service] Background processing started for {uid}")
    _process_links_from_text(content, uid)
    print(f"[file_service] Background processing complete for {uid}")


def _process_links_from_text(content: str, uid: str):
    """Shared logic: extract and process all links from text content."""
    lines = content.splitlines()
    processed = 0
    skipped = 0
    errors = 0

    # Count total links first
    total = count_links_in_text(content)

    # Initialize progress + clear any old cancel flag
    _cancel_flags[uid] = False
    _import_progress[uid] = {
        "processed": 0,
        "skipped": 0,
        "errors": 0,
        "total": total,
        "status": "processing",
    }

    for line in lines:
        # Check cancellation before each link
        if _cancel_flags.get(uid):
            print(f"[file_service] Import cancelled for {uid}")
            _import_progress[uid]["status"] = "cancelled"
            mark_processing_complete(uid)
            return

        match = URL_REGEX.match(line)
        if match:
            date_str, time_str, url = match.groups()
            try:
                # Skip if URL already exists
                if url_exists(uid, url):
                    skipped += 1
                    _import_progress[uid]["skipped"] = skipped
                    print(f"⏭️ Skipping duplicate: {url[:60]}")
                    continue

                timestamp = datetime.strptime(f"{date_str} {time_str}", "%d.%m.%Y %H:%M")
                result = process_url(url, uid, timestamp)
                if result:
                    processed += 1
                    print(f"✅ Processed ({processed}): {url[:60]}")
                else:
                    errors += 1
                    print(f"❌ Failed to process: {url[:60]}")
            except Exception as e:
                errors += 1
                print(f"❌ Error processing {url[:60]}: {e}")
                continue

            # Update progress after each link attempt
            _import_progress[uid].update({
                "processed": processed,
                "skipped": skipped,
                "errors": errors,
            })

    _import_progress[uid]["status"] = "done"
    print(f"[file_service] Done: {processed} processed, {skipped} skipped, {errors} errors")
    mark_processing_complete(uid)


def mark_processing_complete(uid: str):
    try:
        DB.collection("users").document(uid).update({"processing_complete": True})
    except Exception:
        # User doc might not exist yet — create it
        DB.collection("users").document(uid).set({"processing_complete": True}, merge=True)
