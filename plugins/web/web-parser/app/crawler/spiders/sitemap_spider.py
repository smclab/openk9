"""
Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import logging
import ast
import json
import hashlib
from datetime import datetime

from scrapy.spiders import SitemapSpider
from scrapy import signals
from scrapy.utils.sitemap import Sitemap, sitemap_urls_from_robots
from scrapy.http import Request, XmlResponse
from scrapy.utils.gz import gunzip, gzip_magic_number

from crawler.items import GenericWebItem, Payload
from .util.generic.utility import get_favicon, get_title, get_content, post_message
from .util.file.utility import parse_document_by_url
from .util.sitemap.utility import iterloc, regex

logger = logging.getLogger(__name__)


class CustomSitemapSpider(SitemapSpider):

    name = "CustomSitemapSpider"

    sitemap_rules = [('', 'parse')]
    sitemap_follow = ['']
    sitemap_alternate_links = False
    crawled_ids = []
    allowed_types = []
    type_mapping = []

    cont = 0

    def __init__(self, sitemap_urls, allowed_domains, body_tag, title_tag, replace_rule, datasource_id, ingestion_url,
                 delete_url, timestamp, max_length, *a, **kw):
        super(SitemapSpider, self).__init__(*a, **kw)

        self._cbs = []
        for r, c in self.sitemap_rules:
            if isinstance(c, str):
                c = getattr(self, c)
            self._cbs.append((regex(r), c))

        self._follow = [regex(x) for x in self.sitemap_follow]

        self.sitemap_urls = ast.literal_eval(sitemap_urls)
        self.body_tag = body_tag
        self.title_tag = title_tag

        self.datasource_id = datasource_id
        self.end_timestamp = datetime.utcnow().timestamp() * 1000
        self.ingestion_url = ingestion_url
        self.delete_url = delete_url
        self.timestamp = timestamp
        self.allowed_domains = ast.literal_eval(allowed_domains)
        self.replace_rule = ast.literal_eval(replace_rule)
        self.max_length = int(max_length)

    @classmethod
    def from_crawler(cls, crawler, *args, **kwargs):
        spider = super(SitemapSpider, cls).from_crawler(crawler, *args, **kwargs)
        crawler.signals.connect(spider.spider_closed, signals.spider_closed)
        return spider

    def spider_closed(self, spider):

        logger.info("Ingestion completed")

        payload = {
            "datasourceId": self.datasource_id,
            "contentIds": self.crawled_ids
        }

        # post_message(self.delete_url, payload)

    def start_requests(self):
        for url in self.sitemap_urls:
            yield Request(url, self._parse_sitemap)
            
    def sitemap_replace(self, loc, rule):
        new_loc = loc.replace(rule[0], rule[1])
        return new_loc

    def sitemap_filter(self, entries):
        """This method can be used to filter sitemap entries by their
        attributes, for example, you can filter locs with lastmod greater
        than a given date (see docs).
        """
        for entry in entries:
            date_time = datetime.fromisoformat(entry['lastmod'])
            lastmod_timestamp = datetime.timestamp(date_time)
            if int(lastmod_timestamp) >= int(self.timestamp):
                yield entry

    def _parse_sitemap(self, response):
        if response.url.endswith('/robots.txt'):
            for url in sitemap_urls_from_robots(response.text, base_url=response.url):
                yield Request(url, callback=self._parse_sitemap)
        else:
            body = self._get_sitemap_body(response)
            if body is None:
                logger.warning("Ignoring invalid sitemap: %(response)s",
                               {'response': response}, extra={'spider': self})
                return

            s = Sitemap(body)

            if s.type == 'sitemapindex':
                for loc in iterloc(s, self.sitemap_alternate_links):
                    if any(x.search(loc) for x in self._follow):
                        yield Request(loc, callback=self._parse_sitemap)
            elif s.type == 'urlset':
                it = self.sitemap_filter(s)
                for loc in iterloc(it, self.sitemap_alternate_links):
                    loc = self.sitemap_replace(loc, self.replace_rule)
                    yield Request(loc, callback=self.parse)

    def _get_sitemap_body(self, response):
        """Return the sitemap body contained in the given response,
        or None if the response is not a sitemap.
        """
        if isinstance(response, XmlResponse):
            return response.body
        elif gzip_magic_number(response):
            return gunzip(response.body)
        # actual gzipped sitemap files are decompressed above ;
        # if we are here (response body is not gzipped)
        # and have a response for .xml.gz,
        # it usually means that it was already gunzipped
        # by HttpCompression middleware,
        # the HTTP response being sent with "Content-Encoding: gzip"
        # without actually being a .xml.gz file in the first place,
        # merely XML gzip-compressed on the fly,
        # in other word, here, we have plain XML
        elif response.url.endswith('.xml') or response.url.endswith('.xml.gz'):
            return response.body

    def parse(self, response, **kwargs):

        if hasattr(response, "text"):

            title = get_title(response, self.title_tag)

            content = get_content(response, self.max_length, self.body_tag)

            web_item = GenericWebItem()
            web_item['url'] = response.url
            web_item['content'] = content
            web_item['title'] = title
            web_item['favicon'] = get_favicon(response.url)

            datasource_payload = {
                "web": dict(web_item)
            }

            tmp_document_anchors = response.css("a")

            document_urls = []
            for anchor in tmp_document_anchors:
                try:
                    href = anchor.attrib['href']
                    document_title = anchor.css("a::text").get()
                    next_page = response.urljoin(href)
                    document_urls.append(next_page)
                    parse_document_by_url(next_page, self, response.url, document_title)
                except KeyError:
                    continue

            payload = Payload()

            url = response.url
            content_id = int(hashlib.sha1(url.encode("utf-8")).hexdigest(), 16) % (10 ** 16)

            payload["parsingDate"] = int(self.end_timestamp)
            payload['datasourceId'] = self.datasource_id
            payload['contentId'] = content_id
            payload['rawContent'] = content
            payload['datasourcePayload'] = datasource_payload
            payload["resources"] = {
                "binaries": []
            }

            yield payload

            self.logger.info("Crawled web page from url: " + str(response.url))

        else:

            return
