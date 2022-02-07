---
id: clean-orphan-entities-api
title: Clean Orphan Entities APIs
slug: /api/clean-orphan-entities-api

---

Delete documents in Elasticsearch by Content Ids

```bash
POST /v1/clean-orphan-entities/
{
    "datasourceId": "3",
    "contentIds": [
        "123",
        "124",
        "1125"
    ]
}
```

### Description

This endpoint allows you to delete documents of specific datasource in Elasticsearch index

### Request Body

`datasourceId`: (int) Id of datasource

`contentIds`: (list) List of document contentIds to delete
