---
id: database-plugin
title: Database Plugin
---

## Connector

A database connector allow to retrieve data from a database.

Source code is available on [Github repository]. A prebuilt docker image is available on [Docker Hub]


### Characteristics

A database connector needs following required parameters:

- **sitemapUrls**: list of sitemap urls or robots.txt url

Additionally, you can specify following optional parameters:

- **allowedDomains**: list of allowed domains from where retrieve contents.
  If not specified, contents from every domain is allowed.
- **bodyTag**: html tag from where get page text content. Every text outside specified tag is discarded.
  If not specified every text content in page is parsed and retrieved.
- **titleTag**: html tag from where get page title. If not specified html title is retrieved.
- **maxLength**: max length of text content for crawled pages. If not specified, no truncation of the text takes place.

## Mapping

A sitemap connector extracts web contents, and eventually documents of different type.

### Web

Web pages extracted contains following fields:

- title
- content
- url
- favicon


### Document
