---
id: patch-enrichpipeline
title: Patch Enrich Pipeline API
slug: /api/patch-enrichpipeline
.
---

Patch enrich pipeline


```bash
PATCH /v2/enrichpipeline
{
    "active": false
}
```

### Description

Allows you to update individual information in enrich pipeline

### Request Body

`active`: (boolean) Specific if enrich pipeline is enabled or not

`name`: (string) Name of enrich pipeline

`datasourceId`: (integer) Id of related datasourceId
