---
id: doc1
title: Getting Started
---

Openk9 project is available on Github repository. Clone it and try on your machine.

## Usage

### Prerequisites

To execute Openk9 you need to install:

- Git
- Docker
- Docker-Compose

Openk9 requires the use of Elasticsearch in Docker. 

*To learn more about using Elasticsearch in docker read this:* [Using Docker images in production](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html#docker-prod-prerequisites)

### Clone the repository

Once you have installed the necessary dependencies and setting Elasticsearch, clone the repository on your machine:

```
git clone https://github.com/smclab/openk9.git
```

### Start Openk9

To start Openk9 execute:

```
docker-compose up -d
```

You can see logs with:

```
docker-compose logs -f
```

or if you want to see logs for a specific container run:

```
docker-compose logs -f <container-name>
```

Once the startup is finished go to [http://localhost:8888/](http://localhost:8888/) to access the browser Openk9 interface.

### Basic environment

In his basic version, Openk9 is configured with web crawler plugin, to extract web pages from a specific domain.<br />
In particular, crawling starting from some Wikipedia page, is configured.

Once the startup is finished, an assigned bundle will automatically start crawling pages from the web. Every content will go into an enrichment pipeline with a Named Entity Recognition processor. <br />
So, for each page, entities of type organization, person, geographic or product, will be recognized.

Wait a few minutes for more documents to be indexed. Then access the OpenK9 search interface and try to search for results, specifying the filters and semantic entities of your interest.

If you want to crawl and index pages from the site of your preference, go to Configuration section and learn how to configure your data source. 

### Demo