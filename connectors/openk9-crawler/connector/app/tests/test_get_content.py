import pytest
from scrapy.http import HtmlResponse

from generic_crawler.spiders.util.generic.utility import get_content

HTML = b"<div>page four</div>"


def build_response():
    return HtmlResponse(url="https://example.com", body=HTML, encoding="utf-8")


@pytest.mark.parametrize("max_length", [-1, -5, None])
def test_get_content_without_limit(max_length):
    assert get_content(build_response(), max_length) == "page four"


def test_get_content_positive_max_length_truncates():
    assert get_content(build_response(), 4) == "page"


def test_get_content_zero_max_length_is_empty():
    assert get_content(build_response(), 0) == ""
