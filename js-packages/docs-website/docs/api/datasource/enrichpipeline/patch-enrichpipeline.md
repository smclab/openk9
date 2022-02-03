---
id: patch-enrichpipeline
title: patch enrichpipeline API
slug: /api/patch-enrichpipeline

---

Patch enrichpipeline


```bash
PATCH /v2/enrichpipeline
{
    "active": false
}
```

### Description

Allows you to update individual information in enrichpipeline

### Request Body

`active`: (boolean) Specific if enrichpipeline is enabled or not

`name`: (date) Name of datasource

`datasourceId`: (int) Id of related datasourceId
