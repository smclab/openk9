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

`enabled: (boolean) Specific if suggestion category field is enabled or not

`name`: (date) Name of suggestion category field

`tenantId`: (integer) Id of related tenant

`categoryId`: (integer) Id of related category

`suggestionCategoryFieldId`: (integer) Id of suggestion category field

`fieldName`: (integer) Field name of suggestion category field

