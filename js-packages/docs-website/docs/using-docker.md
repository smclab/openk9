---
id: using-docker
title: Getting Started using Docker
---

The fastest way to try OpenK9 is by using our prebuilt Docker images and our docker-compose configuration.

## Prerequisites

To run OpenK9 under Docker you need to have installed:

- [Git](https://git-scm.com/)
- [Docker](https://www.docker.com/)
- [docker-compose](https://docs.docker.com/compose/)

:::caution
OpenK9 requires the use of Elasticsearch in Docker, which may require some specific OS–level configuration regarding swap memory.

To learn more about using Elasticsearch in docker read the official article about [using Docker images in production](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html#docker-prod-prerequisites).
:::

### Clone the repository

Once you have installed the necessary dependencies clone the repository on your machine to get all the required files and configs:

```bash
git clone https://github.com/smclab/openk9.git
cd openk9
```

### Start OpenK9

To start OpenK9 execute:

```bash
docker-compose up -d
```

You can see logs with:

```bash
docker-compose logs -f
```

or if you want to see logs for a specific container run:

```bash
docker-compose logs -f <container-name>
```

Once the startup is finished go to [http://localhost:8888/](http://localhost:8888/) to access the browser OpenK9 interface or [http://localhost:8888/admin](http://localhost:8888/admin) for the admin interface.

### Basic environment

In this default configuration, OpenK9 is configured only with a web crawler plugin, extracting web pages from a specific domain. As example configuration it's crawling some Wikipedia pages.

Once the startup is finished, it will automatically start crawling pages from the web. Every crawled page will go into an enrichment pipeline with a Named Entity Recognition processor. For each page entities of type organization, person, geographic or product will be recognized and extracted as entity, and it will be possible to search into them.

Wait a few minutes for more documents to be indexed. Then access the OpenK9 search interface and try to search for results, specifying the filters and semantic entities of your interest.

If you want to crawl and index pages from the site of your preference, go to Configuration section and learn how to configure your data source.
