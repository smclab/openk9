---
id: filter-enrichitem
title: Filter Enrich Item API
slug: /api/filter-enrichitem

---

Filter enrichitem

```bash
POST /v2/enrichitem/filter
{
  "name": "test-enrichitem"
}
```

### Description

Get enrich items that match filter information passed in request body

### Request Body

`enrichPipelineId`: (int) Id of related enrich pipeline

`active`: (boolean) Specific if enrich item is enabled or not

`jsonConfig`: (dict) Configuration of the enrich item. Normally includes some configuration of enrich item.

`name`: (date) Name of enrich item

`_position`: (int) Id of position of enrich item in pipeline

`driverServiceName`: (string) Symbolic name of related enrich processor

