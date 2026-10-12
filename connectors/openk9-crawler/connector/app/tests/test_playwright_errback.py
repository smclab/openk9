import logging
import re

from playwright.async_api import Error as PlaywrightError
from scrapy import Request
from scrapy.http import XmlResponse
from twisted.internet.error import DNSLookupError
from twisted.python.failure import Failure

from generic_crawler.spiders.genericCrawlSpider import GenericCrawlSpider
from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider
from tests.test_playwright_params import CRAWL_SPIDER_ARGS, SITEMAP_SPIDER_ARGS

URL = "https://example.com/page"


def sitemap_spider(max_retries):
    return GenericSitemapSpider(**{**SITEMAP_SPIDER_ARGS, "playwright_max_retries": str(max_retries)})


def failure_of(error, request):
    failure = Failure(error)
    failure.request = request
    return failure


def test_retry_recovers_from_transient_error(caplog):
    spider = sitemap_spider(2)
    request = Request(URL, meta={"playwright": True})

    with caplog.at_level(logging.WARNING):
        retry = spider.playwright_raise_error(failure_of(PlaywrightError("boom"), request))

    assert retry.url == URL
    assert retry.dont_filter is True
    assert retry.meta["playwright"] is True
    assert retry.meta["playwright_retry_count"] == 1
    assert any(r.levelno == logging.WARNING and URL in r.getMessage() for r in caplog.records)


def test_retries_stop_when_exhausted(caplog):
    spider = sitemap_spider(1)
    request = Request(URL, meta={"playwright": True})

    first_retry = spider.playwright_raise_error(failure_of(PlaywrightError("boom"), request))
    with caplog.at_level(logging.ERROR):
        second = spider.playwright_raise_error(failure_of(PlaywrightError("boom"), first_retry))

    assert first_retry is not None
    assert second is None
    error = next(r for r in caplog.records if r.levelno == logging.ERROR)
    assert URL in error.getMessage()
    assert "2 attempt(s)" in error.getMessage()


def test_no_retry_without_max_retries(caplog):
    spider = sitemap_spider(0)

    with caplog.at_level(logging.ERROR):
        result = spider.playwright_raise_error(failure_of(PlaywrightError("boom"), Request(URL)))

    assert result is None
    assert any(r.levelno == logging.ERROR and URL in r.getMessage() for r in caplog.records)


def test_no_retry_for_errors_not_related_to_rendering(caplog):
    spider = sitemap_spider(2)

    with caplog.at_level(logging.ERROR):
        result = spider.playwright_raise_error(failure_of(DNSLookupError("no dns"), Request(URL)))

    assert result is None
    assert any(r.levelno == logging.ERROR and URL in r.getMessage() for r in caplog.records)


def test_sitemap_requests_use_the_errback():
    spider = sitemap_spider(0)
    spider.sitemap_urls = ["https://example.com/sitemap.xml", "https://example.com/robots.txt"]

    start_requests = list(spider.start_requests())
    index = XmlResponse(
        url="https://example.com/sitemap.xml",
        body=b'<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">'
             b'<sitemap><loc>https://example.com/sitemap-1.xml</loc></sitemap></sitemapindex>',
    )
    urlset = XmlResponse(
        url="https://example.com/sitemap-1.xml",
        body=b'<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">'
             b'<url><loc>https://example.com/a</loc></url></urlset>',
    )
    spider._follow = [re.compile(".*")]
    from_index = list(spider._parse_sitemap(index))
    from_urlset = list(spider._parse_sitemap(urlset))

    requests = start_requests + from_index + from_urlset
    assert len(requests) == 4
    assert all(r.errback == spider.playwright_raise_error for r in requests)


def test_crawl_spider_rule_uses_the_errback():
    spider = GenericCrawlSpider(**CRAWL_SPIDER_ARGS)

    assert spider.rules[0].errback == spider.playwright_raise_error
