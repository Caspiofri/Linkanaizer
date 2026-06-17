import json
import os
import time
import requests
import re
from dotenv import load_dotenv

load_dotenv()

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
if not OPENROUTER_API_KEY:
    raise RuntimeError("OPENROUTER_API_KEY not set in .env")


def _post_with_retry(url, headers, json_data, timeout=15, max_retries=5, initial_delay=5):
    """POST request with exponential backoff retry on 429 (Too Many Requests).

    Returns the requests.Response object. Only retries on 429 status codes;
    all other responses (success or error) are returned immediately.
    Raises the last exception if all retries are exhausted on 429.
    """
    last_response = None
    for attempt in range(max_retries + 1):  # 0 = initial attempt, 1..max_retries = retries
        response = requests.post(url, headers=headers, json=json_data, timeout=timeout)
        if response.status_code != 429:
            return response
        # Got a 429 — decide whether to retry
        last_response = response
        if attempt < max_retries:
            delay = initial_delay * (2 ** attempt)  # 5s, 10s, 20s, 40s, 80s
            print(f"Rate limited (429). Retry {attempt + 1}/{max_retries} in {delay}s...")
            time.sleep(delay)
        else:
            print(f"Rate limited (429). All {max_retries} retries exhausted.")
    return last_response


def suggest_emoji_openrouter(category: str) -> str:
    print(f"searching for emoji for category: {category}")
    url = "https://openrouter.ai/api/v1/chat/completions"
    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "Content-Type": "application/json"
    }
    data = {
        "model": "google/gemma-3-4b-it",
        "messages": [
            {
                "role": "user",
                "content": f"Respond with exactly ONE emoji that best represents the category '{category}'. Reply with only the emoji character, nothing else."
            }
        ]
    }

    try:
        # Use fewer retries for emoji — it's non-critical and has a local fallback
        response = _post_with_retry(url, headers, json_data=data, timeout=15, max_retries=1, initial_delay=3)
        print("emoji response status:", response.status_code)

        if response.status_code == 200:
            raw = response.json()["choices"][0]["message"]["content"].strip()
            # Take only the first character/emoji in case the LLM returns extra text
            import emoji as emoji_lib
            emojis_found = [c for c in raw if emoji_lib.is_emoji(c)]
            if emojis_found:
                result = emojis_found[0]
                print(f"emoji found: {result}")
                return result
            # If no emoji detected but response is short, try returning it directly
            if len(raw) <= 4:
                print(f"emoji raw: {raw}")
                return raw
            print(f"LLM returned non-emoji text: {raw}")
            return _fallback_emoji(category)
        else:
            print(f"Emoji API error ({response.status_code}): {response.text}")
            return _fallback_emoji(category)
    except Exception as e:
        print(f"Emoji API exception: {e}")
        return _fallback_emoji(category)


def _fallback_emoji(category: str) -> str:
    """Return a reasonable emoji based on common category names."""
    mapping = {
        "fitness": "💪", "workout": "💪", "exercise": "💪", "gym": "🏋️",
        "cooking": "🍳", "food": "🍕", "recipe": "🍽️", "recipes": "🍽️",
        "tech": "💻", "technology": "💻", "programming": "👨‍💻", "coding": "👨‍💻",
        "music": "🎵", "travel": "✈️", "health": "❤️", "science": "🔬",
        "news": "📰", "sports": "⚽", "gaming": "🎮", "art": "🎨",
        "education": "📚", "business": "💼", "finance": "💰", "shopping": "🛒",
        "fashion": "👗", "beauty": "💄", "movies": "🎬", "books": "📖",
        "ai": "🤖", "design": "🎨", "productivity": "📋", "video": "📹",
    }
    lower = category.lower().strip()
    return mapping.get(lower, "📌")



