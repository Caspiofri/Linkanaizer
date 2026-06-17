from fastapi import APIRouter, Body, Depends, HTTPException
from app.services.category_service import insert_category , get_categories_from_db , delete_category , change_visibility, fix_pin_emojis
from app.utils.token_verifier import verify_token
from fastapi.responses import JSONResponse

router = APIRouter()

@router.post("/insert_category")
async def insert_category_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        category_name = (data.get("category") or "").strip()
        if not category_name:
            raise HTTPException(status_code=400, detail="Category name is required")
        if len(category_name) > 50:
            raise HTTPException(status_code=400, detail="Category name must be 50 characters or less")
        new_category = await insert_category(uid, category_name)
        return JSONResponse(content={"category": new_category})
    except HTTPException as e:
        raise e
    except Exception as e:
        print(f"insert_category error: {e}")
        raise HTTPException(status_code=500, detail="Failed to create category")

@router.get("/get_categories")
async def get_categories(user=Depends(verify_token)):
    try:
        return get_categories_from_db(user["uid"])
    except Exception as e:
        print(f"get_categories error: {e}")
        raise HTTPException(status_code=500, detail="Failed to load categories")

@router.post("/delete_category")
async def delete_category_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        result = await delete_category(uid, data.get("category"))
        return result
    except Exception as e:
        print(f"delete_category error: {e}")
        raise HTTPException(status_code=500, detail="Failed to delete category")

@router.post("/change_visibility")
async def change_visibility_route(data: dict = Body(...), user=Depends(verify_token)):
    try:
        uid = user["uid"]
        category_name = data.get("category")
        new_visibility = data.get("visible", True)
        result = await change_visibility(uid, category_name, new_visibility)
        return result
    except Exception as e:
        print(f"change_visibility error: {e}")
        raise HTTPException(status_code=500, detail="Failed to change visibility")


@router.post("/fix_emojis")
async def fix_emojis_route(user=Depends(verify_token)):
    try:
        uid = user["uid"]
        result = fix_pin_emojis(uid)
        return result
    except Exception as e:
        print(f"fix_emojis error: {e}")
        raise HTTPException(status_code=500, detail="Failed to fix emojis")
