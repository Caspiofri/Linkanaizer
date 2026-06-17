import asyncio
import time
from app.core.firebase_config import DB
from app.services.model_api import query_llama_summary, suggest_emoji_openrouter, _fallback_emoji
from app.services.extract_service import extract_url
from app.services.category_service import update_category_count , update_mapping
from fastapi import HTTPException
from app.utils.url_utils import url_exists , processing_content
from datetime import datetime

async def process_and_store_url(uid: str, url: str, dt=None, category_hint: str = None):
    if url_exists(uid, url):
        raise HTTPException(status_code=409, detail="URL already exists in the database")

    result = await asyncio.to_thread(process_url, url, uid, dt, category_hint)
    if not result:
        raise HTTPException(status_code=400, detail="Failed to process the URL")

    return result


def _sort_and_serialize(docs_list: list) -> list:
    """Sort link dicts by timestamp descending, then convert timestamp to ISO string."""
    def sort_key(doc):
        ts = doc.get("timestamp")
        if ts is None:
            return datetime.min
        return ts if hasattr(ts, "year") else datetime.min

    docs_list.sort(key=sort_key, reverse=True)

    for doc in docs_list:
        ts = doc.get("timestamp")
        if ts is not None:
            doc["timestamp"] = ts.isoformat()

    return docs_list


def get_links_by_category(uid: str, category_name: str):
    print("inside get links")
    try:
        if category_name == "all":
            docs = DB.collection("users").document(uid).collection("urls").stream()
            results = [doc.to_dict() for doc in docs]
            return _sort_and_serialize(results)
        else:
            print("case where category_name != all")
            category_id = get_category_id_by_name(uid, category_name)
            url_ids = get_urls_by_category(uid, category_id)
            url_docs = get_urls_by_ids(uid, url_ids)
            return _sort_and_serialize(url_docs)

    except Exception as e:
        print(f"[url_service] Error fetching links: {e}")
        return []
  

def process_url(url: str, uid: str, dt=None, category_hint: str = None):
    url_metadata = extract_url(url)
    if not url_metadata.get("description"):
        print("❌ Failed to extract URL metadata or no content.")
        return False

    raw_text = url_metadata["description"]
    if url_metadata.get("title"):
        raw_text = url_metadata["title"] + ". " + raw_text

    cleaned_text = processing_content(raw_text)

    try:
        category_name_to_id = get_all_categories_with_ids(uid)
        categories = list(category_name_to_id.keys())
        existing_tags = get_all_tags(uid)

        llm_result = query_llama_summary(cleaned_text, categories, existing_tags)

        name = llm_result["short_name"]
        summary = llm_result["summary"]
        tags = llm_result.get("tags", [])

        # If the user picked a category manually, honour it; otherwise use the AI suggestion
        if category_hint:
            category = category_hint.strip().lower()
            print(f"📌 Using user-provided category hint: '{category}'")
        else:
            category = llm_result["category"].strip().lower()

        # Try exact match first, then fuzzy match for slight differences
        category_id = category_name_to_id.get(category)
        if category_id is None:
            for existing_cat, cat_id in category_name_to_id.items():
                if existing_cat.replace(" ", "") == category.replace(" ", ""):
                    category_id = cat_id
                    category = existing_cat  # use the stored name
                    print(f"🔄 Fuzzy-matched category '{category}'")
                    break

    except Exception as e:
        print("❌ LLM summarization failed:", e)
        return False

    try:
        # Auto-create category if it doesn't exist
        if category_id is None:
            print(f"📂 Category '{category}' not found — creating it automatically")
            # Try local fallback first to avoid back-to-back API calls
            emoji = _fallback_emoji(category)
            if emoji == "📌":
                time.sleep(3)  # Space out from the summary call above
                emoji = suggest_emoji_openrouter(category)
            new_cat_ref = DB.collection('users').document(uid).collection('categories').document()
            new_cat_ref.set({
                'category': category,
                'count': 0,
                'emoji': emoji,
                'visible': True,
                'timestamp': datetime.now()
            })
            category_id = new_cat_ref.id
            print(f"✅ Auto-created category '{category}' with id {category_id}")

        doc_ref = DB.collection('users').document(uid).collection('urls').document()
        doc_ref.set({
            'name': name,
            'url': url,
            'category': category,
            'summary': summary,
            'category_id': category_id,
            'thumbnail': url_metadata["thumbnail"],
            'tags': tags,
            'timestamp': dt if dt else datetime.now()
        })
        url_id = doc_ref.id
        print("✅ URL saved to Firestore.")

        if(update_category_count(uid, category)):
            print(f"category count is updated! ")
        else:
            print(f"category count failed to be updated ")

        if(update_mapping(uid,url_id, category_id)):
            print(f"mapping is updated! ")
        else:
            print(f"mapping failed to be updated ")


        return {
            "success": True,
            "name": name,
            "category": category,
        }

    except Exception as e:
        print("❌ Failed to save to Firestore:", e)
        return False


