import asyncio
import logging

import pytest
from fastapi.testclient import TestClient
from playwright.async_api import Error as PlaywrightError
from scrapy import Request
from scrapy.dupefilters import RFPDupeFilter
from scrapy.http import HtmlResponse
from scrapy.settings import Settings
from twisted.python.failure import Failure

from generic_crawler.items import Payload
from generic_crawler.spiders.genericCrawlSpider import GenericCrawlSpider
from main import CrawlRequest, app, set_up_crawl_endpoint
from tests.test_playwright_params import CRAWL_REQUEST, CRAWL_SPIDER_ARGS, PLAYWRIGHT_FIELDS

SEED = "https://example.com/"
PAGE_A = "https://example.com/a"
PAGE_B = "https://example.com/b"


def crawl_spider(**overrides):
    spider = GenericCrawlSpider(**{
        **CRAWL_SPIDER_ARGS,
        "start_urls": f"['{SEED}', '{PAGE_A}']",
        **overrides,
    })
    spider._follow_links = True  # set by CrawlSpider.from_crawler
    return spider


def html_response(url, links):
    anchors = "".join(f'<a href="{link}">link</a>' for link in links)
    return HtmlResponse(
        url=url,
        body=f"<html><head><title>t</title></head><body>{anchors}</body></html>".encode(),
        encoding="utf-8",
        request=Request(url),
    )


def collect(async_iterable):
    async def collect_all():
        return [item async for item in async_iterable]

    return asyncio.run(collect_all())


@pytest.fixture(autouse=True)
def no_favicon_lookup(monkeypatch):
    monkeypatch.setattr("generic_crawler.spiders.genericCrawlSpider.get_favicon", lambda url: "")


def test_seed_requests_use_playwright_and_the_errback():
    spider = crawl_spider()

    seeds = collect(spider.start())

    assert [seed.url for seed in seeds] == [SEED, PAGE_A]
    for seed in seeds:
        assert seed.callback == spider.parse_seed
        assert seed.errback == spider.playwright_raise_error
        assert seed.meta["playwright"] is True
        assert seed.meta["playwright_page_goto_kwargs"] == {"timeout": 3000, "wait_until": "load"}


def test_rule_requests_use_playwright_and_keep_the_rule_meta():
    spider = crawl_spider()

    requests = list(spider._requests_to_follow(html_response(SEED, [PAGE_A, PAGE_B])))

    assert [request.url for request in requests] == [PAGE_A, PAGE_B]
    for request in requests:
        assert request.meta["playwright"] is True
        assert request.meta["rule"] == 0
        assert request.meta["link_text"] == "link"


def test_requests_do_not_use_playwright_when_disabled():
    spider = crawl_spider(use_playwright="False")

    seeds = collect(spider.start())
    rule_requests = list(spider._requests_to_follow(html_response(SEED, [PAGE_A])))

    for request in seeds + rule_requests:
        assert not any(key.startswith("playwright") for key in request.meta)


@pytest.mark.parametrize("use_playwright, expected", [
    ("True", {"http": "scrapy_playwright.handler.ScrapyPlaywrightDownloadHandler",
              "https": "scrapy_playwright.handler.ScrapyPlaywrightDownloadHandler"}),
    ("False", {}),
])
def test_download_handlers_follow_use_playwright_for_the_crawl_spider(use_playwright, expected):
    crawler_settings = Settings()
    crawler_settings.set("USE_PLAYWRIGHT", use_playwright, priority="cmdline")

    GenericCrawlSpider.update_settings(crawler_settings)

    assert crawler_settings.getdict("DOWNLOAD_HANDLERS") == expected


def test_seed_page_is_indexed_and_its_links_followed():
    spider = crawl_spider()

    results = collect(spider.parse_seed(html_response(SEED, [PAGE_A, PAGE_B])))

    payloads = [result for result in results if isinstance(result, Payload)]
    requests = [result for result in results if isinstance(result, Request)]
    assert [payload["datasourcePayload"]["web"]["url"] for payload in payloads] == [SEED]
    assert [request.url for request in requests] == [PAGE_A, PAGE_B]


def test_only_seed_pages_are_indexed_without_follow():
    spider = crawl_spider(follow="False")

    results = collect(spider.parse_seed(html_response(SEED, [PAGE_A, PAGE_B])))

    assert [type(result) for result in results] == [Payload]


def test_seed_linked_from_another_page_is_filtered():
    spider = crawl_spider()
    dupefilter = RFPDupeFilter()
    seed = collect(spider.start())[1]  # PAGE_A

    assert seed.dont_filter is False
    assert dupefilter.request_seen(seed) is False

    link_to_seed = list(spider._requests_to_follow(html_response(SEED, [PAGE_A])))[0]

    assert dupefilter.request_seen(link_to_seed) is True


def test_rendering_error_is_retried_then_logged(caplog):
    spider = crawl_spider(playwright_max_retries="1")
    request = list(spider._requests_to_follow(html_response(SEED, [PAGE_A])))[0]

    first_failure = Failure(PlaywrightError("boom"))
    first_failure.request = request
    retries = list(spider._errback(first_failure))

    assert len(retries) == 1
    retry = retries[0]
    assert retry.dont_filter is True
    assert retry.meta["playwright_retry_count"] == 1
    assert retry.meta["rule"] == 0
    assert retry.callback == request.callback
    assert retry.errback == request.errback

    second_failure = Failure(PlaywrightError("boom"))
    second_failure.request = retry
    with caplog.at_level(logging.ERROR):
        assert list(spider._errback(second_failure)) == []

    error = next(record for record in caplog.records if record.levelno == logging.ERROR)
    assert PAGE_A in error.getMessage()
    assert "2 attempt(s)" in error.getMessage()


def test_crawl_form_has_playwright_fields_without_debug():
    form = TestClient(app).get("/urls/form").json()
    fields = {field["name"]: field for field in form["fields"]}

    for name in ["usePlaywright", "playwrightSelector", "playwrightTimeout", "playwrightWaitUntil",
                 "playwrightMaxRetries"]:
        assert name in fields
    assert "playwrightDebug" not in fields
    assert [v["value"] for v in fields["playwrightWaitUntil"]["values"] if v["isDefault"]] == ["domcontentloaded"]
    assert [v["value"] for v in fields["playwrightMaxRetries"]["values"] if v["isDefault"]] == [0]


def test_crawl_endpoint_enables_playwright_for_the_job():
    payload = set_up_crawl_endpoint(CrawlRequest(**CRAWL_REQUEST, **PLAYWRIGHT_FIELDS))

    assert "USE_PLAYWRIGHT=True" in payload["setting"]
