from generic_crawler.spiders import genericSitemapSpider
from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider
from test_playwright_params import SITEMAP_SPIDER_ARGS


def test_spider_closed_sends_the_last_message(monkeypatch):
    sent = []
    monkeypatch.setattr(genericSitemapSpider, "post_message", lambda url, payload: sent.append(payload))
    spider = GenericSitemapSpider(**SITEMAP_SPIDER_ARGS)

    spider.spider_closed(spider)

    assert len(sent) == 1
    assert sent[0]["last"] is True
    assert "type" not in sent[0]
