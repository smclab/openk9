---
id: create-enrichpipeline
title: Create enrichpipeline API
slug: /api/create-enrichpipeline

---

Create new datasource

```bash
POST /v2/datasource
{
  "active": true,
  "datasourceId": 123,
  "name": "example pipeline"
}
```

### Description

Allows you to create a new datasource.

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`name`: (date) Name of datasource

`datasourceId`: (int) Id of related datasourceId