def delete_link(uid: str, url: str):
    """Delete a link by URL. Removes from urls collection, decrements category count, removes from mapping."""
    try:
        urls_ref = DB.collection("users").document(uid).collection("urls")
        query = urls_ref.where("url", "==", url).limit(1).stream()

        doc = None
        for d in query:
            doc = d
            break

        if not doc:
            return {"success": False, "message": "Link not found"}

        data = doc.to_dict()
        category_name = data.get("category", "")
        category_id = data.get("category_id", "")
        url_id = doc.id

        # 1. Delete the link document
        doc.reference.delete()
        print(f"🗑️ Deleted link: {url[:60]}")

        # 2. Decrement category count
        if category_name:
            cats_ref = DB.collection("users").document(uid).collection("categories")
            cat_query = cats_ref.where("category", "==", category_name).limit(1).stream()
            for cat_doc in cat_query:
                cat_data = cat_doc.to_dict()
                new_count = max(0, cat_data.get("count", 1) - 1)
                cat_doc.reference.update({"count": new_count})
                print(f"📉 Category '{category_name}' count → {new_count}")
                break

        # 3. Remove from mapping
        if category_id:
            mapping_ref = DB.collection("users").document(uid).collection("mapping").document(category_id)
            mapping_doc = mapping_ref.get()
            if mapping_doc.exists:
                urls_list = mapping_doc.to_dict().get("urls", [])
                if url_id in urls_list:
                    urls_list.remove(url_id)
                    mapping_ref.update({"urls": urls_list})
                    print(f"📋 Removed from mapping")

        return {"success": True, "message": "Link deleted successfully"}

    except Exception as e:
        print(f"❌ delete_link error: {e}")
        return {"success": False, "message": f"Error deleting link: {e}"}


