import asyncio
import logging

import pytest
from playwright.async_api import TimeoutError as PlaywrightTimeoutError
from pydantic import ValidationError

from generic_crawler.spiders.genericCrawlSpider import GenericCrawlSpider
from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider
from generic_crawler.spiders.util.generic.utility import wait_for_selector
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
    "playwrightMaxRetries": 2,
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
    "playwright_max_retries": "2",
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
    assert request.playwrightMaxRetries == 2


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
def test_max_retries_defaults_to_zero(request_class, request_data):
    request = request_class(**request_data)

    assert request.playwrightMaxRetries == 0


@pytest.mark.parametrize("request_class, request_data", REQUESTS)
def test_max_retries_rejects_negative_values(request_class, request_data):
    with pytest.raises(ValidationError):
        request_class(**request_data, playwrightMaxRetries=-1)


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
    assert payload["playwright_max_retries"] == 2


def test_sitemap_form_has_max_retries_number_defaulting_to_zero():
    from fastapi.testclient import TestClient
    from main import app

    form = TestClient(app).get("/sitemap/form").json()
    field = next(f for f in form["fields"] if f["name"] == "playwrightMaxRetries")

    assert field["type"] == "number"
    assert [v["value"] for v in field["values"] if v["isDefault"]] == [0]


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


class FakeLocator:
    def __init__(self, error=None):
        self.error = error
        self.calls = []

    @property
    def first(self):
        return self

    async def wait_for(self, state, timeout):
        self.calls.append((state, timeout))
        if self.error:
            raise self.error


class FakePage:
    url = "https://example.com/page"

    def __init__(self, error=None):
        self.locator_instance = FakeLocator(error)
        self.selectors = []

    def locator(self, selector):
        self.selectors.append(selector)
        return self.locator_instance


@pytest.mark.parametrize("spider_class, spider_args", [
    (GenericSitemapSpider, SITEMAP_SPIDER_ARGS),
    (GenericCrawlSpider, CRAWL_SPIDER_ARGS),
])
def test_spider_meta_uses_page_methods(spider_class, spider_args):
    spider = spider_class(**spider_args)

    assert "playwright_page_coroutines" not in spider.playwright_meta
    load_state, selector_wait = spider.playwright_meta["playwright_page_methods"]
    assert load_state.method == "wait_for_load_state"
    assert load_state.args == ("load",)
    assert load_state.kwargs == {"timeout": 3000}
    assert selector_wait.method is wait_for_selector
    assert selector_wait.args == ("div.content",)
    assert selector_wait.kwargs == {"timeout": 3000}


def test_wait_for_selector_waits_for_first_visible_match():
    page = FakePage()

    asyncio.run(wait_for_selector(page, "div.content", timeout=3000))

    assert page.selectors == ["div.content"]
    assert page.locator_instance.calls == [("visible", 3000)]


def test_wait_for_selector_logs_and_continues_on_timeout(caplog):
    page = FakePage(error=PlaywrightTimeoutError("timeout"))

    with caplog.at_level(logging.WARNING):
        asyncio.run(wait_for_selector(page, "div.content", timeout=3000))

    assert "https://example.com/page" in caplog.text
    assert "div.content" in caplog.text
