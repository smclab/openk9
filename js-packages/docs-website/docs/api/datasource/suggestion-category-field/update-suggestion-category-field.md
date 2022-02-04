---
id: update-suggestion-category-field
title: Update Suggestion Category Field  API
slug: /api/update-suggestion-category-field

---

Update existing suggestion category field

```bash
PUT /v2/suggestion-category-field
{
    "tenantId": 2,
    "name": "openk9.io",
    "virtualHost": "openk9.io",
    "jsonConfig": "{}"
  }
```

### Description

Update entire suggestion category field with information specified in request body

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`name`: (date) Name of suggestion category field

`datasourceId`: (int) Id of related datasourceId
