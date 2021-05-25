from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
import ast
import json
from bs4 import BeautifulSoup
from datetime import datetime
import re
import tika
from tika import parser
import base64
import requests
from PIL import Image
from io import BytesIO
from pdf2image import convert_from_path, convert_from_bytes

from .util import get_favicon, map_type, get_path


class MySpider(CrawlSpider):

    name = 'WebCrawler'

    cont = 0

    def __init__(self, allowed_domains, start_urls, allowed_paths, excluded_paths, datasource_id,
                 ingestion_url, *args, **kwargs):
        super(CrawlSpider, self).__init__(*args, **kwargs)
        self.allowed_domains = ast.literal_eval(allowed_domains)
        self.start_urls = ast.literal_eval(start_urls)
        self.allowed_paths = ast.literal_eval(allowed_paths)
        self.excluded_paths = ast.literal_eval(excluded_paths)
        self.ingestion_url = ingestion_url
        self.datasource_id = datasource_id

        MySpider.rules = (
            # Extract links matching 'item.php' and parse them with the spider's method parse_item
            Rule(LinkExtractor(allow=self.allowed_paths, deny=self.excluded_paths, ),
                 callback='parse', follow=True,),
        )
        super(MySpider, self)._compile_rules()
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

        tika.initVM()

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

    def get_as_base64(self, url):

        r = requests.get(url, stream=True)
        images = convert_from_bytes(r.content)
        try:
            im = images[0]
            size = 128, 128
            im.thumbnail(size, Image.ANTIALIAS)
            buffered = BytesIO()
            im.save(buffered, "PNG")
        except IOError:
            self.logger.error("cannot create thumbnail")
            return
        uri = ("data:image/png;" + "base64,"
               + base64.b64encode(buffered.getvalue()).decode("utf-8"))
        return uri

    def parse(self, response, **kwargs):

        if hasattr(response, "text"):
            title = response.css('title::text').get()
            if title is not None:
                title = title.strip()
            else:
                title = "Unknown title"
            body = response.body
            soup = BeautifulSoup(body, 'lxml')
            content = ''
            for p in soup.find_all("p"):
                content = content + p.get_text()
            content = content.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
            content = re.sub(' +', ' ', content)
            self.cont = self.cont + 1

            datasource_payload = {
                "web": {
                    "url": response.url,
                    "title": title,
                    "content": content,
                    "favicon": get_favicon(response.url),
                }
            }

            self.logger.info("Crawled web page from url: " + str(response.url))

        else:
            parsed = parser.from_file(response.url)

            metadata = parsed['metadata']
            content_type = metadata['Content-Type']

            if content_type in self.allowed_types:

                # saving content of pdf
                # you can also bring text only, by parsed_pdf['text']
                # parsed_pdf['content'] returns string
                content = parsed['content']
                content = content.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
                content = re.sub(' +', ' ', content)

                try:
                    title = metadata['title']
                except KeyError:
                    title = metadata["resourceName"].replace("'b", "")
                self.cont = self.cont + 1
                self.logger.info("Crawled document from url: " + str(response.url))

                datasource_payload = {
                    "file": {
                        "lastModifiedDate": metadata['Last-Modified'],
                        "path": get_path(response.url)
                    },
                    "document": {
                        "URL": response.url,
                        "contentType": content_type,
                        "title": title,
                        "content": content,
                        "previewUrl": self.get_as_base64(response.url),
                        "previewURLs": []
                    }
                }

                type_values = {}

                mapping = map_type(content_type, self.type_mapping)
                self.logger.info(content_type)
                self.logger.info(mapping)
                if mapping is not None:
                    datasource_payload[mapping] = type_values

            else:
                self.logger.info("Document with url: " + str(response.url) + " excluded")
                return

        self.logger.info("Crawled " + str(self.cont) + " elements")

        payload = {
            "datasourceId": self.datasource_id,
            "contentId": hash(str(response.url)),
            "parsingDate": int(self.end_timestamp),
            "rawContent": title + " " + content,
            "datasourcePayload": json.dumps(datasource_payload)
        }

        self.post_message(self.ingestion_url, payload, 10)
