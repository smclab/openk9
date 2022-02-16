---
id: update-suggestion-category
title: Update suggestion-category API
slug: /api/update-suggestion-category

---

Update existing enrichpipeline

```bash
PUT /v2/suggestion-category
{
  "suggestionCategoryId": 1,
  "tenantId": 1,
  "parentCategoryId": -1,
  "name": "Test2",
  "enabled": true
}
```

### Description

Update entire suggestion category with information specified in request body

### Request Body

`enabled: (boolean) Specific if suggestion category is enabled or not

`name`: (date) Name of suggestion category

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of category

`parentcategoryId`: (integer) Id of parent category
