---
id: patch-suggestion-category
title: patch Suggestion Category API
slug: /api/patch-suggestion-category

---

Patch suggestion category


```bash
PATCH /v2/suggestion-category
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
