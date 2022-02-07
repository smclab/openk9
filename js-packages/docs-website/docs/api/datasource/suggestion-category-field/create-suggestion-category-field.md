---
id: create-suggestion-category-field
title: Create Suggestion Category API
slug: /api/create-suggestion-category-field

---

Create new suggestion category field

```bash
POST /v2/suggestion-category-field
{
  "active": true,
  "datasourceId": 123,
  "name": "example pipeline"
}
```

### Description

Allows you to create a new suggestion category field.

### Request Body

`enabled: (boolean) Specific if suggestion category field is enabled or not

`name`: (date) Name of suggestion category field

`tenantId`: (integer  ) Id of related tenant

`categoryId`: (integer) Id of related category

`fieldName`: (integer) Name of suggestion category field

