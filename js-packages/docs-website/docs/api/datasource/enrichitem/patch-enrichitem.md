---
id: patch-enrichitem
title: patch enrichitem API
slug: /api/patch-enrichitem

---

Patch enrichitem


```bash
PATCH /v2/enrichitem
{
    "active": false
}
```

### Description

Allows you to update individual information in enrichitem

### Request Body

`enrichPipelineId`: (int) Id of related enrichpipeline

`active`: (boolean) Specific if enrichitem is enabled or not

`jsonConfig`: (dict) Configuration of the enrichitem. Normally includes some configuration of enrichitem.

`name`: (date) Name of enrichitem

`_position`: (int) Id of position of enrichitem in pipeline

`driverServiceName`: (string) Symbolic name of related enrichprocessor
