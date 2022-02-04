---
id: patch-suggestion-category-field
title: patch Suggestion Category Field API
slug: /api/patch-suggestion-category-field

---

Patch suggestion category field


```bash
PATCH /v2/suggestion-category-field
{
    "active": false
}
```

### Description

Allows you to update individual information in suggestion category field

### Request Body

`active`: (boolean) Specific if suggestion category field is enabled or not

`name`: (date) Name of datasource

`datasourceId`: (int) Id of related datasourceId
