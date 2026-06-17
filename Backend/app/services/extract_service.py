import requests
from bs4 import BeautifulSoup
import re
from datetime import datetime
from urllib.parse import urlparse
import ipaddress


def _is_safe_url(url: str) -> bool:
    """Reject private/internal IPs and non-HTTP schemes (SSRF protection)."""
    try:
        parsed = urlparse(url)
        if parsed.scheme not in ("http", "https"):
            return False
        hostname = parsed.hostname
        if not hostname:
            return False
        try:
            ip = ipaddress.ip_address(hostname)
            if ip.is_private or ip.is_loopback or ip.is_reserved or ip.is_link_local:
                return False
        except ValueError:
            # hostname is a domain name, not an IP — check for localhost
            if hostname.lower() in ("localhost", "127.0.0.1", "0.0.0.0"):
                return False
        return True
    except Exception:
        return False


def extract_url(url):
    print("Extracting:", url)
    if not _is_safe_url(url):
        print(f"Blocked unsafe URL: {url}")
        return {"description": None, "title": None, "thumbnail": None, "url": url}
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        soup = BeautifulSoup(response.content, 'html.parser')

        # Prefer Open Graph tags (YouTube, Instagram, TikTok populate these with real content)
        og_description = soup.find('meta', property='og:description')
        meta_description = soup.find('meta', attrs={'name': 'description'})

        if og_description and og_description.get('content', '').strip():
            url_content = og_description['content'].strip()
        elif meta_description and meta_description.get('content', '').strip():
            url_content = meta_description['content'].strip()
        else:
            url_content = soup.get_text()[:500]

        # Extract title — og:title has better content on video platforms
        og_title = soup.find('meta', property='og:title')
        title_tag = soup.find('title')

        if og_title and og_title.get('content', '').strip():
            title = og_title['content'].strip()
        elif title_tag and title_tag.get_text(strip=True):
            title = title_tag.get_text(strip=True)
        else:
            title = None

        og_image = soup.find('meta', property='og:image')
        thumbnail = og_image['content'] if og_image else None

        return {
            "description": url_content,
            "title": title,
            "thumbnail": thumbnail,
            "url": url
        }
    except requests.exceptions.Timeout:
        print(f"Timeout extracting URL: {url}")
        return {"description": None, "title": None, "thumbnail": None, "url": url}
    except Exception as e:
        print(f"Failed to extract URL: {e}")
        return {"description": None, "title": None, "thumbnail": None, "url": url}


def extract_datetime_urls(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            print(f"📄 Processing line: {line.strip()}")
            match = re.search(r"(\d{1,2}\.\d{1,2}\.\d{4}), (\d{1,2}:\d{2}) - .*?: (https?://[^\s]+)", line.strip())
            if match:
                date_str, time_str, url = match.groups()
                date_time_obj = datetime.strptime(f"{date_str} {time_str}", "%d.%m.%Y %H:%M")
                print(f"🧠 Extracted: {url} at {date_time_obj}")
                from app.services.url_service import process_url  # delayed import to avoid circular dep
                process_url(url, uid="system")
    return True
