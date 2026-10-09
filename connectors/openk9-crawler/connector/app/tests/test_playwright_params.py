import pytest
from pydantic import ValidationError

from generic_crawler.spiders.genericCrawlSpider import GenericCrawlSpider
from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider
from main import (
    CrawlRequest,
    SitemapRequest,
    set_up_crawl_endpoint,
    set_up_sitemap_endpoint,
)

BASE_REQUEST = {
    "maxSizeBytes": 1000,
    "datasourceId": 1,
    "scheduleId": "schedule",
    "timestamp": 1,
}

SITEMAP_REQUEST = {**BASE_REQUEST, "sitemapUrls": ["https://example.com/sitemap.xml"]}
CRAWL_REQUEST = {**BASE_REQUEST, "startUrls": ["https://example.com"]}

PLAYWRIGHT_FIELDS = {
    "usePlaywright": True,
    "playwrightSelector": "div.content",
    "playwrightTimeout": 3000,
    "playwrightWaitUntil": "load",
}

REQUESTS = [(SitemapRequest, SITEMAP_REQUEST), (CrawlRequest, CRAWL_REQUEST)]

SPIDER_ARGS = {
    "allowed_domains": "[]",
    "body_tag": "body",
    "excluded_body_tags": "[]",
    "title_tag": "title::text",
    "datasource_id": 1,
    "schedule_id": "schedule",
    "ingestion_url": "http://ingestion",
    "timestamp": 1,
    "additional_metadata": "{}",
    "max_length": "-1",
    "max_size_bytes": "1000",
    "tenant_id": "",
    "excluded_paths": "[]",
    "allowed_paths": "[]",
    "document_file_extensions": "[]",
    "do_use_mimetype_map": "True",
    "do_use_default_mimetype_map": "True",
    "mimetype_map": "{}",
    "custom_metadata": "{}",
    "do_extract_docs": "False",
    "cert_verification": "True",
    "use_playwright": "True",
    "playwright_selector": "div.content",
    "playwright_timeout": "3000",
    "playwright_wait_until": "load",
}

SITEMAP_SPIDER_ARGS = {
    **SPIDER_ARGS,
    "sitemap_urls": "[]",
    "replace_rule": "['', '']",
    "links_to_follow": "[]",
}

CRAWL_SPIDER_ARGS = {**SPIDER_ARGS, "start_urls": "[]", "follow": "True"}


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
def test_wait_until_defaults_to_domcontentloaded(request_class, request_data):
    request = request_class(**request_data)

    assert request.playwrightWaitUntil == "domcontentloaded"


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
@pytest.mark.parametrize("wait_until", ["load", "domcontentloaded", "commit"])
def test_wait_until_accepts_allowed_values(request_class, request_data, wait_until):
    request = request_class(**request_data, playwrightWaitUntil=wait_until)

    assert request.playwrightWaitUntil == wait_until


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
def test_wait_until_rejects_other_values(request_class, request_data):
    with pytest.raises(ValidationError):
        request_class(**request_data, playwrightWaitUntil="networkidle")


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
def test_selector_required_when_playwright_enabled(request_class, request_data):
    with pytest.raises(ValidationError):
        request_class(**request_data, usePlaywright=True)


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
def test_both_requests_accept_playwright_fields(request_class, request_data):
    request = request_class(**request_data, **PLAYWRIGHT_FIELDS)

    assert request.usePlaywright is True
    assert request.playwrightSelector == "div.content"
    assert request.playwrightTimeout == 3000
    assert request.playwrightWaitUntil == "load"


@pytest.mark.parametrize("set_up, request_class, request_data", [
    (set_up_sitemap_endpoint, SitemapRequest, SITEMAP_REQUEST),
    (set_up_crawl_endpoint, CrawlRequest, CRAWL_REQUEST),
])
def test_payload_contains_playwright_fields(set_up, request_class, request_data):
    payload = set_up(request_class(**request_data, **PLAYWRIGHT_FIELDS))

    assert payload["use_playwright"] is True
    assert payload["playwright_selector"] == "div.content"
    assert payload["playwright_timeout"] == 3000
    assert payload["playwright_wait_until"] == "load"


def test_sitemap_form_has_wait_until_select():
    from fastapi.testclient import TestClient
    from main import app

    form = TestClient(app).get("/sitemap/form").json()
    field = next(f for f in form["fields"] if f["name"] == "playwrightWaitUntil")

    assert field["type"] == "select"
    assert [v["value"] for v in field["values"]] == ["load", "domcontentloaded", "commit"]
    assert [v["value"] for v in field["values"] if v["isDefault"]] == ["domcontentloaded"]


@pytest.mark.parametrize("spider_class, spider_args", [
    (GenericSitemapSpider, SITEMAP_SPIDER_ARGS),
    (GenericCrawlSpider, CRAWL_SPIDER_ARGS),
])
def test_spider_builds_playwright_meta_with_wait_until(spider_class, spider_args):
    spider = spider_class(**spider_args)

    goto_kwargs = spider.playwright_meta["playwright_page_goto_kwargs"]
    assert spider.playwright_meta["playwright"] is True
    assert goto_kwargs == {"timeout": 3000, "wait_until": "load"}


@pytest.mark.parametrize("spider_class, spider_args", [
    (GenericSitemapSpider, SITEMAP_SPIDER_ARGS),
    (GenericCrawlSpider, CRAWL_SPIDER_ARGS),
])
def test_spider_meta_is_empty_without_playwright(spider_class, spider_args):
    spider = spider_class(**{**spider_args, "use_playwright": "False"})

    assert spider.playwright_meta == {}
