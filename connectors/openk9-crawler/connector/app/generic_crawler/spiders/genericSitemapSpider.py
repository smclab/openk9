import scrapy
from twisted.python.log import logerr

from .util.file.utility import get_path
from .util.generic.utility import clean_extraction, get_as_base64, get_content, get_favicon, get_title, generate_item, extract_text
from .util.sitemap.utility import iterloc, regex, is_absolute
from datetime import datetime
from generic_crawler.items import BinaryItem, DocumentItem, FileItem, Payload
from generic_crawler.spiders.util.generic.utility import post_message
from generic_crawler.spiders.abstractBaseCrawlSpider import AbstractBaseCrawlSpider
from requests_futures.sessions import FuturesSession
from scrapy import signals, Item, Field
from scrapy.http import Request, XmlResponse
from scrapy.linkextractors import LinkExtractor
from scrapy_playwright.page import PageMethod
from scrapy.spiders import SitemapSpider, Rule
from scrapy.utils.gz import gunzip, gzip_magic_number
from scrapy.utils.sitemap import Sitemap, sitemap_urls_from_robots
from urllib.parse import urlparse
import ast
import hashlib
import logging
import mimetypes
import random 
import re

logger = logging.getLogger(__name__)


class GenericSitemapSpider(AbstractBaseCrawlSpider, SitemapSpider):

    name = "genericSitemapSpider"
    sitemap_rules = [('', 'parse')]
    sitemap_follow = ['']
    sitemap_alternate_links = False
    
    cont = 0
    content = ''

    payload = {}

    def __init__(self, sitemap_urls, allowed_domains, body_tag, excluded_bodyTags, title_tag, replace_rule, links_to_follow,
                 use_playwright, playwright_selector, playwright_timeout, datasource_id, schedule_id, ingestion_url, timestamp,
                 additional_metadata, max_length, max_size_bytes, tenant_id, excluded_paths, allowed_paths,
                 document_file_extensions, custom_metadata, do_extract_docs, cert_verification, *a, **kw):

        super(GenericSitemapSpider, self).__init__(ingestion_url, body_tag, excluded_bodyTags, title_tag, allowed_domains,
                                                   excluded_paths, allowed_paths, max_length, max_size_bytes, document_file_extensions,
                                                   custom_metadata, additional_metadata, do_extract_docs, cert_verification, datasource_id,
                                                   schedule_id, timestamp, tenant_id, *a, **kw)

        self._cbs = []
        for r, c in self.sitemap_rules:
            if isinstance(c, str):
                c = getattr(self, c)
            self._cbs.append((regex(r), c))

        self._follow = [regex(x) for x in self.sitemap_follow]

        self.sitemap_urls = ast.literal_eval(sitemap_urls)
        self.replace_rule = ast.literal_eval(replace_rule)
        self.links_to_follow = ast.literal_eval(links_to_follow)
        self.use_playwright = ast.literal_eval(use_playwright)
        self.playwright_selector = ast.literal_eval(playwright_selector)
        self.playwright_timeout = ast.literal_eval(playwright_timeout)

    @classmethod
    def from_crawler(cls, crawler, *args, **kwargs):
        spider = super(GenericSitemapSpider, cls).from_crawler(crawler, *args, **kwargs)
        crawler.signals.connect(spider.spider_closed, signals.spider_closed)
        return spider

    def spider_closed(self, spider):

        logger.info("Ingestion completed")

        payload = Payload()

        payload["parsingDate"] = int(self.end_timestamp)
        payload['datasourceId'] = self.datasource_id
        payload['tenantId'] = self.tenant_id
        payload['contentId'] = None
        payload['rawContent'] = None
        payload['datasourcePayload'] = {}
        payload["resources"] = {
            "binaries": []
        }
        payload["scheduleId"] = self.schedule_id
        payload["last"] = True

        post_message(self.ingestion_url, dict(payload))

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
            if len(self.allowed_paths) == 0 or any(allowed_path in entry.get('loc') for allowed_path in self.allowed_paths):
                if not any(excluded_path in entry.get('loc') for excluded_path in self.excluded_paths):
                    try:
                        date_time = datetime.fromisoformat(entry['lastmod'])
                        lastmod_timestamp = int(datetime.timestamp(date_time) * 1000)
                        logger.debug("Lastmod timestamp is " + str(lastmod_timestamp) )
                        if int(lastmod_timestamp) >= int(self.timestamp):
                            yield entry
                    except KeyError:
                        logger.info("No lastmod present, so proceeding with ingestion without data validation")
                        yield entry
                else:
                    logger.info("Excluded entry " + str(entry.get('loc')))
            else:
                logger.info("Not allowed entry " + str(entry.get('loc')))

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

        url = response.url
        title = get_title(response, self.title_tag)
        content = get_content(response, self.max_length, self.body_tag, self.excluded_bodyTags)

        web_item_fields = ['url', 'title', 'content', 'favicon']

        for key, value in self.custom_metadata.items():
            web_item_fields.append(key)

        web_item = generate_item(web_item_fields)
        web_item['url'] = url
        web_item['title'] = title
        web_item['content'] = content
        try:
            web_item['favicon'] = get_favicon(url)
        except Exception as e:
            logger.error("Something goes wrong getting favicon")

        extracted_custom_metadata = {}

        custom_item = generate_item([])

        if self.custom_metadata:
            for key, value in self.custom_metadata.items():
                extracted_elements = response.xpath(value).getall()
                if len(extracted_elements) > 0:
                    extracted_elements_list = [extracted_element.strip() for extracted_element in extracted_elements]
                    custom_item[key] = extracted_elements_list
                    extracted_custom_metadata[key] = extracted_elements_list
                else:
                    custom_item[key] = None
                    extracted_custom_metadata[key] = None


        anchors = response.css('a')
        try:
            self.try_parse_documents(anchors, response.request.url, extracted_custom_metadata)
        except Exception as e:
            logger.error(e)

        datasource_payload = {
            "web": dict(web_item),
            "custom": dict(custom_item)
        }

        logger.info(datasource_payload)

        payload = Payload()

        content_id = int(hashlib.sha1(url.encode("utf-8")).hexdigest(), 16) % (10 ** 16)

        payload["parsingDate"] = int(self.end_timestamp)
        payload['datasourceId'] = self.datasource_id
        payload['contentId'] = content_id
        payload['rawContent'] = content
        payload['datasourcePayload'] = datasource_payload
        payload["resources"] = {
            "binaries": []
        }
        payload["scheduleId"] = self.schedule_id
        payload["tenantId"] = self.tenant_id

        yield payload

        self.logger.info("Crawled web page from url: " + str(url))

        if self.links_to_follow:
            meta_key = "is_link_to_follow"
            if meta_key not in response.meta:
                # Extracts all links from `link_to_follow`
                extracted_links_to_follow = []
                for link_to_follow in self.links_to_follow:
                    extracted_links_to_follow.extend(response.xpath(link_to_follow).getall())

                # Each extracted link will yield a Request passing as meta "is_link_to_follow"
                for extracted_link_to_follow in extracted_links_to_follow:
                    # if is relative url convert to absolute
                    if not is_absolute(extracted_link_to_follow):
                        extracted_link_to_follow = response.urljoin(extracted_link_to_follow)
                    yield Request(extracted_link_to_follow,
                                  callback=self.parse,
                                  meta={
                                      meta_key: True,
                                      "playwright": self.use_playwright,
                                      "playwright_page_coroutines": [
                                          # We'll just wait a short time — if not found, no crash
                                          PageMethod("wait_for_selector", self.playwright_selector, timeout=self.playwright_timeout)
                                      ],
                                      "errback": self.errback_close_page,  # cleanup
                                  })
