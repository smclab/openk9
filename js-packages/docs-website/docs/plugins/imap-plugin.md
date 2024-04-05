---
id: imap-plugin
title: Imap Plugin
---

## Connector

An Imap connector allow to retrieve email and attachments from an imap server.

Source code is available on [Github repository].


### Characteristics

An imap connector needs following required parameters:

- **mailServer**: list of sitemap urls or robots.txt url
- **username**: list of sitemap urls or robots.txt url
- **password**: list of sitemap urls or robots.txt url

Additionally, you can specify following optional parameters:

- **port**: list of allowed domains from where retrieve contents.
  If not specified, contents from every domain is allowed.
- **folder**: html tag from where get page text content. Every text outside specified tag is discarded.
  If not specified every text content in page is parsed and retrieved.
- **getAttachments**: html tag from where get page title. If not specified html title is retrieved.


### Deployment

A prebuilt docker image is available on [Docker Hub].

You can deploy image adding follow script on your Docker Compose file:

```bash
gitlab-parser:
  image: smclab/openk9-web-parser:latest
  container_name: web-parser
  environment:
      INGESTION_URL: " http://ingestion:8080/v1/ingestion/"
  ports:
      - "5000:5000"
```

Alternatively you can install it using Helm Chart.

After deploy on your environment, before run first data ingestion, you need to configure it adding configuration on
consul.

Add




#### Tika Parser

If extract documents, you need to configure tika parsing enrichment activity to extract text and metadata from
crawled documents.

#### Character Optical Recognition

If you want Openk9 to extract text from image files or documents containing images, you need to configure
an appropriate enrichment activity.

## Document Type Definition

An imap connector extracts email contents and documents as attachments.

A base definition of mappings for these types of data is present inside plugin definition you can find on [Github repository]


