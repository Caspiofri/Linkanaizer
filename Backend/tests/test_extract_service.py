"""Unit tests for _is_safe_url — SSRF protection."""
from app.services.extract_service import _is_safe_url


def test_allows_public_https():
    assert _is_safe_url("https://www.bbc.com/sport/football") is True


def test_allows_public_http():
    assert _is_safe_url("http://example.com/page") is True


def test_rejects_private_ip():
    assert _is_safe_url("http://192.168.1.1/admin") is False
    assert _is_safe_url("http://10.0.0.5/internal") is False
    assert _is_safe_url("http://172.16.0.1/") is False


def test_rejects_loopback():
    assert _is_safe_url("http://127.0.0.1:8000/ping") is False
    assert _is_safe_url("http://localhost/secret") is False
    assert _is_safe_url("http://[::1]/") is False


def test_rejects_link_local():
    # cloud metadata endpoint — classic SSRF target
    assert _is_safe_url("http://169.254.169.254/latest/meta-data/") is False


def test_rejects_non_http_schemes():
    assert _is_safe_url("ftp://example.com/file") is False
    assert _is_safe_url("file:///etc/passwd") is False
    assert _is_safe_url("javascript:alert(1)") is False


def test_rejects_garbage():
    assert _is_safe_url("not a url") is False
    assert _is_safe_url("") is False