def move_link(uid: str, url: str, new_category: str):
    """Move a link to a different category."""
    try:
        urls_ref = DB.collection("users").document(uid).collection("urls")
        query = urls_ref.where("url", "==", url).limit(1).stream()

        doc = None
        for d in query:
            doc = d
            break

        if not doc:
            return {"success": False, "message": "Link not found"}

        data = doc.to_dict()
        old_category = data.get("category", "")
        old_category_id = data.get("category_id", "")
        url_id = doc.id

        # Find new category ID
        new_category_lower = new_category.strip().lower()
        cats_ref = DB.collection("users").document(uid).collection("categories")
        new_cat_query = cats_ref.where("category", "==", new_category_lower).limit(1).stream()

        new_category_id = None
        for cat_doc in new_cat_query:
            new_category_id = cat_doc.id
            break

        if not new_category_id:
            return {"success": False, "message": f"Category '{new_category}' not found"}

        # 1. Update the link document
        doc.reference.update({
            "category": new_category_lower,
            "category_id": new_category_id,
        })
        print(f"🔄 Moved link from '{old_category}' → '{new_category_lower}'")

        # 2. Decrement old category count
        if old_category:
            old_cat_query = cats_ref.where("category", "==", old_category).limit(1).stream()
            for cat_doc in old_cat_query:
                cat_data = cat_doc.to_dict()
                new_count = max(0, cat_data.get("count", 1) - 1)
                cat_doc.reference.update({"count": new_count})
                break

        # 3. Increment new category count
        new_cat_query2 = cats_ref.where("category", "==", new_category_lower).limit(1).stream()
        for cat_doc in new_cat_query2:
            cat_data = cat_doc.to_dict()
            new_count = cat_data.get("count", 0) + 1
            cat_doc.reference.update({"count": new_count})
            break

        # 4. Remove from old mapping
        if old_category_id:
            old_mapping_ref = DB.collection("users").document(uid).collection("mapping").document(old_category_id)
            old_mapping_doc = old_mapping_ref.get()
            if old_mapping_doc.exists:
                urls_list = old_mapping_doc.to_dict().get("urls", [])
                if url_id in urls_list:
                    urls_list.remove(url_id)
                    old_mapping_ref.update({"urls": urls_list})

        # 5. Add to new mapping
        new_mapping_ref = DB.collection("users").document(uid).collection("mapping").document(new_category_id)
        new_mapping_doc = new_mapping_ref.get()
        if new_mapping_doc.exists:
            urls_list = new_mapping_doc.to_dict().get("urls", [])
            if url_id not in urls_list:
                urls_list.append(url_id)
                new_mapping_ref.update({"urls": urls_list})
        else:
            new_mapping_ref.set({"urls": [url_id]})

        return {"success": True, "message": f"Link moved to '{new_category_lower}'"}

    except Exception as e:
        print(f"❌ move_link error: {e}")
        return {"success": False, "message": f"Error moving link: {e}"}


def toggle_favorite(uid: str, url: str):
    """Toggle the is_favorite flag on a link. Returns the new state."""
    try:
        urls_ref = DB.collection("users").document(uid).collection("urls")
        query = urls_ref.where("url", "==", url).limit(1).stream()
        doc = next(iter(query), None)
        if not doc:
            return {"success": False, "is_favorite": False}
        current = doc.to_dict().get("is_favorite", False)
        new_val = not current
        doc.reference.update({"is_favorite": new_val})
        return {"success": True, "is_favorite": new_val}
    except Exception as e:
        print(f"❌ toggle_favorite error: {e}")
        return {"success": False, "is_favorite": False}


def mark_read(uid: str, url: str):
    """Mark a link as read (is_read = True)."""
    try:
        urls_ref = DB.collection("users").document(uid).collection("urls")
        query = urls_ref.where("url", "==", url).limit(1).stream()
        doc = next(iter(query), None)
        if not doc:
            return {"success": False}
        doc.reference.update({"is_read": True})
        return {"success": True}
    except Exception as e:
        print(f"❌ mark_read error: {e}")
        return {"success": False}


