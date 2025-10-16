# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
import os
import logging
from scrapy import signals
from scrapy.exporters import CsvItemExporter
from .spiders.util.generic.utility import post_message
from scrapy.exceptions import DropItem

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://ingestion:8080/api/ingestion/v1/ingestion/"

logger = logging.getLogger(__name__)


class GenericCrawlerPipeline:

    @classmethod
    def from_crawler(cls, crawler):
        pipeline = cls()
        crawler.signals.connect(pipeline.spider_opened, signals.spider_opened)
        crawler.signals.connect(pipeline.spider_closed, signals.spider_closed)
        return pipeline

    def spider_opened(self, spider):
        if not os.path.exists("result/" + spider.name):
            os.makedirs("result/" + spider.name)

        self.file = open("result/" + spider.name + "/output.csv", 'w+b')
        self.exporter = CsvItemExporter(self.file)

        self.exporter.start_exporting()

    def spider_closed(self, spider):
        self.exporter.finish_exporting()
        self.file.close()

    def process_item(self, item, spider):

        if item['contentId'] in spider.crawled_ids:
            raise DropItem(f"Duplicate item found: {item['contentId']!r}")
        else:
            # uncomment next line to save crawled data to csv files
            # self.exporter.export_item(item)

            # logger.info(dict(item))

            # logger.info("ContentId " + str(item['contentId']))
            post_message(ingestion_url, dict(item))

            spider.crawled_ids.append(item['contentId'])
            spider.count = spider.count + 1
            logger.info("Crawled " + str(spider.count) + " elements")

            return item

