---
id: patch-suggestion-category-field
title: patch Suggestion Category Field API
slug: /api/patch-suggestion-category-field

---

Patch suggestion category field


```bash
PATCH /v2/suggestion-category-field
{
    "enabled": false
}
```

### Description

Allows you to update individual information in suggestion category field

### Request Body

`enabled: (boolean) Specific if suggestion category field is enabled or not

`name`: (date) Name of suggestion category field

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of related category

`suggestionCategoryFieldId`: (integer) Id of suggestion category field

`fieldName`: (integer) Field name of suggestion category field