def update_link(uid: str, url: str, name: str = None, summary: str = None, tags: list = None, new_category: str = None):
    """Update summary, tags, or category of a link."""
    try:
        urls_ref = DB.collection("users").document(uid).collection("urls")
        query = urls_ref.where("url", "==", url).limit(1).stream()
        doc = next(iter(query), None)
        if not doc:
            return {"success": False, "message": "Link not found"}

        updates = {}
        if name is not None and name.strip():
            updates["name"] = name.strip()
        if summary is not None:
            updates["summary"] = summary.strip()
        if tags is not None:
            clean = [t.strip().lower() for t in tags if isinstance(t, str) and t.strip()]
            updates["tags"] = clean[:3]  # max 3 tags
        if new_category is not None:
            old_data = doc.to_dict()
            old_category = old_data.get("category", "")
            old_category_id = old_data.get("category_id", "")
            new_cat_lower = new_category.strip().lower()

            if new_cat_lower != old_category:
                # Find new category doc
                cats_ref = DB.collection("users").document(uid).collection("categories")
                new_cat_docs = cats_ref.where("category", "==", new_cat_lower).limit(1).stream()
                new_cat_doc = next(iter(new_cat_docs), None)
                if not new_cat_doc:
                    return {"success": False, "message": f"Category '{new_category}' not found"}

                new_category_id = new_cat_doc.id
                updates["category"] = new_cat_lower
                updates["category_id"] = new_category_id

                # Adjust counts
                if old_category:
                    old_cat_docs = cats_ref.where("category", "==", old_category).limit(1).stream()
                    for cd in old_cat_docs:
                        cd_data = cd.to_dict()
                        cd.reference.update({"count": max(0, cd_data.get("count", 1) - 1)})
                new_cat_doc.reference.update({"count": new_cat_doc.to_dict().get("count", 0) + 1})

                # Update mappings
                url_id = doc.id
                if old_category_id:
                    old_map = DB.collection("users").document(uid).collection("mapping").document(old_category_id)
                    old_map_doc = old_map.get()
                    if old_map_doc.exists:
                        ul = old_map_doc.to_dict().get("urls", [])
                        if url_id in ul:
                            ul.remove(url_id)
                            old_map.update({"urls": ul})
                new_map = DB.collection("users").document(uid).collection("mapping").document(new_category_id)
                new_map_doc = new_map.get()
                if new_map_doc.exists:
                    ul = new_map_doc.to_dict().get("urls", [])
                    if url_id not in ul:
                        ul.append(url_id)
                        new_map.update({"urls": ul})
                else:
                    new_map.set({"urls": [url_id]})

        if updates:
            doc.reference.update(updates)

        return {"success": True, "message": "Link updated"}
    except Exception as e:
        print(f"❌ update_link error: {e}")
        return {"success": False, "message": str(e)}


def get_all_tags(uid):
    """Collect all unique tags from the user's saved links."""
    try:
        docs = DB.collection("users").document(uid).collection("urls").stream()
        tags = set()
        for doc in docs:
            data = doc.to_dict()
            doc_tags = data.get("tags", [])
            if isinstance(doc_tags, list):
                for t in doc_tags:
                    if isinstance(t, str) and t.strip():
                        tags.add(t.strip().lower())
        return list(tags)
    except Exception as e:
        print(f"❌ Failed to fetch tags: {e}")
        return []


def get_all_categories_with_ids(uid):
    try:
        docs = DB.collection("users").document(uid).collection("categories").stream()
        return {
            doc.to_dict().get("category").strip().lower(): doc.id
            for doc in docs if doc.to_dict().get("category")
        }
    except Exception as e:
        print("❌ Failed to fetch categories:", e)
        return {}
    
def get_category_id_by_name(uid, category_name):
    try:
        query = DB.collection("users").document(uid).collection("categories") \
           .where("category", "==", category_name.strip()).limit(1).stream()

        for doc in query:
            return doc.id  

        return None  

    except Exception as e:
        print("❌ Failed to fetch category ID:", e)
        return None
    

def get_urls_by_category(uid, category_id):
    try:

        doc_ref = DB.collection("users").document(uid).collection("mapping").document(category_id)
        doc = doc_ref.get()
        if doc.exists:
            data = doc.to_dict()
            return data.get("urls", [])
        else:
            print("❌ Category mapping not found.")
            return []
    except Exception as e:
        print("❌ Failed to fetch URLs by category:", e)
        return []
    

def get_urls_by_ids(uid, url_ids):
    try:
        results = []
        urls_collection = DB.collection("users").document(uid).collection("urls")

        for url_id in url_ids:
            doc = urls_collection.document(url_id).get()
            if doc.exists:
                results.append(doc.to_dict())

        return results

    except Exception as e:
        print("❌ Failed to fetch URLs:", e)
        return []
