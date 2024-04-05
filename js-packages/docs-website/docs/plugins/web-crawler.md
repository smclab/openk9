---
id: web-crawler
title: Web Crawler
---

## Connector

A sitemap connector allow to retrieve contents from a website using sitemap.xml or robots.txt.

Source code is available on [Github repository]. A prebuilt docker image is available on [Docker Hub]


### Characteristics

A sitemap connector needs following required parameters:

- **sitemapUrls**: list of sitemap urls or robots.txt url

Additionally, you can specify following optional parameters:

- **allowedDomains**: list of allowed domains from where retrieve contents.
  If not specified, contents from every domain is allowed.
- **bodyTag**: html tag from where get page text content. Every text outside specified tag is discarded.
  If not specified every text content in page is parsed and retrieved.
- **titleTag**: html tag from where get page title. If not specified html title is retrieved.
- **maxLength**: max length of text content for crawled pages. If not specified, no truncation of the text takes place.


### Tika Parser

If extract documents, you need to configure tika parsing enrichment activity to extract text and metadata from
crawled documents.

### Character Optical Recognition

If you want Openk9 to extract text from image files or documents containing images, you need to configure
an appropriate enrichment activity.

## Document Type Definition

A sitemap connector extracts web contents, and eventually documents of different type.

### Web

Web pages extracted contains following fields:

- title
- content
- url
- favicon


### Document
