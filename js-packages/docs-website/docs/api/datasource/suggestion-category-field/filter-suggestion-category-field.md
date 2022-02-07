---
id: filter-suggestion-category-field
title: Filter Suggestion Category Field API
slug: /api/filter-suggestion-category-field

---

Filter suggestion category field

```bash
POST /v2/suggestion-category-field/filter
{
  "name": "test-suggestion-category-field"
}
```

### Description

Get suggestion category fields that match filter information passed in request body

### Request Body

`active`: (boolean) Specific if enrich pipeline is enabled or not

`name`: (string) Name of enrich pipeline

`datasourceId`: (integer) Id of related datasourceId

