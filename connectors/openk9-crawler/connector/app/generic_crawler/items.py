# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class GenericCrawlerItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    pass

    
class FileItem(scrapy.Item):
    lastModifiedDate = scrapy.Field()
    path = scrapy.Field()
    fileName = scrapy.Field()


class BinaryItem(scrapy.Item):
    id = scrapy.Field()
    name = scrapy.Field()
    contentType = scrapy.Field()
    data = scrapy.Field()


class DocumentItem(scrapy.Item):
    url = scrapy.Field()
    mimeType = scrapy.Field()
    extension = scrapy.Field()


class Payload(scrapy.Item):
    datasourceId = scrapy.Field()
    scheduleId = scrapy.Field()
    contentId = scrapy.Field()
    rawContent = scrapy.Field()
    parsingDate = scrapy.Field()
    datasourcePayload = scrapy.Field()
    resources = scrapy.Field()
    acl = scrapy.Field()
    tenantId = scrapy.Field()
    last = scrapy.Field()