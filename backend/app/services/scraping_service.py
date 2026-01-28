"""
Service for interacting with the Scraping API (ScrapingBee).
"""
import httpx
from typing import Optional, Dict, Any

from app.core.config import settings


class ScrapingService:
    """Service for scraping operations using ScrapingBee API."""

    def __init__(self):
        self.api_key = settings.SCRAPING_API_KEY
        # Ensure base URL doesn't have trailing slash or /scrape
        base_url = settings.SCRAPING_API_URL.rstrip("/")
        if base_url.endswith("/scrape"):
            base_url = base_url[:-7]  # Remove /scrape
        self.base_url = base_url.rstrip("/")
        self.timeout = settings.SCRAPING_API_TIMEOUT

    async def scrape_url(self, url: str, return_html: bool = True, **kwargs) -> str:
        """
        Scrape content from a URL using ScrapingBee API and return raw HTML.

        Args:
            url: The URL to scrape
            return_html: If True, request raw HTML from the scraping API
            **kwargs: Additional parameters for the scraping API

        Returns:
            Raw HTML content as a string

        Raises:
            httpx.HTTPError: If the API request fails
            ValueError: If the response format is unexpected
        """
        # ScrapingBee uses GET requests with query parameters
        params = {
            "api_key": self.api_key,
            "url": url,
        }
        
        # Add Instagram/social media support
        params["render_js"] = "true"
        params["block_resources"] = "false"
        
        # Add any additional parameters from kwargs
        for key, value in kwargs.items():
            params[key] = str(value).lower() if isinstance(value, bool) else str(value)

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            try:
                # ScrapingBee endpoint is just the base URL with query parameters
                response = await client.get(
                    self.base_url,
                    params=params,
                )
                response.raise_for_status()
                
                # ScrapingBee can return HTML directly or JSON with html field
                content_type = response.headers.get("content-type", "").lower()
                
                if "application/json" in content_type:
                    # JSON response format
                    result = response.json()
                    html = result.get("html") or result.get("body") or result.get("content", "")
                    if not html:
                        raise ValueError("ScrapingBee returned JSON but no HTML field found in response")
                    return html
                else:
                    # Direct HTML response
                    html = response.text
                    if not html or len(html) < 100:
                        raise ValueError(f"Received empty or too short HTML response from ScrapingBee: {len(html)} characters")
                    return html
                
            except httpx.HTTPStatusError as e:
                # Handle API errors with better messages
                error_detail = f"ScrapingBee API error: {e.response.status_code}"
                try:
                    error_body = e.response.json()
                    error_detail = error_body.get("message", error_detail)
                except:
                    error_detail = e.response.text[:200] if e.response.text else error_detail
                raise ValueError(f"{error_detail}") from e
            except httpx.RequestError as e:
                raise ValueError(f"Network error connecting to ScrapingBee: {str(e)}") from e

    async def get_scraping_status(self, job_id: str) -> Dict[str, Any]:
        """
        Check the status of a scraping job.

        Args:
            job_id: The ID of the scraping job

        Returns:
            Dictionary containing job status
        """
        headers = {
            "Authorization": f"Bearer {self.api_key}",
        }

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.get(
                f"{self.base_url}/jobs/{job_id}",
                headers=headers,
            )
            response.raise_for_status()
            return response.json()


scraping_service = ScrapingService()
