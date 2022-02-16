---
id: update-enrichitem
title: Update Enrich Item API
slug: /api/update-enrichitem

---

Update existing Enrich Item

```bash
POST /v2/enrichitem
{
    "_position": 1,
    "active": true,
    "jsonConfig": "{}",
    "enrichPipelineId": 123,
    "name": "example",
    "serviceName": "io.openk9.plugins.example.enrichprocessor.ExampleNerEnrichProcessor"
}
```

### Description

Update entire enrich item with information specified in request body

### Request Body

`enrichPipelineId`: (int) Id of related enrich pipeline

`active`: (boolean) Specific if enrich item is enabled or not

`jsonConfig`: (dict) Configuration of the enrich item. Normally includes some configuration of enrich item.

`name`: (date) Name of enrich item

`_position`: (int) Id of position of enrich item in pipeline

`driverServiceName`: (string) Symbolic name of related enrich processor
