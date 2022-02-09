---
id: create-suggestion category
title: Create Suggestion Category API
slug: /api/create-suggestion-category

---

Create new suggestion category

```bash
POST /v2/suggestion category
{
  "suggestionCategoryId": 1,
  "tenantId": 1,
  "parentCategoryId": -1,
  "name": "Category",
  "enabled": true
}
```

### Description

Allows you to create a new suggestion category.

### Request Body

`enabled: (boolean) Specific if suggestion category is enabled or not

`name`: (date) Name of suggestion category

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of category

`parentcategoryId`: (integer) Id of parent category
