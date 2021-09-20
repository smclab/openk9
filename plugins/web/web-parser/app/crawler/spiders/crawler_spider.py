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

import ast
import json
from datetime import datetime
import logging
import hashlib
import concurrent.futures

from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
from scrapy import signals

from crawler.items import GenericWebItem, Payload
from .util.generic.utility import get_favicon, get_title, get_content, post_message
from .util.tika.utility import parse_document_by_url

logger = logging.getLogger(__name__)


class CustomCrawlerSpider(CrawlSpider):

    name = 'RandomSpider'

    cont = 0

    crawled_ids = []
    allowed_types = []
    type_mapping = []

    def __init__(self, allowed_domains, start_urls, allowed_paths, excluded_paths, body_tag, title_tag, follow,
                 datasource_id, ingestion_url, delete_url, *args, **kwargs):
        super(CrawlSpider, self).__init__(*args, **kwargs)

        self.allowed_domains = ast.literal_eval(allowed_domains)
        self.start_urls = ast.literal_eval(start_urls)
        self.allowed_paths = ast.literal_eval(allowed_paths)
        self.excluded_paths = ast.literal_eval(excluded_paths)
        self.datasource_id = datasource_id
        self.body_tag = body_tag
        self.title_tag = title_tag
        self.follow = follow

        self.ingestion_url = ingestion_url
        self.delete_url = delete_url

        CustomCrawlerSpider.rules = (
            # Extract links matching 'item.php' and parse them with the spider's method parse_item
            Rule(LinkExtractor(allow=self.allowed_paths, deny=self.excluded_paths, ),
                 callback='parse', follow=self.follow,),
        )
        super(CustomCrawlerSpider, self)._compile_rules()
        self.end_timestamp = datetime.utcnow().timestamp() * 1000

        try:
            with open("./crawler/spiders/mapping_config.json") as config_file:
                self.config = json.load(config_file)
        except (FileNotFoundError, json.decoder.JSONDecodeError):
            self.logger.error("Ingestion configuration file is missing or there is some error in it.")
            return

        self.type_mapping = self.config["TYPE_MAPPING"]
        self.allowed_types = self.type_mapping.keys()
        self.logger.info(self.allowed_types)

        self.executor = concurrent.futures.ThreadPoolExecutor(8)

    @classmethod
    def from_crawler(cls, crawler, *args, **kwargs):
        spider = super(CustomCrawlerSpider, cls).from_crawler(crawler, *args, **kwargs)
        crawler.signals.connect(spider.spider_closed, signals.spider_closed)
        return spider

    def spider_closed(self, spider):
        if spider.executor is not None:
            spider.executor.shutdown(wait=True)

        logger.info("Ingestion completed")

        payload = {
            "datasourceId": self.datasource_id,
            "contentIds": self.crawled_ids
        }

        logger.info(payload)

        post_message(self.delete_url, payload)

    def parse(self, response, **kwargs):

        if hasattr(response, "text"):

            title = get_title(response, self.title_tag)

            content = get_content(response, self.body_tag)

            web_item = GenericWebItem()
            web_item['url'] = response.url
            web_item['content'] = content
            web_item['title'] = title
            web_item['favicon'] = get_favicon(response.url)

            datasource_payload = {
                "web": dict(web_item)
            }

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

            self.executor.submit(parse_document_by_url, response.url, self)
