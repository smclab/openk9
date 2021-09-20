# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class CrawlerItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    pass


class BinaryItem(scrapy.Item):
    id = scrapy.Field()
    name = scrapy.Field()
    contentType = scrapy.Field()
    data = scrapy.Field()


class GenericWebItem(scrapy.Item):
    url = scrapy.Field()
    title = scrapy.Field()
    content = scrapy.Field()
    favicon = scrapy.Field()


class Payload(scrapy.Item):
    datasourceId = scrapy.Field()
    contentId = scrapy.Field()
    rawContent = scrapy.Field()
    parsingDate = scrapy.Field()
    datasourcePayload = scrapy.Field()
    resources = scrapy.Field()


class FileItem(scrapy.Item):
    lastModifiedDate = scrapy.Field()
    path = scrapy.Field()


class DocumentItem(scrapy.Item):
    url = scrapy.Field()
    contentType = scrapy.Field()
    title = scrapy.Field()
    content = scrapy.Field()
    relativeUrl = scrapy.Field()