def query_llama_summary(url_metadata_desc, categories, existing_tags=None):
    print("running lamma")
    tags_section = ""
    if existing_tags:
        tags_section = f"""
        Existing tags (reuse when relevant): {', '.join(existing_tags)}"""

    prompt = f"""
        You are a categorization assistant.

        Your ONLY job is to return a JSON response with EXACTLY this structure:
        {{
        "summary": "<one sentence summary under 20 words>",
        "short_name": "<short name for the link (1–5 words, human-friendly, theme-related)>",
        "category": "<category>",
        "tags": ["<tag1>", "<tag2>"]
        }}

        DO NOT return code, explanations, or any other text outside the JSON structure.

        1. Summarize this metadata in one concise sentence (under 20 words).
        Write the summary as a direct description — do NOT start with "This link is", "This is", "This page" or similar.
        Just describe the content directly. For example: "Quick 15-minute full body workout with no equipment needed."
        \"\"\"
        {url_metadata_desc}
        \"\"\"

        2. Give a human-friendly short name that reflects the content or theme of the URL.
        It should be 1–5 words. Examples:
        - "Morning Yoga"
        - "Crypto Trends"
        - "HTML Tutorial"
        - "Chest Day Routine"

        3. Choose one category that is **directly relevant** to the content.
        DO NOT choose categories just because they are available in the list.
        Only include categories that clearly reflect the subject, goal, or theme of the content.
        If NONE of the available categories fit the content well, you may suggest a NEW short category name (1-2 words, lowercase).

        Available categories:
        {categories if categories else "(none yet — suggest a new category)"}

        4. Pick 1-2 short keyword tags (1-2 words each, lowercase) that describe the specific content.
        Tags help filter links within a category. For example, a "workout" category might have tags like "chest", "legs", "cardio", "home workout".
        Reuse existing tags when they fit. Only create new tags if none of the existing ones are relevant.
        Return 0 tags if the content is too generic to tag meaningfully. Maximum 2 tags.{tags_section}
"""
    print("prompt:", prompt)

    try:
        response = _post_with_retry(
            "https://openrouter.ai/api/v1/chat/completions",
            headers={
                "Authorization": f"Bearer {OPENROUTER_API_KEY}",
                "Content-Type": "application/json",
                "HTTP-Referer": "YOUR_SITE_URL",  # Replace with your actual site URL
                "X-Title": "YOUR_SITE_NAME"  # Replace with your actual site name
            },
            json_data={
                "model": "google/gemma-3-4b-it",
                "messages": [{"role": "user", "content": prompt}]
            }
        )

        # Check if request was successful
        response.raise_for_status()
        
        # Parse the response
        result = response.json()
        
        # Extract the content from the response
        content = result["choices"][0]["message"]["content"]
        print("Raw response:", content)
        
        # Parse the content as JSON
        try:
            parsed_json = extract_json(content)
            print("parsed_json:", parsed_json)

            # Verify that the JSON has the required fields
            if "summary" not in parsed_json or "category" not in parsed_json or "short_name" not in parsed_json:
                raise ValueError("Response is missing required fields")

            # Validate and clean tags
            raw_tags = parsed_json.get("tags", [])
            if not isinstance(raw_tags, list):
                raw_tags = []
            # Lowercase, strip, max 2 words per tag, max 2 tags
            clean_tags = []
            for t in raw_tags[:2]:
                if isinstance(t, str):
                    tag = t.strip().lower()
                    if tag and len(tag.split()) <= 2:
                        clean_tags.append(tag)
            parsed_json["tags"] = clean_tags
            print(f"tags: {clean_tags}")

            return parsed_json
            
        except json.JSONDecodeError:
            print("Failed to parse response as JSON")
            print(f"Response was:\n{content}")
            return {"summary": "Failed to generate summary", "categories": ["Error"]}
            
    except requests.exceptions.RequestException as e:
        print(f"API request failed: {e}")
        return {"summary": "API request failed", "categories": ["Error"]}
    

def judge_classification(url_metadata_text: str, llm_result: dict) -> dict:
    """Second LLM call that verifies the classification output. Returns verdict + reasoning."""
    category = llm_result.get("category", "")
    tags = llm_result.get("tags", [])
    summary = llm_result.get("summary", "")
    tags_str = ", ".join(tags) if tags else "none"

    prompt = f"""You are a classification quality checker.

A URL was analyzed and classified:
- Category: "{category}"
- Tags: {tags_str}
- Summary: "{summary}"

Source content:
\"\"\"{url_metadata_text[:500]}\"\"\"

Is this category accurate for this content?
Reply with ONLY valid JSON, nothing else:
{{"verdict": "YES", "reasoning": "<one sentence>"}} or {{"verdict": "NO", "reasoning": "<one sentence>"}}\
"""

    try:
        t0 = time.time()
        response = _post_with_retry(
            "https://openrouter.ai/api/v1/chat/completions",
            headers={
                "Authorization": f"Bearer {OPENROUTER_API_KEY}",
                "Content-Type": "application/json",
            },
            json_data={
                "model": "google/gemma-3-4b-it",
                "messages": [{"role": "user", "content": prompt}],
            },
            timeout=15,
            max_retries=1,
            initial_delay=3,
        )
        latency_ms = int((time.time() - t0) * 1000)

        if response.status_code != 200:
            print(f"Judge API error ({response.status_code})")
            return {"verdict": "UNKNOWN", "reasoning": "Judge call failed", "latency_ms": latency_ms}

        content = response.json()["choices"][0]["message"]["content"]
        print(f"Judge raw response: {content}")
        parsed = extract_json(content)
        verdict = parsed.get("verdict", "UNKNOWN").upper()
        if verdict not in ("YES", "NO"):
            verdict = "UNKNOWN"
        return {
            "verdict": verdict,
            "reasoning": parsed.get("reasoning", ""),
            "latency_ms": latency_ms,
        }
    except Exception as e:
        print(f"Judge call failed: {e}")
        return {"verdict": "UNKNOWN", "reasoning": str(e), "latency_ms": 0}


# Enhanced JSON extraction
def extract_json(text):
    """Extract JSON from text with fallback methods"""
    text = text.strip()
    
    # Method 1: Direct parse (if response is clean JSON)
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass
        
    # Method 2: Remove markdown code blocks
    clean_text = re.sub(r'``````', '', text)
    try:
        return json.loads(clean_text)
    except json.JSONDecodeError:
        pass
        
    # Method 3: Find first/last JSON substring
    json_match = re.search(r'\{[\s\S]*\}', text)
    if json_match:
        try:
            return json.loads(json_match.group())
        except json.JSONDecodeError:
            pass
            
    raise ValueError("No valid JSON found in response")