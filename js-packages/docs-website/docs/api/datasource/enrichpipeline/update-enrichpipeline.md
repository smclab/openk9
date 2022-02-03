---
id: update-enrichpipeline
title: Update enrichpipeline API
slug: /api/update-enrichpipeline

---

Update existing enrichpipeline

```bash
PUT /v2/datasource
{
    "tenantId": 2,
    "name": "openk9.io",
    "virtualHost": "openk9.io",
    "jsonConfig": "{}"
  }
```

### Description

Update entire enrichpipeline with information specified in request body

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`name`: (date) Name of enrichpipeline

`datasourceId`: (int) Id of related datasourceId
