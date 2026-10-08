import importlib

import pytest
from scrapy.settings import Settings

from generic_crawler import settings
from generic_crawler.spiders.genericSitemapSpider import GenericSitemapSpider

PLAYWRIGHT_HANDLER = 'scrapy_playwright.handler.ScrapyPlaywrightDownloadHandler'
PLAYWRIGHT_DOWNLOAD_HANDLERS = {
    'http': PLAYWRIGHT_HANDLER,
    'https': PLAYWRIGHT_HANDLER,
}


def _download_handlers(use_playwright=None):
    crawler_settings = Settings()
    if use_playwright is not None:
        # scrapyd passes "-s USE_PLAYWRIGHT=<value>", i.e. a string at cmdline priority
        crawler_settings.set("USE_PLAYWRIGHT", str(use_playwright), priority="cmdline")

    GenericSitemapSpider.update_settings(crawler_settings)

    return crawler_settings.getdict("DOWNLOAD_HANDLERS")


@pytest.fixture
def restore_settings(monkeypatch):
    yield
    monkeypatch.undo()
    importlib.reload(settings)


def test_playwright_download_handlers_set_when_use_playwright_true():
    assert _download_handlers(use_playwright=True) == PLAYWRIGHT_DOWNLOAD_HANDLERS


@pytest.mark.parametrize("use_playwright", [False, None])
def test_default_download_handlers_kept_when_use_playwright_not_enabled(use_playwright):
    assert _download_handlers(use_playwright=use_playwright) == {}


def test_download_handlers_not_driven_by_use_playwright_env(monkeypatch, restore_settings):
    monkeypatch.setenv("USE_PLAYWRIGHT", "True")

    reloaded = importlib.reload(settings)

    assert not hasattr(reloaded, "DOWNLOAD_HANDLERS")
    assert not hasattr(reloaded, "USE_PLAYWRIGHT")


ENV_VAR = "PLAYWRIGHT_MAX_PAGES_PER_CONTEXT"


@pytest.fixture
def reload_settings(monkeypatch):
    def _reload(value=None):
        if value is None:
            monkeypatch.delenv(ENV_VAR, raising=False)
        else:
            monkeypatch.setenv(ENV_VAR, value)
        return importlib.reload(settings)

    yield _reload
    monkeypatch.delenv(ENV_VAR, raising=False)
    importlib.reload(settings)


def test_max_pages_defaults_to_concurrent_requests(reload_settings):
    s = reload_settings()
    assert s.PLAYWRIGHT_MAX_PAGES_PER_CONTEXT == s.CONCURRENT_REQUESTS


def test_max_pages_below_concurrent_requests_is_raised(reload_settings):
    s = reload_settings("4")
    assert s.PLAYWRIGHT_MAX_PAGES_PER_CONTEXT == s.CONCURRENT_REQUESTS


def test_max_pages_above_concurrent_requests_is_kept(reload_settings):
    s = reload_settings("16")
    assert s.PLAYWRIGHT_MAX_PAGES_PER_CONTEXT == 16
