import os
import tempfile
import requests
from fastapi import APIRouter, BackgroundTasks, Body, Depends, HTTPException
from pydantic import BaseModel
from app.utils.token_verifier import verify_token
from app.services.file_service import (
    import_from_file_url, count_links_in_text, process_links_background,
    get_import_progress, cancel_import,
)

router = APIRouter()

class ImportFileRequest(BaseModel):
    file_url: str


MAX_IMPORT_SIZE = 1_000_000  # 1MB max for text import


@router.post("/import")
async def import_file_route(req: ImportFileRequest, user=Depends(verify_token)):
    try:
        result = await import_from_file_url(req.file_url, user["uid"])
        return result
    except Exception as e:
        print(f"import error: {e}")
        raise HTTPException(status_code=500, detail="Failed to import file")


@router.post("/import_text")
async def import_text_route(
    data: dict = Body(...),
    user=Depends(verify_token),
    background_tasks: BackgroundTasks = None,
):
    try:
        content = data.get("content", "")
        if not content.strip():
            raise HTTPException(status_code=400, detail="No content provided")
        if len(content) > MAX_IMPORT_SIZE:
            raise HTTPException(status_code=400, detail="File content is too large (max 1MB)")

        uid = user["uid"]
        links_found = count_links_in_text(content)

        if links_found == 0:
            return {"message": "No links found in the file", "links_found": 0}

        background_tasks.add_task(process_links_background, content, uid)

        return {
            "message": f"Import started! Processing {links_found} links in the background.",
            "links_found": links_found,
        }
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"import_text error: {e}")
        raise HTTPException(status_code=500, detail="Failed to import text")


@router.post("/import_progress")
async def import_progress_route(user=Depends(verify_token)):
    uid = user["uid"]
    progress = get_import_progress(uid)
    return progress


@router.post("/cancel_import")
async def cancel_import_route(user=Depends(verify_token)):
    uid = user["uid"]
    cancel_import(uid)
    return {"message": "Cancel requested"}
