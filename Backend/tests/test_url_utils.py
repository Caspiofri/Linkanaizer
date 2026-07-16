"""Unit tests for processing_content — metadata cleanup before LLM classification."""
from app.utils.url_utils import processing_content


def test_lowercases_content():
    assert processing_content("Home CHEST Workout") == "home chest workout"


def test_removes_emojis():
    result = processing_content("workout \U0001F4AA plan \U0001F525")
    assert "\U0001F4AA" not in result
    assert "\U0001F525" not in result
    assert "workout" in result and "plan" in result


def test_strips_likes_and_comments_prefix():
    result = processing_content("6,653 likes, 38 comments - my workout video")
    assert "likes" not in result
    assert "comments" not in result
    assert "my workout video" in result


def test_strips_author_and_date():
    result = processing_content("lifeflowerez on November 9, 2024: great recipe here")
    assert "lifeflowerez" not in result
    assert "2024" not in result
    assert "great recipe here" in result


def test_collapses_whitespace():
    assert processing_content("hello   world\n\nfoo") == "hello world foo"


def test_strips_leading_trailing_whitespace():
    assert processing_content("  padded content  ") == "padded content"
