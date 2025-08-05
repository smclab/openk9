---
id: gitlab-plugin
title: Gitlab Plugin
---

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

### Document Type Definition

A sitemap connector extracts web contents, and eventually documents of different type.

#### Web

A web content extracted contains following fields:

- title
- content
- url
- favicon

#### Document

A document content is added to Ingestion Payload inside resources field.

If you have installed File Handling package, Openk9 is then able to parse text from binaries.


### Installation and Configuration

To install Sitemap connector

### Apis

import ApiDocMdx from '@theme/ApiDocMdx';

<ApiDocMdx id="api-crawler" />