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
import humanfriendly
import os

from bs4 import BeautifulSoup
from urllib.parse import urlparse
from datetime import datetime

from crawler.items import Payload, DocumentItem, FileItem, BinaryItem
from ..generic.utility import post_message, get_as_base64

logger = logging.getLogger(__name__)
max_file_size = os.environ.get("MAX_FILE_SIZE")


def map_type(content_type, type_mapping):
    try:
        document_type = type_mapping[content_type]
        return document_type
    except KeyError:
        return None


def get_path(url):
    base_url = urlparse(url)
    return base_url.path


def parse_document_by_url(url, spider):

    try:
        logger.info(url)
        response = requests.get(url)
        payload = response.content

        if len(payload) < humanfriendly.parse_size(max_file_size):

            r = requests.request("PUT", "http://localhost:9998/tika", data=payload)

            element = r.text
            soup = BeautifulSoup(element, 'html.parser')

            content_type = soup.find("meta", {"name": "Content-Type"})
            if content_type is not None:
                content_type = content_type["content"]

            if content_type in spider.allowed_types:

                title = soup.find("meta", {"name": "dc:title"})
                if title is not None:
                    title = title["content"].strip()
                    if len(title) == 0:
                        title = url
                else:
                    title = url

                content = soup.find('body').get_text()
                if content is not None:
                    content = content.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
                    content = re.sub(' +', ' ', content)
                else:
                    content = ""

                last_modified_date = soup.find("meta", {"name": "Last-Modified"})
                if content_type is None:
                    last_modified_date = datetime.fromtimestamp(0).isoformat()

                file_item = FileItem()
                file_item['path'] = get_path(url)
                file_item['lastModifiedDate'] = last_modified_date

                document_item = DocumentItem()
                document_item['url'] = url
                document_item['contentType'] = content_type
                document_item['title'] = title
                document_item['content'] = content

                datasource_payload = {
                    "file": dict(file_item),
                    "document": dict(document_item)
                }

                type_values = {}

                mapping = map_type(content_type, spider.type_mapping)
                if mapping is not None:
                    datasource_payload[mapping] = type_values

                content_id = int(hashlib.sha1(url.encode("utf-8")).hexdigest(), 16) % (10 ** 16)

                binary_item = BinaryItem()
                binary_item["id"] = content_id
                binary_item["name"] = title
                binary_item["contentType"] = content_type
                binary_item["data"] = get_as_base64(response)

                binaries = [dict(binary_item)]

                payload = Payload()

                payload["parsingDate"] = int(spider.end_timestamp)
                payload['datasourceId'] = spider.datasource_id
                payload['contentId'] = content_id
                payload['rawContent'] = content
                payload['datasourcePayload'] = datasource_payload
                payload["resources"] = {
                    "binaries": binaries
                }

                post_message(spider.ingestion_url, dict(payload))

                spider.crawled_ids.append(str(content_id))
                logger.info("Crawled document from url: " + str(url))
                spider.cont = spider.cont + 1
                logger.info("Crawled " + str(spider.cont) + " elements")
        else:
            return

    except Exception as e:
        logger.error(e)
