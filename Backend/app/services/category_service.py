from datetime import datetime
from app.core.firebase_config import DB
from app.services.model_api import suggest_emoji_openrouter
from fastapi import HTTPException
from datetime import datetime


def get_categories_from_db(uid):
    try:
        categories_ref = DB.collection("users").document(uid).collection("categories")
        docs = categories_ref.stream()

        categories_data = []
        for doc in docs:
            data = doc.to_dict()
            ts = data.get("timestamp")
            if ts is not None:
                try:
                    ts = ts.isoformat()
                except Exception:
                    ts = str(ts)
            category_info = {
                "category": data.get("category"),
                "count": data.get("count", 0),
                "emoji": data.get("emoji", "📌"),
                "visible":  data.get("visible", True),
                "timestamp": ts
            }
            categories_data.append(category_info)

        return {
            "uid": uid,
            "categories": categories_data
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
async def insert_category(uid: str, category: str):
    if not category:
        raise HTTPException(status_code=400, detail="Category name is required")

    category = category.strip().lower()
    emoji = suggest_emoji_openrouter(category)

    doc_ref = DB.collection('users').document(uid).collection('categories').document()  # auto-ID
    try:
        new_category = {
            'category': category,
            'count': 0,
            'emoji': emoji,
            'visible': True,
            'timestamp': datetime.now()
        }
        doc_ref.set(new_category)

        new_category["timestamp"] = new_category["timestamp"].isoformat()
        new_category["id"] = doc_ref.id 
        print(f"✅ Category '{category}' inserted for user {uid}")
        return new_category

    except Exception as e:
        print(f"❌ Failed to insert category: {e}")
        raise HTTPException(status_code=500, detail=str(e))

def update_category_count(uid, category_name):
    try:
        categories_ref = DB.collection("users").document(uid).collection("categories")
        query = categories_ref.where("category", "==", category_name).limit(1).stream()

        for doc in query:
            data = doc.to_dict()
            current_count = data.get("count", 0)
            doc.reference.update({"count": current_count + 1})
            return True

        # Create or update 'undefined' fallback
        fallback = categories_ref.where("category", "==", "undefined").limit(1).stream()
        fallback_doc = next(fallback, None)

        if fallback_doc:
            fallback_doc.reference.update({"count": fallback_doc.to_dict().get("count", 0) + 1})
        else:
            categories_ref.add({"category": "undefined", "count": 1})

        return True
    except Exception as e:
        print(f"❌ Category count update failed: {e}")
        return False
    
def update_mapping(uid,url_id, category_id):
    try:
        print("inside update mapping !!")

        mapping_doc_ref = DB.collection("users").document(uid).collection("mapping").document(category_id)
        doc = mapping_doc_ref.get()
        if doc.exists:
            data = doc.to_dict()
            urls = data.get("urls", [])
            if url_id not in urls:
                urls.append(url_id)
                mapping_doc_ref.update({"urls": urls})
        else:
            # אם המסמך לא קיים - צור אותו עם הרשימה שמכילה את url_id
            mapping_doc_ref.set({"urls": [url_id]})

        return True

    except Exception as e:
        print(f"❌ Failed to update mapping: {e}")
        return False

    
async def delete_category(uid, category):
    try:
        print(f"in delete category: {category}")
        categories_ref = DB.collection("users").document(uid).collection("categories")
        query = categories_ref.where("category", "==", category).limit(1).stream()

        category_doc = None
        category_id = None
        for doc in query:
            category_doc = doc
            category_id = doc.id
            break

        if not category_doc:
            return {"success": False, "message": f"Category '{category}' not found", "deleted_links": 0}

        # 1. Delete all links in this category
        deleted_links = 0
        urls_ref = DB.collection("users").document(uid).collection("urls")
        link_query = urls_ref.where("category", "==", category).stream()
        for link_doc in link_query:
            link_doc.reference.delete()
            deleted_links += 1
        print(f"🗑️ Deleted {deleted_links} links from category '{category}'")

        # 2. Delete the mapping document
        if category_id:
            mapping_ref = DB.collection("users").document(uid).collection("mapping").document(category_id)
            mapping_doc = mapping_ref.get()
            if mapping_doc.exists:
                mapping_ref.delete()
                print(f"🗑️ Deleted mapping for category '{category}'")

        # 3. Delete the category itself
        category_doc.reference.delete()
        print(f"✅ Deleted category '{category}' with {deleted_links} links")

        return {
            "success": True,
            "message": f"Category '{category}' and {deleted_links} links deleted",
            "deleted_links": deleted_links,
        }

    except Exception as e:
        print(f"Error deleting category: {str(e)}")
        return {"success": False, "message": f"Error deleting category: {str(e)}", "deleted_links": 0}


async def change_visibility(uid, category_name , new_visibility):
    try:
        print("in visibility change")
      
        if not category_name:
            raise HTTPException(status_code=400, detail="Category name is required")

        # Find the category document by querying for category name
        categories_ref = DB.collection("users").document(uid).collection("categories")
        query = categories_ref.where("category", "==", category_name).limit(1).stream()
        
        found = False
        for doc in query:
            doc.reference.update({"visible": new_visibility})
            found = True
            break

        if not found:
            raise HTTPException(status_code=404, detail="Category not found")

        print("✅ Visibility updated in Firestore.")
        return {"message": "Visibility updated successfully"}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def fix_pin_emojis(uid):
    """Migrate categories with default pin emoji to AI-suggested emojis."""
    import time
    from app.services.model_api import _fallback_emoji
    try:
        categories_ref = DB.collection("users").document(uid).collection("categories")
        docs = list(categories_ref.stream())
        fixed = 0
        max_fixes = 3  # Only fix up to 3 per request to avoid rate limits

        for doc in docs:
            if fixed >= max_fixes:
                print(f"⏸️ Reached batch limit ({max_fixes}). Remaining will be fixed on next load.")
                break
            data = doc.to_dict()
            if data.get("emoji") == "📌":
                category_name = data.get("category", "")
                # Try fallback map first (no API call needed)
                fallback = _fallback_emoji(category_name)
                if fallback != "📌":
                    doc.reference.update({"emoji": fallback})
                    fixed += 1
                    print(f"✅ '{category_name}' → {fallback} (fallback)")
                    continue
                # Only call OpenRouter if fallback didn't match
                if fixed > 0:
                    time.sleep(5)  # 5 second gap between API calls
                print(f"🔄 Fixing emoji for '{category_name}' via API...")
                new_emoji = suggest_emoji_openrouter(category_name)
                if new_emoji and new_emoji != "📌":
                    doc.reference.update({"emoji": new_emoji})
                    fixed += 1
                    print(f"✅ '{category_name}' → {new_emoji}")

        return {"fixed": fixed, "message": f"Updated {fixed} categories with proper emojis"}
    except Exception as e:
        print(f"❌ fix_pin_emojis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
