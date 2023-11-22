---
id: filter-enrichpipeline
title: Filter Enrich Pipeline API
slug: /api/filter-enrichpipeline

---

Filter enrichpipeline

```bash
POST /v2/enrichpipeline/filter
{
  "name": "test-enrichpipeline"
}
```

### Description

Get enrich pipelines that match filter information passed in request body

### Request Body

`active`: (boolean) Specific if enrich pipeline is enabled or not

`name`: (string) Name of enrich pipeline

`datasourceId`: (integer) Id of related datasourceId

