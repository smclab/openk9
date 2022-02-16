---
id: update-suggestion-category-field
title: Update Suggestion Category Field  API
slug: /api/update-suggestion-category-field

---

Update existing suggestion category field

```bash
PUT /v2/suggestion-category-field
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

Update entire suggestion category field with information specified in request body

### Request Body

`enabled: (boolean) Specific if suggestion category field is enabled or not

`name`: (date) Name of suggestion category field

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of related category

`suggestionCategoryFieldId`: (integer) Id of suggestion category field

`fieldName`: (integer) Field name of suggestion category field
