"""
Service that integrates scraping and LLM services for link classification.
"""
from typing import Optional, List

from app.services.scraping_service import scraping_service
from app.services.llm_service import llm_service
from app.schemas.link_classification import LinkClassification


class LinkClassificationService:
    """
    Service that orchestrates scraping and LLM parsing for link classification.
    """

    async def classify_link(
        self,
        url: str,
        categories: Optional[List[str]] = None,
    ) -> LinkClassification:
        """
        Classify a link by scraping its HTML and parsing it with LLM.

        This method:
        1. Scrapes the URL to get raw HTML
        2. Sends HTML to LLM for structured parsing
        3. Returns validated LinkClassification schema

        Args:
            url: The URL to classify
            categories: Optional list of allowed categories

        Returns:
            LinkClassification object with category, summary, and keywords

        Raises:
            httpx.HTTPError: If scraping API fails
            ValueError: If LLM cannot parse or validate the response
        """
        # Step 1: Scrape the URL to get raw HTML
        html = await scraping_service.scrape_url(url, return_html=True)
        
        # Step 2: Parse HTML with LLM into structured JSON schema
        classification = await llm_service.parse_html_to_classification(
            html=html,
            url=url,
            categories=categories,
        )
        
        return classification


link_classification_service = LinkClassificationService()
