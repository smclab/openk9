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

from scrapy.spiders import SitemapSpider
import json
import requests
from datetime import datetime
from bs4 import BeautifulSoup
import ast
import re

from .util import get_favicon, str_to_bool


class WebSitemapSpider(SitemapSpider):

    name = "SitemapWebCrawler"

    def __init__(self, timestamp, datasource_id, ingestion_url, sitemap_urls, allowed_domains, sitemap_rules,
                 follow, *args, **kwargs):
        super(WebSitemapSpider, self).__init__(*args, **kwargs)
        self.sitemap_urls = ast.literal_eval(sitemap_urls)
        self.allowed_domains = ast.literal_eval(allowed_domains)
        self.sitemap_rules = ast.literal_eval(sitemap_rules)
        self.ingestion_url = ingestion_url
        self.datasource_id = datasource_id
        self.timestamp = int(timestamp)
        self.follow = str_to_bool(follow)

        start_datetime = datetime.fromtimestamp(self.timestamp/1000)
        start_date = datetime.strftime(start_datetime, "%d-%b-%Y")

        self.logger.info("Getting pages from " + start_date)

        self.end_timestamp = datetime.utcnow().timestamp()*1000

    def post_message(self, url, payload, timeout):

        try:
            r = requests.post(url, data=payload, timeout=timeout)
            if r.status_code == 200:
                return
            else:
                r.raise_for_status()
        except requests.RequestException as e:
            self.logger.error(str(e) + " during request at url: " + str(url))
            raise e

    def parse(self, response, **kwargs):

        self.logger.info('Crawling of page with url: ' + str(response.url))

        title = response.css('title::text').get()
        if title is not None:
            title = title.strip()
        else:
            title = "Unknown title"
        body = response.body.decode('utf-8', 'ignore')
        soup = BeautifulSoup(body, features="html.parser").get_text()
        soup = soup.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
        soup = re.sub(' +', ' ', soup)

        datasource_payload = {
            "web": {
                "url": response.url,
                "title": title,
                "content": soup,
                "favicon": get_favicon(response.url),
            }
        }

        payload = {
            "datasourceId": self.datasource_id,
            "contentId": hash(str(response.url)),
            "parsingDate": int(self.end_timestamp),
            "rawContent": soup,
            "datasourcePayload": json.dumps(datasource_payload)
        }
        
        sself.post_message(self.ingestion_url, payload, 10)
        self.logger.info('Page with url: ' + str(response.url) + ' ingestioned')
        if self.follow:
            for href in response.css('a::attr(href)'):
                try:
                    yield response.follow(href, callback=self.parse)
                except ValueError:
                    self.logger.info("Filtered url " + str(href))
