import scrapy
from twisted.python.log import logerr

from .util.file.utility import get_path
from .util.generic.utility import clean_extraction, get_as_base64, get_content, get_favicon, get_title, generate_item, extract_text
from .util.sitemap.utility import iterloc, regex
from datetime import datetime
from generic_crawler.items import BinaryItem, DocumentItem, FileItem, Payload
from generic_crawler.spiders.util.generic.utility import post_message
from generic_crawler.spiders.abstractBaseCrawlSpider import AbstractBaseCrawlSpider
from requests_futures.sessions import FuturesSession
from scrapy import signals, Item, Field
from scrapy.http import Request, XmlResponse
from scrapy.linkextractors import LinkExtractor
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

    def __init__(self, sitemap_urls, allowed_domains, body_tag, title_tag, replace_rule, datasource_id, schedule_id,
                ingestion_url, timestamp, additional_metadata, max_length, tenant_id, excluded_paths, allowed_paths,
                 document_file_extensions, specific_tags, do_extract_docs, *a, **kw):

        super(GenericSitemapSpider, self).__init__(ingestion_url, body_tag, title_tag, allowed_domains,
                                                   excluded_paths, allowed_paths, max_length, document_file_extensions,
                                                   specific_tags, additional_metadata, do_extract_docs, datasource_id,
                                                   schedule_id, timestamp, tenant_id, *a, **kw)

        self._cbs = []
        for r, c in self.sitemap_rules:
            if isinstance(c, str):
                c = getattr(self, c)
            self._cbs.append((regex(r), c))

        self._follow = [regex(x) for x in self.sitemap_follow]

        self.sitemap_urls = ast.literal_eval(sitemap_urls)
        self.replace_rule = ast.literal_eval(replace_rule)

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
        content = get_content(response, self.max_length, self.body_tag)

        hrefs = response.css('a::attr(href)').getall()
        try:
            self.try_parse_documents(hrefs, response.request.url)
        except Exception as e:
            logger.error(e)

        web_item_fields = ['url', 'title', 'content', 'favicon']

        for element in self.specific_tags:
            web_item_fields.append(list(element.keys())[0])

        web_item = generate_item(web_item_fields)
        web_item['url'] = url
        web_item['title'] = title
        web_item['content'] = content
        try:
            web_item['favicon'] = get_favicon(url)
        except Exception as e:
            logger.error("Something goes wrong getting favicon")

        if len(self.specific_tags) > 0:
            for element in self.specific_tags:
                metadata, values = list(element.items())[0]
                html_element = values["html_element"]
                try:
                    index = values["index"]
                except KeyError:
                    index = None

                if "id" in values.keys():
                    html_element_id = values['id']
                    specific_web_item_content = response.css(f"{html_element}#{html_element_id}").getall()
                elif "class" in values.keys():
                    html_element_class = values['class']
                    specific_web_item_content = response.css(f"{html_element}.{html_element_class}").getall()
                else:
                    specific_web_item_content = response.css(f"{html_element}").getall()

                if len(specific_web_item_content) == 1:
                    web_item[metadata] = extract_text(specific_web_item_content[0])
                elif len(specific_web_item_content) > 1:
                    specific_web_item_content = [extract_text(element) for element in specific_web_item_content]
                    if index == None:
                        web_item[metadata] = specific_web_item_content
                    elif len(specific_web_item_content) > index - 1:
                        try:
                            web_item[metadata] = specific_web_item_content[index]
                        except IndexError as e:
                            web_item[metadata] = specific_web_item_content
        datasource_payload = {
            "web": dict(web_item)
        }

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
