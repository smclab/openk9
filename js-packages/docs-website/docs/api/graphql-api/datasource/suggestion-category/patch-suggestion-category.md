---
id: patch-suggestion-category
title: patch Suggestion Category API
slug: /api/patch-suggestion-category

---

Patch suggestion category


```bash
PATCH /v2/suggestion-category
{
    "enabled": false
}
```

### Description

Allows you to update individual information in suggestion category

### Request Body

`enabled: (boolean) Specific if suggestion category is enabled or not

`name`: (date) Name of suggestion category

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of category

`parentcategoryId`: (integer) Id of parent category
