---
id: create-suggestion-category-field
title: Create Suggestion Category API
slug: /api/create-suggestion-category-field

---

Create new suggestion category

```bash
POST /v2/suggestion-category-field
{
  "active": true,
  "datasourceId": 123,
  "name": "example pipeline"
}
```

### Description

Allows you to create a new datasource.

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`name`: (date) Name of datasource

`datasourceId`: (int) Id of related datasourceId
