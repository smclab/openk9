---
id: delete-data-documents-api
title: Delete Data Documents APIs
slug: /api/delete-data-documents-api

---

Delete documents in Elasticsearch by Content Ids

```bash
POST /v1/delete-data-documents/
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

`contentIds`: ([integer]) List of document contentIds to delete
