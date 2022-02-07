---
id: filter-suggestion-category
title: Filter Suggestion Category API
slug: /api/filter-suggestion-category

---

Filter suggestion category

```bash
POST /v2/suggestion-category/filter
{
  "name": "test-suggestion-category"
}
```

### Description

Get suggestion categories that match filter information passed in request body

### Request Body

`enabled: (boolean) Specific if suggestion category is enabled or not

`name`: (date) Name of suggestion category

`tenantId`: (integer  ) Id of related tenant

`categoryId`: (integer) Id of parent category

`fieldName`: (integer) Name of suggestion category

