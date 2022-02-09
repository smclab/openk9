---
id: patch-enrichitem
title: Patch Enrich Item API
slug: /api/patch-enrichitem

---

Patch Enrich Item


```bash
PATCH /v2/enrichitem
{
    "active": false
}
```

### Description

Allows you to update individual information in enrich item

### Request Body

`enrichPipelineId`: (int) Id of related enrich pipeline

`active`: (boolean) Specific if enrich item is enabled or not

`jsonConfig`: (dict) Configuration of the enrich item. Normally includes some configuration of enrich item.

`name`: (date) Name of enrich item

`_position`: (int) Id of position of enrich item in pipeline

`driverServiceName`: (string) Symbolic name of related enrich processor
