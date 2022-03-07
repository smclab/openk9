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

import requests
import re
import logging
import hashlib
import os

from bs4 import BeautifulSoup
from urllib.parse import urlparse
from datetime import datetime

from crawler.items import Payload, DocumentItem, FileItem, BinaryItem
from ..generic.utility import post_message, get_as_base64

logger = logging.getLogger(__name__)

extension_list = ["pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"]


def map_type(content_type, type_mapping):
    try:
        document_type = type_mapping[content_type]
        return document_type
    except KeyError:
        return None


def get_path(url):
    base_url = urlparse(url)
    return base_url.path


def parse_document_by_url(url, spider, relative_url=None, title=None):

    for extension in extension_list:

        if extension in url:

            response = requests.get(url)
            payload = response.content

            file_item = FileItem()
            file_item['path'] = get_path(url)

            document_item = DocumentItem()
            document_item['url'] = url
            document_item['title'] = title
            document_item['relativeUrl'] = relative_url

            datasource_payload = {
                "file": dict(file_item),
                "document": dict(document_item)
            }

            content_id = str(int(hashlib.sha1(url.encode("utf-8")).hexdigest(), 16))

            binary_item = BinaryItem()
            binary_item["id"] = content_id
            binary_item["name"] = url
            binary_item["contentType"] = None
            binary_item["data"] = get_as_base64(payload)

            binaries = [dict(binary_item)]

            payload = Payload()

            payload["parsingDate"] = int(spider.end_timestamp)
            payload['datasourceId'] = spider.datasource_id
            payload['contentId'] = content_id
            payload['rawContent'] = None
            payload['datasourcePayload'] = datasource_payload
            payload["resources"] = {
                "binaries": binaries
            }

            if content_id not in spider.crawled_ids:
                post_message(spider.ingestion_url, dict(payload))

                spider.crawled_ids.append(content_id)

                logger.info("Crawled document from url: " + str(url))

                spider.cont = spider.cont + 1

                logger.info("Crawled " + str(spider.cont) + " elements")
            else:
                logger.info("Duplicate document with id " + str(content_id))
                return

            break
