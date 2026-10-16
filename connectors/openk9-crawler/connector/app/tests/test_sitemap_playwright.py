import pytest
from scrapy.http import HtmlResponse, XmlResponse

from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider
from test_playwright_params import SITEMAP_SPIDER_ARGS

URLSET = b"""<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <url><loc>https://example.com/a</loc></url>
    <url><loc>https://example.com/b</loc></url>
</urlset>"""

SITEMAP_INDEX = b"""<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <sitemap><loc>https://example.com/sitemap-1.xml</loc></sitemap>
    <sitemap><loc>https://example.com/sitemap-2.xml</loc></sitemap>
</sitemapindex>"""

ROBOTS = b"Sitemap: https://example.com/sitemap.xml\n"


def spider(use_playwright="True", **kwargs):
    return GenericSitemapSpider(**{**SITEMAP_SPIDER_ARGS, "use_playwright": use_playwright, **kwargs})


def parse_sitemap(spider, url, body):
    response_class = XmlResponse if url.endswith(".xml") else HtmlResponse
    return list(spider._parse_sitemap(response_class(url, body=body)))


def test_pages_listed_in_the_sitemap_use_playwright():
    sitemap_spider = spider()

    requests = parse_sitemap(sitemap_spider, "https://example.com/sitemap.xml", URLSET)

    assert [r.url for r in requests] == ["https://example.com/a", "https://example.com/b"]
    for request in requests:
        assert request.callback == sitemap_spider.parse
        assert request.meta["playwright"] is True


def test_pages_listed_in_the_sitemap_do_not_use_playwright_when_disabled():
    requests = parse_sitemap(spider("False"), "https://example.com/sitemap.xml", URLSET)

    assert len(requests) == 2
    for request in requests:
        assert "playwright" not in request.meta


@pytest.mark.parametrize("url, body", [
    ("https://example.com/sitemap-index.xml", SITEMAP_INDEX),
    ("https://example.com/robots.txt", ROBOTS),
])
def test_sitemap_files_do_not_use_playwright(url, body):
    sitemap_spider = spider()

    requests = parse_sitemap(sitemap_spider, url, body)

    assert requests
    for request in requests:
        assert request.callback == sitemap_spider._parse_sitemap
        assert "playwright" not in request.meta


def test_sitemap_urls_do_not_use_playwright():
    sitemap_spider = spider(sitemap_urls="['https://example.com/sitemap.xml']")

    requests = list(sitemap_spider.start_requests())

    assert len(requests) == 1
    assert "playwright" not in requests[0].meta


def test_links_to_follow_still_use_playwright():
    sitemap_spider = spider(links_to_follow="['//a/@href']")
    response = HtmlResponse("https://example.com/a", body=b'<a href="/c">c</a>')

    requests = list(sitemap_spider.try_follow_link(response))

    assert [r.url for r in requests] == ["https://example.com/c"]
    assert requests[0].meta["playwright"] is True
