---
id: search-entities
title: Search Entity API
slug: /api/search-entities

---

Search Entities on Index

```bash
POST /v1/entity
{
  "all": "name",
  "size": [0,1000]
}
```

### Description

Allows you to execute a search query on entities index and get back a list of entities that match the query.
You can provide search queries using the following request body.

### Request Body

`all`: (string) Query for search

`range`: ([integer]) Range to get results


