---
id: create-enrichpipeline
title: Create Enrich Pipeline API
slug: /api/create-enrichpipeline

---

Create new enrich pipeline

```bash
POST /v2/enrichpipeline
{
  "active": true,
  "datasourceId": 123,
  "name": "example pipeline"
}
```

### Description

Allows you to create a new enrich pipeline.

### Request Body

`active`: (boolean) Specific if enrich pipeline is enabled or not

`name`: (string) Name of enrich pipeline

`datasourceId`: (integer) Id of related datasourceId
