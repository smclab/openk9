---
id: create-enrichitem
title: Create enrichitem API
slug: /api/create-enrichitem

---

Create new enrichitem

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

Allows you to create a new enrichitem.

### Request Body

`enrichPipelineId`: (int) Id of related enrichpipeline

`active`: (boolean) Specific if enrichitem is enabled or not

`jsonConfig`: (dict) Configuration of the enrichitem. Normally includes some configuration of enrichitem.

`name`: (date) Name of enrichitem

`_position`: (int) Id of position of enrichitem in pipeline

`driverServiceName`: (string) Symbolic name of related enrichprocessor


