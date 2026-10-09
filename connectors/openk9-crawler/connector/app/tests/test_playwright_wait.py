import asyncio
import logging

from playwright.async_api import TimeoutError as PlaywrightTimeoutError

from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider
from generic_crawler.spiders.util.generic.utility import wait_for_selector

SITEMAP_SPIDER_ARGS = {
    "sitemap_urls": "[]",
    "allowed_domains": "[]",
    "body_tag": "body",
    "excluded_body_tags": "[]",
    "title_tag": "title::text",
    "replace_rule": "['', '']",
    "links_to_follow": "[]",
    "use_playwright": "True",
    "playwright_selector": "div.content",
    "playwright_timeout": "3000",
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
}


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


def test_spider_meta_uses_page_methods():
    spider = GenericSitemapSpider(**SITEMAP_SPIDER_ARGS)

    assert "playwright_page_coroutines" not in spider.playwright_meta
    (selector_wait,) = spider.playwright_meta["playwright_page_methods"]
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
