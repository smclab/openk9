import scrapy

from .util.file.utility import get_path
from .util.generic.utility import (clean_extraction, str_to_bool, get_as_base64, get_content, get_favicon, get_title,
                                   generate_item, extract_text)
from .util.sitemap.utility import iterloc, regex
from datetime import datetime
from generic_crawler.items import BinaryItem, DocumentItem, FileItem, Payload
from generic_crawler.spiders.util.generic.utility import post_message
from generic_crawler.spiders.abstractBaseCrawlSpider import AbstractBaseCrawlSpider
from requests_futures.sessions import FuturesSession
from scrapy import signals, Item, Field
from scrapy.http import Request, XmlResponse
from scrapy.linkextractors import LinkExtractor
from scrapy.spiders import CrawlSpider, Rule
from scrapy.utils.gz import gunzip, gzip_magic_number
from urllib.parse import urlparse
import ast
import hashlib
import logging
import mimetypes
import random
import re

logger = logging.getLogger(__name__)


class GenericCrawlSpider(AbstractBaseCrawlSpider, CrawlSpider):
    name = "genericCrawlSpider"
    cont = 0

    allowed_types = []
    type_mapping = []

    def __init__(self, allowed_domains, start_urls, allowed_paths, excluded_paths, body_tag, title_tag, follow,
                 max_length, datasource_id, schedule_id, tenant_id, ingestion_url, document_file_extensions,
                 custom_metadata, additional_metadata, do_extract_docs, timestamp, *a, **kw):
        super(GenericCrawlSpider, self).__init__(ingestion_url, body_tag, title_tag, allowed_domains, excluded_paths,
                                                 allowed_paths, max_length, document_file_extensions, custom_metadata,
                                                 additional_metadata, do_extract_docs, datasource_id, schedule_id,
                                                 timestamp, tenant_id, *a, **kw)

        self.start_urls = ast.literal_eval(start_urls) if isinstance(ast.literal_eval(start_urls), list) \
            else []
        self.follow = str_to_bool(follow)

        GenericCrawlSpider.rules = (
            # Extract links matching 'item.php' and parse them with the spider's method parse_item
            Rule(LinkExtractor(allow=self.allowed_paths, deny=self.excluded_paths, ),
                 callback='parse', follow=self.follow, ),
        )
        super(GenericCrawlSpider, self)._compile_rules()

    @classmethod
    def from_crawler(cls, crawler, *args, **kwargs):
        spider = super(GenericCrawlSpider, cls).from_crawler(crawler, *args, **kwargs)
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

    def parse(self, response, **kwargs):

        if hasattr(response, "text"):

            url = response.url
            title = get_title(response, self.title_tag)
            content = get_content(response, self.max_length, self.body_tag)

            hrefs = response.css('a::attr(href)').getall()

            try:
                self.try_parse_documents(hrefs, response.request.url)
            except Exception as e:
                logger.error(e)

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

            if self.custom_metadata:
                for key, value in self.custom_metadata.items():
                    extracted_elements = response.xpath(value).getall()
                    if len(extracted_elements) == 1:
                        web_item[key] = extracted_elements[0].strip()
                    elif len(extracted_elements) > 1:
                        web_item[key] = [extracted_element.strip() for extracted_element in extracted_elements]
                    else:
                        web_item[key] = None

            datasource_payload = {
                "web": dict(web_item)
            }

            for key, value in self.additional_metadata.items():
                datasource_payload[key] = value

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
