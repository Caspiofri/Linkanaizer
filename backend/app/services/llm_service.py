"""
Service for interacting with LLM providers.
"""
import json
import re
import logging
import httpx
from typing import Optional, Dict, Any, List

from app.core.config import settings
from app.schemas.link_classification import LinkClassification

logger = logging.getLogger(__name__)


class LLMService:
    """Service for LLM operations."""

    def __init__(self):
        self.provider = settings.LLM_PROVIDER.lower()
        self.api_key = settings.LLM_API_KEY
        self.model = settings.LLM_MODEL
        self.temperature = settings.LLM_TEMPERATURE
        self.max_tokens = settings.LLM_MAX_TOKENS
        self.base_url = settings.LLM_API_URL or self._get_default_url()

    def _get_default_url(self) -> str:
        """Get default API URL based on provider."""
        url_map = {
            "openai": "https://api.openai.com/v1",
            "anthropic": "https://api.anthropic.com/v1",
            "groq": "https://api.groq.com/openai/v1",
            "google": "https://generativelanguage.googleapis.com/v1beta",
        }
        return url_map.get(self.provider, "https://api.openai.com/v1")

    async def generate_completion(
        self,
        prompt: str,
        system_prompt: Optional[str] = None,
        temperature: Optional[float] = None,
        max_tokens: Optional[int] = None,
    ) -> Dict[str, Any]:
        """
        Generate a completion using the LLM.

        Args:
            prompt: The user prompt
            system_prompt: Optional system prompt
            temperature: Override default temperature
            max_tokens: Override default max tokens

        Returns:
            Dictionary containing the LLM response
        """
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        # Build request payload based on provider
        if self.provider == "anthropic":
            messages = []
            if system_prompt:
                messages.append({"role": "system", "content": system_prompt})
            messages.append({"role": "user", "content": prompt})

            payload = {
                "model": self.model,
                "messages": messages,
                "temperature": temperature or self.temperature,
                "max_tokens": max_tokens or self.max_tokens,
            }
            endpoint = f"{self.base_url}/messages"
        else:
            # OpenAI-compatible format
            messages = []
            if system_prompt:
                messages.append({"role": "system", "content": system_prompt})
            messages.append({"role": "user", "content": prompt})

            payload = {
                "model": self.model,
                "messages": messages,
                "temperature": temperature or self.temperature,
                "max_tokens": max_tokens or self.max_tokens,
            }
            endpoint = f"{self.base_url}/chat/completions"

        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                endpoint,
                json=payload,
                headers=headers,
            )
            response.raise_for_status()
            return response.json()

    async def classify_content(
        self, content: str, categories: List[str]
    ) -> Dict[str, Any]:
        """
        Classify content into categories using the LLM.

        Args:
            content: The content to classify
            categories: List of possible categories

        Returns:
            Dictionary containing classification result
        """
        system_prompt = f"""You are a content classification assistant.
        Classify the given content into one of these categories: {', '.join(categories)}.
        Return only the category name, nothing else."""

        result = await self.generate_completion(
            prompt=content,
            system_prompt=system_prompt,
            temperature=0.3,  # Lower temperature for classification
        )

        return result

    async def parse_html_to_classification(
        self,
        html: str,
        url: str,
        categories: Optional[List[str]] = None,
    ) -> LinkClassification:
        """
        Parse HTML content into a LinkClassification schema using structured JSON output.
        Ensures strict JSON validation matching the Pydantic schema.

        Args:
            html: Raw HTML content to parse
            url: The URL being classified (for context)
            categories: Optional list of allowed categories

        Returns:
            LinkClassification object validated against the schema

        Raises:
            ValueError: If the LLM response cannot be parsed or validated
        """
        logger.info(f"Parsing HTML to classification for URL: {url}")
        logger.debug(f"HTML length: {len(html)} characters, Categories: {categories}")
        
        # Build the JSON schema from Pydantic model
        json_schema = LinkClassification.model_json_schema()
        
        # Create system prompt with strict JSON requirements and category enforcement
        if categories:
            categories_list = ', '.join([f"'{cat}'" for cat in categories])
            category_instruction = f"\n\nCRITICAL: The 'category' field MUST be exactly one of these values: {categories_list}. Do not use any other category name."
        else:
            category_instruction = ""
        
        system_prompt = f"""You are a web content analyzer. Analyze the provided HTML content and extract:
1. Category: The type of content. Must be one of the allowed categories if specified.{category_instruction}
2. Category Emoji: A single relevant emoji that represents the category. Choose an appropriate emoji based on the category type:
   - Fitness/Workout: 💪 🏋️ 🤸
   - Video/Media: 🎥 📹 🎬
   - Product: 📱 🛍️ 💻
   - Document/Article: 📄 📝 📚
   - Tutorial/Education: 🎓 📖 ✏️
   - Food: 🍕 🍔 🍎
   - Travel: ✈️ 🗺️ 🏖️
   - Technology: 💻 🔧 ⚙️
   - Music: 🎵 🎸 🎤
   - Sports: ⚽ 🏀 🎾
   - Other: Choose the most relevant single emoji
   - Return only ONE emoji character (not multiple)
3. Title: A short, concise title (up to 3 lines, max 200 characters) that describes the content in a minimal and viewer-friendly way. 
   - Make it direct, engaging, and immediately clear what the content is about
   - Examples: "Advanced Calisthenics Workout", "Product Review: iPhone 15", "Python Tutorial: Async Programming"
   - Format: Just the title itself, no extra words like "Title:" or "About:"
3. Summary: A focused, direct description of the content (10-500 characters). 
   - CRITICAL RULE: Start DIRECTLY with the main content - NO introductory phrases whatsoever
   - FORBIDDEN phrases (DO NOT USE): 
     * "This Instagram reel from..."
     * "This video shows..."
     * "This post features..."
     * "This content is about..."
     * "In this video..."
     * "This is a..."
     * "This reel showcases..."
     * "This post demonstrates..."
   - CORRECT format: Start immediately with what the content contains or demonstrates
   - Examples of CORRECT summaries:
     * "Advanced bodyweight exercises focusing on pull-ups, dips, and muscle-ups. Demonstrates proper form and progression techniques."
     * "Comprehensive review of the iPhone 15 Pro, covering camera improvements, performance benchmarks, and battery life tests."
     * "Step-by-step tutorial on async/await in Python, including error handling and best practices for concurrent operations."
     * "Calisthenics training session with pull-up variations and muscle-up progressions. Includes form tips and scaling options."
   - Examples of INCORRECT summaries (NEVER USE):
     * "This Instagram reel from @fitnesscoach showcases an advanced calisthenics workout..."
     * "This video shows a comprehensive review of the iPhone 15 Pro..."
     * "In this post, we'll explore async programming in Python..."
     * "This reel demonstrates proper form for pull-ups and muscle-ups..."
4. Keywords: 1-20 relevant keywords extracted from the content

IMPORTANT: The JSON response must include 'category', 'category_emoji', 'title', 'summary', and 'keywords' fields.

CRITICAL INSTRUCTIONS:
- Identify the category from the list provided in the request (if categories are specified)
- Title: Minimal, clear, viewer-friendly (no unnecessary words, no "This is..." or "About...")
- Summary: MUST start with the actual content description, never with meta-commentary about the content
- Return ONLY the JSON object - no markdown, no backticks, no explanatory text
- The JSON must match this exact schema:
{json.dumps(json_schema, indent=2)}

Return the output strictly as a valid JSON object. Do not include markdown formatting or backticks."""

        # Limit HTML to avoid token limits
        html_preview = html[:50000]
        logger.debug(f"HTML preview (first 500 chars): {html_preview[:500]}")
        
        # Simplified prompt structure for Gemini 2.5 Flash
        if categories:
            categories_str = ', '.join(categories)
            user_prompt = f"""Identify the category from this list: [{categories_str}] and analyze this link content: {url}

{html_preview}

Return JSON with 'category', 'title', 'summary', and 'keywords' fields.

Title Requirements:
- Short, minimal, viewer-friendly (up to 3 words, max 200 characters)
- Direct description of what the content is (e.g., "Calisthenics Workout", "Product Review", "Tutorial")
- No unnecessary words or meta-descriptions

Summary Requirements:
- CRITICAL: Start DIRECTLY with the main content description - NO introductory phrases
- FORBIDDEN phrases (NEVER START WITH): "This video shows...", "This Instagram reel from...", "This post features...", "In this content...", "This is a...", "This reel showcases...", "This post demonstrates...", "This content is about..."
- CORRECT format examples (start immediately with content):
  * "Advanced bodyweight exercises focusing on pull-ups and muscle-ups. Demonstrates proper form."
  * "iPhone 15 Pro review covering camera improvements, performance benchmarks, and battery life."
  * "Python async/await tutorial with error handling and best practices for concurrent operations."
  * "Calisthenics training session with pull-up variations and muscle-up progressions."
- INCORRECT format (NEVER USE):
  * "This Instagram reel from @fitnesscoach showcases an advanced calisthenics workout..."
  * "This video shows a comprehensive review of the iPhone 15 Pro..."
  * "In this post, we'll explore async programming in Python..."
- Rule: Start with WHAT the content is, not HOW it's presented or WHERE it's from"""
        else:
            user_prompt = f"""Analyze this HTML content from {url} and provide a classification:

{html_preview}

Return JSON with 'category', 'category_emoji', 'title', 'summary', and 'keywords' fields.

Category Emoji Requirements:
- A single relevant emoji that represents the category
- Examples: 💪 (fitness), 🎥 (video), 📱 (product), 📄 (document), 🎓 (education)
- Return only ONE emoji character

Title Requirements:
- Short, minimal, viewer-friendly (up to 3 words, max 200 characters)
- Direct description of what the content is

Summary Requirements:
- Start DIRECTLY with the main content description
- DO NOT use introductory phrases like "This video shows..." or "This Instagram reel from..."
- Example format: "Advanced bodyweight exercises focusing on pull-ups and muscle-ups. Demonstrates proper form."
- Be concise, focused, and immediately informative"""

        # Use JSON mode for OpenAI-compatible providers
        if self.provider == "openai" or self.provider == "groq":
            result = await self._generate_json_completion_openai(
                prompt=user_prompt,
                system_prompt=system_prompt,
                json_schema=json_schema,
            )
        elif self.provider == "anthropic":
            result = await self._generate_json_completion_anthropic(
                prompt=user_prompt,
                system_prompt=system_prompt,
                json_schema=json_schema,
            )
        elif self.provider == "google":
            result = await self._generate_json_completion_google(
                prompt=user_prompt,
                system_prompt=system_prompt,
                json_schema=json_schema,
            )
        else:
            # Fallback for other providers
            result = await self._generate_json_completion_fallback(
                prompt=user_prompt,
                system_prompt=system_prompt,
            )

        # Validate and parse with Pydantic
        try:
            classification = LinkClassification(**result)
            
            # Validate category is in allowed list if categories were provided
            if categories and classification.category not in categories:
                raise ValueError(
                    f"Category '{classification.category}' is not in the allowed list: {categories}"
                )
            
            return classification
        except ValueError as e:
            # Re-raise ValueError as-is (for category validation)
            raise
        except Exception as e:
            raise ValueError(f"Failed to validate LLM response against schema: {e}") from e

    async def generate_category_emoji(self, category_name: str) -> str:
        """
        Generate a single relevant emoji for a given category name.

        This is used when a new logical category is created, independent
        of any specific link/content.
        """
        logger.info(f"Generating emoji for category: {category_name}")

        json_schema: Dict[str, Any] = {
            "type": "object",
            "properties": {
                "emoji": {
                    "type": "string",
                    "minLength": 1,
                    "maxLength": 10,
                    "description": "A single emoji character representing the category",
                }
            },
            "required": ["emoji"],
            "additionalProperties": False,
        }

        system_prompt = (
            "You are an assistant that assigns a single emoji to represent a category.\n"
            "Given a category name, choose ONE emoji that best represents it.\n"
            "Return ONLY JSON matching the provided schema, no markdown or explanation."
        )

        user_prompt = f"""
Category name: "{category_name}"

Rules:
- Choose exactly ONE emoji character that best represents this category.
- The emoji should be intuitive and easy to understand at a glance.
- Do not include any text besides the emoji in the 'emoji' field.
"""

        # Use the same provider-specific JSON completion helpers
        if self.provider == "openai" or self.provider == "groq":
            result = await self._generate_json_completion_openai(
                prompt=user_prompt,
                system_prompt=system_prompt,
                json_schema=json_schema,
            )
        elif self.provider == "anthropic":
            result = await self._generate_json_completion_anthropic(
                prompt=user_prompt,
                system_prompt=system_prompt,
                json_schema=json_schema,
            )
        elif self.provider == "google":
            result = await self._generate_json_completion_google(
                prompt=user_prompt,
                system_prompt=system_prompt,
                json_schema=json_schema,
            )
        else:
            result = await self._generate_json_completion_fallback(
                prompt=user_prompt,
                system_prompt=system_prompt,
            )

        emoji = result.get("emoji")
        if not isinstance(emoji, str) or not emoji.strip():
            raise ValueError(f"LLM did not return a valid emoji for category '{category_name}': {result}")

        # In case the model returns more than one character, keep the first glyph
        emoji = emoji.strip()
        logger.info(f"Generated emoji '{emoji}' for category '{category_name}'")
        return emoji

    async def _generate_json_completion_openai(
        self,
        prompt: str,
        system_prompt: str,
        json_schema: Dict[str, Any],
    ) -> Dict[str, Any]:
        """Generate JSON completion using OpenAI-compatible API with JSON mode."""
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        payload = {
            "model": self.model,
            "messages": messages,
            "temperature": 0.3,  # Lower temperature for structured output
            "max_tokens": self.max_tokens,
            "response_format": {"type": "json_object"},  # Force JSON mode
        }

        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{self.base_url}/chat/completions",
                json=payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()
            
            # Extract JSON from response
            content = result["choices"][0]["message"]["content"]
            return self._extract_json_from_text(content)

    async def _generate_json_completion_anthropic(
        self,
        prompt: str,
        system_prompt: str,
        json_schema: Dict[str, Any],
    ) -> Dict[str, Any]:
        """Generate JSON completion using Anthropic API with structured outputs."""
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "anthropic-version": "2023-06-01",
        }

        messages = [{"role": "user", "content": prompt}]

        # Try structured outputs first (Claude 3.5+)
        payload = {
            "model": self.model,
            "max_tokens": self.max_tokens,
            "temperature": 0.3,
            "system": system_prompt,
            "messages": messages,
        }

        # Add structured outputs if supported
        if "claude-3" in self.model.lower() or "claude-3.5" in self.model.lower():
            payload["response_format"] = {
                "type": "json_schema",
                "json_schema": {
                    "name": "link_classification",
                    "strict": True,
                    "schema": json_schema,
                },
            }

        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{self.base_url}/messages",
                json=payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()
            
            # Extract JSON from response
            if "content" in result and len(result["content"]) > 0:
                content = result["content"][0].get("text", "")
                # If structured outputs were used, extract from the structured content
                if "type" in result["content"][0] and result["content"][0]["type"] == "json":
                    return json.loads(result["content"][0]["json"])
                return self._extract_json_from_text(content)
            else:
                raise ValueError("No content in Anthropic response")

    async def _generate_json_completion_fallback(
        self,
        prompt: str,
        system_prompt: str,
    ) -> Dict[str, Any]:
        """Fallback method for providers without JSON mode support."""
        result = await self.generate_completion(
            prompt=prompt,
            system_prompt=system_prompt,
            temperature=0.3,
        )
        
        # Extract content from response
        if isinstance(result, dict):
            content = result.get("choices", [{}])[0].get("message", {}).get("content", "")
            if not content:
                content = result.get("content", "")
        else:
            content = str(result)
        
        return self._extract_json_from_text(content)

    async def _generate_json_completion_google(
        self,
        prompt: str,
        system_prompt: str,
        json_schema: Dict[str, Any],
    ) -> Dict[str, Any]:
        """Generate JSON completion using Google Gemini API (v1beta for 2.5 models)."""
        
        headers = {
            "Content-Type": "application/json",
        }

        # Google Gemini uses API key as query parameter
        params = {
            "key": self.api_key,
        }

        # Combine system prompt and user prompt
        full_prompt = f"{system_prompt}\n\n{prompt}"
        
        # Log the prompt being sent (truncated for readability)
        logger.info(f"Sending request to Gemini API. Prompt length: {len(full_prompt)} characters")
        logger.debug(f"Gemini prompt preview: {full_prompt[:500]}...")

        # Payload for Gemini 2.5 Flash with response_mime_type
        payload = {
            "contents": [{
                "parts": [{
                    "text": full_prompt
                }]
            }],
            "generationConfig": {
                "response_mime_type": "application/json"
            }
        }

        # Use v1beta endpoint for Gemini 2.5 models
        endpoint = f"{self.base_url}/models/{self.model}:generateContent"
        logger.debug(f"Gemini API endpoint: {endpoint}")
        logger.debug(f"Gemini API key: {self.api_key[:10]}... (truncated)")
        logger.debug(f"Payload structure: {json.dumps(payload, indent=2)}")

        # Use 30-second timeout as requested
        async with httpx.AsyncClient(timeout=30.0) as client:
            try:
                response = await client.post(
                    endpoint,
                    json=payload,
                    headers=headers,
                    params=params,
                )
                response.raise_for_status()
                
                # Debug: Print raw response for troubleshooting
                print(f"DEBUG: Response from Gemini - Status: {response.status_code}")
                print(f"DEBUG: Response from Gemini - Headers: {dict(response.headers)}")
                print(f"DEBUG: Response from Gemini - Text: {response.text[:1000]}")
                
                result = response.json()
                
                # Extract JSON from response
                if "candidates" in result and len(result["candidates"]) > 0:
                    candidate = result["candidates"][0]
                    content_parts = candidate.get("content", {}).get("parts", [])
                    if not content_parts:
                        logger.error("Gemini API returned no parts in response")
                        raise ValueError("No content parts in Google API response")
                    
                    content = content_parts[0].get("text", "")
                    if not content:
                        logger.error("Gemini API returned empty text in response")
                        raise ValueError("No content text in Google API response")
                    
                    logger.info(f"Successfully received response from Gemini API. Content length: {len(content)}")
                    logger.debug(f"Gemini response preview: {content[:200]}...")
                    
                    # Parse JSON with fallback for markdown code blocks
                    return self._extract_json_from_text(content)
                else:
                    logger.error(f"Gemini API returned no candidates. Response: {result}")
                    raise ValueError("No candidates in Google API response")
            except httpx.HTTPStatusError as e:
                error_msg = f"Gemini API error: {e.response.status_code}"
                try:
                    error_body = e.response.json()
                    error_detail = error_body.get("error", {}).get("message", str(error_body))
                    error_msg = f"Gemini API error {e.response.status_code}: {error_detail}"
                except:
                    error_msg = f"Gemini API error {e.response.status_code}: {e.response.text[:200]}"
                logger.error(f"{error_msg}. Endpoint: {endpoint}")
                raise ValueError(error_msg) from e
            except httpx.RequestError as e:
                error_msg = f"Network error connecting to Gemini API: {str(e)}"
                logger.error(error_msg)
                raise ValueError(error_msg) from e

    def _extract_json_from_text(self, text: str) -> Dict[str, Any]:
        """
        Extract JSON from text, handling markdown code blocks and plain JSON.
        Strips markdown backticks before parsing.
        
        Args:
            text: Text that may contain JSON
            
        Returns:
            Parsed JSON dictionary
            
        Raises:
            ValueError: If no valid JSON can be extracted
        """
        # Remove markdown code blocks if present (handles ```json, ```, etc.)
        text = text.strip()
        
        # Strip markdown backticks if present
        if text.startswith('```'):
            # Remove opening backticks (```json or ```)
            text = re.sub(r'^```(?:json)?\s*', '', text, flags=re.MULTILINE)
            # Remove closing backticks
            text = re.sub(r'\s*```\s*$', '', text, flags=re.MULTILINE)
            text = text.strip()
            logger.debug("Stripped markdown backticks from response")
        
        # Try to find JSON in markdown code blocks first (if still wrapped)
        json_match = re.search(r'```(?:json)?\s*(\{.*?\})\s*```', text, re.DOTALL)
        if json_match:
            text = json_match.group(1).strip()
            logger.debug("Extracted JSON from markdown code block")
        
        # If still not found, try to find JSON object directly (most common case)
        if not text.startswith('{'):
            json_match = re.search(r'\{.*\}', text, re.DOTALL)
            if json_match:
                text = json_match.group(0).strip()
                logger.debug("Extracted JSON object from text")
        
        # Parse JSON with detailed error handling
        try:
            parsed = json.loads(text)
            logger.debug("Successfully parsed JSON from response")
            return parsed
        except json.JSONDecodeError as e:
            logger.error(f"JSON decode error: {e}. Text preview: {text[:500]}")
            raise ValueError(f"Failed to parse JSON from LLM response: {e}. Response was: {text[:500]}") from e


llm_service = LLMService()
