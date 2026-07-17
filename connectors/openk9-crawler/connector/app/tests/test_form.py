from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def _field_names(form):
    return {field["name"] for field in form["fields"]}


def test_sitemap_form_returns_sitemap_fields():
    response = client.get("/sitemap/form")

    assert response.status_code == 200
    names = _field_names(response.json())
    assert "sitemapUrls" in names
    assert "startUrls" not in names


def test_urls_form_returns_url_fields():
    response = client.get("/urls/form")

    assert response.status_code == 200
    names = _field_names(response.json())
    assert "startUrls" in names
    assert "sitemapUrls" not in names


def test_sitemap_and_urls_forms_differ():
    sitemap_form = client.get("/sitemap/form").json()
    urls_form = client.get("/urls/form").json()

    assert sitemap_form != urls_form
