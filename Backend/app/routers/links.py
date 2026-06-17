from fastapi import APIRouter, Body, Depends, HTTPException
from app.utils.token_verifier import verify_token
from app.services.url_service import (
    process_and_store_url, get_links_by_category, delete_link, move_link,
    toggle_favorite, mark_read, update_link,
)
from fastapi.responses import JSONResponse

router = APIRouter()

@router.post("/process_url")
async def process_url_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        url = (data.get("url") or "").strip()
        if not url:
            raise HTTPException(status_code=400, detail="URL is required")
        if len(url) > 2048:
            raise HTTPException(status_code=400, detail="URL is too long")
        if not url.startswith(("http://", "https://")):
            raise HTTPException(status_code=400, detail="URL must start with http:// or https://")
        category_hint = (data.get("category_hint") or "").strip() or None
        result = await process_and_store_url(uid, url, category_hint=category_hint)
        return result
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"process_url error: {e}")
        raise HTTPException(status_code=500, detail="Failed to process URL")


@router.post("/toggle_favorite")
async def toggle_favorite_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        url = data.get("url")
        if not url:
            raise HTTPException(status_code=400, detail="URL is required")
        return toggle_favorite(uid, url)
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"toggle_favorite error: {e}")
        raise HTTPException(status_code=500, detail="Failed to toggle favorite")


@router.post("/mark_read")
async def mark_read_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        url = data.get("url")
        if not url:
            raise HTTPException(status_code=400, detail="URL is required")
        return mark_read(uid, url)
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"mark_read error: {e}")
        raise HTTPException(status_code=500, detail="Failed to mark as read")


@router.post("/update_link")
async def update_link_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        url = data.get("url")
        if not url:
            raise HTTPException(status_code=400, detail="URL is required")
        name = data.get("name")
        summary = data.get("summary")
        tags = data.get("tags")
        new_category = data.get("new_category")
        return update_link(uid, url, name=name, summary=summary, tags=tags, new_category=new_category)
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"update_link error: {e}")
        raise HTTPException(status_code=500, detail="Failed to update link")


@router.post("/get_links")
async def get_links_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        category_name = data.get("categoryname")
        if not category_name:
            raise HTTPException(status_code=400, detail="Category name is required")

        links = get_links_by_category(uid, category_name)
        return JSONResponse(content={"links": links})

    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"get_links error: {e}")
        raise HTTPException(status_code=500, detail="Failed to load links")


@router.post("/delete_link")
async def delete_link_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        url = data.get("url")
        if not url:
            raise HTTPException(status_code=400, detail="URL is required")
        result = delete_link(uid, url)
        return result
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"delete_link error: {e}")
        raise HTTPException(status_code=500, detail="Failed to delete link")


@router.post("/move_link")
async def move_link_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        url = data.get("url")
        new_category = data.get("new_category")
        if not url or not new_category:
            raise HTTPException(status_code=400, detail="URL and new_category are required")
        result = move_link(uid, url, new_category)
        return result
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"move_link error: {e}")
        raise HTTPException(status_code=500, detail="Failed to move link")
