---
id: create-suggestion-category-field
title: Create Suggestion Category API
slug: /api/create-suggestion-category-field

---

Create new suggestion category field

```bash
POST /v2/suggestion-category-field
{
  "suggestionCategoryId": 1,
  "tenantId": 1,
  "categoryId": 1,
  "fieldName": "web.category",
  "enabled": true,
  "name": "Category"
}
```

### Description

Allows you to create a new suggestion category field.

### Request Body

`enabled: (boolean) Specific if suggestion category field is enabled or not

`name`: (date) Name of suggestion category field

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of related category

`suggestionCategoryFieldId`: (integer) Id of suggestion category field

`fieldName`: (integer) Field name of suggestion category field
