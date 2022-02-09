---
id: clean-orphan-entities-api
title: Clean Orphan Entities APIs
slug: /api/clean-orphan-entities-api

---

Delete documents in Elasticsearch by Content Ids

```bash
DELETE /v1/clean-orphan-entities/{tenantId}
```

### Description

This endpoint allows you to delete orphan entities of specific tenant.
Orphan entities are those that are no longer tied to any document

### Parameters

`id`: (integer) Id of datasource
