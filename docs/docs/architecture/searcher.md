---
id: searcher
title: Searcher
slug: /searcher

---

Searcher is the component delegated to perform every search action in OpenK9 indexes. It exposes headless api,
which can be used to perform searches and other actions on indexed data

![img](../../static/img/searcher.png)

### Query Builder

Defines, based on criteria, the set of contributor queries to apply to the search query

### Query Contributor

Pieces that deal with intercepting search tokens and transforming them into elastic queries.
They can be of different types(DOCTYPE, TEXT, DATASOURCE, ENTITY,...)

### Query Analysis

Query analysis performs an understanding analysis on natural language query. In particular it returns a list of
search tokens based on different concepts and meanings founded in query. It is developed using a modern technique
called [Semantic Parsing](https://en.wikipedia.org/wiki/Semantic_parsing)

### OpenSearch Adapter

OpenK9 uses an OpenSearch client to comunicate with Its APIs. See
[client documentation](https://docs.opensearch.org/latest/clients/) to realize adapter.

### Rest Api

See more on [Api Documentation](../api/api-searcher.md)
