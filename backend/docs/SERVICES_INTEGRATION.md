# Services Integration Guide

This document explains how the `scraping_service` and `llm_service` work together to classify links.

## Overview

The integration flow:
1. **Scraping Service** → Fetches raw HTML from a URL
2. **LLM Service** → Parses HTML into structured JSON matching `LinkClassification` schema
3. **Link Classification Service** → Orchestrates the workflow

## Service Interaction Flow

```
┌─────────────────┐
│  API Endpoint   │
│  /api/v1/links/ │
│    /classify    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│ LinkClassificationService   │
│  classify_link()            │
└────────┬────────────────────┘
         │
         ├─────────────────┐
         │                 │
         ▼                 ▼
┌─────────────────┐  ┌─────────────────┐
│ ScrapingService │  │   LLMService     │
│ scrape_url()    │  │ parse_html_to_   │
│                 │  │ classification() │
│ Returns: HTML   │  │                  │
└────────┬────────┘  │ Returns:         │
         │            │ LinkClassification│
         │            └───────────────────┘
         │
         └──────────────┐
                        │
                        ▼
              ┌──────────────────┐
              │ LinkClassification│
              │ (Pydantic Schema) │
              └───────────────────┘
```

## Scraping Service

**File:** `app/services/scraping_service.py`

### Key Method: `scrape_url()`

```python
async def scrape_url(url: str, return_html: bool = True, **kwargs) -> str
```

- **Input:** URL string
- **Output:** Raw HTML string
- **Behavior:** 
  - Calls scraping API with `format: "html"`
  - Extracts HTML from API response
  - Returns raw HTML content

### Example Usage:
```python
html = await scraping_service.scrape_url("https://example.com")
# Returns: "<html>...</html>"
```

## LLM Service

**File:** `app/services/llm_service.py`

### Key Method: `parse_html_to_classification()`

```python
async def parse_html_to_classification(
    html: str,
    url: str,
    categories: Optional[List[str]] = None,
) -> LinkClassification
```

- **Input:** Raw HTML string, URL, optional categories
- **Output:** Validated `LinkClassification` Pydantic model
- **Behavior:**
  - Sends HTML to LLM with strict JSON schema requirements
  - Uses provider-specific JSON modes for guaranteed valid JSON
  - Validates response against Pydantic schema
  - Raises `ValueError` if validation fails

### JSON Validation Strategy

The service ensures strict JSON output using multiple techniques:

#### 1. **OpenAI/Groq Providers:**
- Uses `response_format: {"type": "json_object"}` to force JSON mode
- Extracts JSON from response text
- Validates with Pydantic schema

#### 2. **Anthropic Provider:**
- Uses structured outputs (Claude 3.5+) with `response_format` containing JSON schema
- Falls back to JSON extraction if structured outputs unavailable
- Validates with Pydantic schema

#### 3. **Fallback Method:**
- Extracts JSON from markdown code blocks or plain text
- Uses regex to find JSON objects
- Validates with Pydantic schema

### JSON Extraction Logic

The `_extract_json_from_text()` method handles:
- Markdown code blocks: `` ```json {...} ``` ``
- Plain JSON objects: `{...}`
- Text with JSON embedded: Extracts first JSON object found

### Example Usage:
```python
classification = await llm_service.parse_html_to_classification(
    html="<html>...</html>",
    url="https://example.com",
    categories=["article", "product", "video"],
)
# Returns: LinkClassification(
#     category="article",
#     summary="...",
#     keywords=["keyword1", "keyword2"]
# )
```

## Link Classification Schema

**File:** `app/schemas/link_classification.py`

### Schema Definition:
```python
class LinkClassification(BaseModel):
    category: str      # 1-100 characters
    summary: str       # 10-500 characters
    keywords: List[str]  # 1-20 items
```

### Validation:
- **Pydantic validation** ensures type safety
- **Field constraints** enforce length/quantity limits
- **JSON schema** generated automatically for LLM instructions

## Link Classification Service

**File:** `app/services/link_classification_service.py`

### Key Method: `classify_link()`

Orchestrates the full workflow:

```python
async def classify_link(
    url: str,
    categories: Optional[List[str]] = None,
) -> LinkClassification
```

**Workflow:**
1. Calls `scraping_service.scrape_url()` → Gets HTML
2. Calls `llm_service.parse_html_to_classification()` → Gets structured data
3. Returns validated `LinkClassification` object

## API Endpoint

**File:** `app/api/v1/endpoints/links.py`

### Endpoint: `POST /api/v1/links/classify`

**Request:**
```json
{
  "url": "https://example.com/article",
  "categories": ["article", "product", "video"]
}
```

**Response:**
```json
{
  "category": "article",
  "summary": "A comprehensive guide to FastAPI best practices",
  "keywords": ["fastapi", "python", "web development", "api"]
}
```

## Error Handling

### Scraping Errors:
- `httpx.HTTPError`: Scraping API failures
- Handled at service level, propagated to API

### LLM Errors:
- `ValueError`: JSON parsing or validation failures
- Multiple retry strategies for JSON extraction
- Clear error messages indicating what went wrong

### Validation Errors:
- Pydantic validation ensures schema compliance
- Automatic error messages for invalid fields

## Ensuring Strict JSON Output

The system uses multiple layers to ensure valid JSON:

1. **Provider JSON Modes:**
   - OpenAI: `response_format: {"type": "json_object"}`
   - Anthropic: Structured outputs with JSON schema
   - Groq: Same as OpenAI

2. **System Prompts:**
   - Explicit instructions to return ONLY JSON
   - JSON schema included in prompt
   - No markdown formatting allowed

3. **Post-Processing:**
   - Regex extraction of JSON from text
   - Handles markdown code blocks
   - Strips non-JSON content

4. **Pydantic Validation:**
   - Final validation against schema
   - Type checking and constraint enforcement
   - Clear error messages for invalid data

## Example: Complete Workflow

```python
from app.services.link_classification_service import link_classification_service

# Classify a link
result = await link_classification_service.classify_link(
    url="https://example.com/article",
    categories=["article", "blog", "news"],
)

print(result.category)   # "article"
print(result.summary)    # "A guide to..."
print(result.keywords)   # ["guide", "tutorial", ...]
```

## Testing

To test the integration:

1. **Unit Tests:** Test each service independently
2. **Integration Tests:** Test the full workflow
3. **Mock Services:** Mock scraping API and LLM responses
4. **Schema Validation:** Test with various HTML inputs

## Configuration

Environment variables required:
- `SCRAPING_API_KEY`: API key for scraping service
- `LLM_API_KEY`: API key for LLM provider
- `LLM_PROVIDER`: Provider name (openai, anthropic, groq)
- `LLM_MODEL`: Model name (e.g., "gpt-4", "claude-3-5-sonnet")

See `.env.example` for all configuration options.
