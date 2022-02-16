---
id: update-enrichpipeline
title: Update Enrich Pipeline API
slug: /api/update-enrichpipeline

---

Update existing enrich pipeline

```bash
PUT /v2/enrichpipeline
{
  "active": true,
  "datasourceId": 222,
  "name": "example2 pipeline"
}
```

### Description

Update entire enrich pipeline with information specified in request body

### Request Body

`active`: (boolean) Specific if enrich pipeline is enabled or not

`name`: (string) Name of enrich pipeline

`datasourceId`: (integer) Id of related datasourceId
