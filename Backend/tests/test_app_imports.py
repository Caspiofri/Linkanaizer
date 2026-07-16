"""Smoke test: the whole app imports cleanly (catches syntax errors,
missing dependencies, and bad imports in any router/service)."""


def test_app_imports():
    import app.main

    assert app.main.app is not None


def test_all_routers_registered():
    import app.main

    routes = {route.path for route in app.main.app.routes}
    assert "/ping" in routes
    assert "/process_url" in routes
    assert "/get_categories" in routes
